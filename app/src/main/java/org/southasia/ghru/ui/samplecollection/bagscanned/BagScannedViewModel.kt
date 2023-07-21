package org.southasia.ghru.ui.samplecollection.bagscanned

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import org.southasia.ghru.repository.SampleRepository
import org.southasia.ghru.repository.SampleRequestRepository
import org.southasia.ghru.repository.UserRepository
import org.southasia.ghru.util.AbsentLiveData
import org.southasia.ghru.vo.*
import org.southasia.ghru.vo.request.ParticipantRequest
import org.southasia.ghru.vo.request.SampleCreateRequest
import org.southasia.ghru.vo.request.SampleRequest
import javax.inject.Inject


class BagScannedViewModel
@Inject constructor(
    sampleRepository: SampleRepository,
    sampleRequestRepository: SampleRequestRepository,
    userRepository: UserRepository
) : ViewModel() {

    private val _participantRequestRemote: MutableLiveData<SampleId> = MutableLiveData()

    private val _sampleRequestLocal: MutableLiveData<SampleRequest> = MutableLiveData()

    lateinit var sampleId: String

    var comment : Comment? = null

    private val _email = MutableLiveData<String>()

    var sample: LiveData<Resource<ResourceData<SampleData>>>? = Transformations
        .switchMap(_participantRequestRemote) { input ->
            input.ifExists { participantRequest, sampleId,comment ->
                sampleRepository.syncSample(participantRequest, sampleId,comment!!)
            }
        }

    var sampleRequestLocal: LiveData<Resource<SampleRequest>>? = Transformations
        .switchMap(_sampleRequestLocal) { sampleRequestLocal ->
            if (sampleRequestLocal == null) {
                AbsentLiveData.create()
            } else {
                sampleRequestRepository.insertSampleRequest(sampleRequestLocal)
            }
        }


    val user: LiveData<Resource<User>>? = Transformations
        .switchMap(_email) { emailx ->
            if (emailx == null) {
                AbsentLiveData.create()
            } else {
                userRepository.loadUserDB()
            }
        }


    fun setSample(
        participantRequest: ParticipantRequest?, sampleIdx: String,
        comment: SampleCreateRequest
    ) {

        val update = SampleId(participantRequest, sampleIdx,comment)
        if (_participantRequestRemote.value == update) {
            return
        }
        _participantRequestRemote.value = update
    }

    fun setSampleLocal(sampleRequest: SampleRequest) {
        if (_sampleRequestLocal.value != sampleRequest) {
            _sampleRequestLocal.postValue(sampleRequest)
        }
    }

    fun setUser(email: String?) {
        if (_email.value != email) {
            _email.value = email
        }
    }

    data class SampleId(val participantRequest: ParticipantRequest?, val sampleId: String , val comment : SampleCreateRequest) {
        fun <T> ifExists(f: (ParticipantRequest, String,SampleCreateRequest) -> LiveData<T>): LiveData<T> {
            return if (participantRequest == null) {
                AbsentLiveData.create()
            } else {
                f(participantRequest, sampleId,comment)
            }
        }
    }
}
