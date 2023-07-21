package org.southasia.ghru.sync

import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import io.reactivex.disposables.CompositeDisposable
import org.southasia.ghru.db.HouseholdRequestDao
import org.southasia.ghru.vo.request.HouseholdRequest
import timber.log.Timber

/**
 * Updates local database after remote comment sync requests
 */
class SyncHouseholdLifecycleObserver(var householdDao: HouseholdRequestDao) : LifecycleObserver {
    private val disposables = CompositeDisposable()

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onResume() {
        Timber.d("onResume SyncHouseholdLifecycleObserver")
        disposables.add(
            SyncHouseholdRequestRxBus.getInstance().toObservable()
                .subscribe({ result ->
                    Log.d("Result", "household SyncHouseholdLifecycleObserver ${result.householdRequest}")
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

    private fun handleSyncResponse(response: SyncHouseholdRequestResponse) {
        if (response.eventType === SyncResponseEventType.SUCCESS) {
            onSyncCommentSuccess(response)
        } else {
            onSyncCommentFailed(response.householdRequest)
        }
    }

    private fun onSyncCommentSuccess(household: SyncHouseholdRequestResponse) {
        Timber.d("received sync SyncHouseholdLifecycleObserver success event for comment %s", household)
        household.householdRequest.syncPending = false
        var householdId = householdDao.update(household.householdRequest)
        Timber.d("received sync comment success event for householdId %s", householdId)


    }

    private fun onSyncCommentFailed(household: HouseholdRequest) {
        Timber.d("received sync comment SyncHouseholdLifecycleObserver event for comment %s", household)
//        disposables.add(deleteCommentUseCase.deleteComment(comment)
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe({ Timber.d("delete comment success") },
//                        { t -> Timber.e(t, "delete comment error") }))
    }
}