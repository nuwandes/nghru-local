package org.southasia.ghru.sync

import org.southasia.ghru.vo.request.Member

class SyncHouseholdMemberListResponse(val eventType: SyncResponseEventType, val member: List<Member>)