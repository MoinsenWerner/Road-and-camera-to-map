from sqlalchemy import Column, Integer, Float, String, DateTime, JSON
from geoalchemy2 import Geometry
from database import Base

class RoadSegment(Base):
    __tablename__ = "segments"
    id = Column(Integer, primary_key=True, index=True)
    segment_hash = Column(String, unique=True)
    geometry = Column(Geometry("LINESTRING", srid=4326))
    speed_limit = Column(Integer)  # z.B. 50
    source = Column(String)  # camera / online
    updated_at = Column(DateTime)
    extra_data = Column(JSON)