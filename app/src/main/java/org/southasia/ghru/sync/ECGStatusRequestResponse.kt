package org.southasia.ghru.sync

import org.southasia.ghru.vo.ECGStatus


class ECGStatusRequestResponse(
    val eventType: SyncResponseEventType,
    val ecgStatus: ECGStatus) {

}