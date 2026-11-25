package com.scrollstop.app.detection

/**
 * Detection result returned by detectors.
 */
sealed class DetectionResult {
    object NotDetected : DetectionResult()
    data class Detected(
        val contentType: com.scrollstop.app.model.ContentType,
        val confidence: Float,  // 0.0 – 1.0
        val signals: List<String>
    ) : DetectionResult()
}
