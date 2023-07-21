package org.southasia.ghru.sync

import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import io.reactivex.disposables.CompositeDisposable
import org.southasia.ghru.db.SampleRequestDao
import org.southasia.ghru.vo.request.SampleRequest
import timber.log.Timber

/**
 * Updates local database after remote comment sync requests
 */
class SyncSampleRequestLifecycleObserver(var sampleRequestDao: SampleRequestDao) : LifecycleObserver {
    private val disposables = CompositeDisposable()

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onResume() {
        Timber.d("onResume SyncHouseholdLifecycleObserver")
        disposables.add(
            SyncSampleRequestRxBus.getInstance().toObservable()
                .subscribe({ result ->
                    Log.d("Result", "household SyncHouseholdLifecycleObserver ${result.sampleRequest}")
                    handleSyncResponse(result)
                }, { error ->
                    error.printStackTrace()
                })
        )
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    fun onPause() {
        Timber.d("onPause SyncHouseholdLifecycleObserver")
        disposables.clear()
    }

    private fun handleSyncResponse(response: SyncSampleRequestResponse) {
        if (response.eventType === SyncResponseEventType.SUCCESS) {
            onSyncCommentSuccess(response)
        } else {
            onSyncCommentFailed(response.sampleRequest)
        }
    }

    private fun onSyncCommentSuccess(sampleRequest: SyncSampleRequestResponse) {
        Timber.d("received sync SyncHouseholdLifecycleObserver success event for comment %s", sampleRequest)
        sampleRequest.sampleRequest.syncPending = false
        var householdId = sampleRequestDao.update(sampleRequest.sampleRequest?.screeningId!!)
        Timber.d("received sync householdId success event for householdId %s", householdId)


    }

    private fun onSyncCommentFailed(sampleRequest: SampleRequest) {
        Timber.d("received sync comment SyncHouseholdLifecycleObserver event for comment %s", sampleRequest)
//        disposables.add(deleteCommentUseCase.deleteComment(comment)
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe({ Timber.d("delete comment success") },
//                        { t -> Timber.e(t, "delete comment error") }))
    }
}