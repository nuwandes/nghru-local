package org.southasia.ghru.sync

import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import io.reactivex.disposables.CompositeDisposable
import org.southasia.ghru.db.SampleStorageRequestDao
import org.southasia.ghru.vo.request.SampleStorageRequest
import timber.log.Timber

/**
 * Updates local database after remote comment sync requests
 */
class SyncSampleStorageRequestLifecycleObserver(var sampleRequestDao: SampleStorageRequestDao) : LifecycleObserver {
    private val disposables = CompositeDisposable()

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onResume() {
        Timber.d("onResume SyncHouseholdLifecycleObserver")
        disposables.add(
            SyncSampleStorageRequestRxBus.getInstance().toObservable()
                .subscribe({ result ->
                    Log.d("Result", "household SyncHouseholdLifecycleObserver ${result.sampleStorageRequest}")
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

    private fun handleSyncResponse(response: SyncSampleStorageRequestResponse) {
        if (response.eventType === SyncResponseEventType.SUCCESS) {
            onSyncCommentSuccess(response)
        } else {
            onSyncCommentFailed(response.sampleStorageRequest)
        }
    }

    private fun onSyncCommentSuccess(sampleRequest: SyncSampleStorageRequestResponse) {
        Timber.d("received sync SyncHouseholdLifecycleObserver success event for comment %s", sampleRequest)
        sampleRequest.sampleStorageRequest.syncPending = false
        //  var householdId = sampleRequestDao.update(sampleRequest.sampleStorageRequest)
        //Timber.d("received sync comment success event for householdId %s", householdId)


    }

    private fun onSyncCommentFailed(sampleRequest: SampleStorageRequest) {
        Timber.d("received sync comment SyncHouseholdLifecycleObserver event for comment %s", sampleRequest)
//        disposables.add(deleteCommentUseCase.deleteComment(comment)
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe({ Timber.d("delete comment success") },
//                        { t -> Timber.e(t, "delete comment error") }))
    }
}