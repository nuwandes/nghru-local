package org.southasia.ghru.ui.fundoscopy.reading

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import org.southasia.ghru.repository.AssertRepository
import org.southasia.ghru.repository.FundoscopyRepository
import org.southasia.ghru.repository.StationDevicesRepository
import org.southasia.ghru.util.AbsentLiveData
import org.southasia.ghru.vo.*
import org.southasia.ghru.vo.request.ParticipantRequest
import javax.inject.Inject


class FundoscopyReadingViewModel
@Inject constructor(
    val assertRepository: AssertRepository,
    fundoscopyRepository: FundoscopyRepository,
    stationDevicesRepository: StationDevicesRepository
) : ViewModel() {
    var fundoscopySyncError: MutableLiveData<Boolean>? = MutableLiveData<Boolean>().apply { }


    private val _participantId: MutableLiveData<ParticipantRequest> = MutableLiveData()

    private val _participantIdComplte: MutableLiveData<ParticipantRequest> = MutableLiveData()
    private var comment: String? = null
    private var device_id: String? = null
    private var pupil_dilation : Boolean = false
    private var isOnline : Boolean = false
    private var cataractObservation : String? = null

    private val _stationName = MutableLiveData<String>()

    fun setStationName(stationName: Measurements) {
        val update = stationName.toString().toLowerCase()
        if (_stationName.value == update) {
            return
        }
        _stationName.value = update
    }

    var stationDeviceList: LiveData<Resource<List<StationDeviceData>>>? = Transformations
        .switchMap(_stationName) { input ->
            stationDevicesRepository.getStationDeviceList(_stationName.value!!)
        }

    var asserts: LiveData<Resource<ResourceData<List<Asset>>>>? = Transformations
        .switchMap(_participantId) { participantId ->
            if (participantId == null) {
                AbsentLiveData.create()
            } else {
                assertRepository.getAssets(participantId, "fundoscopy")
            }
        }

    var fundoscopyComplete: LiveData<Resource<ECG>>? = Transformations
        .switchMap(_participantIdComplte) { participantId ->
            if (participantId == null) {
                AbsentLiveData.create()
            } else {
                fundoscopyRepository.syncFundoscopy(participantId, comment, device_id,pupil_dilation,isOnline,cataractObservation!!)
            }
        }

    fun setParticipant(participantId: ParticipantRequest, mComment: String?, mDevice_id: String,dilation: Boolean,observation : String) {
        comment = mComment
        device_id = mDevice_id
        pupil_dilation = dilation
        cataractObservation = observation

        if (_participantId.value == participantId) {
            return
        }
        _participantId.value = participantId
    }

    fun setParticipantComplete(participantId: ParticipantRequest, mComment: String?, mDevice_id: String,dilation: Boolean,online: Boolean,observation : String) {
        comment = mComment
        device_id = mDevice_id
        pupil_dilation = dilation
        isOnline = online
        cataractObservation = observation

        if (_participantIdComplte.value == participantId) {
            return
        }
        _participantIdComplte.value = participantId
    }
}
