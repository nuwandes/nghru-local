package org.southasia.ghru.jobs

import com.birbit.android.jobqueue.Job
import com.birbit.android.jobqueue.Params
import com.birbit.android.jobqueue.RetryConstraint
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import org.southasia.ghru.sync.SyncHouseholdMemberListRxBus
import org.southasia.ghru.sync.SyncResponseEventType
import org.southasia.ghru.util.Constants
import org.southasia.ghru.vo.request.HouseholdRequest
import org.southasia.ghru.vo.request.Member
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class SyncHouseholdMemberJob(private val household: HouseholdRequest, private val member: List<Member>) : Job(
    Params(JobPriority.MEMBERS)
        .setRequiresNetwork(true)
        .groupBy("HouseholdMember")
        .persist()
) {


    override fun onAdded() {
        //L.d("Executing onAdded() for comment $household")
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
        //L.d("Executing onRun() for household $household")

        val memberDto: ArrayList<MemberDTO> = ArrayList()
        member.forEach {
            val myFormat = Constants.dataFormat // mention the format you need
            val sdf = SimpleDateFormat(myFormat, Locale.US)
            val date = sdf.parse(it.dateOfBirth)
            val cal = Calendar.getInstance();
            cal.setTime(date);
            val mamber = MemberDTO(
                given_name = it.name!!,
                family_name = it.familyName!!,
                preferred_name = if (it.nickName!!.isEmpty()) null else it.nickName,
                gender = it.gender!!.toLowerCase(),
                contact_number = if (it.contactNo!!.isEmpty()) null else it.contactNo,
                age = it.age!!.toInt(),
                primary_contact = it.isPrimaryContact,
                residence_status = it.isStay!!,
                screening_attendance = it.isAbleToScreening!!,
                birth_date = DateX(
                    day = cal.get(Calendar.DAY_OF_MONTH),
                    month = cal.get(Calendar.MONTH) + 1,
                    year = cal.get(Calendar.YEAR)
                ),
                contact_number_alternate = "contact_number_alternate",
                info_source = "respondent",
                uuid = it.uuid!!,
                unavailability = it.reason!!,
                appointment_date = it.appointment_date
            )
            memberDto.add(mamber)
        }

        RemoteHouseholdService().getInstance().addMemmber(household, memberDto)
        member.forEach {
            it.syncPending = false
            RemoteHouseholdService().getInstance().provideDb(this.getApplicationContext()).memberDao().update(it)

        }
        SyncHouseholdMemberListRxBus.getInstance().post(SyncResponseEventType.SUCCESS, member)
    }

    override fun onCancel(cancelReason: Int, throwable: Throwable?) {
        //L.d("canceling job. reason: %d, throwable: %s", cancelReason, throwable)
        //Crashlytics.logException(throwable)

        //Crashlytics.log("member " + member.toString())
        // sync to remote failed
        SyncHouseholdMemberListRxBus.getInstance().post(SyncResponseEventType.FAILED, member)
    }

    data class MemberDTO(
        @Expose @SerializedName("given_name") val given_name: String,
        @Expose @SerializedName("family_name") val family_name: String,
        @Expose @SerializedName("preferred_name") val preferred_name: String?,
        @Expose @SerializedName("gender") val gender: String,
        @Expose @SerializedName("contact_number") val contact_number: String?,
        @Expose @SerializedName("contact_number_alternate") val contact_number_alternate: String,
        @Expose @SerializedName("age") val age: Int,
        @Expose @SerializedName("primary_contact") val primary_contact: Boolean,
        @Expose @SerializedName("residence_status") val residence_status: Boolean,
        @Expose @SerializedName("screening_attendance") val screening_attendance: Boolean,
        @Expose @SerializedName("birth_date") val birth_date: DateX, @Expose @SerializedName("info_source") val info_source: String, @Expose @SerializedName(
            "uuid"
        ) val uuid: String,
        @Expose @SerializedName("unavailability") val unavailability: String,
        @Expose @field:SerializedName("appointment_date") val appointment_date: String?
    )

    data class DateX(
        @Expose @SerializedName("day") val day: Int, @Expose @SerializedName("month") val month: Int, @Expose @SerializedName(
            "year"
        ) val year: Int
    )
}