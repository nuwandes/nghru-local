package org.southasia.ghru.sync

import org.southasia.ghru.vo.ECG
import org.southasia.ghru.vo.ECGStatus
import org.southasia.ghru.vo.FundoscopyRequest

class FundoscopyRequestResponce(
    val eventType: SyncResponseEventType,
    val fundoscopyRequest: FundoscopyRequest
)
{
}