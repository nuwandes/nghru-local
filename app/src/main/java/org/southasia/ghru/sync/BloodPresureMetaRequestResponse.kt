package org.southasia.ghru.sync

import org.southasia.ghru.vo.request.BloodPressureMetaRequest


class BloodPresureMetaRequestResponse(
    val eventType: SyncResponseEventType,
    val bloodPressureMetaRequest: BloodPressureMetaRequest
)