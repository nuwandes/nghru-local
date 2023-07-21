package org.southasia.ghru

import android.os.Handler
import android.os.Looper
import java.io.Serializable

import java.util.concurrent.Executor
import java.util.concurrent.Executors

import javax.inject.Inject
import javax.inject.Singleton

/**
 * Global executor pools for the whole application.
 *
 * Grouping tasks like this avoids the effects of task starvation (e.g. disk reads don't wait behind
 * webservice requests).
 */
@Singleton
open class AppExecutors(
    private val diskIO: Executor,
    private val networkIO: Executor,
    private val mainThread: Executor
) : Serializable {

    @Inject
    constructor() : this(
        Executors.newSingleThreadExecutor(),
        Executors.newFixedThreadPool(3),
        MainThreadExecutor()
    )

    @Synchronized
    fun diskIO(): Executor {
        return diskIO
    }

    @Synchronized
    fun networkIO(): Executor {
        return networkIO
    }

    @Synchronized
    fun mainThread(): Executor {
        return mainThread
    }

    private class MainThreadExecutor : Executor, Serializable {
        private val mainThreadHandler = Handler(Looper.getMainLooper())
        override fun execute(command: Runnable) {
            mainThreadHandler.post(command)
        }
    }
}
