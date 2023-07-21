package org.southasia.ghru.sync

class JanacareResponse(val eventType: CholesterolcomEventType, val result: AinaResponce)

data class AinaResponce(val result: String, val lotNumber: String )