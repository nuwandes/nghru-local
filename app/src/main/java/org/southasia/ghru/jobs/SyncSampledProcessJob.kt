package org.southasia.ghru.jobs

import com.birbit.android.jobqueue.Job
import com.birbit.android.jobqueue.Params
import com.birbit.android.jobqueue.RetryConstraint
import org.southasia.ghru.sync.SyncResponseEventType
import org.southasia.ghru.sync.SyncSampleProcessRxBus
import org.southasia.ghru.vo.SampleProcess
import org.southasia.ghru.vo.request.SampleRequest
import timber.log.Timber

class SyncSampledProcessJob(private val sampleProcess: SampleProcess, private val sampleRequest: SampleRequest?) : Job(
    Params(JobPriority.SAMPLE_PROCESS)
        .setRequiresNetwork(true)
        .groupBy("SampleCollect")
        .persist()
) {


    override fun onAdded() {
        Timber.d("Executing onAdded() for comment $sampleProcess")
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
        Timber.d("Executing onRun() for household $sampleProcess")
        RemoteHouseholdService().getInstance().addSampleProcess(sampleProcess, sampleRequest)
        SyncSampleProcessRxBus.getInstance().post(SyncResponseEventType.SUCCESS, sampleProcess)
    }

    override fun onCancel(cancelReason: Int, throwable: Throwable?) {
        Timber.d("canceling job. reason: %d, throwable: %s", cancelReason, throwable)
        //Crashlytics.logException(throwable)
        // sync to remote failed
        //Crashlytics.log("sampleProcess " + sampleProcess.toString())
        SyncSampleProcessRxBus.getInstance().post(SyncResponseEventType.FAILED, sampleProcess)
    }
}