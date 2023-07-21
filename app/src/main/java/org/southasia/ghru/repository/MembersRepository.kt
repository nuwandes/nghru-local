package org.southasia.ghru.repository

import androidx.lifecycle.LiveData
import org.southasia.ghru.AppExecutors
import org.southasia.ghru.api.ApiResponse
import org.southasia.ghru.api.NghruService
import org.southasia.ghru.db.MemberDao
import org.southasia.ghru.db.NGRHUDb
import org.southasia.ghru.jobs.SyncHouseholdMemberJob
import org.southasia.ghru.util.Constants
import org.southasia.ghru.vo.MemberData
import org.southasia.ghru.vo.Resource
import org.southasia.ghru.vo.ResourceData
import org.southasia.ghru.vo.request.Household
import org.southasia.ghru.vo.request.HouseholdRequestMeta
import org.southasia.ghru.vo.request.Member
import org.southasia.ghru.vo.request.ParticipantRequest
import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton


/**
 * Repository that handles User objects.
 */

@Singleton
class MembersRepository @Inject constructor(
    private val appExecutors: AppExecutors,
    private val memberDao: MemberDao,
    private val nghruService: NghruService,
    private val nGRHUDb: NGRHUDb
) : Serializable {

    fun syncMember(
        member: Member, household: Household
    ): LiveData<Resource<ResourceData<MemberData>>> {
        return object : NetworkOnlyBoundResource<ResourceData<MemberData>>(appExecutors) {
            override fun createCall(): LiveData<ApiResponse<ResourceData<MemberData>>> {
                return nghruService.addMember(member, household.householdId)
            }
        }.asLiveData()
    }


    fun syncMembers(
        members: ArrayList<Member>?, householdRequest: HouseholdRequestMeta?
    ): LiveData<Resource<ResourceData<List<SyncHouseholdMemberJob.MemberDTO>>>> {
        return object : NetworkOnlyBoundResource<ResourceData<List<SyncHouseholdMemberJob.MemberDTO>>>(appExecutors) {
            override fun createCall(): LiveData<ApiResponse<ResourceData<List<SyncHouseholdMemberJob.MemberDTO>>>> {
                val memberDto: ArrayList<SyncHouseholdMemberJob.MemberDTO> = ArrayList()
                members?.forEach {
                    val myFormat = Constants.dataFormat // mention the format you need
                    val sdf = SimpleDateFormat(myFormat, Locale.US)
                    val date = sdf.parse(it.dateOfBirth)
                    val cal = Calendar.getInstance();
                    cal.setTime(date);
                    val mamber = SyncHouseholdMemberJob.MemberDTO(
                        given_name = it.name!!,
                        family_name = it.familyName!!,
                        preferred_name = if (it.nickName!!.isEmpty()) null else it.nickName,
                        gender = it.gender!!.toLowerCase(),
                        contact_number = if (it.contactNo!!.isEmpty()) null else it.contactNo,
                        age = it.age!!.toInt(),
                        primary_contact = it.isPrimaryContact,
                        residence_status = it.isStay!!,
                        screening_attendance = it.isAbleToScreening!!,
                        birth_date = SyncHouseholdMemberJob.DateX(
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
                return nghruService.addMembers(memberDto, householdRequest?.householdRequest?.enumerationId!!)
            }
        }.asLiveData()
    }

    fun insertMember(
        household: Member
    ): LiveData<Resource<Member>> {
        return object : LocalBoundInsertResource<Member>(appExecutors) {
            override fun loadFromDb(rowId: Long): LiveData<Member> {
                return memberDao.getMember(rowId)
            }

            override fun insertDb(): Long {
                return memberDao.insert(household)
            }
        }.asLiveData()
    }

    fun updateMember(
        participant: ParticipantRequest
    ): LiveData<Resource<Member>> {
        return object : LocalBoundUpateResource<Member, Long>(appExecutors) {
            override fun updateDb(): Long {
                //L.d(participant.toString())
                return memberDao.updateRegisted(participant.memberId!!, true)
            }

            override fun loadFromDb(rowId: Long): LiveData<Member> {
                return memberDao.getMemberByHouseHold(participant.householdId!!)
            }

        }.asLiveData()
    }

    fun insertMembers(
        members: ArrayList<Member>,
        value: HouseholdRequestMeta?
    ): LiveData<Resource<List<Member>>> {
        return object : LocalBoundInsertAllResource<List<Member>>(appExecutors) {
            override fun loadFromDb(): LiveData<List<Member>> {
                return memberDao.getHouseHoldMembers(value?.householdRequest?.enumerationId!!)
            }

            override fun insertDb(): Unit {
                return memberDao.insertAll(members)
            }
        }.asLiveData()
    }


    fun getMembers(
    ): LiveData<Resource<List<Member>>> {
        return object : LocalBoundResource<List<Member>>(appExecutors) {
            override fun loadFromDb(): LiveData<List<Member>> {
                return memberDao.getMembers()
            }
        }.asLiveData()
    }

    fun getMemberByHousehold(
        householdQR: String
    ): LiveData<Resource<List<Member>>> {
        return object : LocalBoundResource<List<Member>>(appExecutors) {
            override fun loadFromDb(): LiveData<List<Member>> {
                return memberDao.getHouseHoldMemberQr(householdQR)
            }
        }.asLiveData()
    }

    fun getHouseHoldMembers(
        householdQR: String
    ): LiveData<Resource<List<Member>>> {
        return object : LocalBoundResource<List<Member>>(appExecutors) {
            override fun loadFromDb(): LiveData<List<Member>> {
                return memberDao.getHouseHoldMembers(householdQR)
            }
        }.asLiveData()
    }

    fun insertMembersSave(
        members: List<Member>
    ): LiveData<Resource<List<Member>>> {
        return object : LocalBoundInsertAllResource<List<Member>>(appExecutors) {
            override fun loadFromDb(): LiveData<List<Member>> {
                return memberDao.getMembers()
            }

            override fun insertDb(): Unit {

                nGRHUDb.beginTransaction()
                return try {
                    memberDao.deleteAll()
                    memberDao.insertAll(members)
                    nGRHUDb.setTransactionSuccessful()
                } catch (e: Exception) {

                } finally {
                    nGRHUDb.endTransaction()
                }

            }
        }.asLiveData()
    }

}
