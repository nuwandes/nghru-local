package org.southasia.ghru.repository

import androidx.annotation.MainThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import org.southasia.ghru.AppExecutors
import org.southasia.ghru.vo.Message
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
abstract class LocalBoundResource<ResultType> @MainThread
internal constructor(private val appExecutors: AppExecutors) {

    private val result = MediatorLiveData<Resource<ResultType>>()

    init {
        result.value = Resource.loading(null)
        val dbSource = loadFromDb()
        result.addSource(dbSource) { data ->
            if (data != null) {
                result.removeSource(dbSource)
                result.addSource(dbSource) { newData -> result.setValue(Resource.success(newData)) }
            } else {
                result.removeSource(dbSource)
                result.addSource(dbSource) { result.setValue(Resource.error(Message("false", "Data not found"), null)) }
            }
        }
    }

    protected fun onFetchFailed() {}

    fun asLiveData(): LiveData<Resource<ResultType>> {
        return result
    }

    @MainThread
    protected abstract fun loadFromDb(): LiveData<ResultType>
}

abstract class MyLocalBoundResource<ResultType> @MainThread
internal constructor(private val appExecutors: AppExecutors) {

    private val result = MediatorLiveData<ResultType>()

    init {
        result.value = null
        val dbSource = loadFromDb()
        result.addSource(dbSource) { data ->
            if (data != null) {
                result.removeSource(dbSource)
                result.addSource(dbSource) { newData -> result.setValue(newData) }
            } else {
                result.removeSource(dbSource)
                result.addSource(dbSource) { result.setValue(null) }
            }
        }
    }

    protected fun onFetchFailed() {}

    fun asLiveData(): MutableLiveData<ResultType> {
        return result
    }

    @MainThread
    protected abstract fun loadFromDb(): MutableLiveData<ResultType>
}
