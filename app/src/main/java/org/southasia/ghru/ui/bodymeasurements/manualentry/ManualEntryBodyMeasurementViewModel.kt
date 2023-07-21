package org.southasia.ghru.ui.bodymeasurements.manualentry

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import org.southasia.ghru.repository.ParticipantRepository
import org.southasia.ghru.util.AbsentLiveData
import org.southasia.ghru.vo.Resource
import org.southasia.ghru.vo.ResourceData
import org.southasia.ghru.vo.request.ParticipantRequest
import javax.inject.Inject

class ManualEntryBodyMeasurementViewModel
@Inject constructor(participantRepository: ParticipantRepository) : ViewModel() {

    private val _screeningId: MutableLiveData<String> = MutableLiveData()
    val screeningId: LiveData<String>
        get() = _screeningId

    private val _screeningIdOffline: MutableLiveData<String> = MutableLiveData()
    val screeningIdOffline: LiveData<String>
        get() = _screeningIdOffline

    var participantOffline: LiveData<Resource<ParticipantRequest>>? = Transformations
        .switchMap(_screeningIdOffline) { screeningIdOffline ->
            if (screeningIdOffline == null) {
                AbsentLiveData.create()
            } else {
                participantRepository.getParticipantOffline(screeningIdOffline)
            }
        }

    fun setScreeningIdOffline(screeningIdOffline: String?) {
        if (_screeningIdOffline.value == screeningIdOffline) {
            return
        }
        _screeningIdOffline.value = screeningIdOffline
    }

    var participant: LiveData<Resource<ResourceData<ParticipantRequest>>> = Transformations
        .switchMap(_screeningId) { screeningId ->
            if (screeningId == null) {
                AbsentLiveData.create()
            } else {
                participantRepository.getParticipantRequest(screeningId, "body-measurement")
            }
        }

    fun setScreeningId(screeningId: String?) {
        if (_screeningId.value == screeningId) {
            return
        }
        _screeningId.value = screeningId
    }
}