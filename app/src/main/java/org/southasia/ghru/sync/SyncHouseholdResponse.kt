package org.southasia.ghru.sync

import org.southasia.ghru.vo.request.HouseholdRequest

class SyncHouseholdResponse(val eventType: SyncResponseEventType, val household: HouseholdRequest)