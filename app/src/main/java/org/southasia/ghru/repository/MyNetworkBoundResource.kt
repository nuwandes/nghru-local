package org.southasia.ghru.repository

import androidx.annotation.MainThread
import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import org.southasia.ghru.AppExecutors
import org.southasia.ghru.api.ApiEmptyResponse
import org.southasia.ghru.api.ApiErrorResponse
import org.southasia.ghru.api.ApiResponse
import org.southasia.ghru.api.ApiSuccessResponse
import org.southasia.ghru.vo.Resource

/**
 * A generic class that can provide a resource backed by both the sqlite database and the network.
 *
 *
 * You can read more about it in the [Architecture
 * Guide](https://developer.android.com/arch).
 * @param <ResultType>
 * @param <RequestType>
</RequestType></ResultType> */
abstract class MyNetworkBoundResource<ResultType, RequestType>
@MainThread constructor(private val appExecutors: AppExecutors) {

    private val result = MediatorLiveData<Resource<RequestType>>()

    init {
        result.value = Resource.loading(null)

        if (!isNetworkAvilable()) { // sync false in default
            val apiResponse = createCall()
            // result.addSource(apiResponse, newData -> result.setValue(Resource.loading(newData.body)));
            result.addSource(apiResponse) { response ->

                when (response) {
                    is ApiSuccessResponse -> {
                        appExecutors.mainThread().execute {
                            // reload from disk whatever we had
                            result.postValue(Resource.success(response.body))
                            asLiveData()
                        }
                    }

                    is ApiEmptyResponse -> {
                        appExecutors.mainThread().execute {
                            // reload from disk whatever we had
                            result.postValue(Resource.success(null))
                            asLiveData()
                        }
                    }

                    is ApiErrorResponse -> {

                        result.postValue(Resource.error(response.errorMessage, null))
                        onFetchFailed()
                    }

                }

            }
        } else {

            val insertedID = saveDb()
            println("insertedID $insertedID")

            createJob(insertedID)
            result.postValue(Resource.success(null))

        }
    }

    @MainThread
    private fun setValue(newValue: Resource<RequestType>) {
        if (result.value != newValue) {
            result.value = newValue
        }
    }

    protected open fun onFetchFailed() {}

    fun asLiveData() = result as LiveData<Resource<ResultType>>



    @MainThread
    protected abstract fun isNetworkAvilable(): Boolean

    protected abstract fun createJob( insertedID: Long )

    @MainThread
    protected abstract fun saveDb(): Long

    @MainThread
    protected abstract fun createCall(): LiveData<ApiResponse<RequestType>>
}
