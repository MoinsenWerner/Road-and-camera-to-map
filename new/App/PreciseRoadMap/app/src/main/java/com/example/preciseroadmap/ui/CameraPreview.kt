CameraPreview(onLimitDetected = { limit ->
    viewModel.setDetectedSpeedLimit(limit)
})