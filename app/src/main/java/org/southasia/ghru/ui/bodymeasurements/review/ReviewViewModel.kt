package org.southasia.ghru.ui.bodymeasurements.review

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import org.southasia.ghru.repository.BodyMeasurementRequestRepository
import org.southasia.ghru.util.AbsentLiveData
import org.southasia.ghru.vo.Resource
import org.southasia.ghru.vo.ResourceData
import org.southasia.ghru.vo.request.BloodPresureItemRequest
import org.southasia.ghru.vo.request.BodyMeasurementRequest
import org.southasia.ghru.vo.request.ParticipantRequest
import javax.inject.Inject


class ReviewViewModel
@Inject constructor(val bodyMeasurementRequestRepository: BodyMeasurementRequestRepository) : ViewModel() {

    private val _bodyMeasurementRequestLocal: MutableLiveData<BodyMeasurementRequest> = MutableLiveData()
    private val _bodyMeasurementRequestRemote: MutableLiveData<MeasurementRequestId> = MutableLiveData()

    fun setBodyMeasurementRequestLocal(bodyMeasurementRequest: BodyMeasurementRequest) {
        if (_bodyMeasurementRequestLocal.value != bodyMeasurementRequest) {
            _bodyMeasurementRequestLocal.postValue(bodyMeasurementRequest)
        }
    }

    private val _bodyMeasurementRequestLocalBP: MutableLiveData<BloodPresureRequestId> = MutableLiveData()
    fun setBodyMeasurementRequestLocalBP(
        bloodPresureRequest: List<BloodPresureItemRequest>,
        bodyMeasurementRequest: BodyMeasurementRequest
    ) {

        val bloodPresureRequestId = BloodPresureRequestId(bloodPresureRequest, bodyMeasurementRequest)
        if (_bodyMeasurementRequestLocalBP.value == bloodPresureRequestId) {
            return
        }
        _bodyMeasurementRequestLocalBP.value = bloodPresureRequestId
    }

    fun setBodyMeasurementRequestRemote(
        bodyMeasurementRequest: BodyMeasurementRequest,
        participant: ParticipantRequest
    ) {
        val measurementRequestId = MeasurementRequestId(bodyMeasurementRequest, participant)
        if (_bodyMeasurementRequestRemote.value == measurementRequestId) {
            return
        }
        _bodyMeasurementRequestRemote.value = measurementRequestId
    }

    var bodyMeasurementRequestLocal: LiveData<Resource<BodyMeasurementRequest>>? = Transformations
        .switchMap(_bodyMeasurementRequestLocal) { bodyMeasurementRequestLocalX ->
            if (bodyMeasurementRequestLocalX == null) {
                AbsentLiveData.create()
            } else {
                bodyMeasurementRequestRepository.insertBodyMeasurementRequest(bodyMeasurementRequestLocalX)
            }
        }

//    var bodyMeasurementRequestLocalBP: LiveData<Resource<List<BloodPresureItemRequest>>>? = Transformations
//
//            .switchMap(_bodyMeasurementRequestLocalBP) { member ->
//                member.ifExists { bodyMeasurementRequestY, participantRequest ->
//                    bodyMeasurementRequestRepository.insertBPs(bodyMeasurementRequestY!!, participantRequest)
//                }
//            }

    var bodyMeasurementRequestRemote: LiveData<Resource<ResourceData<BodyMeasurementRequest>>>? = Transformations
        .switchMap(_bodyMeasurementRequestRemote) { member ->
            member.ifExists { bodyMeasurementRequestY, participantRequest ->
                bodyMeasurementRequestRepository.syncBodyMeasurementRequest(
                    bodyMeasurementRequestY!!,
                    participantRequest
                )
            }
        }


    data class MeasurementRequestId(
        val bodyMeasurementRequest: BodyMeasurementRequest?,
        val participant: ParticipantRequest?
    ) {
        fun <T> ifExists(f: (BodyMeasurementRequest?, ParticipantRequest) -> LiveData<T>): LiveData<T> {
            return if (bodyMeasurementRequest == null || participant == null) {
                AbsentLiveData.create()
            } else {
                f(bodyMeasurementRequest, participant)
            }
        }
    }

    data class BloodPresureRequestId(
        val bloodPresureRequestList: List<BloodPresureItemRequest>?,
        val bodyMeasurementRequest: BodyMeasurementRequest?
    ) {
        fun <T> ifExists(f: (List<BloodPresureItemRequest>?, BodyMeasurementRequest) -> LiveData<T>): LiveData<T> {
            return if (bloodPresureRequestList == null || bodyMeasurementRequest == null) {
                AbsentLiveData.create()
            } else {
                f(bloodPresureRequestList, bodyMeasurementRequest)
            }
        }
    }

}
