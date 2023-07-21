package org.southasia.ghru.ui.samplemanagement.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import org.southasia.ghru.repository.SampleRepository
import org.southasia.ghru.repository.SampleRequestRepository
import org.southasia.ghru.util.AbsentLiveData
import org.southasia.ghru.vo.*
import org.southasia.ghru.vo.request.SampleRequest
import javax.inject.Inject


class SampleMangementHomeViewModel
@Inject constructor(sampleRepository: SampleRepository, sampleRequestRepository: SampleRequestRepository) :
    ViewModel() {


    private val _sampleMangementId: MutableLiveData<SampleMangementId> = MutableLiveData()

    private val _sampleMangementIdLocal: MutableLiveData<SampleMangementOfflineId> = MutableLiveData()

    //var sampleValidationError: MutableLiveData<Boolean> = MutableLiveData<Boolean>().apply { }


    fun setSync(
        hb1Ac: Hb1AcDto?,
        fastingBloodGlucose: FastingBloodGlucoseDto?,
        lipidProfile: LipidProfileAllDto?,
        hOGTT: HOGTTDto?,
        hemo: HemoglobinDto?,
        sampleId: SampleRequest?
    ) {
        val update = SampleMangementId(hb1Ac, fastingBloodGlucose, lipidProfile, hOGTT, hemo, sampleId)
        if (_sampleMangementId.value == update) {
            return
        }
        _sampleMangementId.value = update
    }

    fun setSyncLocal(
        hb1Ac: Hb1AcDto?,
        fastingBloodGlucose: FastingBloodGlucoseDto?,
        lipidProfile: LipidProfileAllDto?,
        hOGTT: HOGTTDto?,
        hemo: HemoglobinDto?,
        sampleId: SampleRequest?,
        syncPending: Boolean
    ) {
        val update = SampleMangementOfflineId(hb1Ac, fastingBloodGlucose, lipidProfile, hOGTT, hemo, sampleId, syncPending)
        if (_sampleMangementIdLocal.value == update) {
            return
        }
        _sampleMangementIdLocal.value = update
    }

    var sampleMangementPocess: LiveData<Resource<Message>>? = Transformations
        .switchMap(_sampleMangementId) { input ->
            input.ifExists { hb1Ac, fastingBloodGlucose, lipidProfile, hOGTT, hemo, sampleId ->

                sampleRepository.syncSampleProcess(hb1Ac, fastingBloodGlucose, lipidProfile, hOGTT, hemo, sampleId)
            }
        }

    var sampleMangementPocessLocal: LiveData<Resource<SampleProcess>>? = Transformations
        .switchMap(_sampleMangementIdLocal) { input ->
            input.ifExists { hb1Ac, fastingBloodGlucose, lipidProfile, hOGTT, hemo, sampleId, syncPending ->

                sampleRepository.insertSampleRequest(
                    hb1Ac,
                    fastingBloodGlucose,
                    lipidProfile,
                    hOGTT,
                    hemo,
                    sampleId,
                    syncPending
                )
            }
        }

    var sampleRequestLocal: LiveData<Resource<SampleRequest>>? = Transformations
        .switchMap(_sampleMangementIdLocal) { input ->
            input.ifExists { hb1Ac, fastingBloodGlucose, lipidProfile, hOGTT, hemo, sampleId, syncPending ->

                sampleRequestRepository.updattSampleRequestBySampleIdProcccesed(sampleId)
            }
        }

    data class SampleMangementId(
        val hb1Ac: Hb1AcDto?,
        val fastingBloodGlucose: FastingBloodGlucoseDto?,
        val lipidProfile: LipidProfileAllDto?,
        val hOGTT: HOGTTDto?,
        val hemo: HemoglobinDto?,
        val sampleId: SampleRequest?
    ) {
        fun <T> ifExists(f: (Hb1AcDto?, FastingBloodGlucoseDto?, LipidProfileAllDto, HOGTTDto?, HemoglobinDto?, SampleRequest?) -> LiveData<T>): LiveData<T> {
            return if (lipidProfile == null && (hb1Ac == null || fastingBloodGlucose == null || hOGTT == null) || sampleId == null) {
                AbsentLiveData.create()
            } else {
                f(hb1Ac, fastingBloodGlucose, lipidProfile!!, hOGTT, hemo, sampleId)
            }
        }
    }

    data class SampleMangementOfflineId(
        val hb1Ac: Hb1AcDto?,
        val fastingBloodGlucose: FastingBloodGlucoseDto?,
        val lipidProfile: LipidProfileAllDto?,
        val hOGTT: HOGTTDto?,
        val hemo: HemoglobinDto?,
        val sampleId: SampleRequest?,
        val syncPending: Boolean
    ) {
        fun <T> ifExists(f: (Hb1AcDto?, FastingBloodGlucoseDto?, LipidProfileAllDto, HOGTTDto?, HemoglobinDto?, SampleRequest, Boolean) -> LiveData<T>): LiveData<T> {
            return if (lipidProfile == null && (hb1Ac == null || fastingBloodGlucose == null || hOGTT == null) || sampleId == null) {
                AbsentLiveData.create()
            } else {
                f(hb1Ac, fastingBloodGlucose, lipidProfile!!, hOGTT, hemo, sampleId, syncPending)
            }
        }
    }
}
