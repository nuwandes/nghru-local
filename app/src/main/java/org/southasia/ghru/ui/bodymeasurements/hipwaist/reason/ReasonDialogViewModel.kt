package org.southasia.ghru.ui.bodymeasurements.hipwaist.reason

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import org.southasia.ghru.repository.CancelRequestRepository
import org.southasia.ghru.util.AbsentLiveData
import org.southasia.ghru.vo.Message
import org.southasia.ghru.vo.MessageCancel
import org.southasia.ghru.vo.Resource
import org.southasia.ghru.vo.ResourceData
import org.southasia.ghru.vo.request.CancelRequest
import org.southasia.ghru.vo.request.ParticipantRequest
import javax.inject.Inject


class ReasonDialogViewModel
@Inject constructor(cancelRequestRepository: CancelRequestRepository) : ViewModel() {

    private val _cancelId: MutableLiveData<CancelId> = MutableLiveData()


    var cancelId: LiveData<Resource<MessageCancel>>? = Transformations
        .switchMap(_cancelId) { input ->
            input.ifExists { participantRequest, cancelRequest ->
                cancelRequestRepository.syncCancelRequest(participantRequest, cancelRequest)
            }
        }

    fun setLogin(participantRequest: ParticipantRequest?, cancelRequest: CancelRequest?) {
        val update = CancelId(participantRequest, cancelRequest)
        if (_cancelId.value == update) {
            return
        }
        _cancelId.value = update
    }

    data class CancelId(val participantRequest: ParticipantRequest?, val cancelRequest: CancelRequest?) {
        fun <T> ifExists(f: (ParticipantRequest, CancelRequest) -> LiveData<T>): LiveData<T> {
            return if (participantRequest == null || cancelRequest == null) {
                AbsentLiveData.create()
            } else {
                f(participantRequest, cancelRequest)
            }
        }
    }
}
