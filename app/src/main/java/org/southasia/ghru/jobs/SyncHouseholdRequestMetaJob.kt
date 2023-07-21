package org.southasia.ghru.jobs

import com.birbit.android.jobqueue.Job
import com.birbit.android.jobqueue.Params
import com.birbit.android.jobqueue.RetryConstraint
import org.southasia.ghru.sync.SyncHouseholdRequestmetaRxBus
import org.southasia.ghru.sync.SyncResponseEventType
import org.southasia.ghru.vo.request.HouseholdRequestMeta
import timber.log.Timber

class SyncHouseholdRequestMetaJob(private val household: HouseholdRequestMeta) : Job(
    Params(JobPriority.HOUSEHOLD)
        .setRequiresNetwork(true)
        .groupBy("Household")
        .persist()
) {

    override fun onAdded() {
        Timber.d("Executing onAdded() for comment $household")
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
        Timber.d("Executing onRun() for household $household")
        RemoteHouseholdService().getInstance().addHouseholdRequestMeta(household)
        household.syncPending = false
        RemoteHouseholdService().getInstance().provideDb(this.getApplicationContext()).householdRequestMetaMetaDao()
            .update(household)
        SyncHouseholdRequestmetaRxBus.getInstance().post(SyncResponseEventType.SUCCESS, household)
    }

    override fun onCancel(cancelReason: Int, throwable: Throwable?) {
        Timber.d("canceling job. reason: %d, throwable: %s", cancelReason, throwable)
        //Crashlytics.logException(throwable)
        //Crashlytics.log("household " + household.toString())

        // sync to remote failed
        //SyncHouseholdRequestRxBus.getInstance().post(SyncResponseEventType.FAILED, household)
    }
}