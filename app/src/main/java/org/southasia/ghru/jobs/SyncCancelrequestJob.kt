package org.southasia.ghru.jobs

import com.birbit.android.jobqueue.Job
import com.birbit.android.jobqueue.Params
import com.birbit.android.jobqueue.RetryConstraint
import org.southasia.ghru.sync.CancelRequestRxBus
import org.southasia.ghru.sync.SyncResponseEventType
import org.southasia.ghru.vo.request.CancelRequest
import org.southasia.ghru.vo.request.ParticipantRequest
import timber.log.Timber

class SyncCancelrequestJob(
    private val participantRequest: ParticipantRequest,
    private val cancelRequest: CancelRequest
) : Job(
    Params(JobPriority.CANCEL_REQUEST)
        .setRequiresNetwork(true)
        .groupBy("survey")
        .persist()
) {


    override fun onAdded() {
        Timber.d("Executing onAdded() for comment $participantRequest")
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
        Timber.d("Executing onRun() for household $participantRequest")
        if(cancelRequest.stationType == "axivity" || cancelRequest.stationType == "spirometry" || cancelRequest.stationType == "blood-pressure")
        {
            RemoteHouseholdService().getInstance().addCancelAxivityRequestSync(participantRequest, cancelRequest)
        }
        else {
            RemoteHouseholdService().getInstance().addCancelRequestSync(participantRequest, cancelRequest)
        }
        //  SyncSampleStorageRequestRxBus.getInstance().post(SyncResponseEventType.SUCCESS, survey)
        CancelRequestRxBus.getInstance().post(SyncResponseEventType.SUCCESS,cancelRequest)
    }

    override fun onCancel(cancelReason: Int, throwable: Throwable?) {
        Timber.d("canceling job. reason: %d, throwable: %s", cancelReason, throwable)
        //Crashlytics.logException(throwable)
        //Crashlytics.log("sampleStorageRequest " + survey.toString())
        // sync to remote failed
        //  SyncSampleStorageRequestRxBus.getInstance().post(SyncResponseEventType.FAILED, survey)
        CancelRequestRxBus.getInstance().post(SyncResponseEventType.FAILED,cancelRequest)
    }
}