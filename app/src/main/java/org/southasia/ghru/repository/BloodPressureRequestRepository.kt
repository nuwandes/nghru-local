package org.southasia.ghru.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.birbit.android.jobqueue.JobManager
import org.southasia.ghru.AppExecutors
import org.southasia.ghru.api.ApiResponse
import org.southasia.ghru.api.NghruService
import org.southasia.ghru.db.BloodPressureMetaRequestDao
import org.southasia.ghru.db.BloodPresureItemRequestDao
import org.southasia.ghru.db.BloodPresureRequestDao
import org.southasia.ghru.db.MetaNewDao
import org.southasia.ghru.jobs.SyncBloodPresureRequestJob
import org.southasia.ghru.vo.*
import org.southasia.ghru.vo.request.BloodPressureMetaRequest
import org.southasia.ghru.vo.request.BloodPresureItemRequest
import org.southasia.ghru.vo.request.BloodPresureRequest
import org.southasia.ghru.vo.request.ParticipantRequest
import java.io.Serializable
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BloodPressureRequestRepository @Inject constructor(
    private val appExecutors: AppExecutors,
    private val bloodPressureRequestDao: BloodPresureRequestDao,
    private val bloodPressureItemRequestDao: BloodPresureItemRequestDao,
    private val bloodPressureMetaRequestDao: BloodPressureMetaRequestDao,
    private val metaNewDao : MetaNewDao,
    private val nghruService: NghruService,
    private val jobManager: JobManager
) : Serializable {

    fun syncBloodPressureMetaRequest(
        bloodPressureMetaRequest: BloodPressureMetaRequest,
        particapant: ParticipantRequest

    ): LiveData<Resource<BloodPressureMetaRequest>> {
        return object : MyNetworkBoundResource<BloodPressureMetaRequest,ResourceData<BloodPressureMetaRequest>>(appExecutors) {
            override fun createJob(insertedID: Long) {
                bloodPressureMetaRequest?.body.id = insertedID
                jobManager.addJobInBackground(
                    SyncBloodPresureRequestJob(
                        particapant?.screeningId!!,
                        bloodPressureMetaRequest!!
                    )
                )
            }
            override fun isNetworkAvilable(): Boolean {
                return bloodPressureMetaRequest.syncPending
            }
            override fun saveDb(): Long {

                var idMeta = metaNewDao.insert(bloodPressureMetaRequest?.meta)
                bloodPressureMetaRequest?.body?.metaId = idMeta
                var idBloodPressureRequest = bloodPressureRequestDao.insert(bloodPressureMetaRequest?.body)
                for(t in bloodPressureMetaRequest?.body?.bloodPresureRequestList!!)
                {
                    t.bloodPresureRequestId=idBloodPressureRequest
                    bloodPressureItemRequestDao.insert(t)
                }

                return idBloodPressureRequest
            }
            override fun createCall(): LiveData<ApiResponse<ResourceData<BloodPressureMetaRequest>>> {
                return nghruService.addBloodPresureRequest(particapant.screeningId, bloodPressureMetaRequest)
            }
        }.asLiveData()
    }

    fun getBloodPressureMetaRequestFromLocalDB(

    ): MutableLiveData<MutableList<BloodPressureMetaRequest>>{
        return object : MyLocalBoundResource<MutableList<BloodPressureMetaRequest>>(appExecutors) {
            override fun loadFromDb(): MutableLiveData<MutableList<BloodPressureMetaRequest>> {

                var request = MutableLiveData<MutableList<BloodPressureMetaRequest>>()
                var requestList : MutableList<BloodPressureMetaRequest> = ArrayList()
                var bloodPresureRequestList : List<BloodPresureRequest> = bloodPressureRequestDao.getAllBloodPressureRequestsSyncPending()
                for(bloodPresureRequest in bloodPresureRequestList)
                {
                    bloodPresureRequest.bloodPresureRequestList = bloodPressureItemRequestDao.getBloodPressureItemsByBloodPresureRequestID(bloodPresureRequest.id)
                    //var metaRequest : BloodPressureMetaRequest = bloodPressureMetaRequestDao.getBloodPressureMetaRequestByBloodPressureID(bloodPresureRequest.id)
                    var bloodPressureMetaRequest : BloodPressureMetaRequest = BloodPressureMetaRequest(metaNewDao.getNewMeta(bloodPresureRequest.id),bloodPresureRequest)
                    requestList.add(bloodPressureMetaRequest)

                }
                request.postValue(requestList)
                return request
            }
        }.asLiveData()
    }

//    fun getBloodPressureMetaRequestFromLocalDB(
//
//    ): List<BloodPressureMetaRequest>{
//
//                var request : MutableList<BloodPressureMetaRequest> = ArrayList()
//
//                for(bloodPresureRequest in bloodPressureRequestDao.getAllBloodPressureRequestsSyncPending())
//                {
//                    bloodPresureRequest.bloodPresureRequestList = bloodPressureItemRequestDao.getBloodPressureItemsByBloodPresureRequestID(bloodPresureRequest.id)
//                    var metaRequest : BloodPressureMetaRequest = bloodPressureMetaRequestDao.getBloodPressureMetaRequestByBloodPressureID(bloodPresureRequest.id)
//                    var bloodPressureMetaRequest : BloodPressureMetaRequest = BloodPressureMetaRequest(metaNewDao.getNewMeta(metaRequest.id),bloodPresureRequest)
//                    request.add(bloodPressureMetaRequest)
//
//                }
//                return request
//
//    }



//    fun insertBloodPressureMetaRequest(
//        bloodPressureMetaRequest: BloodPressureMetaRequest
//    ): LiveData<Resource<BloodPressureMetaRequest>> {
//        return object : LocalBoundInsertResource<BloodPressureMetaRequest>(appExecutors) {
//            override fun loadFromDb(rowId: Long): LiveData<BloodPressureMetaRequest> {
//                return bloodPressureMetaRequestDao.getBloodPressureMetaRequest(rowId)
//            }
//
//            override fun insertDb(): Long {
//                return bloodPressureMetaRequestDao.insert(bloodPressureMetaRequest)
//            }
//
//        }.asLiveData()
//    }

//    fun insertBloodPressureRequest(
//        bloodPressureRequest: BloodPresureRequest
//    ): LiveData<Resource<BloodPresureRequest>> {
//        return object : LocalBoundInsertResource<BloodPresureRequest>(appExecutors) {
//
//            override fun loadFromDb(rowId: Long): LiveData<BloodPresureRequest> {
//                return bloodPressureRequestDao.getBloodPressureRequest(rowId)
//            }
//
//            override fun insertDb(): Long {
//                return bloodPressureRequestDao.insert(bloodPressureRequest)
//            }
//        }.asLiveData()
//    }

//    fun insertBloodPressureItemRequest(
//        bloodPressureItemRequest: BloodPresureItemRequest
//
//    ): LiveData<Resource<BloodPresureItemRequest>> {
//        return object : LocalBoundInsertResource<BloodPresureItemRequest>(appExecutors) {
//
//            override fun loadFromDb(rowId: Long): LiveData<BloodPresureItemRequest> {
//                return bloodPressureItemRequestDao.getBloodPressureItemRequest(bloodPressureItemRequest.id)
//            }
//            override fun insertDb(): Long {
//                return bloodPressureItemRequestDao.insert(bloodPressureItemRequest)
//            }
//        }.asLiveData()
//    }

    fun syncBloodPressure(
        bloodPressureMetaRequest: BloodPressureMetaRequest,
        screeningId: String
    ): LiveData<Resource<ResourceData<BloodPressureMetaRequest>>> {
        return object : SyncNetworkOnlyBcakgroundBoundResource<ResourceData<BloodPressureMetaRequest>>(appExecutors) {
            override fun createCall(): LiveData<ApiResponse<ResourceData<BloodPressureMetaRequest>>> {
                return nghruService.addBloodPresureRequest(screeningId, bloodPressureMetaRequest)
            }

            override fun deleteCall() {
                bloodPressureRequestDao.deleteRequest(bloodPressureMetaRequest.body.id)
            }
        }.asLiveData()
    }
}
