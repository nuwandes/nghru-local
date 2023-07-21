package org.southasia.ghru.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.birbit.android.jobqueue.JobManager
import org.southasia.ghru.AppExecutors
import org.southasia.ghru.api.ApiResponse
import org.southasia.ghru.api.NghruService
import org.southasia.ghru.db.ECGStatusDao
import org.southasia.ghru.db.MetaNewDao
import org.southasia.ghru.jobs.SyncBodyMeasurementMetaJob
import org.southasia.ghru.jobs.SyncECGJob
import org.southasia.ghru.vo.*
import org.southasia.ghru.vo.request.BloodPressureMetaRequest
import org.southasia.ghru.vo.request.BodyMeasurementMeta
import org.southasia.ghru.vo.request.ParticipantRequest
import java.io.Serializable
import javax.inject.Inject
import javax.inject.Singleton


/**
 * Repository that handles User objects.
 */

@Singleton
class ECGRepository @Inject constructor(
    private val appExecutors: AppExecutors,
    private val nghruService: NghruService,
    private val jobManager: JobManager,
    private val metaNewDao: MetaNewDao,
    private val ecgStatusDao: ECGStatusDao
) : Serializable {

    fun syncECG(
        participantRequest: ParticipantRequest,
        status: String,
        comment: String?,
        device_id: String,
        isOnline : Boolean
    ): LiveData<Resource<ECG>> {
        return object : MyNetworkBoundResource<ECG,ResourceData<ECG>>(appExecutors) {

            override fun createJob(insertedID: Long) {
                val mECGStatus = ECGStatus(status, comment, device_id, meta= participantRequest?.meta)
                mECGStatus.syncPending = !isOnline
                mECGStatus.id = insertedID
                jobManager.addJobInBackground(

                    SyncECGJob(participantRequest, mECGStatus)
                )
            }
            override fun isNetworkAvilable(): Boolean {

                return !isOnline
            }
            override fun saveDb(): Long {
                val mECGStatus = ECGStatus(status, comment, device_id, meta= participantRequest?.meta)
                mECGStatus.syncPending = !isOnline
                var ecgMetaNewId = metaNewDao.insert(participantRequest?.meta!!)
                mECGStatus.metaId = ecgMetaNewId
                mECGStatus.screeningId = participantRequest?.screeningId
                return  ecgStatusDao.insert(mECGStatus)
            }
            override fun createCall(): LiveData<ApiResponse<ResourceData<ECG>>> {
                val mECGStatus = ECGStatus(status, comment, device_id, meta= participantRequest?.meta)
                mECGStatus.syncPending = !isOnline
                return nghruService.addECGSync(participantRequest.screeningId, mECGStatus)
            }
        }.asLiveData()
    }

//    fun getECGRequestFromLocalDB(
//
//    ): MutableLiveData<MutableList<ECGStatus>> {
//        return object : MyLocalBoundResource<MutableList<ECGStatus>>(appExecutors) {
//            override fun loadFromDb(): MutableLiveData<MutableList<ECGStatus>> {
//
//                var request = MutableLiveData<MutableList<ECGStatus>>()
//                var requestList : MutableList<ECGStatus> = ArrayList()
//                var ecgRequestList : List<ECGStatus> = ecgStatusDao.getECGStatusesSyncPending()
//                for(ecg in ecgRequestList)
//                {
//                    var ecgStatus : ECGStatus = ECGStatus(ecg.traceStatus,ecg.comment,ecg.device_id, metaNewDao.getNewMeta(ecg.metaId))
//                    ecgStatus.screeningId = ecg.screeningId
//                    requestList.add(ecgStatus)
//
//                }
//                request.postValue(requestList)
//                return request
//            }
//        }.asLiveData()
//    }

    fun getECGRequestFromLocalDB(

    ): LiveData<Resource<List<ECGStatus>>> {
        return object : LocalBoundResource<List<ECGStatus>>(appExecutors) {
            override fun loadFromDb(): LiveData<List<ECGStatus>> {
                return ecgStatusDao.getECGStatusesSyncPending()
            }
        }.asLiveData()
    }

    fun syncECGStatus(
        mECGStatus:ECGStatus
    ): LiveData<Resource<ResourceData<ECG>>> {
        return object : SyncNetworkOnlyBcakgroundBoundResource<ResourceData<ECG>>(appExecutors) {
            override fun createCall(): LiveData<ApiResponse<ResourceData<ECG>>> {
                return nghruService.addECGSync(mECGStatus.screeningId, mECGStatus)
            }

            override fun deleteCall() {
                return ecgStatusDao.deleteRequest(mECGStatus.id)
            }
        }.asLiveData()
    }
}
