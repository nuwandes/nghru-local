package org.southasia.ghru.jobs

import com.birbit.android.jobqueue.Job
import com.birbit.android.jobqueue.Params
import com.birbit.android.jobqueue.RetryConstraint
import org.southasia.ghru.vo.StorageDto
import org.southasia.ghru.vo.request.SampleRequest
import timber.log.Timber

class SyncSampledStorageFreezeIDJob(
    private val sampleStorageRequest: SampleRequest?,
    private val storageDto: StorageDto
) : Job(
    Params(JobPriority.SAMPLE_STORAGE)
        .setRequiresNetwork(true)
        .groupBy("SampleSorage")
        .persist()
) {


    override fun onAdded() {
        Timber.d("Executing onAdded() for comment $sampleStorageRequest")
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
        Timber.d("Executing onRun() for household $sampleStorageRequest")
        RemoteHouseholdService().getInstance().addStorage(sampleStorageRequest, storageDto)
        // SyncSampleStorageRequestRxBus.getInstance().post(SyncResponseEventType.SUCCESS, sampleStorageRequest)
    }

    override fun onCancel(cancelReason: Int, throwable: Throwable?) {
        Timber.d("canceling job. reason: %d, throwable: %s", cancelReason, throwable)
        //Crashlytics.logException(throwable)
        //Crashlytics.log("sampleStorageRequest " + sampleStorageRequest.toString())
        // sync to remote failed
        // SyncSampleStorageRequestRxBus.getInstance().post(SyncResponseEventType.FAILED, sampleStorageRequest)
    }
}