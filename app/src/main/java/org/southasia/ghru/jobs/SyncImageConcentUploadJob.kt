package org.southasia.ghru.jobs

import com.birbit.android.jobqueue.Job
import com.birbit.android.jobqueue.Params
import com.birbit.android.jobqueue.RetryConstraint
import timber.log.Timber

class SyncImageConcentUploadJob(val concentPhoto: String?, val screeningId: String?) : Job(
    Params(JobPriority.PARICIPANT_IMAGE)
        .setRequiresNetwork(true)
        .groupBy("SampleSorage")
        .persist()
) {


    override fun onAdded() {
        Timber.d("Executing onAdded() for comment $concentPhoto")
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
        Timber.d("Executing onRun() for household $concentPhoto")
        RemoteHouseholdService().getInstance().uploadConcent(concentPhoto, screeningId)
    }

    override fun onCancel(cancelReason: Int, throwable: Throwable?) {
        Timber.d("canceling job. reason: %d, throwable: %s", cancelReason, throwable)
        //Crashlytics.logException(throwable)
        //Crashlytics.log("uploadBackground " + concentPhoto.toString())
        // sync to remote failed
        //  SyncSampleStorageRequestRxBus.getInstance().post(SyncResponseEventType.FAILED, sampleStorageRequest)
    }
}