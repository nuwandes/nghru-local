package org.southasia.ghru.sync

import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import io.reactivex.disposables.CompositeDisposable
import org.southasia.ghru.db.MemberDao
import timber.log.Timber

/**
 * Updates local database after remote comment sync requests
 */
class SyncHouseholdMemberLifecycleObserver(var memberDao: MemberDao) : LifecycleObserver {
    private val disposables = CompositeDisposable()

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onResume() {
        Timber.d("onResume SyncHouseholdMemberLifecycleObserver event.")
        disposables.add(
            SyncHouseholdMemberListRxBus.getInstance().toObservable()
                .subscribe({ result ->
                    Log.d("Result", "household SyncCommentLifecycleObserver ${result.member}")
                    handleSyncResponse(result)
                }, { error ->
                    error.printStackTrace()
                })
        )
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    fun onPause() {
        Timber.d("onPause SyncHouseholdMemberLifecycleObserver event.")
        disposables.clear()
    }

    private fun handleSyncResponse(response: SyncHouseholdMemberListResponse) {
        if (response.eventType === SyncResponseEventType.SUCCESS) {
            onSyncCommentSuccess(response)
        } else {
            onSyncCommentFailed(response)
        }
    }

    private fun onSyncCommentSuccess(household: SyncHouseholdMemberListResponse) {
        Timber.d("received sync SyncHouseholdMemberLifecycleObserver success event for comment %s", household)
        household.member.forEach({
            it.syncPending = false
            memberDao.update(it)

        })
        Timber.d(
            "received sync SyncHouseholdMemberLifecycleObserver success event for householdId %s",
            household.toString()
        )


    }

    private fun onSyncCommentFailed(household: SyncHouseholdMemberListResponse) {
        Timber.d("received sync comment failed event for comment %s", household)
//        disposables.add(deleteCommentUseCase.deleteComment(comment)
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe({ Timber.d("delete comment success") },
//                        { t -> Timber.e(t, "delete comment error") }))
    }
}