package org.southasia.ghru.ui.spirometry.tests

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.nuvoair.sdk.launcher.NuvoairLauncherMeasurement
import org.southasia.ghru.repository.BodyMeasurementRequestRepository
import org.southasia.ghru.repository.SpirometryRepository
import org.southasia.ghru.repository.StationDevicesRepository
import org.southasia.ghru.util.AbsentLiveData
import org.southasia.ghru.vo.*
import org.southasia.ghru.vo.request.BodyMeasurementMeta
import org.southasia.ghru.vo.request.BodyMeasurementMetaResonce
import org.southasia.ghru.vo.request.ParticipantRequest
import javax.inject.Inject


class TestModel
@Inject constructor(
    spirometryRepository: SpirometryRepository,
    stationDevicesRepository: StationDevicesRepository,
    bodyMeasurementRequestRepository: BodyMeasurementRequestRepository
) : ViewModel() {
    private val _testId: MutableLiveData<TestId> = MutableLiveData()


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

    var sync: LiveData<Resource<ResourceData<CommonResponce>>>? = Transformations
        .switchMap(_testId) { input ->
            input.ifExists { participantRequest, spirometryRecordList, comment, device_id, turbine_id , nuvoairLauncherMeasurement ->
                spirometryRepository.syncSampleProcess(
                    participantRequest = participantRequest,
                    spirometryRecordList = spirometryRecordList,
                    comment = comment,
                    device_id = device_id,
                    turbine_id = turbine_id,
                    nuvoairLauncherMeasurement = nuvoairLauncherMeasurement
                )
            }
        }

    fun setData(
        participant: ParticipantRequest?,
        recordList: ArrayList<SpirometryRecord>,
        comment: String?,
        device_id: String?,
        turbine_id: String?,
        nuvoairLauncherMeasurement: NuvoairLauncherMeasurement?
    ) {
        val update = TestId(participant, recordList, comment, device_id, turbine_id, nuvoairLauncherMeasurement)
        if (_testId.value == update) {
            return
        }
        _testId.value = update
    }


    private val _bodyMeasurementMetaId: MutableLiveData<BodyMeasurementMetaId> = MutableLiveData()


    var bodyMeasurementMeta: LiveData<Resource<BodyMeasurementMeta>>? = Transformations
        .switchMap(_bodyMeasurementMetaId) { input ->
            input.ifExists { participant, isOnline ->
                bodyMeasurementRequestRepository.getBodyMeasurementMeta(participant, isOnline)

            }
        }




    fun setParticipant(participantRequest: ParticipantRequest, isOnline : Boolean) {
        val update = BodyMeasurementMetaId(participantRequest, isOnline)
        if (_bodyMeasurementMetaId.value == update) {
            return
        }
        _bodyMeasurementMetaId.value = update
    }

    private val _spirometryRequest: MutableLiveData<SpirometryRequest> = MutableLiveData()

    val spirometryRequest: LiveData<Resource<BodyMeasurementMeta>>? = Transformations
        .switchMap(_spirometryRequest) { spirometryRequest ->
            if (spirometryRequest == null) {
                AbsentLiveData.create()
            } else {
                spirometryRepository.bodyMeasurementMeta(spirometryRequest)
            }
        }

    fun setSpirometryRequest(spirometryRequest: SpirometryRequest) {
        if (_spirometryRequest.value == spirometryRequest) {
            return
        }
        _spirometryRequest.value = spirometryRequest
    }

    data class BodyMeasurementMetaId(
        val participant: ParticipantRequest?,
        val isOnline: Boolean
    ) {
        fun <T> ifExists(f: (ParticipantRequest, Boolean) -> LiveData<T>): LiveData<T> {
            return if (participant == null || !isOnline) {
                AbsentLiveData.create()
            } else {
                f(participant, isOnline)
            }
        }
    }


    data class TestId(
        val participant: ParticipantRequest?,
        val recordList: ArrayList<SpirometryRecord>?,
        val comment: String?,
        val device_id: String?,
        val turbine_id: String?,
        val nuvoairLauncherMeasurement: NuvoairLauncherMeasurement?
    ) {
        fun <T> ifExists(f: (ParticipantRequest, ArrayList<SpirometryRecord>, String?, String?, String?, NuvoairLauncherMeasurement? ) -> LiveData<T>): LiveData<T> {
            return if (participant == null || recordList == null) {
                AbsentLiveData.create()
            } else {
                f(participant!!, recordList!!, comment, device_id, turbine_id, nuvoairLauncherMeasurement)
            }
        }
    }
}