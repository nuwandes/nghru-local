package org.southasia.ghru.ui.ecg.trace

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import org.southasia.ghru.repository.ECGRepository
import org.southasia.ghru.repository.StationDevicesRepository
import org.southasia.ghru.vo.Measurements
import org.southasia.ghru.vo.Resource
import org.southasia.ghru.vo.StationDeviceData
import org.southasia.ghru.vo.request.ParticipantRequest
import javax.inject.Inject


class TraceViewModel
@Inject constructor(
    eCGRepository: ECGRepository,
    stationDevicesRepository: StationDevicesRepository
) : ViewModel() {

    private val _participantRequestRemote: MutableLiveData<ParticipantRequest> = MutableLiveData()

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

//
//    var eCGSaveRemote: LiveData<Resource<ResourceData<ECG>>>? = Transformations
//            .switchMap(_participantRequestRemote) { participant ->
//                if (participant == null) {
//                    AbsentLiveData.create()
//                } else {
//                    eCGRepository.syncECG(participant)
//                }
//            }
//
//    fun setECGRemote(participantRequest: ParticipantRequest) {
//        if (_participantRequestRemote.value != participantRequest) {
//            _participantRequestRemote.postValue(participantRequest)
//        }
//    }
}
