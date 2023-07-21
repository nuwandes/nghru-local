package org.southasia.ghru.ui.registerpatient_new.confirmation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import org.southasia.ghru.repository.AssertRepository
import org.southasia.ghru.repository.MembersRepository
import org.southasia.ghru.repository.ParticipantMetaRepository
import org.southasia.ghru.repository.ParticipantRequestRepository
import org.southasia.ghru.util.AbsentLiveData
import org.southasia.ghru.vo.*
import org.southasia.ghru.vo.request.Member
import org.southasia.ghru.vo.request.ParticipantMeta
import org.southasia.ghru.vo.request.ParticipantRequest
import javax.inject.Inject


class ConfirmationViewModelNew
@Inject constructor(
    participantRequestRepository: ParticipantRequestRepository,
    assertRepository: AssertRepository,
    participantMetaRepository: ParticipantMetaRepository,
    membersRepository: MembersRepository
) : ViewModel() {

    private val _participantRequestLocal: MutableLiveData<ParticipantRequest> = MutableLiveData()

    private val _participantRequestRemote: MutableLiveData<ParticipantRequest> = MutableLiveData()

    private val _participantMetaRemote: MutableLiveData<ParticipantMeta> = MutableLiveData()


    private val _uploadId: MutableLiveData<UploadId> = MutableLiveData()

    private val _uploadConcent: MutableLiveData<UploadConcentId> = MutableLiveData()


    var participantRequestSaveLocal: LiveData<Resource<ParticipantRequest>>? = Transformations
        .switchMap(_participantRequestLocal) { participant ->
            if (participant == null) {
                AbsentLiveData.create()
            } else {

                participantRequestRepository.insertParticipantRequest(participant)
            }
        }


    var participantMemberupdateLocal: LiveData<Resource<Member>>? = Transformations
        .switchMap(_participantRequestLocal) { participant ->
            if (participant == null) {
                AbsentLiveData.create()
            } else {
                membersRepository.updateMember(participant)
            }
        }
    var participantRequestSaveRemote: LiveData<Resource<ResourceData<Participant>>>? = Transformations
        .switchMap(_participantRequestRemote) { participant ->
            if (participant == null) {
                AbsentLiveData.create()
            } else {
                participantRequestRepository.syncParticipantRequest(participant)
            }
        }






    var participantMetaSaveRemote: LiveData<Resource<ResourceData<CommonResponce>>>? = Transformations
        .switchMap(_participantMetaRemote) { participant ->
            if (participant == null) {
                AbsentLiveData.create()
            } else {
                participantMetaRepository.syncParticipantMeta(participant)
            }
        }






//    var uploadProfileRemote: LiveData<Resource<Message>>? = Transformations
//
//            .switchMap(_uploadId) { upload ->
//                upload.ifExists { member, participantRequest ->
//                    assertRepository.uploadProfile(member, participantRequest)
//                }
//            }

    var uploadConcent: LiveData<Resource<Message>>? = Transformations
        .switchMap(_uploadConcent) { upload ->
            upload.ifExists { concentPhoto, screeningId ->
                assertRepository.uploadConcent(concentPhoto, screeningId)
            }
        }
    var uploadIdCardRemote: LiveData<Resource<Message>>? = Transformations
        .switchMap(_uploadId) { upload ->
            upload.ifExists { member, participantRequest ->
                assertRepository.uploadIdCard(member, participantRequest)
            }
        }


    fun setParticipantRequestLocal(participantRequest: ParticipantRequest) {
        if (_participantRequestLocal.value != participantRequest) {
            _participantRequestLocal.postValue(participantRequest)
        }
    }

    fun setParticipantRequestRemote(participantRequest: ParticipantRequest) {
        if (_participantRequestRemote.value != participantRequest) {
            _participantRequestRemote.postValue(participantRequest)
        }
    }

    fun setParticipantMetaRemote(participantMeta: ParticipantMeta) {
        if (_participantMetaRemote.value != participantMeta) {
            _participantMetaRemote.postValue(participantMeta)
        }
    }


    fun setUploadConcent(concentPhoto: String?, screeningId: String?) {
        //uploadparticipantRequest = participantRequest
        val update = UploadConcentId(concentPhoto, screeningId)
        if (_uploadConcent.value == update) {
            return
        }
        _uploadConcent.value = update
    }

    fun setUploadId(member: String, participantRequest: ParticipantRequest) {
        //uploadparticipantRequest = participantRequest
        val update = UploadId(member, participantRequest)
        if (_uploadId.value == update) {
            return
        }
        _uploadId.value = update
    }


    data class UploadId(val identityImage: String?, val participantRequest: ParticipantRequest?) {
        fun <T> ifExists(f: (String, ParticipantRequest) -> LiveData<T>): LiveData<T> {
            return if (identityImage == null || participantRequest == null) {
                AbsentLiveData.create()
            } else {
                f(identityImage, participantRequest)
            }
        }
    }

    data class UploadConcentId(val concentPhoto: String?, val screeningId: String?) {
        fun <T> ifExists(f: (String, String) -> LiveData<T>): LiveData<T> {
            return if (concentPhoto == null || screeningId == null) {
                AbsentLiveData.create()
            } else {
                f(concentPhoto, screeningId)
            }
        }
    }

}
