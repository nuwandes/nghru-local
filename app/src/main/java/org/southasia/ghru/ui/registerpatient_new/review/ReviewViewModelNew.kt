package org.southasia.ghru.ui.registerpatient_new.review

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.southasia.ghru.vo.Date
import org.southasia.ghru.vo.request.Gender
import javax.inject.Inject


class ReviewViewModelNew
@Inject constructor() : ViewModel() {

    var gender: MutableLiveData<String> = MutableLiveData<String>()

    var birthYear: Int = 1998

    var birthDate: MutableLiveData<String> = MutableLiveData<String>()

    var birthDateVal: MutableLiveData<Date> = MutableLiveData<Date>()

    var contactNo: MutableLiveData<String> = MutableLiveData<String>()

    var age: MutableLiveData<String> = MutableLiveData<String>()

    fun setGender(g: Gender) {
        gender.postValue(g.gender)
    }

}
