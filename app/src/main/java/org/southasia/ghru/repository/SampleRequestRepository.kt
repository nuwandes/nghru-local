package org.southasia.ghru.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.birbit.android.jobqueue.JobManager
import org.southasia.ghru.AppExecutors
import org.southasia.ghru.api.ApiResponse
import org.southasia.ghru.api.NghruService
import org.southasia.ghru.db.MetaNewDao
import org.southasia.ghru.db.SampleRequestDao
import org.southasia.ghru.jobs.SyncAxivityJob
import org.southasia.ghru.jobs.SyncSampledStorageFreezeIDJob
import org.southasia.ghru.vo.*
import org.southasia.ghru.vo.request.BloodPressureMetaRequest
import org.southasia.ghru.vo.request.BloodPresureRequest
import org.southasia.ghru.vo.request.CancelRequest
import org.southasia.ghru.vo.request.SampleRequest
import java.io.Serializable
import javax.inject.Inject
import javax.inject.Singleton


/**
 * Repository that handles User objects.
 */

@Singleton
class SampleRequestRepository @Inject constructor(
    private val appExecutors: AppExecutors,
    private val sampleRequestDao: SampleRequestDao,
    private val nghruService: NghruService,
    private val jobManager: JobManager,
    private val metaNewDao : MetaNewDao
    ) : Serializable {


    fun insertSampleRequest(
        sampleRequest: SampleRequest
    ): LiveData<Resource<SampleRequest>> {
        return object : LocalBoundInsertResource<SampleRequest>(appExecutors) {
            override fun loadFromDb(rowId: Long): LiveData<SampleRequest> {
                return sampleRequestDao.getSampleRequest(sampleRequest.sampleId)
            }

            override fun insertDb(): Long {
                var idMeta = metaNewDao.insert(sampleRequest?.meta!!)
                sampleRequest.metaId = idMeta
                return sampleRequestDao.insert(sampleRequest)
            }
        }.asLiveData()
    }

    fun updateSampleRequest(
        sampleRequest: SampleRequest
    ): LiveData<Resource<SampleRequest>> {
        return object : LocalBoundUpateResource<SampleRequest, Int>(appExecutors) {
            override fun loadFromDb(rowId: Int): LiveData<SampleRequest> {
                return sampleRequestDao.getSampleRequest(sampleRequest.sampleId)
            }

            override fun updateDb(): Int {
                return sampleRequestDao.update(sampleRequest)
            }
        }.asLiveData()
    }


    fun getSampleRequests(
    ): LiveData<Resource<List<SampleRequest>>> {
        return object : LocalBoundResource<List<SampleRequest>>(appExecutors) {
            override fun loadFromDb(): LiveData<List<SampleRequest>> {
                return sampleRequestDao.getSampleRequests(statusCode = 1, isCancelled = 0)
            }
        }.asLiveData()
    }


    fun getSampleRequestBySampleIdOffline(
        sampleId: String
    ): LiveData<Resource<SampleRequest>> {
        return object : LocalBoundResource<SampleRequest>(appExecutors) {
            override fun loadFromDb(): LiveData<SampleRequest> {
                return sampleRequestDao.getSampleRequestBySampleId(sampleId, 0)
            }
        }.asLiveData()
    }


    fun getSampleRequestByStorageIDOfflineByStorageID(
        storageID: String
    ): LiveData<Resource<SampleRequest>> {
        return object : LocalBoundResource<SampleRequest>(appExecutors) {
            override fun loadFromDb(): LiveData<SampleRequest> {
                return sampleRequestDao.getSampleRequestByStorageIDOfflineByStorageID(storageID, 0)
            }
        }.asLiveData()
    }

    fun getSamplePendingOnline(
    ): LiveData<Resource<List<SampleRequest>>> {
        return object : NetworkBoundResource<List<SampleRequest>, ResourceData<List<SampleRequest>>>(appExecutors) {
            override fun saveCallResult(item: ResourceData<List<SampleRequest>>) {
                //sampleRequestDao.nukeTable()
                sampleRequestDao.insert(item.data!!)
            }

            override fun shouldFetch(data: List<SampleRequest>?) = true

            override fun loadFromDb(): LiveData<List<SampleRequest>> {
                return sampleRequestDao.getSampleRequests(statusCode = 1, isCancelled = 0)
            }

            override fun createCall(): LiveData<ApiResponse<ResourceData<List<SampleRequest>>>> {

                return nghruService.getSamplePending("progress")
            }
        }.asLiveData()
    }

    fun getSampleStoragePendingOnline(
    ): LiveData<Resource<List<SampleRequest>>> {
        return object : NetworkBoundResource<List<SampleRequest>, ResourceData<List<SampleRequest>>>(appExecutors) {
            override fun saveCallResult(item: ResourceData<List<SampleRequest>>) {
                //sampleRequestDao.nukeTable()
                sampleRequestDao.insert(item.data!!)
            }

            override fun shouldFetch(data: List<SampleRequest>?): Boolean = data == null || data.size <= 0

            override fun loadFromDb(): LiveData<List<SampleRequest>> {
                return sampleRequestDao.getSampleRequestsStorage(statusCode = 1000, isCancelled = 0)
            }

            override fun createCall(): LiveData<ApiResponse<ResourceData<List<SampleRequest>>>> {

                return nghruService.getSamplePending("processed")
            }
        }.asLiveData()
    }


    fun syncSampleStorageFRequest(
        sampleStorageRequest: SampleRequest?,
        storageDto: StorageDto

    ): LiveData<Resource<Message>> {
        return object : MyNetworkBoundResource<Message,Message>(appExecutors) {

            override fun createJob(insertedID: Long) {
                jobManager.addJobInBackground(SyncSampledStorageFreezeIDJob(storageDto = storageDto, sampleStorageRequest = sampleStorageRequest))
            }
            override fun isNetworkAvilable(): Boolean {

                return sampleStorageRequest?.syncPending!!
            }
            override fun saveDb(): Long {

                return sampleRequestDao.insert(sampleStorageRequest!!)
            }
            override fun createCall(): LiveData<ApiResponse<Message>> {
                return nghruService.addStorageSync(sampleStorageRequest?.storageId!!, storageDto)
            }
        }.asLiveData()
    }


    fun delete(
        sampleRequest: SampleRequest
    ): LiveData<Resource<SampleRequest>> {
        return object : LocalBoundIDeleteResource<SampleRequest>(appExecutors) {
            override fun deleteDb() {
                return sampleRequestDao.delete(sampleRequest)
            }

        }.asLiveData()
    }


    fun getSamples(
    ): LiveData<Resource<List<SampleRequest>>> {
        return object : NetworkBoundResource<List<SampleRequest>, ResourceData<List<SampleRequest>>>(appExecutors) {
            override fun saveCallResult(item: ResourceData<List<SampleRequest>>) {

                sampleRequestDao.insert(item.data!!)
            }

            override fun shouldFetch(data: List<SampleRequest>?): Boolean = true

            override fun loadFromDb(): LiveData<List<SampleRequest>> {
                return sampleRequestDao.getSampleRequestAlls()
            }

            override fun createCall(): LiveData<ApiResponse<ResourceData<List<SampleRequest>>>> {
                return nghruService.getSamples()
            }

        }.asLiveData()
    }


    fun getItemId(
        sampleId: String
    ): LiveData<Resource<SampleRequest>> {
        return object : NetworkBoundResource<SampleRequest, ResourceData<SampleRequest>>(appExecutors) {
            override fun saveCallResult(item: ResourceData<SampleRequest>) {
                if(item.data != null && item.data.sampleId !=null) {
                    sampleRequestDao.insert(item.data!!)
                }
            }


            override fun shouldFetch(data: SampleRequest?): Boolean = data == null

            override fun loadFromDb(): LiveData<SampleRequest> {
                return sampleRequestDao.getSampleRequest(sampleId)
            }

            override fun createCall(): LiveData<ApiResponse<ResourceData<SampleRequest>>> {
                return nghruService.getSampleBySampleId(sampleId)
            }

        }.asLiveData()
    }


    fun getStorageId(
        storageId: String
    ): LiveData<Resource<SampleRequest>> {
        return object : NetworkBoundResource<SampleRequest, ResourceData<SampleRequest>>(appExecutors) {
            override fun saveCallResult(item: ResourceData<SampleRequest>) {
                sampleRequestDao.insert(item.data!!)
            }


            override fun shouldFetch(data: SampleRequest?): Boolean = data == null

            override fun loadFromDb(): LiveData<SampleRequest> {
                return sampleRequestDao.getSampleRequestByStorageId(storageId)
            }

            override fun createCall(): LiveData<ApiResponse<ResourceData<SampleRequest>>> {
                return nghruService.getSampleByStorageId(storageId)
            }

        }.asLiveData()
    }


    fun updattSampleRequestBySampleIdProcccesed(sampleRequest: SampleRequest
    ): LiveData<Resource<SampleRequest>> {
        return object : LocalBoundUpateResource<SampleRequest, Int>(appExecutors) {
            override fun loadFromDb(rowId: Int): LiveData<SampleRequest> {
                return sampleRequestDao.getSampleRequestByID(sampleRequest.sampleId)
            }

            override fun updateDb(): Int {
                return sampleRequestDao.updattSampleRequestBySampleId(statusCode = 1000, sampleId = sampleRequest.sampleId, storageId = sampleRequest.storageId!!)

            }

        }.asLiveData()
    }

    fun getSampleRequestFromLocalDB(
    ): MutableLiveData<MutableList<SampleRequest>> {
        return object : MyLocalBoundResource<MutableList<SampleRequest>>(appExecutors) {
            override fun loadFromDb(): MutableLiveData<MutableList<SampleRequest>> {

                var request = MutableLiveData<MutableList<SampleRequest>>()
                var requestList : MutableList<SampleRequest> = ArrayList()
                var sampleRequestRequestList : List<SampleRequest> = sampleRequestDao.getSampleRequestSyncPending()
                for(sampleRequest in sampleRequestRequestList) {
                    sampleRequest.meta = metaNewDao.getNewMeta(sampleRequest.metaId)
                    requestList.add(sampleRequest)
                }
                request.postValue(requestList)
                return  request
            }
        }.asLiveData()
    }
    fun syncSample(
        sampleRequest: SampleRequest,
        storageDto: StorageDto
    ): LiveData<Resource<Message>> {
        return object : SyncNetworkOnlyBcakgroundBoundResource<Message>(appExecutors) {
            override fun createCall(): LiveData<ApiResponse<Message>> {
                return nghruService.addStorageSync(sampleRequest?.storageId!!, storageDto)
            }
            override fun deleteCall() {
                sampleRequestDao.deleteRequest(sampleRequest.sampleId)
            }
        }.asLiveData()
    }

}
