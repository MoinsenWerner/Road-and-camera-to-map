from fastapi import FastAPI, Request, Depends
from sqlalchemy.ext.asyncio import AsyncSession
from database import SessionLocal, engine, Base
from models import RoadSegment
from datetime import datetime
from shapely.geometry import LineString
import geoalchemy2.shape
import hashlib

app = FastAPI()

async def get_db():
    async with SessionLocal() as session:
        yield session

@app.on_event("startup")
async def init_db():
    async with engine.begin() as conn:
        await conn.run_sync(Base.metadata.create_all)

def calc_hash(coords):
    return hashlib.sha256(str(coords).encode()).hexdigest()

@app.post("/upload_segment")
async def upload_segment(data: dict, db: AsyncSession = Depends(get_db)):
    coords = data["coordinates"]  # z. B. [[lat1, lon1], [lat2, lon2], ...]
    speed_limit = data["speed_limit"]
    source = data["source"]
    segment_hash = calc_hash(coords)
    geom = LineString([(lon, lat) for lat, lon in coords])
    segment = RoadSegment(
        segment_hash=segment_hash,
        geometry=f"SRID=4326;{geom.wkt}",
        speed_limit=speed_limit,
        source=source,
        updated_at=datetime.utcnow(),
        extra_data=data.get("extra_data", {})
    )
    await db.merge(segment)
    await db.commit()
    return {"status": "ok"}

@app.get("/download_area")
async def download_area(lat_min: float, lat_max: float, lon_min: float, lon_max: float, db: AsyncSession = Depends(get_db)):
    query = await db.execute(f"""
        SELECT id, ST_AsGeoJSON(geometry) AS geometry, speed_limit, source, updated_at
        FROM segments
        WHERE geometry && ST_MakeEnvelope(:lon_min, :lat_min, :lon_max, :lat_max, 4326)
    """, {"lat_min": lat_min, "lat_max": lat_max, "lon_min": lon_min, "lon_max": lon_max})
    rows = query.fetchall()
    return [{"geometry": row.geometry, "speed_limit": row.speed_limit, "source": row.source, "updated_at": row.updated_at.isoformat()} for row in rows]

await db.merge(segment)

from sqlalchemy import text
import httpx

existing = await db.execute(text("""
    SELECT speed_limit, source FROM segments WHERE segment_hash = :hash
"""), {"hash": segment_hash})
row = existing.first()

# Wenn Segment noch nicht existiert → speichern
if not row:
    await db.merge(segment)
else:
    current_limit = row.speed_limit
    current_source = row.source
    incoming_limit = speed_limit
    incoming_source = source

    # Bei gleichem Limit → nichts tun
    if current_limit == incoming_limit:
        pass

    # Online gewinnt gegen Kamera
    elif current_source == "camera" and incoming_source == "online":
        await db.merge(segment)

    # Kamera vs Kamera → prüfe online
    elif current_source == "camera" and incoming_source == "camera":
        # Beispielhafter API-Call an OpenStreetMap-ähnliche Quelle
        verified_limit = await fetch_verified_speed_limit(geom)
        if verified_limit == incoming_limit:
            await db.merge(segment)

    # Neue Kamera-Info auf fetch → ignorieren
    elif current_source == "online" and incoming_source == "camera":
        pass