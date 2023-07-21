package org.southasia.ghru.repository

import androidx.lifecycle.LiveData
import org.southasia.ghru.AppExecutors
import org.southasia.ghru.api.ApiResponse
import org.southasia.ghru.api.NghruService
import org.southasia.ghru.vo.CommonResponce
import org.southasia.ghru.vo.QuestionMeta
import org.southasia.ghru.vo.Resource
import org.southasia.ghru.vo.ResourceData
import org.southasia.ghru.vo.request.ParticipantRequest
import org.southasia.ghru.vo.request.ReportRequestMeta
import java.io.Serializable
import javax.inject.Inject
import javax.inject.Singleton


/**
 * Repository that handles User objects.
 */

@Singleton
class SurveyRepository @Inject constructor(
    private val appExecutors: AppExecutors,
    private val nghruService: NghruService
) : Serializable {

    fun syncSurvey(
        questionMeta: QuestionMeta

    ): LiveData<Resource<ResourceData<CommonResponce>>> {
        return object : NetworkOnlyBcakgroundBoundResource<ResourceData<CommonResponce>>(appExecutors) {
            override fun createCall(): LiveData<ApiResponse<ResourceData<CommonResponce>>> {
                return nghruService.addSurveySync(questionMeta)
            }
        }.asLiveData()
    }

    fun syncSurveyComplte(
        participant: ParticipantRequest, reportRequestMeta: ReportRequestMeta
    ): LiveData<Resource<ResourceData<CommonResponce>>> {
        return object : NetworkOnlyBcakgroundBoundResource<ResourceData<CommonResponce>>(appExecutors) {
            override fun createCall(): LiveData<ApiResponse<ResourceData<CommonResponce>>> {
                return nghruService.addSurveyCompleteSync(participant.screeningId, reportRequestMeta)
            }
        }.asLiveData()
    }


}
