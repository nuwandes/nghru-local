package org.southasia.ghru.sync

import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import io.reactivex.disposables.CompositeDisposable
import org.southasia.ghru.db.ECGStatusDao
import timber.log.Timber

class ECGStatusLifecycleObserver(var ecgStatusDao: ECGStatusDao) : LifecycleObserver {

    private val disposables = CompositeDisposable()

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onResume() {
        Timber.d("onResume lifecycle event.")
        disposables.add(
            ECGStatusRxBus.getInstance().toObservable()
                .subscribe({ result ->
                    Log.d("Result", "household SyncCommentLifecycleObserver ${result.ecgStatus}")
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

    private fun handleSyncResponse(response: ECGStatusRequestResponse) {
        if (response.eventType === SyncResponseEventType.SUCCESS) {
            onSyncCommentSuccess(response)
        } else {
            onSyncCommentFailed(response)
        }
    }

    private fun onSyncCommentSuccess(ecgStatusRequestResponse: ECGStatusRequestResponse) {
        //Timber.d("received sync comment success event for comment %s", household.bodyMeasurementRequest)
        ecgStatusRequestResponse?.ecgStatus.syncPending = false
        ecgStatusRequestResponse?.ecgStatus.screeningId = "00000"
        val udatedId=ecgStatusDao.update()
        //Timber.d("received sync comment success event for householdId %s", household.bodyMeasurementRequest)
    }

    private fun onSyncCommentFailed(ecgStatusRequestResponse: ECGStatusRequestResponse) {
        Timber.d("received sync comment failed event for comment %s", ecgStatusRequestResponse)
//        disposables.add(deleteCommentUseCase.deleteComment(comment)
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe({ Timber.d("delete comment success") },
//                        { t -> Timber.e(t, "delete comment error") }))
    }
}