package org.southasia.ghru.sync

import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import io.reactivex.disposables.CompositeDisposable
import org.southasia.ghru.db.SampleProcessDao
import org.southasia.ghru.vo.SampleProcess

/**
 * Updates local database after remote comment sync requests
 */
class SyncSampleProcessLifecycleObserver(var sampleRequestDao: SampleProcessDao) : LifecycleObserver {
    private val disposables = CompositeDisposable()

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onResume() {
        //L.d("onResume SyncHouseholdLifecycleObserver")
        disposables.add(
            SyncSampleProcessRxBus.getInstance().toObservable()
                .subscribe({ result ->
                    Log.d("Result", "household SyncHouseholdLifecycleObserver ${result.sampleProcess}")
                    handleSyncResponse(result)
                }, { error ->
                    error.printStackTrace()
                })
        )
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    fun onPause() {
        //L.d("onPause SyncHouseholdLifecycleObserver")
        disposables.clear()
    }

    private fun handleSyncResponse(response: SyncSampleProcessResponse) {
        if (response.eventType === SyncResponseEventType.SUCCESS) {
            onSyncCommentSuccess(response)
        } else {
            onSyncCommentFailed(response.sampleProcess)
        }
    }

    private fun onSyncCommentSuccess(household: SyncSampleProcessResponse) {
        //L.d("received sync SyncHouseholdLifecycleObserver success event for comment %s", household)
        household.sampleProcess.syncPending = false
        // var householdId = sampleRequestDao.update(household.sampleProcess)
        //L.d("received sync comment success event for householdId %s", householdId)


    }

    private fun onSyncCommentFailed(household: SampleProcess) {
        //L.d("received sync comment SyncHouseholdLifecycleObserver event for comment %s", household)
//        disposables.add(deleteCommentUseCase.deleteComment(comment)
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe({//L.d("delete comment success") },
//                        { t -> L.e(t, "delete comment error") }))
    }
}