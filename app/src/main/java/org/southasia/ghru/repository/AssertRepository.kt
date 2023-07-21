package org.southasia.ghru.repository

import androidx.lifecycle.LiveData
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.southasia.ghru.AppExecutors
import org.southasia.ghru.api.ApiResponse
import org.southasia.ghru.api.NghruService
import org.southasia.ghru.db.AssetDao
import org.southasia.ghru.util.TokenManager
import org.southasia.ghru.vo.Asset
import org.southasia.ghru.vo.Message
import org.southasia.ghru.vo.Resource
import org.southasia.ghru.vo.ResourceData
import org.southasia.ghru.vo.request.ParticipantRequest
import java.io.File
import java.io.Serializable
import javax.inject.Inject
import javax.inject.Singleton


/**
 * Repository that handles User objects.
 */

@Singleton
class AssertRepository @Inject constructor(
    private val appExecutors: AppExecutors,
    private val assetDao: AssetDao,
    private val nghruService: NghruService,
    private val tokenManager: TokenManager
) : Serializable {

//    fun syncAsset(
//            household: Asset
//    ): LiveData<Resource<ResourceData<Asset>>> {
//        return object : NetworkOnlyBoundResource<Asset, ResourceData<Asset>>(appExecutors) {
//            override fun createCall(): LiveData<ApiResponse<ResourceData<Asset>>> {
//                return nghruService.addAsset(tokenManager.getToken().accessToken, household)
//            }
//        }.asLiveData()
//    }

    fun getAssets(
        participant: ParticipantRequest,
        purpose: String
    ): LiveData<Resource<ResourceData<List<Asset>>>> {
        return object : NetworkOnlyBoundResource<ResourceData<List<Asset>>>(appExecutors) {
            override fun createCall(): LiveData<ApiResponse<ResourceData<List<Asset>>>> {
                return nghruService.getAssets(screeningId = participant.screeningId, purpose = purpose)
            }
        }.asLiveData()
    }

    fun uploadProfile(
        profileImage: String,
        participantRequest: ParticipantRequest
    ): LiveData<Resource<Message>> {
        return object : NetworkOnlyBoundResource<Message>(appExecutors) {
            override fun createCall(): LiveData<ApiResponse<Message>> {
                val file = File(profileImage)
                val reqFile = RequestBody.create(MediaType.parse("image/jpeg"), file)
                val body = MultipartBody.Part.createFormData("image", file.getName(), reqFile)
                val screeningId = RequestBody.create(MediaType.parse("text/plain"), participantRequest.screeningId)
                val subject = RequestBody.create(MediaType.parse("text/plain"), participantRequest.screeningId)
                val patient = RequestBody.create(MediaType.parse("text/plain"), "Screening")
                val id = RequestBody.create(MediaType.parse("text/plain"), "profile")
                return nghruService.upload(
                    screeningId = screeningId,
                    subjectId = subject,
                    subjectType = patient,
                    purpose = id,
                    image = body
                )
            }
        }.asLiveData()
    }

    fun uploadConcent(
        profileImage: String,
        screeningId: String
    ): LiveData<Resource<Message>> {
        return object : NetworkOnlyBoundResource<Message>(appExecutors) {
            override fun createCall(): LiveData<ApiResponse<Message>> {
                val file = File(profileImage)
                val reqFile = RequestBody.create(MediaType.parse("image/jpeg"), file)
                val body = MultipartBody.Part.createFormData("image", file.getName(), reqFile)
                val screeningIdX = RequestBody.create(MediaType.parse("text/plain"), screeningId)
                val subject = RequestBody.create(MediaType.parse("text/plain"), screeningId.toString())
                val patient = RequestBody.create(MediaType.parse("text/plain"), "Screening")
                val id = RequestBody.create(MediaType.parse("text/plain"), "consent")
                return nghruService.upload(
                    screeningId = screeningIdX,
                    subjectId = subject,
                    subjectType = patient,
                    purpose = id,
                    image = body
                )
            }
        }.asLiveData()
    }

    fun uploadIdCard(
        identityImage: String,
        participantRequest: ParticipantRequest
    ): LiveData<Resource<Message>> {
        return object : NetworkOnlyBoundResource<Message>(appExecutors) {
            override fun createCall(): LiveData<ApiResponse<Message>> {
                val file = File(identityImage)
                val reqFile = RequestBody.create(MediaType.parse("image/jpeg"), file)
                val body = MultipartBody.Part.createFormData("image", file.getName(), reqFile)
                val screeningId = RequestBody.create(MediaType.parse("text/plain"), participantRequest.screeningId)
                val subject = RequestBody.create(MediaType.parse("text/plain"), participantRequest.screeningId)
                val patient = RequestBody.create(MediaType.parse("text/plain"), "Screening")
                val id = RequestBody.create(MediaType.parse("text/plain"), "id")
                return nghruService.upload(
                    screeningId = screeningId,
                    subjectId = subject,
                    subjectType = patient,
                    purpose = id,
                    image = body
                )
            }
        }.asLiveData()
    }


    fun insertAsset(
        household: Asset
    ): LiveData<Resource<Asset>> {
        return object : LocalBoundInsertResource<Asset>(appExecutors) {
            override fun loadFromDb(rowId: Long): LiveData<Asset> {
                return assetDao.getAsset(rowId)
            }

            override fun insertDb(): Long {
                return assetDao.insert(household)
            }
        }.asLiveData()
    }


    fun getAssets(
    ): LiveData<Resource<List<Asset>>> {
        return object : LocalBoundResource<List<Asset>>(appExecutors) {
            override fun loadFromDb(): LiveData<List<Asset>> {
                var households = assetDao.getAssets()
                return households
            }
        }.asLiveData()
    }

}
