package org.southasia.ghru.repository

import androidx.lifecycle.LiveData
import com.birbit.android.jobqueue.JobManager
import org.southasia.ghru.AppExecutors
import org.southasia.ghru.api.ApiResponse
import org.southasia.ghru.api.NghruService
import org.southasia.ghru.db.BodyMeasurementMetaDao
import org.southasia.ghru.jobs.SyncBodyMeasurementMetaJob
import org.southasia.ghru.vo.Message
import org.southasia.ghru.vo.Resource
import org.southasia.ghru.vo.ResourceData
import org.southasia.ghru.vo.StationDeviceData
import org.southasia.ghru.vo.request.BloodPressureMetaRequest
import org.southasia.ghru.vo.request.BodyMeasurementMeta
import java.io.Serializable
import javax.inject.Inject
import javax.inject.Singleton


/**
 * Repository that handles User objects.
 */

@Singleton
class BodyMeasurementMetaRepository @Inject constructor(
    private val appExecutors: AppExecutors,
    private val bodyMeasurementMetaDao: BodyMeasurementMetaDao,
    private val nghruService: NghruService,
    private val jobManager: JobManager
) : Serializable {

    fun bodyMeasurementMeta(
        bodyMeasurementMeta: BodyMeasurementMeta
    ): LiveData<Resource<BodyMeasurementMeta>> {
        return object : MyNetworkBoundResource<BodyMeasurementMeta, ResourceData<Message>>(appExecutors) {
            override fun createJob(insertedID: Long) {
                bodyMeasurementMeta.id = insertedID
                jobManager.addJobInBackground(

                    SyncBodyMeasurementMetaJob(
                        bodyMeasurementMeta = bodyMeasurementMeta,
                        screeningId = bodyMeasurementMeta.screeningId
                    )
                )
            }

            override fun isNetworkAvilable(): Boolean {
                return bodyMeasurementMeta.syncPending
            }

            override fun saveDb(): Long {
                return bodyMeasurementMetaDao.insert(bodyMeasurementMeta)
            }


            override fun createCall(): LiveData<ApiResponse<ResourceData<Message>>> {
                return nghruService.addBodyMeasurementMeta(bodyMeasurementMeta.screeningId, bodyMeasurementMeta)
            }
        }.asLiveData()
    }

    fun getBodyMeasurementMetaListFromLocalDB(

    ): LiveData<Resource<List<BodyMeasurementMeta>>> {
        return object : LocalBoundResource<List<BodyMeasurementMeta>>(appExecutors) {
            override fun loadFromDb(): LiveData<List<BodyMeasurementMeta>> {
                return bodyMeasurementMetaDao.getBodyMeasurementMetasSyncPending()
            }
        }.asLiveData()
    }

    fun syncBodyMeasurementMeta(
        bodyMeasurementMeta: BodyMeasurementMeta,
        screeningID : String
    ): LiveData<Resource<ResourceData<Message>>> {
        return object : SyncNetworkOnlyBcakgroundBoundResource<ResourceData<Message>>(appExecutors) {
            override fun createCall(): LiveData<ApiResponse<ResourceData<Message>>> {
                return  nghruService.addBodyMeasurementMeta(screeningID,bodyMeasurementMeta)
            }

            override fun deleteCall() {
                bodyMeasurementMetaDao.deleteRequest(bodyMeasurementMeta.id)
            }
        }.asLiveData()
    }
}
