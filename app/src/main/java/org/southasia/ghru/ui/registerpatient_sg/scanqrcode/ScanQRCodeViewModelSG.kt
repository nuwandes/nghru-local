package org.southasia.ghru.ui.registerpatient_sg.scanqrcode

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import org.southasia.ghru.repository.HouseholdRequestRepository
import org.southasia.ghru.repository.MemberRepository
import org.southasia.ghru.repository.MembersRepository
import org.southasia.ghru.util.AbsentLiveData
import org.southasia.ghru.vo.HouseholdBodyData
import org.southasia.ghru.vo.Resource
import org.southasia.ghru.vo.ResourceData
import org.southasia.ghru.vo.request.HouseholdRequestMeta
import org.southasia.ghru.vo.request.Member
import javax.inject.Inject


class ScanQRCodeViewModelSG @Inject constructor(
    memberRepository: MemberRepository,
    membersRepository: MembersRepository,
    houseHoldRequest: HouseholdRequestRepository
) : ViewModel() {

    private val _householdId: MutableLiveData<String> = MutableLiveData()

    private val _householdIdOfline: MutableLiveData<String> = MutableLiveData()


    private val _eumarationId: MutableLiveData<String> = MutableLiveData()

    private val _eumarationIdOffline: MutableLiveData<String> = MutableLiveData()

    var members: LiveData<Resource<ResourceData<List<Member>>>>? = Transformations
        .switchMap(_householdId) { householdId ->
            if (householdId == null) {
                AbsentLiveData.create()
            } else {
                memberRepository.getMember(householdId)
            }
        }

    fun setHouseholdId(householdId: String?) {
//        if (_householdId.value == householdId) {
//            return
//        }
        _householdId.value = householdId
    }


    var membersOfline: LiveData<Resource<List<Member>>>? = Transformations
        .switchMap(_householdIdOfline) { householdId ->
            if (householdId == null) {
                AbsentLiveData.create()
            } else {
                membersRepository.getHouseHoldMembers(householdId)
            }
        }

    fun setHouseholdIdOffline(householdId: String?) {
//        if (_householdIdOfline.value == householdId) {
//            return
//        }
        _householdIdOfline.value = householdId
    }


    var houseHoldBodyOffline: LiveData<Resource<HouseholdRequestMeta>>? = Transformations
        .switchMap(_eumarationIdOffline) { enumarationId ->
            if (enumarationId == null) {
                AbsentLiveData.create()
            } else {
                houseHoldRequest.getHouseholdByEnumerationId(enumarationId)
            }
        }

    fun setEnumarationIdOffline(enumarationId: String?) {
        if (_eumarationIdOffline.value == enumarationId) {
            return
        }
        _eumarationIdOffline.value = enumarationId
    }

    var houseHoldBody: LiveData<Resource<ResourceData<HouseholdBodyData>>>? = Transformations
        .switchMap(_eumarationId) { enumarationId ->
            if (enumarationId == null) {
                AbsentLiveData.create()
            } else {
                houseHoldRequest.getHouseHold(enumarationId)
            }
        }

    fun setEnumarationId(enumarationId: String?) {
        if (_eumarationId.value == enumarationId) {
            return
        }
        _eumarationId.value = enumarationId
    }
}