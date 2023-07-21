package org.southasia.ghru.sync

import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import io.reactivex.disposables.CompositeDisposable
import org.southasia.ghru.db.ParticipantRequestDao
import timber.log.Timber

/**
 * Updates local database after remote comment sync requests
 */
class SyncParticipantRequestLifecycleObserver(var participantRequestDao: ParticipantRequestDao) : LifecycleObserver {
    private val disposables = CompositeDisposable()

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onResume() {
        Timber.d("onResume lifecycle event.")
        disposables.add(
            SyncParticipantRequestRxBus.getInstance().toObservable()
                .subscribe({ result ->
                    Log.d("Result", "participantRequest SyncCommentLifecycleObserver ${result.participantRequest}")
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

    private fun handleSyncResponse(response: SyncParticipantRequestResponse) {
        if (response.eventType === SyncResponseEventType.SUCCESS) {
            onSyncCommentSuccess(response)
        } else {
            onSyncCommentFailed(response)
        }
    }

    private fun onSyncCommentSuccess(participantRequest: SyncParticipantRequestResponse) {
        Timber.d("received sync comment success event for comment %s", participantRequest)
       // participantRequestDao.update(participantRequest.participantRequest)
        participantRequestDao.updateSyncPendingHouseholdRequest()
        Timber.d(
            "received sync comment success event for participantRequestId %s",
            participantRequest.participantRequest
        )


    }

    private fun onSyncCommentFailed(participantRequest: SyncParticipantRequestResponse) {
        Timber.d("received sync comment failed event for comment %s", participantRequest)
//        disposables.add(deleteCommentUseCase.deleteComment(comment)
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe({ Timber.d("delete comment success") },
//                        { t -> Timber.e(t, "delete comment error") }))
    }
}