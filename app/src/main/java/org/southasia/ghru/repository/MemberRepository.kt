package org.southasia.ghru.repository

import androidx.lifecycle.LiveData
import org.southasia.ghru.AppExecutors
import org.southasia.ghru.api.ApiResponse
import org.southasia.ghru.api.NghruService
import org.southasia.ghru.vo.Resource
import org.southasia.ghru.vo.ResourceData
import org.southasia.ghru.vo.request.Member
import java.io.Serializable
import javax.inject.Inject
import javax.inject.Singleton


/**
 * Repository that handles User objects.
 */

@Singleton
class MemberRepository @Inject constructor(
    private val appExecutors: AppExecutors,
    private val nghruService: NghruService
) : Serializable {


    fun getMember(
        householdId: String
    ): LiveData<Resource<ResourceData<List<Member>>>> {
        return object : NetworkOnlyBoundResource<ResourceData<List<Member>>>(appExecutors) {
            override fun createCall(): LiveData<ApiResponse<ResourceData<List<Member>>>> {
                return nghruService.getMember(householdId, "0")
            }
        }.asLiveData()
    }


//    fun getMemberLocal(householdId: String
//    ): LiveData<Resource<ResourceData<List<Member>>>> {
//        return object : NetworkOnlyBoundResource<MemberData, ResourceData<List<Member>>>(appExecutors) {
//            override fun createCall(): LiveData<ApiResponse<ResourceData<List<Member>>>> {
//                return nghruService.getMember(tokenManager.getToken().accessToken!!, householdId)
//            }
//        }.asLiveData()
//    }

}
