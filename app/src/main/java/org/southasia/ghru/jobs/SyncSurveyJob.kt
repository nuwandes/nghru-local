package org.southasia.ghru.jobs

import com.birbit.android.jobqueue.Job
import com.birbit.android.jobqueue.Params
import com.birbit.android.jobqueue.RetryConstraint
import org.southasia.ghru.vo.QuestionMeta
import timber.log.Timber

class SyncSurveyJob(private val survey: QuestionMeta) : Job(
    Params(JobPriority.SURVEY)
        .setRequiresNetwork(true)
        .groupBy("survey")
        .persist()
) {


    override fun onAdded() {
        Timber.d("Executing onAdded() for comment $survey")
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
        Timber.d("Executing onRun() for household $survey")
        RemoteHouseholdService().getInstance().addSurvey(survey)
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