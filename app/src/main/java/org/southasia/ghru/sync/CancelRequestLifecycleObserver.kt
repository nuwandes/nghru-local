package org.southasia.ghru.sync

import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import io.reactivex.disposables.CompositeDisposable
import org.southasia.ghru.db.CancelRequestDao
import timber.log.Timber

class CancelRequestLifecycleObserver(var cancelRequestDao: CancelRequestDao) : LifecycleObserver {

    private val disposables = CompositeDisposable()

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onResume() {
        Timber.d("onResume lifecycle event.")
        disposables.add(
            CancelRequestRxBus.getInstance().toObservable()
                .subscribe({ result ->
                    Log.d("Result", "household SyncCommentLifecycleObserver ${result.cancelRequest}")
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

    private fun handleSyncResponse(response: CancelRequestResponse) {
        if (response.eventType === SyncResponseEventType.SUCCESS) {
            onSyncCommentSuccess(response)
        } else {
            onSyncCommentFailed(response)
        }
    }

    private fun onSyncCommentSuccess(cancelRequestResponse: CancelRequestResponse) {

        cancelRequestResponse.cancelRequest.syncPending = false
        cancelRequestDao.update(cancelRequestResponse.cancelRequest.screeningId)
        //Timber.d("received sync comment success event for householdId %s", household.bodyMeasurementRequest)


    }

    private fun onSyncCommentFailed(cancelRequestResponse: CancelRequestResponse) {
        Timber.d("received sync comment failed event for comment %s", cancelRequestResponse)
//        disposables.add(deleteCommentUseCase.deleteComment(comment)
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe({ Timber.d("delete comment success") },
//                        { t -> Timber.e(t, "delete comment error") }))
    }
}