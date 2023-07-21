package org.southasia.ghru.sync

import org.southasia.ghru.vo.request.BodyMeasurementRequest

class BodyMeasurementRequestResponse(
    val eventType: SyncResponseEventType,
    val bodyMeasurementRequest: BodyMeasurementRequest
)