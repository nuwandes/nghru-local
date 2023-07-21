package org.southasia.ghru.ui.intake.readings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import org.southasia.ghru.repository.IntakeRepository
import org.southasia.ghru.ui.report.web.WebViewModel
import org.southasia.ghru.util.AbsentLiveData
import org.southasia.ghru.vo.*
import org.southasia.ghru.vo.request.*

import javax.inject.Inject

class IntakeReadingsViewModel @Inject constructor(
    intakeRepository: IntakeRepository
) : ViewModel() {

    private val _intakeRequest: MutableLiveData<IntakeRequestNew> = MutableLiveData()
    private val _intakeUpdateRequest: MutableLiveData<IntakeRequestNew> = MutableLiveData()
    private var _participantId: String? = null
    private val _metaId: MutableLiveData<MetaId> = MutableLiveData()
//    private val _updateMetaId: MutableLiveData<UpdateMetaId> = MutableLiveData()

//    var intakePostComplete:LiveData<Resource<ResourceData<CommonResponce>>>? = Transformations
//        .switchMap(_intakeRequest) { intakeRequest ->
//            if (intakeRequest == null) {
//                AbsentLiveData.create()
//            } else {
//                intakeRepository.addIntake(intakeRequest,_participantId!!)
//            }
//        }

    var intakePostComplete: LiveData<Resource<ResourceData<IntakeResponse>>>? = Transformations
        .switchMap(_metaId) { input ->
            input.ifExists { requestMeta, screen_id ->
                intakeRepository.addIntake( intakeRequest = requestMeta!!, screening_id = screen_id!!)
            }
        }

    var intakeUpdateComplete:LiveData<Resource<ResourceData<IntakeResponse>>>? = Transformations
        .switchMap(_intakeUpdateRequest) { intakeRequest ->
            if (intakeRequest == null) {
                AbsentLiveData.create()
            } else {
                intakeRepository.updateIntake(intakeRequest,_participantId!!)
            }
        }

//    var intakeUpdateComplete: LiveData<Resource<ResourceData<IntakeResponse>>>? = Transformations
//        .switchMap(_updateMetaId) { input ->
//            input.ifExists { requestMeta, screen_id ->
//                intakeRepository.updateIntake( intakeRequest = requestMeta!!, screening_id = screen_id!!)
//            }
//        }

    fun setParticipant(intakeRequest: IntakeRequestNew, participantId: String?) {
        _participantId = participantId
        if (_intakeRequest.value == intakeRequest) {
            return
        }
        _intakeRequest.value = intakeRequest
    }
    fun updateParticipant(intakeRequest: IntakeRequestNew, participantId: String?) {
        _participantId = participantId
        if (_intakeUpdateRequest.value == intakeRequest) {
            return
        }
        _intakeUpdateRequest.value = intakeRequest
    }

    data class MetaId( val intakeRequest: IntakeRequestNew?, val screen_id: String?) {

        fun <T> ifExists(f: (IntakeRequestNew?, String? ) -> LiveData<T>): LiveData<T> {
            return if (screen_id == null && intakeRequest == null) {
                AbsentLiveData.create()
            } else {
//                axivity!!.comment="COMMENT"
//                axivity!!.startTime="START_TIME"
//                axivity!!.endTime="END_TIME"
//                axivity!!.meta!!.collectedBy = "COLLECTED_BY"
//                axivity!!.meta!!.startTime = "META_START_TIME"
//                axivity!!.meta!!.endTime = "META_END_TIME"
                f(intakeRequest, screen_id )
            }
        }
    }

    fun setIntakeMeta(screen_id: String?, intakeRequest: IntakeRequestNew?) {
        val create = MetaId(
            screen_id = screen_id,
            intakeRequest = intakeRequest
        )
        if (_metaId.value == create) {
            return
        }
        _metaId.value = create
    }

//    data class UpdateMetaId( val intakeRequest: IntakeRequestNew?, val screen_id: String?) {
//
//        fun <T> ifExists(f: (IntakeRequestNew?, String? ) -> LiveData<T>): LiveData<T> {
//            return if (screen_id == null && intakeRequest == null) {
//                AbsentLiveData.create()
//            } else {
////                axivity!!.comment="COMMENT"
////                axivity!!.startTime="START_TIME"
////                axivity!!.endTime="END_TIME"
////                axivity!!.meta!!.collectedBy = "COLLECTED_BY"
////                axivity!!.meta!!.startTime = "META_START_TIME"
////                axivity!!.meta!!.endTime = "META_END_TIME"
//                f(intakeRequest, screen_id )
//            }
//        }
//    }

//    fun setIntakeMetaUpdate(screen_id: String?, intakeRequest: IntakeRequestNew?) {
//        val update = UpdateMetaId(
//            screen_id = screen_id,
//            intakeRequest = intakeRequest
//        )
//        if (_updateMetaId.value == update) {
//            return
//        }
//        _updateMetaId.value = update
//    }

}
