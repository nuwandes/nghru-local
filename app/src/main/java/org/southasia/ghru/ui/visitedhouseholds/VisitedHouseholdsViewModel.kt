package org.southasia.ghru.ui.visitedhouseholds

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import org.southasia.ghru.repository.HouseholdRequestRepository
import org.southasia.ghru.repository.MembersRepository
import org.southasia.ghru.repository.ParticipantMetaRepository
import org.southasia.ghru.repository.SampleRequestRepository
import org.southasia.ghru.util.AbsentLiveData
import org.southasia.ghru.vo.Resource
import org.southasia.ghru.vo.ResourceData
import org.southasia.ghru.vo.request.*
import javax.inject.Inject


class VisitedHouseholdViewModel
@Inject constructor(
    householdRequestRepository: HouseholdRequestRepository,
    membersRepository: MembersRepository,
    participantMetaRepository: ParticipantMetaRepository,
    sampleRequestRepository: SampleRequestRepository
) : ViewModel() {
    private val _visitedHousehold = MutableLiveData<String>()
    private val _visitedHouseholdOffline = MutableLiveData<String>()
    private var mSyncStatus = MutableLiveData<Boolean>()

    val visitedHouseholdItem: LiveData<Resource<ResourceData<List<HouseholdRequestMetaResponce>>>>? = Transformations
        .switchMap(_visitedHousehold) { login ->
            if (login == null) {
                AbsentLiveData.create()
            } else {
                householdRequestRepository.getHouseHolds();
            }
        }


    val visitedHouseholdItemRead: LiveData<Resource<List<HouseholdRequestMeta>>>? = Transformations
        .switchMap(_visitedHouseholdOffline) { login ->
            if (login == null) {
                AbsentLiveData.create()
            } else {
                householdRequestRepository.getHouseHoldsRead();
            }
        }

    fun setId(lang: String?) {
        if (_visitedHousehold.value != lang) {
            _visitedHousehold.value = lang
        }
    }

    fun setIdOffline(lang: String?) {
        if (_visitedHouseholdOffline.value != lang) {
            _visitedHouseholdOffline.value = lang
        }
    }

    fun setSyncStatus(status: Boolean) {

        mSyncStatus.value = status;
    }

    val getHouseholdRequest: LiveData<Resource<List<HouseholdRequestMeta>>>? = Transformations
        .switchMap(mSyncStatus) { status ->
            if (mSyncStatus == null) {
                AbsentLiveData.create()
            } else {
                householdRequestRepository.getHouseholdRequestMetasByStatus(true)
            }
        }
    val getHouseholdAllRequest: LiveData<Resource<List<HouseholdRequestMeta>>>? = Transformations
        .switchMap(mSyncStatus) { status ->
            if (mSyncStatus == null) {
                AbsentLiveData.create()
            } else {
                householdRequestRepository.getHouseholdRequestMetas()
            }
        }
    private val _search = MutableLiveData<String>()

    val searchItems: LiveData<Resource<List<HouseholdRequestMeta>>>? = Transformations
        .switchMap(_search) { search ->
            if (search == null) {
                AbsentLiveData.create()
            } else {
                householdRequestRepository.searchHouseholds(search)
            }
        }

    fun setSearch(search: String?) {
        if (_search.value != search) {
            _search.value = search
        }
    }


    private val _householdRequestMetas = MutableLiveData<List<HouseholdRequestMeta>>()

    val householdRequestMetasSave: LiveData<Resource<List<HouseholdRequestMeta>>>? = Transformations
        .switchMap(_householdRequestMetas) { search ->
            if (search == null) {
                AbsentLiveData.create()
            } else {
                householdRequestRepository.insertHouseholdRequestAll(search)
            }
        }

    fun setHouseholdRequestMetas(householdRequestMetas: List<HouseholdRequestMeta>?) {
        if (_householdRequestMetas.value != householdRequestMetas) {
            _householdRequestMetas.value = householdRequestMetas
        }
    }


    private val _member = MutableLiveData<List<Member>>()

    val MembersSave: LiveData<Resource<List<Member>>>? = Transformations
        .switchMap(_member) { search ->
            if (search == null) {
                AbsentLiveData.create()
            } else {
                membersRepository.insertMembersSave(search)
            }
        }

    fun setMembers(members: List<Member>?) {
        if (_member.value != members) {
            _member.value = members
        }
    }


    val participantMetas: LiveData<Resource<List<ParticipantRequest>>>? = Transformations
        .switchMap(_visitedHousehold) { emailx ->
            if (emailx == null) {
                AbsentLiveData.create()
            } else {
                participantMetaRepository.syncParticipantMetas()
            }
        }

    private val _sampleIdAll: MutableLiveData<String> = MutableLiveData()

    var screeningIdCheckAll: LiveData<Resource<List<SampleRequest>>>? = Transformations
        .switchMap(_sampleIdAll) { sampleId ->
            if (sampleId == null) {
                AbsentLiveData.create()
            } else {
                sampleRequestRepository.getSamples()
            }
        }

    fun setSampleIdAll(sampleId: String?) {
        _sampleIdAll.value = sampleId
    }


}
