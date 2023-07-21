package org.southasia.ghru.jobs

import com.birbit.android.jobqueue.Job
import com.birbit.android.jobqueue.Params
import com.birbit.android.jobqueue.RetryConstraint
import org.southasia.ghru.sync.SyncParticipantRequestRxBus
import org.southasia.ghru.sync.SyncResponseEventType
import org.southasia.ghru.vo.request.ParticipantMeta
import timber.log.Timber

class SyncparticipantMetaJob(private var participantMeta: ParticipantMeta) : Job(
    Params(JobPriority.PARICIPANT)
        .requireNetwork()
        .groupBy("ParticipantMeta")
        .persist()
) {


    override fun onAdded() {
        Timber.d("Executing onAdded() for bodyMeasurementRequest $participantMeta")
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
        Timber.d("Executing onRun() for bodyMeasurementRequest $participantMeta")

        RemoteHouseholdService().getInstance().addParticipantMeta(participantMeta)
        SyncParticipantRequestRxBus.getInstance().post(SyncResponseEventType.SUCCESS, participantMeta.body)
    }

    override fun onCancel(cancelReason: Int, throwable: Throwable?) {
        Timber.d("canceling job. reason: %d, throwable: %s", cancelReason, throwable)
        SyncParticipantRequestRxBus.getInstance().post(SyncResponseEventType.FAILED, participantMeta.body)

    }

    companion object {

        private val TAG = SyncparticipantMetaJob::class.java.getCanonicalName()
    }

}