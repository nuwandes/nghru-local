package org.southasia.ghru.ui.ecg.trace.complete

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import org.southasia.ghru.repository.ECGRepository
import org.southasia.ghru.util.AbsentLiveData
import org.southasia.ghru.vo.ECG
import org.southasia.ghru.vo.Resource
import org.southasia.ghru.vo.ResourceData
import org.southasia.ghru.vo.request.ParticipantRequest
import javax.inject.Inject


class CompleteDialogViewModel
@Inject constructor(eCGRepository: ECGRepository) : ViewModel() {

    private val _participantRequestRemote: MutableLiveData<ECGId> = MutableLiveData()
    private var isOnline : Boolean = false

//    var eCGSaveRemote: LiveData<Resource<ResourceData<ECG>>>? = Transformations
//            .switchMap(_participantRequestRemote) { participant ->
//                if (participant == null) {
//                    AbsentLiveData.create()
//                } else {
//                    eCGRepository.syncECG(participant)
//                }
//            }

    var eCGSaveRemote: LiveData<Resource<ECG>>? = Transformations
        .switchMap(_participantRequestRemote) { input ->
            input.ifExists { participantRequest, status, comment, device_id ->
                eCGRepository.syncECG(participantRequest, status, comment, device_id,isOnline)
            }
        }

    fun setECGRemote(participantRequest: ParticipantRequest, status: String, comment: String?, device_id: String, online : Boolean) {

        isOnline = online
        val update = ECGId(participantRequest, status, comment, device_id)
        if (_participantRequestRemote.value == update) {
            return
        }
        _participantRequestRemote.value = update
//        if (_participantRequestRemote.value != participantRequest) {
//            _participantRequestRemote.postValue(participantRequest)
//        }
    }

    data class ECGId(
        val participantRequest: ParticipantRequest?,
        val status: String?,
        val comment: String?,
        val device_id: String
    ) {
        fun <T> ifExists(f: (ParticipantRequest, String, String?, String) -> LiveData<T>): LiveData<T> {
            return if (participantRequest == null || status.isNullOrBlank()) {
                AbsentLiveData.create()
            } else {
                f(participantRequest, status!!, comment, device_id)
            }
        }
    }
}
