package org.southasia.ghru.sync

import org.southasia.ghru.vo.Axivity
import org.southasia.ghru.vo.request.CancelRequest

class AxivityRequestResponce( val eventType: SyncResponseEventType,
                              val axivity: Axivity
) {
}