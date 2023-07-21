package org.southasia.ghru.jobs

import com.birbit.android.jobqueue.Job
import com.birbit.android.jobqueue.Params
import com.birbit.android.jobqueue.RetryConstraint
import org.southasia.ghru.sync.BodyMeasurementRequestRxBus
import org.southasia.ghru.sync.SyncResponseEventType
import org.southasia.ghru.vo.request.BodyMeasurementRequest
import timber.log.Timber

class SyncBodyMeasurementRequestJob(
    private var screeningId: String,
    private var bodyMeasurementRequest: BodyMeasurementRequest
) : Job(
    Params(JobPriority.BODY_MESEASUMENT)
        .requireNetwork()
        .groupBy("Measurement")
        .persist()
) {


    override fun onAdded() {
        Timber.d("Executing onAdded() for comment $bodyMeasurementRequest")
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
        Timber.d("Executing onRun() for household $bodyMeasurementRequest")

        RemoteHouseholdService().getInstance().addBodyMeasurementRequestSync(screeningId, bodyMeasurementRequest)
        BodyMeasurementRequestRxBus.getInstance().post(SyncResponseEventType.SUCCESS, bodyMeasurementRequest)
    }

    override fun onCancel(cancelReason: Int, throwable: Throwable?) {
        Timber.d("canceling job. reason: %d, throwable: %s", cancelReason, throwable)
        //Crashlytics.logException(throwable)
        //Crashlytics.log("bodyMeasurementRequest " + bodyMeasurementRequest.toString())
        // sync to remote failed
        BodyMeasurementRequestRxBus.getInstance().post(SyncResponseEventType.FAILED, bodyMeasurementRequest)
    }

    companion object {

        private val TAG = SyncBodyMeasurementRequestJob::class.java.getCanonicalName()
    }

}