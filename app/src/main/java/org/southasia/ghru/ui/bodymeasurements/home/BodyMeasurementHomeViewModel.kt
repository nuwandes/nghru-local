package org.southasia.ghru.ui.bodymeasurements.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import org.southasia.ghru.repository.BodyMeasurementMetaRepository
import org.southasia.ghru.repository.BodyMeasurementRequestRepository
import org.southasia.ghru.repository.SampleRepository
import org.southasia.ghru.repository.UserRepository
import org.southasia.ghru.util.AbsentLiveData
import org.southasia.ghru.vo.*
import org.southasia.ghru.vo.request.BodyMeasurementMeta
import org.southasia.ghru.vo.request.ParticipantRequest
import org.southasia.ghru.vo.request.SampleRequest
import javax.inject.Inject


class BodyMeasurementHomeViewModel
@Inject constructor(
    sampleRepository: SampleRepository,
    userRepository: UserRepository,
    bodyMeasurementRequestRepository: BodyMeasurementRequestRepository,
    bodyMeasurementMetaRepository: BodyMeasurementMetaRepository

) : ViewModel() {


    private val _sampleMangementId: MutableLiveData<BodyMeasurementId> = MutableLiveData()


    //var sampleValidationError: MutableLiveData<Boolean> = MutableLiveData<Boolean>().apply { }


    fun setSync(
        hb1Ac: Hb1AcDto?,
        fastingBloodGlucose: FastingBloodGlucoseDto?,
        lipidProfile: LipidProfileAllDto?,
        hOGTT: HOGTTDto?,
        hemoglobin: HemoglobinDto?,
        sampleId: SampleRequest?
    ) {
        val update = BodyMeasurementId(hb1Ac, fastingBloodGlucose, lipidProfile, hOGTT, hemoglobin, sampleId)
        if (_sampleMangementId.value == update) {
            return
        }
        _sampleMangementId.value = update
    }

    var sampleMangementPocess: LiveData<Resource<Message>>? = Transformations
        .switchMap(_sampleMangementId) { input ->
            input.ifExists { hb1Ac, fastingBloodGlucose, lipidProfile, hOGTT, hemo, sampleId ->

                sampleRepository.syncSampleProcess(hb1Ac, fastingBloodGlucose, lipidProfile, hOGTT, hemo, sampleId)
            }
        }

    data class BodyMeasurementId(
        val hb1Ac: Hb1AcDto?,
        val fastingBloodGlucose: FastingBloodGlucoseDto?,
        val lipidProfile: LipidProfileAllDto?,
        val hOGTT: HOGTTDto?,
        val hemoglobin: HemoglobinDto?,
        val sampleId: SampleRequest?
    ) {
        fun <T> ifExists(f: (Hb1AcDto?, FastingBloodGlucoseDto?, LipidProfileAllDto, HOGTTDto?, HemoglobinDto?, SampleRequest?) -> LiveData<T>): LiveData<T> {
            return if (lipidProfile == null && (hb1Ac == null || fastingBloodGlucose == null || hOGTT == null) || sampleId == null) {
                AbsentLiveData.create()
            } else {
                f(hb1Ac, fastingBloodGlucose, lipidProfile!!, hOGTT, hemoglobin, sampleId)
            }
        }
    }

    data class BodyMeasurementOfflineId(
        val hb1Ac: Hb1AcDto?,
        val fastingBloodGlucose: FastingBloodGlucoseDto?,
        val lipidProfile: LipidProfileAllDto?,
        val hOGTT: HOGTTDto?,
        val sampleId: SampleRequest?,
        val syncPending: Boolean
    ) {
        fun <T> ifExists(f: (Hb1AcDto?, FastingBloodGlucoseDto?, LipidProfileAllDto, HOGTTDto?, SampleRequest, Boolean) -> LiveData<T>): LiveData<T> {
            return if (lipidProfile == null && (hb1Ac == null || fastingBloodGlucose == null || hOGTT == null) || sampleId == null) {
                AbsentLiveData.create()
            } else {
                f(hb1Ac, fastingBloodGlucose, lipidProfile!!, hOGTT, sampleId, syncPending)
            }
        }
    }

    private val _email = MutableLiveData<String>()

    private val _bodyMeasurementMeta = MutableLiveData<BodyMeasurementMetaId>()


    private val _bodyMeasurementMetaOffline = MutableLiveData<BodyMeasurementMeta>()


    val user: LiveData<Resource<User>>? = Transformations
        .switchMap(_email) { emailx ->
            if (emailx == null) {
                AbsentLiveData.create()
            } else {
                userRepository.loadUserDB()
            }
        }


    val bodyMeasurementMetaOffline: LiveData<Resource<BodyMeasurementMeta>>? = Transformations
        .switchMap(_bodyMeasurementMetaOffline) { bodyMeasurementMetaX ->
            if (bodyMeasurementMetaX == null) {
                AbsentLiveData.create()
            } else {
                bodyMeasurementMetaRepository.bodyMeasurementMeta(bodyMeasurementMetaX)
            }
        }

    fun setBodyMeasurementMeta(
        bodyMeasurementRequest: BodyMeasurementMeta
    ) {
        if (_bodyMeasurementMetaOffline.value == bodyMeasurementRequest) {
            return
        }
        _bodyMeasurementMetaOffline.value = bodyMeasurementRequest
    }


    fun setBodyMeasurementMeta(
        bodyMeasurementRequest: BodyMeasurementMeta,
        particap: ParticipantRequest
    ) {
        val update = BodyMeasurementMetaId(bodyMeasurementRequest, particap)
        if (_bodyMeasurementMeta.value == update) {
            return
        }
        _bodyMeasurementMeta.value = update
    }


    val participantMetas: LiveData<Resource<ResourceData<Message>>>? = Transformations
        .switchMap(_bodyMeasurementMeta) { input ->
            input.ifExists { bodyMeasurementMeta, participantRequest ->

                bodyMeasurementRequestRepository.syncBodyMeasurementMeta(bodyMeasurementMeta!!, participantRequest!!)
            }
        }


    data class BodyMeasurementMetaId(
        val bodyMeasurementMeta: BodyMeasurementMeta?,
        val participantRequest: ParticipantRequest?
    ) {
        fun <T> ifExists(f: (BodyMeasurementMeta?, ParticipantRequest?) -> LiveData<T>): LiveData<T> {
            return if (bodyMeasurementMeta == null && participantRequest == null) {
                AbsentLiveData.create()
            } else {
                f(bodyMeasurementMeta, participantRequest)
            }
        }
    }

    fun setUser(email: String?) {
        if (_email.value != email) {
            _email.value = email
        }
    }
}
