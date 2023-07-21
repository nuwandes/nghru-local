package org.southasia.ghru.repository

import androidx.lifecycle.LiveData
import org.southasia.ghru.AppExecutors
import org.southasia.ghru.api.ApiResponse
import org.southasia.ghru.api.NghruService
import org.southasia.ghru.db.ParticipantMetaDao
import org.southasia.ghru.db.ParticipantRequestDao
import org.southasia.ghru.vo.CommonResponce
import org.southasia.ghru.vo.Resource
import org.southasia.ghru.vo.ResourceData
import org.southasia.ghru.vo.request.ParticipantMeta
import org.southasia.ghru.vo.request.ParticipantRequest
import java.io.Serializable
import javax.inject.Inject
import javax.inject.Singleton


/**
 * Repository that handles User objects.
 */

@Singleton
class ParticipantMetaRepository @Inject constructor(
    private val appExecutors: AppExecutors,
    private val participantRequestRequestDao: ParticipantMetaDao,
    private val participantRequestDao: ParticipantRequestDao,
    private val nghruService: NghruService
) : Serializable {

    fun syncParticipantMeta(
        participantRequest: ParticipantMeta
    ): LiveData<Resource<ResourceData<CommonResponce>>> {
        return object : NetworkOnlyBoundResource<ResourceData<CommonResponce>>(appExecutors) {
            override fun createCall(): LiveData<ApiResponse<ResourceData<CommonResponce>>> {
                 return nghruService.addParticipantMetaSync(participantRequest)
            }
        }.asLiveData()
    }

    fun insertParticipantMeta(
        participantRequest: ParticipantMeta
    ): LiveData<Resource<ParticipantMeta>> {
        return object : LocalBoundInsertResource<ParticipantMeta>(appExecutors) {
            override fun loadFromDb(rowId: Long): LiveData<ParticipantMeta> {
                return participantRequestRequestDao.getParticipantMeta(rowId)
            }

            override fun insertDb(): Long {
                return participantRequestRequestDao.insert(participantRequest)
            }
        }.asLiveData()
    }


    fun getItemId(
        screeningId: String
    ): LiveData<Resource<ParticipantRequest>> {
        return object : NetworkBoundResource<ParticipantRequest, ResourceData<ParticipantMeta>>(appExecutors) {
            override fun saveCallResult(item: ResourceData<ParticipantMeta>) {
                //L.d(item.data.toString())
                //L.d(item.data?.toString())

                val participantRequest = ParticipantRequest(
                    firstName = item.data?.body?.firstName!!,
                    lastName = item.data.body.lastName,
                    age = item.data.body.age,
                    gender = item.data.body.gender.toString().toLowerCase(),
                    idNumber = item.data.body.idNumber!!,
                    fatherName = "fathers name",
                    idType = "NID",
                    screeningId = item.data.body.screeningId,
                    householdId = item.data.body.enumerationId,
                    memberId = item.data.body.memberId,
                    profileImage = "",
                    identityImage = item.data.body.identityImage,
                    contactNumber = item.data.body.contactDetails.phoneNumberPreferred!!,
                    comment = item.data.body.comment
                )
                participantRequestDao.insert(participantRequest)
            }


            override fun shouldFetch(data: ParticipantRequest?): Boolean = data == null

            override fun loadFromDb(): LiveData<ParticipantRequest> {
                return participantRequestDao.getParticipantRequestByScreenId(screeningId)
            }

            override fun createCall(): LiveData<ApiResponse<ResourceData<ParticipantMeta>>> {
                return nghruService.getParticipantRequestMeta(screeningId)
            }

        }.asLiveData()
    }


    fun syncParticipantMetas(
    ): LiveData<Resource<List<ParticipantRequest>>> {
        return object :
            NetworkBoundResource<List<ParticipantRequest>, ResourceData<List<ParticipantMeta>>>(appExecutors) {
            override fun saveCallResult(item: ResourceData<List<ParticipantMeta>>) {
                ////L.d(item.data.toString())
                //L.d(item.data?.toString())
                val participantRequestList = arrayListOf<ParticipantRequest>()
                item.data?.forEach({
                    val participantRequest = ParticipantRequest(
                        firstName = it.body.firstName,
                        lastName = it.body.lastName,
                        age = it.body.age,
                        gender = it.body.gender.toString().toLowerCase(),
                        idNumber = it.body.idNumber!!,
//                        idNumber = "123456789",
                        fatherName = "fathers name",
                        idType = "NID",
                        screeningId = it.body.screeningId,
                        householdId = it.body.enumerationId,
                        memberId = it.body.memberId,
                        profileImage = "",
                        identityImage = it.body.identityImage,
                        contactNumber = it.body.contactDetails.phoneNumberPreferred,
//                        contactNumber = "0112123456",
                        comment = it.body.comment
                    )
                    participantRequestList.add(participantRequest)
                }
                )

                participantRequestDao.insert(participantRequestList)
            }


            override fun shouldFetch(data: List<ParticipantRequest>?): Boolean = data == null || data.size <= 0

            override fun loadFromDb(): LiveData<List<ParticipantRequest>> {
                return participantRequestDao.getParticipantRequests()
            }

            override fun createCall(): LiveData<ApiResponse<ResourceData<List<ParticipantMeta>>>> {
                return nghruService.getParticipantRequestMetas()
            }

        }.asLiveData()
    }

}
