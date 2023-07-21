package org.southasia.ghru.sync

import org.southasia.ghru.vo.request.HouseholdRequest

class SyncHouseholdRequestResponse(val eventType: SyncResponseEventType, val householdRequest: HouseholdRequest)