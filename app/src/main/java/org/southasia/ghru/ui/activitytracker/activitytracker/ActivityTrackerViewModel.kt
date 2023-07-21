package org.southasia.ghru.ui.activitytracker.activitytracker


import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import org.southasia.ghru.repository.AssertRepository
import org.southasia.ghru.repository.AxivityRepository
import org.southasia.ghru.util.AbsentLiveData
import org.southasia.ghru.vo.*
import org.southasia.ghru.vo.request.ParticipantRequest
import javax.inject.Inject


class ActivityTackeViewModel @Inject constructor(
    val assertRepository: AssertRepository,
    axivityRepository: AxivityRepository
) : ViewModel() {
    var activitytackerSyncError: MutableLiveData<Boolean>? = MutableLiveData<Boolean>().apply { }


    private val _participantId: MutableLiveData<ParticipantRequest> = MutableLiveData()

    private val _participantIdComplte: MutableLiveData<ParticipantRequest> = MutableLiveData()

    var isChecked: Boolean = false

    private var comment: String? = null


    fun setHasExplained(explained: Boolean) {
        isChecked = explained
    }

    private val _axivityId: MutableLiveData<AxivityId> = MutableLiveData()


    var asserts: LiveData<Resource<ResourceData<List<Asset>>>>? = Transformations
        .switchMap(_participantId) { participantId ->
            if (participantId == null) {
                AbsentLiveData.create()
            } else {
                assertRepository.getAssets(participantId, "activitytacker")
            }
        }

//    var activitytackerComplete: LiveData<Resource<ResourceData<ECG>>>? = Transformations
//            .switchMap(_participantIdComplte) { participantId ->
//                if (participantId == null) {
//                    AbsentLiveData.create()
//                } else {
//                    activitytackerRepository.syncFundoscopy(participantId, comment)
//                }
//            }

    var axivitySync: LiveData<Resource<Message>>? = Transformations
        .switchMap(_axivityId) { input ->
            input.ifExists { axivity, participantRequest ->
                axivityRepository.syncAxivity(axivity = axivity!!, participantId = participantRequest!!)
            }
        }

    fun setParticipant(participantId: ParticipantRequest) {
        if (_participantId.value == participantId) {
            return
        }
        _participantId.value = participantId
    }

    fun setAxivity(participantId: ParticipantRequest, axivity: Axivity) {
        val update = AxivityId(participantRequest = participantId, axivity = axivity)
        if (_axivityId.value == update) {
            return
        }
        _axivityId.value = update
    }

    data class AxivityId(val axivity: Axivity?, val participantRequest: ParticipantRequest?) {

        fun <T> ifExists(f: (Axivity?, ParticipantRequest?) -> LiveData<T>): LiveData<T> {
            return if (axivity == null && participantRequest == null) {
                AbsentLiveData.create()
            } else {
//                axivity!!.comment="COMMENT"
//                axivity!!.startTime="START_TIME"
//                axivity!!.endTime="END_TIME"
//                axivity!!.meta!!.collectedBy = "COLLECTED_BY"
//                axivity!!.meta!!.startTime = "META_START_TIME"
//                axivity!!.meta!!.endTime = "META_END_TIME"
                f(axivity, participantRequest)
            }
        }
    }
}
