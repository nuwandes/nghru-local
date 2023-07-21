package org.southasia.ghru.repository

import androidx.annotation.MainThread
import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import org.southasia.ghru.AppExecutors
import org.southasia.ghru.vo.Resource


/**
 * A generic class that can provide a resource backed by both the sqlite database and the network.
 *
 *
 * You can read more about it in the [Architecture
 * Guide](https://developer.android.com/arch).
 *
 * @param <ResultType>
 * @param <RequestType>
</RequestType></ResultType> */
abstract class LocalBoundUpateResource<ResultType, RequestType> @MainThread
internal constructor(private val appExecutors: AppExecutors) {

    private val result = MediatorLiveData<Resource<ResultType>>()

    init {
        result.value = Resource.loading(null)

        appExecutors.diskIO().execute {
            val rowId = updateDb()
            appExecutors.mainThread().execute {
                // we specially request a new live data,
                // otherwise we will get immediately last cached value,
                // which may not be updated with latest results received from network.
                result.addSource(loadFromDb(rowId)) { newData ->
                    setValue(Resource.success(newData))
                }
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

    @MainThread
    protected abstract fun loadFromDb(rowId: RequestType): LiveData<ResultType>

    @WorkerThread
    protected abstract fun updateDb(): RequestType
}
