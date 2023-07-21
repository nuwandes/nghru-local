package org.southasia.ghru.sync

import org.southasia.ghru.vo.request.CancelRequest

class CancelRequestResponse( val eventType: SyncResponseEventType,
                             val cancelRequest: CancelRequest
) {
}