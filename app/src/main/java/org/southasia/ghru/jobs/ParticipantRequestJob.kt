package org.southasia.ghru.jobs

import com.birbit.android.jobqueue.Job
import com.birbit.android.jobqueue.Params
import com.birbit.android.jobqueue.RetryConstraint
import org.southasia.ghru.sync.SyncParticipantRequestRxBus
import org.southasia.ghru.sync.SyncResponseEventType
import org.southasia.ghru.vo.request.ParticipantRequest
import timber.log.Timber

class ParticipantRequestJob(private val participantRequest: ParticipantRequest) : Job(
    Params(JobPriority.PARICIPANT)
        .requireNetwork()
        .groupBy("Participant")
        .persist()
) {


    override fun onAdded() {
        Timber.d("Executing onAdded() for comment $participantRequest")
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
        Timber.d("Executing onRun() for household $participantRequest")
        RemoteHouseholdService().getInstance().addParticipantRequest(participantRequest)
       // SyncParticipantRequestRxBus.getInstance().post(SyncResponseEventType.SUCCESS, participantRequest)
    }

    override fun onCancel(cancelReason: Int, throwable: Throwable?) {
        Timber.d("canceling job. reason: %d, throwable: %s", cancelReason, throwable)
        //Crashlytics.logException(throwable)
        //Crashlytics.log("participantRequest " + participantRequest.toString())

        // sync to remote failed
       // SyncParticipantRequestRxBus.getInstance().post(SyncResponseEventType.FAILED, participantRequest)
    }

    companion object {

        private val TAG = ParticipantRequestJob::class.java.getCanonicalName()
    }
}