package org.southasia.ghru.vo.request

import androidx.lifecycle.MutableLiveData

class HouseHoldMember(
    var id: String? = "",
    var fullName: MutableLiveData<String> = MutableLiveData<String>().apply { postValue("") },
    var familyName: MutableLiveData<String> = MutableLiveData<String>().apply { postValue("") },
    var nickName: MutableLiveData<String> = MutableLiveData<String>().apply { postValue("") },
    var gender: MutableLiveData<String> = MutableLiveData<String>().apply { postValue("") },
    var isPrimaryContact: MutableLiveData<Boolean> = MutableLiveData<Boolean>().apply { postValue(false) },
    var contactNo: MutableLiveData<String> = MutableLiveData<String>().apply { postValue("") },
    var dOB: MutableLiveData<String> = MutableLiveData<String>().apply { postValue("") },
    var age: MutableLiveData<String> = MutableLiveData<String>().apply { postValue("") },
    var hasStayed: MutableLiveData<Boolean> = MutableLiveData<Boolean>().apply { postValue(false) },
    var infoProvider: MutableLiveData<Boolean> = MutableLiveData<Boolean>().apply { postValue(false) },
    var isAttending: MutableLiveData<Boolean> = MutableLiveData<Boolean>().apply { postValue(false) },
    var isNotAttending: MutableLiveData<Boolean> = MutableLiveData<Boolean>().apply { postValue(true) },
    var reasonForNotAttending: MutableLiveData<String>? = MutableLiveData<String>().apply { postValue("") },
    var appointment_date: MutableLiveData<String> = MutableLiveData<String>().apply { postValue("") }

) {


}

enum class Gender(val gender: String) {
    MALE("male"),
    FEMALE("female"),
    OTHER("other")
}

enum class Reason(val reason: String) {
    UNAVAILABLE("Unavailable during the screening dates"),
    SERIOUS_ILLNESS("Serious illness"),
    OTHER("Other")

}

/*
enum class InfoProvider(val provider: String){
    SELF("Self"),
    RESPONDENT("Respondent")
}

enum class NIDStatus(val status: String){
    YES("Self"),
    NO("Respondent"),
    DONTKNOW("dontknow")
}*/
