package org.southasia.ghru.sync

import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import io.reactivex.disposables.CompositeDisposable
import org.southasia.ghru.db.BloodPressureMetaRequestDao
import org.southasia.ghru.db.BodyMeasurementMetaDao
import org.southasia.ghru.vo.request.BodyMeasurementMeta
import timber.log.Timber

class BodyMeasurementMetaLifecycleObserver(private val bodyMeasurementMetaDao: BodyMeasurementMetaDao) :
    LifecycleObserver {
    private val disposables = CompositeDisposable()

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onResume() {
        Timber.d("onResume lifecycle event.")
        disposables.add(
            BodyMeasurementMetaRxBus.getInstance().toObservable()
                .subscribe({ result ->
                    Log.d("Result", "bodyMeasurementMetaDao BodyMeasurementMetaLifecycleObserver ${result}")
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

    private fun handleSyncResponse(response: BodyMeasurementMeta) {
        onSyncCommentSuccess(response)
    }

    private fun onSyncCommentSuccess(bodyMeasurementMeta: BodyMeasurementMeta) {
        Timber.d("received sync bodyMeasurementMetaDao success event for bodyMeasurementMetaDao %s", bodyMeasurementMetaDao)
        bodyMeasurementMeta.syncPending = false
        val udatedId= bodyMeasurementMetaDao.update(bodyMeasurementMeta.screeningId)
        Timber.d("received sync $udatedId $bodyMeasurementMeta")


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