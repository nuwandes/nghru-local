package org.southasia.ghru.repository

import androidx.lifecycle.LiveData
import com.birbit.android.jobqueue.JobManager
import org.southasia.ghru.AppExecutors
import org.southasia.ghru.api.ApiResponse
import org.southasia.ghru.api.NghruService
import org.southasia.ghru.db.FundoscopyRequestDao
import org.southasia.ghru.db.MetaNewDao
import org.southasia.ghru.jobs.SyncECGJob
import org.southasia.ghru.jobs.SyncFundoscopyJob
import org.southasia.ghru.vo.*
import org.southasia.ghru.vo.request.BodyMeasurementMeta
import org.southasia.ghru.vo.request.ParticipantRequest
import java.io.Serializable
import javax.inject.Inject
import javax.inject.Singleton


/**
 * Repository that handles User objects.
 */

@Singleton
class FundoscopyRepository @Inject constructor(
    private val appExecutors: AppExecutors,
    private val nghruService: NghruService,
    private val jobManager: JobManager,
    private val metaNewDao: MetaNewDao,
    private val fundoscopyRequestDao: FundoscopyRequestDao
    ) : Serializable {

    fun syncFundoscopy(
        participantRequest: ParticipantRequest,
        comment: String?,
        device_id: String?,
        pupil_dilation:Boolean,
        isOnline : Boolean,
        cataractObservation : String
    ): LiveData<Resource<ECG>> {
        return object : MyNetworkBoundResource<ECG,ResourceData<ECG>>(appExecutors) {

            override fun createJob(insertedID: Long) {
                val mFundoscopyRequest =  FundoscopyRequest(comment = comment, device_id = device_id, meta = participantRequest?.meta,pupil_dilation = pupil_dilation,cataract_observation = cataractObservation)
                mFundoscopyRequest.id = insertedID
                mFundoscopyRequest.screeningId = participantRequest?.screeningId!!
                mFundoscopyRequest.syncPending = !isOnline
                jobManager.addJobInBackground(
                    SyncFundoscopyJob(
                        participantRequest,
                        mFundoscopyRequest
                    )

                )
            }
            override fun isNetworkAvilable(): Boolean {
                return !isOnline
            }
            override fun saveDb(): Long {

                //var ecgMetaNewId = metaNewDao.insert(participantRequest?.meta!!)
                val mFundoscopyRequest =  FundoscopyRequest(comment = comment, device_id = device_id, meta = participantRequest?.meta,pupil_dilation = pupil_dilation,cataract_observation = cataractObservation)
                mFundoscopyRequest.screeningId = participantRequest?.screeningId!!
                //mFundoscopyRequest.fundoscopyMetaId = ecgMetaNewId
                mFundoscopyRequest.syncPending = !isOnline
                var id =  fundoscopyRequestDao.insert(mFundoscopyRequest)
                return id
            }
            override fun createCall(): LiveData<ApiResponse<ResourceData<ECG>>> {
                return nghruService.addFundoscopyGSync(
                    participantRequest.screeningId,
                    FundoscopyRequest(comment = comment, device_id = device_id, meta = participantRequest?.meta,pupil_dilation = pupil_dilation,cataract_observation = cataractObservation)
                )
            }
        }.asLiveData()

    }

    fun getFundoscopyRequestFromLocalDB(

    ): LiveData<Resource<List<FundoscopyRequest>>> {
        return object : LocalBoundResource<List<FundoscopyRequest>>(appExecutors) {
            override fun loadFromDb(): LiveData<List<FundoscopyRequest>> {
                return fundoscopyRequestDao.getFundoscopyRequestSyncPending()
            }
        }.asLiveData()
    }

    fun syncFundoscopyRequest(
        fundoscopyRequest: FundoscopyRequest
    ): LiveData<Resource<ResourceData<ECG>>> {
        return object : SyncNetworkOnlyBcakgroundBoundResource<ResourceData<ECG>>(appExecutors) {
            override fun createCall(): LiveData<ApiResponse<ResourceData<ECG>>> {
                return nghruService.addFundoscopyGSync(fundoscopyRequest?.screeningId,fundoscopyRequest)
            }

            override fun deleteCall() {

                fundoscopyRequestDao.deleteRequest(fundoscopyRequest.id)
            }
        }.asLiveData()
    }
}
