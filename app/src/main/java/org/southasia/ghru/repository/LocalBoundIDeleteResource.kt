package org.southasia.ghru.repository

import androidx.annotation.MainThread
import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import org.southasia.ghru.AppExecutors
import org.southasia.ghru.vo.Resource

abstract class LocalBoundIDeleteResource<ResultType> @MainThread
internal constructor(private val appExecutors: AppExecutors) {

    private val result = MediatorLiveData<Resource<ResultType>>()

    init {
        result.value = Resource.loading(null)

        appExecutors.diskIO().execute {
            deleteDb()
            appExecutors.mainThread().execute {
                // we specially request a new live data,
                // otherwise we will get immediately last cached value,
                // which may not be updated with latest results received from network.
                setValue(Resource.success(null))
            }
        }
    }

    protected fun onFetchFailed() {}

    fun asLiveData(): LiveData<Resource<ResultType>> {
        return result
    }

    @MainThread
    private fun setValue(newValue: Resource<ResultType>) {
        if (result.value != newValue) {
            result.value = newValue
        }
    }

    @WorkerThread
    protected abstract fun deleteDb()
}
