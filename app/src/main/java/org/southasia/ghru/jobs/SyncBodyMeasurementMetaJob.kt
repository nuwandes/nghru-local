package org.southasia.ghru.jobs

import com.birbit.android.jobqueue.Job
import com.birbit.android.jobqueue.Params
import com.birbit.android.jobqueue.RetryConstraint
import org.southasia.ghru.sync.BodyMeasurementMetaRxBus
import org.southasia.ghru.vo.request.BodyMeasurementMeta
import timber.log.Timber

class SyncBodyMeasurementMetaJob(
    private var screeningId: String,
    private var bodyMeasurementMeta: BodyMeasurementMeta
) : Job(
    Params(JobPriority.BODY_MESEASUMENT)
        .requireNetwork()
        .groupBy("Measurement")
        .persist()
) {


    override fun onAdded() {
        Timber.d("Executing onAdded() for comment $bodyMeasurementMeta")
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
        Timber.d("Executing onRun() for household $bodyMeasurementMeta")

        RemoteHouseholdService().getInstance().addBodyMeasurementMetaSync(screeningId, bodyMeasurementMeta)
        BodyMeasurementMetaRxBus.getInstance().post(bodyMeasurementMeta)
    }

    override fun onCancel(cancelReason: Int, throwable: Throwable?) {
        Timber.d("canceling job. reason: %d, throwable: %s", cancelReason, throwable)
        //Crashlytics.logException(throwable)
        //Crashlytics.log("bodyMeasurementRequest " + bodyMeasurementRequest.toString())
        // sync to remote failed
        BodyMeasurementMetaRxBus.getInstance().post(bodyMeasurementMeta)
    }

    companion object {

        private val TAG = SyncBodyMeasurementMetaJob::class.java.getCanonicalName()
    }

}