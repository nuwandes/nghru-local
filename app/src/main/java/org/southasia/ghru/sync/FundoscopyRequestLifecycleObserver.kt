package org.southasia.ghru.sync

import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import io.reactivex.disposables.CompositeDisposable
import org.southasia.ghru.db.FundoscopyRequestDao
import timber.log.Timber

class FundoscopyRequestLifecycleObserver (var fundoscopyRequestDao: FundoscopyRequestDao) : LifecycleObserver {

    private val disposables = CompositeDisposable()

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onResume() {
        Timber.d("onResume lifecycle event.")
        disposables.add(
            FundoscopyRequestRxBus.getInstance().toObservable()
                .subscribe({ result ->
                    Log.d("Result", "household SyncCommentLifecycleObserver ${result.fundoscopyRequest}")
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

    private fun handleSyncResponse(response: FundoscopyRequestResponce) {
        if (response.eventType === SyncResponseEventType.SUCCESS) {
            onSyncCommentSuccess(response)
        } else {
            onSyncCommentFailed(response)
        }
    }

    private fun onSyncCommentSuccess(fundoscopyRequestResponce: FundoscopyRequestResponce) {
        //Timber.d("received sync comment success event for comment %s", household.bodyMeasurementRequest)
        fundoscopyRequestResponce?.fundoscopyRequest.syncPending = false
        val udatedId=fundoscopyRequestDao.update(fundoscopyRequestResponce?.fundoscopyRequest.screeningId)
        //Timber.d("received sync comment success event for householdId %s", household.bodyMeasurementRequest)


    }

    private fun onSyncCommentFailed(fundoscopyRequestResponce: FundoscopyRequestResponce) {
        Timber.d("received sync comment failed event for comment %s", fundoscopyRequestResponce)
//        disposables.add(deleteCommentUseCase.deleteComment(comment)
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe({ Timber.d("delete comment success") },
//                        { t -> Timber.e(t, "delete comment error") }))
    }

}