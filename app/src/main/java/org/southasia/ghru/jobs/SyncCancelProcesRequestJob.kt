package org.southasia.ghru.jobs

import com.birbit.android.jobqueue.Job
import com.birbit.android.jobqueue.Params
import com.birbit.android.jobqueue.RetryConstraint
import org.southasia.ghru.vo.request.CancelRequest
import org.southasia.ghru.vo.request.SampleRequest
import timber.log.Timber

class SyncCancelProcesRequestJob(
    private val sampleRequest: SampleRequest, private val cancelRequest: CancelRequest
) : Job(
    Params(JobPriority.CANCEL_REQUEST)
        .setRequiresNetwork(true)
        .groupBy("survey")
        .persist()
) {


    override fun onAdded() {
        Timber.d("Executing onAdded() for comment $sampleRequest")
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
        Timber.d("Executing onRun() for household $sampleRequest")
        RemoteHouseholdService().getInstance()
            .addCancelSampleRequestSync(cancelRequest = cancelRequest, sampleRequest = sampleRequest!!)
        //  SyncSampleStorageRequestRxBus.getInstance().post(SyncResponseEventType.SUCCESS, survey)
    }

    override fun onCancel(cancelReason: Int, throwable: Throwable?) {
        Timber.d("canceling job. reason: %d, throwable: %s", cancelReason, throwable)
        //Crashlytics.logException(throwable)
        //Crashlytics.log("sampleStorageRequest " + survey.toString())
        // sync to remote failed
        //  SyncSampleStorageRequestRxBus.getInstance().post(SyncResponseEventType.FAILED, survey)
    }
}