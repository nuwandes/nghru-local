package org.southasia.ghru.ui.enumeration.member

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import org.southasia.ghru.repository.MembersRepository
import org.southasia.ghru.repository.UserRepository
import org.southasia.ghru.util.AbsentLiveData
import org.southasia.ghru.vo.Resource
import org.southasia.ghru.vo.User
import org.southasia.ghru.vo.request.*
import javax.inject.Inject


class AddHouseHoldMemberViewModel
@Inject constructor(membersRepository: MembersRepository, userRepository: UserRepository) : ViewModel() {

    var hasStaySelected: Boolean = false

    var isScreeningSelected: Boolean = false

    val _member: MutableLiveData<Member> = MutableLiveData()

    var houseHoldMember: MutableLiveData<HouseHoldMember>? = null

    var memberValidationError: MutableLiveData<Boolean>? = MutableLiveData<Boolean>().apply { false }

    private val _memberId: MutableLiveData<MemberId> = MutableLiveData()

    private val _email = MutableLiveData<String>()

    val user: LiveData<Resource<User>>? = Transformations
        .switchMap(_email) { emailx ->
            if (emailx == null) {
                AbsentLiveData.create()
            } else {
                userRepository.loadUserDB()
            }
        }

    fun setUser(email: String?) {
        if (_email.value != email) {
            _email.value = email
        }
    }

    var member: LiveData<Resource<Member>>? = Transformations
        .switchMap(_member) { member ->
            if (member == null) {
                AbsentLiveData.create()
            } else {
                membersRepository.insertMember(member)
            }
        }

//    var householdRemote: LiveData<Resource<ResourceData<MemberData>>>? = Transformations
//            .switchMap(_memberId) { input ->
//                input.ifExists { fullName, familyName, nickName, gender, isPrimaryContact, position, age, syncPending, household ->
//                    membersRepository.syncMember(Member(fullName, familyName, nickName, gender!!, isPrimaryContact, position, age, "", true, false, false, ""), household = household)
//                }
//            }


    fun setIsHouseHoldHead(b: Boolean) {
        getHouseHoldMember().value?.isPrimaryContact?.postValue(b)
    }

    fun setIsStayed(isStay: Boolean) {
        hasStaySelected = true
        getHouseHoldMember().value?.hasStayed?.postValue(isStay)
    }

    fun setISSelf(isSelf: Boolean) {
        getHouseHoldMember().value?.infoProvider?.postValue(isSelf)
    }

    fun setIsScreening(isScreen: Boolean) {
        isScreeningSelected = true
        getHouseHoldMember().value?.isAttending?.postValue(isScreen)
        getHouseHoldMember().value?.isNotAttending?.postValue(isScreen)
    }

    fun setReason(reason: Reason) {
        getHouseHoldMember().value?.reasonForNotAttending?.postValue(reason.name)
    }

    fun setGender(gender: Gender) {
        getHouseHoldMember().value?.gender?.value = gender.name
    }

//    fun setMemberSynced(hMember: HouseHoldMember?) {
//        var newMember = Member(hMember?.fullName?.value!!, hMember.familyName.value!!, hMember.nickName.value!!, hMember.gender.value!!, hMember.isPrimaryContact.value!!, "", "", "", true, false, false, "")
//        if (newMember != member?.value?.data) {
//
//            _member.postValue(newMember)
//        }
//
//    }

    fun getHouseHoldMember(): LiveData<HouseHoldMember> {
        if (houseHoldMember == null) {
            houseHoldMember = MutableLiveData<HouseHoldMember>()
            loadHouseHold()
        }
        return houseHoldMember as LiveData<HouseHoldMember>
    }

    fun loadHouseHold() {
        houseHoldMember?.value = HouseHoldMember()
    }

    data class MemberId(
        val fullName: String?,
        val familyName: String?,
        val nickName: String?,
        val gender: String?,
        val isPrimaryContact: Boolean?,
        val position: String,
        val age: String,
        val syncPending: Boolean?,
        val houehold: Household
    ) {
        fun <T> ifExists(f: (String, String, String, String?, Boolean, String, String, Boolean, Household) -> LiveData<T>): LiveData<T> {
            return if (fullName.isNullOrBlank() || familyName.isNullOrBlank()) {
                AbsentLiveData.create()
            } else {
                f(
                    fullName!!,
                    familyName!!,
                    nickName!!,
                    gender,
                    isPrimaryContact!!,
                    position,
                    age,
                    syncPending!!,
                    houehold
                )
            }
        }
    }

}