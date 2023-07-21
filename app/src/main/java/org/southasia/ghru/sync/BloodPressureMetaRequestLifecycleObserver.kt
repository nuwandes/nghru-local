package org.southasia.ghru.sync

import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import io.reactivex.disposables.CompositeDisposable
import org.southasia.ghru.db.BloodPressureMetaRequestDao
import org.southasia.ghru.db.BloodPresureRequestDao
import timber.log.Timber

class BloodPressureMetaRequestLifecycleObserver(var bloodPressureRequestDao: BloodPresureRequestDao) :
    LifecycleObserver {
    private val disposables = CompositeDisposable()

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onResume() {
        Timber.d("onResume lifecycle event.")
        disposables.add(
            BloodPressureMetaRequestRxBus.getInstance().toObservable()
                .subscribe({ result ->
                    Log.d("Result", "household SyncBloodPressureMetaRequestObserver ${result.bloodPressureMetaRequest}")
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

    private fun handleSyncResponse(response: BloodPresureMetaRequestResponse) {
        if (response.eventType === SyncResponseEventType.SUCCESS) {
            onSyncCommentSuccess(response)
        } else {
            onSyncCommentFailed(response)
        }
    }

    private fun onSyncCommentSuccess(bloodPresureMetaRequest: BloodPresureMetaRequestResponse) {
        //Timber.d("received sync comment success event for comment %s", household.bodyMeasurementRequest)
        bloodPresureMetaRequest.bloodPressureMetaRequest.syncPending = false
        bloodPresureMetaRequest.bloodPressureMetaRequest.body.syncPending = false
        val udatedId=bloodPressureRequestDao.update(bloodPresureMetaRequest.bloodPressureMetaRequest.body.screeningId)
        //Timber.d("received sync comment success event for householdId %s", household.bodyMeasurementRequest)


    }

    private fun onSyncCommentFailed(household: BloodPresureMetaRequestResponse) {
        Timber.d("received sync comment failed event for comment %s", household)
//        disposables.add(deleteCommentUseCase.deleteComment(comment)
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe({ Timber.d("delete comment success") },
//                        { t -> Timber.e(t, "delete comment error") }))
    }
}