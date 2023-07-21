package org.southasia.ghru.repository

import androidx.lifecycle.LiveData
import com.google.gson.GsonBuilder
import org.southasia.ghru.AppExecutors
import org.southasia.ghru.api.ApiResponse
import org.southasia.ghru.api.NghruService
import org.southasia.ghru.db.SampleProcessDao
import org.southasia.ghru.vo.*
import org.southasia.ghru.vo.request.ParticipantRequest
import org.southasia.ghru.vo.request.SampleCreateRequest
import org.southasia.ghru.vo.request.SampleRequest
import java.io.Serializable
import javax.inject.Inject
import javax.inject.Singleton


/**
 * Repository that handles User objects.
 */

@Singleton
class SampleRepository @Inject constructor(
    private val appExecutors: AppExecutors,
    private val nghruService: NghruService,
    private val sampleProcessDao: SampleProcessDao
) : Serializable {

    fun syncSample(
        participantRequest: ParticipantRequest,
        sampleId: String,
        comment : SampleCreateRequest
    ): LiveData<Resource<ResourceData<SampleData>>> {
        return object : NetworkOnlyBoundResource<ResourceData<SampleData>>(appExecutors) {
            override fun createCall(): LiveData<ApiResponse<ResourceData<SampleData>>> {
                return nghruService.addSampleSync(participantRequest.screeningId, sampleId,comment)
            }
        }.asLiveData()
    }

    fun syncSampleProcess(
        hb1Ac: Hb1AcDto?,
        fastingBloodGlucose: FastingBloodGlucoseDto?,
        lipidProfile: LipidProfileAllDto?,
        hOGTT: HOGTTDto?,
        hemoglobin: HemoglobinDto?,
        sampleId: SampleRequest?
    ): LiveData<Resource<Message>> {
        val dataX = Sampleprocessdata(hb1Ac, fastingBloodGlucose, lipidProfile, hOGTT, hemoglobin)
        val gson = GsonBuilder().setPrettyPrinting().create()
        val dataS = gson.toJson(dataX)
        return object : NetworkOnlyBoundResource<Message>(appExecutors) {
            override fun createCall(): LiveData<ApiResponse<Message>> {
                val mSampleProcess = SampleProcess(1000, data = dataS, storageId = sampleId?.storageId!!)
                mSampleProcess?.meta = sampleId.meta
                //L.d(mSampleProcess.toString())
                return nghruService.addSampleProcessSync(sampleId.sampleId, mSampleProcess)
            }
        }.asLiveData()
    }

    fun insertSampleRequest(
        hb1Ac: Hb1AcDto?,
        fastingBloodGlucose: FastingBloodGlucoseDto?,
        lipidProfile: LipidProfileAllDto,
        hOGTT: HOGTTDto?,
        hemoglobin: HemoglobinDto?,
        sampleId: SampleRequest?,
        syncPending: Boolean
    ): LiveData<Resource<SampleProcess>> {
        val dataX = Sampleprocessdata(hb1Ac, fastingBloodGlucose, lipidProfile, hOGTT, hemoglobin)
        val gson = GsonBuilder().setPrettyPrinting().create()
        val dataS = gson.toJson(dataX)
        val mSampleProcess = SampleProcess(1000, data = dataS, storageId = sampleId?.storageId!!)
        mSampleProcess.syncPending = syncPending
        return object : LocalBoundInsertResource<SampleProcess>(appExecutors) {
            override fun loadFromDb(rowId: Long): LiveData<SampleProcess> {
                return sampleProcessDao.getSampleProcess(rowId)
            }

            override fun insertDb(): Long {
                return sampleProcessDao.insert(mSampleProcess)
            }
        }.asLiveData()
    }

}
