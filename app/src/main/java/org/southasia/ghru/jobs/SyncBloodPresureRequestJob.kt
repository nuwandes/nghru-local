package org.southasia.ghru.jobs

import com.birbit.android.jobqueue.Job
import com.birbit.android.jobqueue.Params
import com.birbit.android.jobqueue.RetryConstraint
import org.southasia.ghru.sync.BloodPressureMetaRequestRxBus
import org.southasia.ghru.sync.SyncResponseEventType
import org.southasia.ghru.vo.request.BloodPressureMetaRequest
import timber.log.Timber

class SyncBloodPresureRequestJob(
    private var screeningId: String,
    private var bloodPresureMetaRequest: BloodPressureMetaRequest
) : Job(
    Params(JobPriority.BLOOD_PRESURE)
        .requireNetwork()
        .groupBy("BloodPresure")
        .persist()
) {


    override fun onAdded() {
        Timber.d("Executing onAdded() for comment $bloodPresureMetaRequest")
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
        Timber.d("Executing onRun() for household $bloodPresureMetaRequest")

        RemoteHouseholdService().getInstance().addBloodPressuerMetaRequestSync(screeningId, bloodPresureMetaRequest)
        BloodPressureMetaRequestRxBus.getInstance().post(SyncResponseEventType.SUCCESS, bloodPresureMetaRequest)
    }

    override fun onCancel(cancelReason: Int, throwable: Throwable?) {
        Timber.d("canceling job. reason: %d, throwable: %s", cancelReason, throwable)
        //Crashlytics.logException(throwable)
        //Crashlytics.log("bodyMeasurementRequest " + bodyMeasurementRequest.toString())
        // sync to remote failed
        BloodPressureMetaRequestRxBus.getInstance().post(SyncResponseEventType.FAILED, bloodPresureMetaRequest)
    }

    companion object {

        private val TAG = SyncBodyMeasurementRequestJob::class.java.getCanonicalName()
    }

}