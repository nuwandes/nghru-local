package org.southasia.ghru.repository

import androidx.lifecycle.LiveData
import org.southasia.ghru.AppExecutors
import org.southasia.ghru.api.ApiResponse
import org.southasia.ghru.api.NghruService
import org.southasia.ghru.vo.*
import org.southasia.ghru.vo.request.IntakeRequest
import org.southasia.ghru.vo.request.IntakeRequestNew
import org.southasia.ghru.vo.request.IntakeResponse
import org.southasia.ghru.vo.request.ParticipantRequest
import java.io.Serializable
import javax.inject.Inject

class IntakeRepository  @Inject constructor (
    private val appExecutors: AppExecutors,
    private val nghruService: NghruService

) : Serializable {

    fun addIntake(
        intakeRequest: IntakeRequestNew,
        screening_id: String
    ): LiveData<Resource<ResourceData<IntakeResponse>>> {
        return object : NetworkOnlyBcakgroundBoundResource<ResourceData<IntakeResponse>>(appExecutors) {
            override fun createCall(): LiveData<ApiResponse<ResourceData<IntakeResponse>>> {
                return nghruService.postIntake(intakeRequest,screening_id)
            }
        }.asLiveData()
    }

    fun updateIntake(
        intakeRequest: IntakeRequestNew,
        screening_id: String
    ): LiveData<Resource<ResourceData<IntakeResponse>>> {
        return object : NetworkOnlyBcakgroundBoundResource<ResourceData<IntakeResponse>>(appExecutors) {
            override fun createCall(): LiveData<ApiResponse<ResourceData<IntakeResponse>>> {
                return nghruService.updateIntake(intakeRequest,screening_id)
            }
        }.asLiveData()
    }
}