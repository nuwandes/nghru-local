package org.southasia.ghru.jobs

import com.birbit.android.jobqueue.Job
import com.birbit.android.jobqueue.Params
import com.birbit.android.jobqueue.RetryConstraint
import org.southasia.ghru.sync.SpirometryRequestRxBus
import org.southasia.ghru.vo.SpirometryRequest
import org.southasia.ghru.vo.request.ParticipantRequest
import timber.log.Timber

class SyncSpirometryJob(
    private val spirometryRequest: SpirometryRequest
) : Job(
    Params(JobPriority.SPIROMETRY)
        .setRequiresNetwork(true)
        .groupBy("Spirometry")
        .persist()
) {


    override fun onAdded() {
        Timber.d("Executing onAdded() for comment $spirometryRequest")
    }

    override fun shouldReRunOnThrowable(throwable: Throwable, runCount: Int, maxRunCount: Int): RetryConstraint {
        if (throwable is RemoteException) {

            val statusCode = throwable.response.code()
            if (statusCode >= 422 && statusCode < 500) {
                return RetryConstraint.CANCEL
            }
        }
        // if we are here, most likely the connection was lost during job execution
        return RetryConstraint.createExponentialBackoff(runCount, 1000);
    }

    override fun onRun() {
        Timber.d("Executing onRun() for household $spirometryRequest")
        RemoteHouseholdService().getInstance().addSpirometry(spirometryRequest)
        SpirometryRequestRxBus.getInstance().post(spirometryRequest)
    }

    override fun onCancel(cancelReason: Int, throwable: Throwable?) {
        Timber.d("canceling job. reason: %d, throwable: %s", cancelReason, throwable)
        //Crashlytics.logException(throwable)
        //Crashlytics.log("SyncSpirometryJob " + participantRequest.toString())
        // sync to remote failed
        //    SyncSampleStorageRequestRxBus.getInstance().post(SyncResponseEventType.FAILED, sampleStorageRequest)
    }
}