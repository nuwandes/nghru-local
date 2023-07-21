package org.southasia.ghru.jobs

import android.util.Log
import com.birbit.android.jobqueue.Job
import com.birbit.android.jobqueue.Params
import com.birbit.android.jobqueue.RetryConstraint
import org.southasia.ghru.sync.AxivityRequestRxBus
import org.southasia.ghru.sync.SyncResponseEventType
import org.southasia.ghru.vo.Axivity
import timber.log.Timber

class SyncAxivityJob(private var screeningId: String, private var axivity: Axivity) : Job(
    Params(JobPriority.AXIVITY)
        .requireNetwork()
        .groupBy("Measurement")
        .persist()
) {


    override fun onAdded() {
        Log.d("SYNC_AXIVITY_JOB_ADDED", "META:" + axivity)
        Timber.d("SYNC_AXIVITY_JOB ${axivity}")
    }

    override fun shouldReRunOnThrowable(throwable: Throwable, runCount: Int, maxRunCount: Int): RetryConstraint {
        if (throwable is RemoteException) {

            val statusCode = throwable.response.code()
            if (statusCode >= 400 && statusCode < 500) {
                return RetryConstraint.CANCEL
            }
        }
        // if we are here, most likely the connection was lost during job execution
        return RetryConstraint.createExponentialBackoff(runCount, 1000);
    }

    override fun onRun() {
        Timber.d("Executing onRun() for household $axivity")
        Log.d("SYNC_AXIVITY_JOB_RUN", "META:" + axivity)

        RemoteHouseholdService().getInstance().addAxivitySync(screeningId, axivity)
        AxivityRequestRxBus.getInstance().post(SyncResponseEventType.SUCCESS,axivity)
        // BodyMeasurementRequestRxBus.getInstance().post(SyncResponseEventType.SUCCESS, bodyMeasurementRequest)
    }

    override fun onCancel(cancelReason: Int, throwable: Throwable?) {
        Timber.d("canceling job. reason: %d, throwable: %s", cancelReason, throwable)
        //Crashlytics.logException(throwable)
        //Crashlytics.log("bodyMeasurementRequest " + bodyMeasurementRequest.toString())
        // sync to remote failed
        //  BodyMeasurementRequestRxBus.getInstance().post(SyncResponseEventType.FAILED, bodyMeasurementRequest)
        AxivityRequestRxBus.getInstance().post(SyncResponseEventType.FAILED,axivity)
    }

    companion object {

        private val TAG = SyncAxivityJob::class.java.getCanonicalName()
    }

}