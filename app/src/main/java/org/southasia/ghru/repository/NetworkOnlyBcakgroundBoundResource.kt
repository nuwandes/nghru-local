package org.southasia.ghru.repository

import androidx.annotation.MainThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import org.southasia.ghru.AppExecutors
import org.southasia.ghru.api.ApiEmptyResponse
import org.southasia.ghru.api.ApiErrorResponse
import org.southasia.ghru.api.ApiResponse
import org.southasia.ghru.api.ApiSuccessResponse
import org.southasia.ghru.vo.Resource

abstract class NetworkOnlyBcakgroundBoundResource<RequestType> @MainThread
internal constructor(private val appExecutors: AppExecutors) {

    private val result = MediatorLiveData<Resource<RequestType>>()

    init {
        result.postValue(Resource.loading(null))
        // LiveData<ResultType> dbSource = loadFromDb();

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

    }


    protected fun onFetchFailed() {}

    fun asLiveData(): LiveData<Resource<RequestType>> {
        return result
    }

    @MainThread
    protected abstract fun createCall(): LiveData<ApiResponse<RequestType>>
}
