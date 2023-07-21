package org.southasia.ghru.ui.datamanagement

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import org.southasia.ghru.api.ApiResponse
import org.southasia.ghru.db.BodyMeasurementMetaDao
import org.southasia.ghru.jobs.SyncHouseholdMemberJob
import org.southasia.ghru.repository.*
import org.southasia.ghru.ui.enumeration.createhousehold.CreateHouseholdViewModel
import org.southasia.ghru.util.AbsentLiveData
import org.southasia.ghru.vo.*
import org.southasia.ghru.vo.request.*
import org.southasia.ghru.vo.request.Member
import java.util.ArrayList
import javax.inject.Inject

class DataManagmentViewModel
@Inject constructor(bloodPressureRequestRepository: BloodPressureRequestRepository,
                    bodyMeasurementMetaRepository: BodyMeasurementMetaRepository,
                    ecgGRepository : ECGRepository,
                    spirometryRepository  : SpirometryRepository,
                    fundoscopyRepository : FundoscopyRepository,
                    cancelRequestRepository : CancelRequestRepository,
                    sampleRequestRepository : SampleRequestRepository,
                    axivityRepository : AxivityRepository,
                    householdRequestRepository : HouseholdRequestRepository,
                    participantRequestRepository : ParticipantRequestRepository,
                    membersRepository: MembersRepository


) : ViewModel() {

    private val _stationNameBP = MutableLiveData<String>()
    private val _stationNameBM = MutableLiveData<String>()
    private val _stationNameECG = MutableLiveData<String>()
    private val _stationNameSpiro = MutableLiveData<String>()
    private val _stationNameFundoscopy = MutableLiveData<String>()
    private val _stationCancel = MutableLiveData<String>()
    private val _stationSample = MutableLiveData<String>()
    private val _stationActivity = MutableLiveData<String>()
    private val _stationEnumaration = MutableLiveData<String>()
    private var _stationRegistration =  MutableLiveData<String>()
    private var _memberHouseHoldId = MutableLiveData<String>()
    private val _memberId: MutableLiveData<CreateHouseholdViewModel.MemberId> = MutableLiveData()

    private val _recordBloodPressureMetaRequest: MutableLiveData<BloodPressureMetaRequest> = MutableLiveData()
    private val _recordBodyMeasurementMeta : MutableLiveData<BodyMeasurementMeta> = MutableLiveData()
    private val _recordECGStatus : MutableLiveData<ECGStatus> = MutableLiveData()
    private val _recordSpirometryRequest : MutableLiveData<SpirometryRequest> = MutableLiveData()
    private val _recordFundoscopyRequest : MutableLiveData<FundoscopyRequest> = MutableLiveData()
    private val _recordCancel : MutableLiveData<CancelRequest> = MutableLiveData()
    private val _recordSample : MutableLiveData<SampleRequest> = MutableLiveData()
    private val _recordActivity : MutableLiveData<Axivity> = MutableLiveData()
    private val _householdRequestSync: MutableLiveData<HouseholdRequestMeta> = MutableLiveData()

    fun setStationNameBP(stationName: Measurements) {
        val update = stationName.toString().toLowerCase()
        if (_stationNameBP.value == update) {
            return
        }
        _stationNameBP.value = update
    }
    var stationBPLocalList: LiveData<MutableList<BloodPressureMetaRequest>>? = Transformations
        .switchMap(_stationNameBP) { input ->
            bloodPressureRequestRepository.getBloodPressureMetaRequestFromLocalDB()
        }

    fun setStationNameBM(stationName: Measurements) {
        val update = stationName.toString().toLowerCase()
        if (_stationNameBM.value == update) {
            return
        }
        _stationNameBM.value = update
    }

    var stationBMLocalList: LiveData<Resource<List<BodyMeasurementMeta>>>? = Transformations
        .switchMap(_stationNameBM) { input ->
            bodyMeasurementMetaRepository.getBodyMeasurementMetaListFromLocalDB()
        }

    fun setStationNameECG(stationName: Measurements) {
        val update = stationName.toString().toLowerCase()
        if (_stationNameECG.value == update) {
            return
        }
        _stationNameECG.value = update
    }

    var stationECGLocalList: LiveData<Resource<List<ECGStatus>>>? = Transformations
        .switchMap(_stationNameECG) { input ->
            ecgGRepository.getECGRequestFromLocalDB()
        }

    fun setStationNameSpiro(stationName: Measurements) {
        val update = stationName.toString().toLowerCase()
        if (_stationNameSpiro.value == update) {
            return
        }
        _stationNameSpiro.value = update
    }

    var stationSpiroLocalList: LiveData<Resource<List<SpirometryRequest>>>? = Transformations
        .switchMap(_stationNameSpiro) { input ->
            spirometryRepository.getSpirometryRequestFromLocalDB()
        }

    fun setStationNameFundoscopy(stationName: Measurements) {
        val update = stationName.toString().toLowerCase()
        if (_stationNameFundoscopy.value == update) {
            return
        }
        _stationNameFundoscopy.value = update
    }
    var stationFundoscopyLocalList: LiveData<Resource<List<FundoscopyRequest>>>? = Transformations
        .switchMap(_stationNameFundoscopy) { input ->
            fundoscopyRepository.getFundoscopyRequestFromLocalDB()
        }


    fun setStationCancel(stationName: Measurements) {
        val update = stationName.toString().toLowerCase()
        if (_stationCancel.value == update) {
            return
        }
        _stationCancel.value = update
    }

    var stationCancelLocalList: LiveData<Resource<List<CancelRequest>>>? = Transformations
        .switchMap(_stationCancel) { input ->
            cancelRequestRepository.getCancelRequestListFromLocalDB()
        }

    fun setStationSample(stationName: Measurements) {
        val update = stationName.toString().toLowerCase()
        if (_stationSample.value == update) {
            return
        }
        _stationSample.value = update
    }
    var stationSampleLocalList: LiveData<MutableList<SampleRequest>>? = Transformations
        .switchMap(_stationSample) { input ->
            sampleRequestRepository.getSampleRequestFromLocalDB()
        }

    fun setStationActivity(stationName: Measurements) {
        val update = stationName.toString().toLowerCase()
        if (_stationActivity.value == update) {
            return
        }
        _stationActivity.value = update
    }
    var stationActivityLocalList: LiveData<Resource<List<Axivity>>>? = Transformations
        .switchMap(_stationActivity) { input ->
            axivityRepository.getAxivityListFromLocalDB()
        }


    fun setStationEnumaration(stationName: Measurements) {
        val update = stationName.toString().toLowerCase()
        if (_stationEnumaration.value == update) {
            return
        }
        _stationEnumaration.value = update
    }
    var stationEnumarationList: LiveData<Resource<List<HouseholdRequestMeta>>> = Transformations
        .switchMap(_stationEnumaration) { input ->
            householdRequestRepository.getHouseholdRequestMetasByStatus(true)
        }

    fun setStationRegistration(stationName: Measurements) {
        val update = stationName.toString().toLowerCase()
        if (_stationRegistration.value == update) {
            return
        }
        _stationRegistration.value = update
    }
    var stationRegistration: LiveData<Resource<List<ParticipantRequest>>> = Transformations
        .switchMap(_stationRegistration) { input ->
            participantRequestRepository.getParticipantRequestsBySyncStatus(true)
        }

    fun setMember(memberHouseHoldId : String)
    {
        val update = memberHouseHoldId.toString().toLowerCase()
        if (_memberHouseHoldId.value == update) {
            return
        }
        _memberHouseHoldId.value = update
    }
    var memberList : LiveData<Resource<List<Member>>> = Transformations
        .switchMap(_stationRegistration) { input ->
            membersRepository.getHouseHoldMembers(_memberHouseHoldId.toString())
        }
    //------------------------------------------------ Request -----------------------------------


    fun setRecordBloodPressureMetaRequest(syncRecord : BloodPressureMetaRequest)
    {
        if(_recordBloodPressureMetaRequest.value == syncRecord)
        {
            return
        }
        _recordBloodPressureMetaRequest.value = syncRecord
    }
    var syncRecordBloodPressureMetaRequest : LiveData<Resource<ResourceData<BloodPressureMetaRequest>>>?= Transformations
        .switchMap(_recordBloodPressureMetaRequest) { input ->

               bloodPressureRequestRepository.syncBloodPressure(_recordBloodPressureMetaRequest?.value!!,_recordBloodPressureMetaRequest.value?.body?.screeningId!!)

        }
    fun setRecordBodyMeasurementMetaRequest(syncRecord : BodyMeasurementMeta)
    {
        if(_recordBodyMeasurementMeta.value == syncRecord)
        {
            return
        }
        _recordBodyMeasurementMeta.value = syncRecord
    }
    var syncRecordBodyMeasurementMetaRequest : LiveData<Resource<ResourceData<Message>>>?= Transformations
        .switchMap(_recordBodyMeasurementMeta) { input ->

            bodyMeasurementMetaRepository.syncBodyMeasurementMeta(_recordBodyMeasurementMeta.value!!,_recordBodyMeasurementMeta.value?.screeningId!!)
        }

    fun setRecordECGStatus(syncRecord: ECGStatus)
    {
        if(_recordECGStatus.value == syncRecord)
        {
            return
        }
        _recordECGStatus.value = syncRecord
    }
    var syncRecordECGStatus : LiveData<Resource<ResourceData<ECG>>>?= Transformations
        .switchMap(_recordECGStatus) { input ->
           ecgGRepository.syncECGStatus(_recordECGStatus.value!!)
        }

    fun setSpirometryRequest(syncRecord : SpirometryRequest)
    {
        if(_recordSpirometryRequest.value == syncRecord)
        {
            return
        }
        _recordSpirometryRequest.value = syncRecord
    }
    var syncRecordSpirometryRequest : LiveData<Resource<ResourceData<CommonResponce>>>?= Transformations
        .switchMap(_recordSpirometryRequest) { input ->
            spirometryRepository.syncSpirometryRequest(_recordSpirometryRequest.value!!)
        }

    fun setFundoscopyRequest(syncRecord : FundoscopyRequest)
    {
        if(_recordFundoscopyRequest.value == syncRecord)
        {
            return
        }
        _recordFundoscopyRequest.value = syncRecord
    }
    var syncFundoscopyRequest : LiveData<Resource<ResourceData<ECG>>>?= Transformations
        .switchMap(_recordSpirometryRequest) { input ->
            fundoscopyRepository.syncFundoscopyRequest(_recordFundoscopyRequest?.value!!)
        }

    fun setCancelRequest(syncRecord : CancelRequest)
    {
        if(_recordCancel.value == syncRecord)
        {
            return
        }
        _recordCancel.value = syncRecord
    }

    var syncCancelRequest : LiveData<Resource<ResourceData<MessageCancel>>>?= Transformations
        .switchMap(_recordCancel) { input ->
           cancelRequestRepository.syncCancel(_recordCancel.value!!)
        }

    fun setSampleRequest(syncRecord : SampleRequest)
    {
        if(_recordSample.value == syncRecord)
        {
            return
        }
        _recordSample.value = syncRecord
    }

    var syncSampleRequest : LiveData<Resource<Message>>?= Transformations
        .switchMap(_recordSample) { input ->
            val staorage = StorageDto(freezerId = _recordSample?.value?.freezerId)
            staorage.meta = _recordSample?.value?.meta
            sampleRequestRepository.syncSample(_recordSample?.value!!, staorage)
        }

    fun setAxivityRequest(syncRecord : Axivity)
    {
        if(_recordActivity.value == syncRecord)
        {
            return
        }
        _recordActivity.value = syncRecord
    }
    var syncAxivityRequest : LiveData<Resource<ResourceData<Message>>>?= Transformations
        .switchMap(_recordSample) { input ->

            axivityRepository.syncAxivityRequest(_recordActivity.value,_recordActivity.value?.screeningId!!)
        }

    fun setHouseholdRequestSyncRemote(household: HouseholdRequestMeta) {
        if (_householdRequestSync.value != household) {
            _householdRequestSync.postValue(household)
        }
    }
    var householdRequestSyncRemote: LiveData<Resource<ResponceData>>? = Transformations
        .switchMap(_householdRequestSync) { householdRequest ->
            if (householdRequest == null) {
                AbsentLiveData.create()
            } else {
                householdRequestRepository.syncHousehold(householdRequest)
            }
        }
    fun setMemberSyncedLocal(members: ArrayList<Member>?, household: HouseholdRequestMeta) {
        val memberId = CreateHouseholdViewModel.MemberId(members!!, household)
        if (_memberId.value == memberId) {
            return
        }
        _memberId.value = memberId
    }
    var memberSyncRemote: LiveData<Resource<ResourceData<List<SyncHouseholdMemberJob.MemberDTO>>>>? = Transformations
        .switchMap(_memberId) { member ->
            member.ifExists { memberX, participantRequest ->
                membersRepository.syncMembers(memberX, participantRequest)
            }

        }

}
