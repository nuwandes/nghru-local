package org.southasia.ghru.sync

import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import io.reactivex.disposables.CompositeDisposable
import org.southasia.ghru.db.BodyMeasurementRequestDao
import timber.log.Timber

/**
 * Updates local database after remote comment sync requests
 */
class BodyMeasurementRequestLifecycleObserver(var bodyMeasurementRequestDao: BodyMeasurementRequestDao) :
    LifecycleObserver {
    private val disposables = CompositeDisposable()

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onResume() {
        Timber.d("onResume lifecycle event.")
        disposables.add(
            BodyMeasurementRequestRxBus.getInstance().toObservable()
                .subscribe({ result ->
                    Log.d("Result", "household SyncCommentLifecycleObserver ${result.bodyMeasurementRequest}")
                    handleSyncResponse(result)
                }, { error ->
                    error.printStackTrace()
                })
        )
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    fun onPause() {
        Timber.d("onPause lifecycle event.")
        disposables.clear()
    }

    private fun handleSyncResponse(response: BodyMeasurementRequestResponse) {
        if (response.eventType === SyncResponseEventType.SUCCESS) {
            onSyncCommentSuccess(response)
        } else {
            onSyncCommentFailed(response)
        }
    }

    private fun onSyncCommentSuccess(household: BodyMeasurementRequestResponse) {
        //Timber.d("received sync comment success event for comment %s", household.bodyMeasurementRequest)
        household.bodyMeasurementRequest.syncPending = false
        bodyMeasurementRequestDao.update(household.bodyMeasurementRequest)
        //Timber.d("received sync comment success event for householdId %s", household.bodyMeasurementRequest)


    }

    private fun onSyncCommentFailed(household: BodyMeasurementRequestResponse) {
        Timber.d("received sync comment failed event for comment %s", household)
//        disposables.add(deleteCommentUseCase.deleteComment(comment)
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe({ Timber.d("delete comment success") },
//                        { t -> Timber.e(t, "delete comment error") }))
    }
}