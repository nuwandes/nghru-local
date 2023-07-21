package org.southasia.ghru.jobs

object JobPriority {
    val CANCEL_REQUEST = 10
    val SAMPLE_STORAGE = 250
    val SAMPLE_PROCESS = 500
    val SAMPLE_COLLECT = 1000
    val AXIVITY = 1150
    val SPIROMETRY = 1100
    val FUNDOSCOPY = 1200
    val ECG = 1250
    val SURVEY = 1300
    val BODY_MESEASUMENT = 1500
    val BLOOD_PRESURE = 1600
    val PARICIPANT_IMAGE = 2000
    val PARICIPANT = 2500
    val MEMBERS = 5000
    val HOUSEHOLD = 10000
}
