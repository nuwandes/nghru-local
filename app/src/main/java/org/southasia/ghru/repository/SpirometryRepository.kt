package org.southasia.ghru.repository

import androidx.lifecycle.LiveData
import com.birbit.android.jobqueue.JobManager
import com.google.gson.GsonBuilder
import com.nuvoair.sdk.launcher.NuvoairLauncherMeasurement
import org.southasia.ghru.AppExecutors
import org.southasia.ghru.api.ApiResponse
import org.southasia.ghru.api.NghruService
import org.southasia.ghru.db.SpiromentryRequestDao
import org.southasia.ghru.jobs.SyncBodyMeasurementMetaJob
import org.southasia.ghru.jobs.SyncSpirometryJob
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
class SpirometryRepository @Inject constructor(
    private val appExecutors: AppExecutors,
    private val nghruService: NghruService,
    private val spiromentryRequestDao : SpiromentryRequestDao,
    private val jobManager : JobManager
) : Serializable {

    fun syncSampleProcess(
        participantRequest: ParticipantRequest,
        spirometryRecordList: List<SpirometryRecord>,
        comment: String?,
        device_id: String?,
        turbine_id: String?,
        nuvoairLauncherMeasurement: NuvoairLauncherMeasurement?
    ): LiveData<Resource<ResourceData<CommonResponce>>> {
        val mSpirometryTesList = ArrayList<SpirometryTest>()
        spirometryRecordList.forEachIndexed { index, spirometryRecord ->
            mSpirometryTesList.add(
                SpirometryTest(
                    testNumber = index,
                    fev = spirometryRecord.fev.value.toString(),
                    fvc = spirometryRecord.fvc.value.toString(),
                    ratio = spirometryRecord.ratio.value.toString(),
                    pev = spirometryRecord.pEFR.value.toString()
                    )
            )
        }
        val mSpirometryTests =
            SpirometryTests(tests = mSpirometryTesList, device_id = device_id, turbine_id = turbine_id, deviceData = nuvoairLauncherMeasurement)
        val mSpirometryData = SpirometryData(body = mSpirometryTests)
        val gson = GsonBuilder().setPrettyPrinting().create()
        val mSpirometryRequest = SpirometryRequest(data = gson.toJson(mSpirometryData), comment = comment, meta = participantRequest.meta)



        return object : NetworkOnlyBoundResource<ResourceData<CommonResponce>>(appExecutors) {
            override fun createCall(): LiveData<ApiResponse<ResourceData<CommonResponce>>> {
                return nghruService.addSpirometrySync(participantRequest.screeningId, mSpirometryRequest)
            }
        }.asLiveData()
    }



    fun bodyMeasurementMeta(
        spirometryRequest: SpirometryRequest
    ): LiveData<Resource<BodyMeasurementMeta>> {
        return object : MyNetworkBoundResource<BodyMeasurementMeta, ResourceData<CommonResponce>>(appExecutors) {
            override fun createJob(insertedID: Long) {
                spirometryRequest.id = insertedID
                jobManager.addJobInBackground(SyncSpirometryJob(spirometryRequest))

            }


            override fun isNetworkAvilable(): Boolean {
                return spirometryRequest.syncPending
            }

            override fun saveDb(): Long {
                return spiromentryRequestDao.insert(spirometryRequest)
            }


            override fun createCall(): LiveData<ApiResponse<ResourceData<CommonResponce>>> {
                return nghruService.addSpirometrySync(spirometryRequest.screeningId, spirometryRequest)
            }
        }.asLiveData()
    }


    fun getSpirometryRequestFromLocalDB(

    ): LiveData<Resource<List<SpirometryRequest>>> {
        return object : LocalBoundResource<List<SpirometryRequest>>(appExecutors) {
            override fun loadFromDb(): LiveData<List<SpirometryRequest>> {

                var requestList : LiveData<List<SpirometryRequest>> = spiromentryRequestDao.getSpirometryRequestSyncPending()
                return requestList
            }
        }.asLiveData()
    }

    fun syncSpirometryRequest(
        spirometryRequest: SpirometryRequest
    ): LiveData<Resource<ResourceData<CommonResponce>>> {
        return object : SyncNetworkOnlyBcakgroundBoundResource<ResourceData<CommonResponce>>(appExecutors) {
            override fun createCall(): LiveData<ApiResponse<ResourceData<CommonResponce>>> {
                return nghruService.addSpirometrySync(spirometryRequest.screeningId, spirometryRequest)
            }

            override fun deleteCall() {
                spiromentryRequestDao.deleteRequest(spirometryRequest.id)
            }
        }.asLiveData()
    }

}
