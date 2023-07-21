package org.southasia.ghru.ui.report.web

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import org.southasia.ghru.repository.SurveyRepository
import org.southasia.ghru.util.AbsentLiveData
import org.southasia.ghru.vo.CommonResponce
import org.southasia.ghru.vo.Message
import org.southasia.ghru.vo.Resource
import org.southasia.ghru.vo.ResourceData
import org.southasia.ghru.vo.request.ParticipantRequest
import org.southasia.ghru.vo.request.ReportRequestMeta
import javax.inject.Inject


class WebViewModel
@Inject constructor(surveyRepository: SurveyRepository) : ViewModel() {

    private val _screeningId: MutableLiveData<ParticipantRequest> = MutableLiveData()

    private val _reportRequest: MutableLiveData<ReportRequestMeta> = MutableLiveData()

    private val _metaId: MutableLiveData<MetaId> = MutableLiveData()

    val screeningId: LiveData<ParticipantRequest>
        get() = _screeningId

//    var participant: LiveData<Resource<ResourceData<CommonResponce>>>? = Transformations
//        .switchMap(_screeningId) { screeningId ->
//            if (screeningId == null) {
//                AbsentLiveData.create()
//            } else {
//                surveyRepository.syncSurveyComplte(screeningId)
//            }
//        }

    var participant: LiveData<Resource<ResourceData<CommonResponce>>>? = Transformations
        .switchMap(_metaId) { input ->
            input.ifExists { repoMeta, participantRequest ->
                surveyRepository.syncSurveyComplte( participant = participantRequest!!, reportRequestMeta = repoMeta!! )
            }
        }

//    var axivitySync: LiveData<Resource<Message>>? = Transformations
//        .switchMap(_axivityId) { input ->
//            input.ifExists { axivity, participantRequest ->
//                axivityRepository.syncAxivity(axivity = axivity!!, participantId = participantRequest!!)
//            }
//        }

    fun setScreeningId(screeningId: ParticipantRequest?) {
        if (_screeningId.value == screeningId) {
            return
        }
        _screeningId.value = screeningId
    }

//    fun setReportMeta(reportMeta: ReportRequestMeta?) {
//        if (_reportRequest.value == reportMeta) {
//            return
//        }
//        _reportRequest.value = reportMeta
//    }

    fun setRepoMeta(participantId: ParticipantRequest, reportRequestMeta: ReportRequestMeta?) {
        val update = MetaId(participantRequest = participantId, reportRequestMeta = reportRequestMeta)
        if (_metaId.value == update) {
            return
        }
        _metaId.value = update
    }

    data class MetaId(val reportRequestMeta: ReportRequestMeta?, val participantRequest: ParticipantRequest?) {

        fun <T> ifExists(f: (ReportRequestMeta?, ParticipantRequest?) -> LiveData<T>): LiveData<T> {
            return if (reportRequestMeta == null && participantRequest == null) {
                AbsentLiveData.create()
            } else {
//                axivity!!.comment="COMMENT"
//                axivity!!.startTime="START_TIME"
//                axivity!!.endTime="END_TIME"
//                axivity!!.meta!!.collectedBy = "COLLECTED_BY"
//                axivity!!.meta!!.startTime = "META_START_TIME"
//                axivity!!.meta!!.endTime = "META_END_TIME"
                f(reportRequestMeta, participantRequest)
            }
        }
    }
}
