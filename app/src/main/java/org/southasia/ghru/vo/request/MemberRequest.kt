package org.southasia.ghru.vo.request

import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import org.southasia.ghru.BR
import java.io.Serializable

class MemberRequest : BaseObservable(), Serializable {

    companion object {
        fun build(): MemberRequest {
            val memberRequest = MemberRequest()
            memberRequest.screeningId = String()
            memberRequest.consentObtained = true
            memberRequest.firstName = String()
            memberRequest.lastName = String()
            memberRequest.nickName = String()
            memberRequest.gender = String()
            memberRequest.hoursFasted = 0
            memberRequest.idNumber = String()
            memberRequest.enumerationId = String()
            memberRequest.memberId = String()
            memberRequest.idType = String()
            memberRequest.videoWatched = String()
            memberRequest.contactDetails = ContactsDetail.build()
            memberRequest.alternateContactsDetails = AlternateContactsDetail.build()
            memberRequest.age = Age.build()
            memberRequest.address = AddressX.build()
            return memberRequest
        }
    }

    fun setGender(g: Gender) {
        gender = g.gender
    }

    override fun toString(): String {
        return super.toString()
    }


    var consentObtained: Boolean = true
        set(value) {
            field = value
            notifyPropertyChanged(BR.consentObtained)
        }
        @Bindable get() = field


    var screeningId: String = String()
        set(value) {
            field = value
            notifyPropertyChanged(BR.screeningId)
        }
        @Bindable get() = field


    var firstName: String = String()
        set(value) {
            field = value
            notifyPropertyChanged(BR.firstName)
        }
        @Bindable get() = field


    var nickName: String = String()
        set(value) {
            field = value
            notifyPropertyChanged(BR.nickName)
        }
        @Bindable get() = field


    var lastName: String = String()
        set(value) {
            field = value
            notifyPropertyChanged(BR.lastName)
        }
        @Bindable get() = field


    var gender: String = String()
        set(value) {
            field = value.toLowerCase()
            notifyPropertyChanged(BR.gender)
        }
        @Bindable get() = field


    var hoursFasted: Int = 0
        set(value) {
            field = value
            notifyPropertyChanged(BR.hoursFasted)
        }
        @Bindable get() = field


    var idNumber: String = String()
        set(value) {
            field = value
            notifyPropertyChanged(BR.idNumber)
        }
        @Bindable get() = field


    var enumerationId: String = String()
        set(value) {
            field = value
            notifyPropertyChanged(BR.enumerationId)
        }
        @Bindable get() = field


    var memberId: String = String()
        set(value) {
            field = value
            notifyPropertyChanged(BR.memberId)
        }
        @Bindable get() = field


    var idType: String = String()
        set(value) {
            field = value
            notifyPropertyChanged(BR.idType)
        }
        @Bindable get() = field


    var videoWatched: String = String()
        set(value) {
            field = value
            notifyPropertyChanged(BR.videoWatched)
        }
        @Bindable get() = field


    var contactDetails: ContactsDetail = ContactsDetail.build()
        set(value) {
            field = value
            notifyPropertyChanged(BR.contactDetails)
        }
        @Bindable get() = field


    var alternateContactsDetails: AlternateContactsDetail = AlternateContactsDetail.build()
        set(value) {
            field = value
            notifyPropertyChanged(BR.alternateContactsDetails)
        }
        @Bindable get() = field


    var age: Age = Age.build()
        set(value) {
            field = value
            notifyPropertyChanged(BR.age)
        }
        @Bindable get() = field


    var address: AddressX = AddressX.build()
        set(value) {
            field = value
            notifyPropertyChanged(BR.address)
        }
        @Bindable get() = field


}

class ContactsDetail : BaseObservable(), Serializable {
    companion object {
        fun build(): ContactsDetail {
            val contactsDetail = ContactsDetail()
            contactsDetail.phoneNumberAlternate = String()
            contactsDetail.phoneNumberPreferred = String()
            contactsDetail.email = String()
            return contactsDetail
        }
    }


    var phoneNumberAlternate: String = String()
        set(value) {
            field = value
            notifyPropertyChanged(BR.phoneNumberAlternate)
        }
        @Bindable get() = field


    var phoneNumberPreferred: String = String()
        set(value) {
            field = value
            notifyPropertyChanged(BR.phoneNumberPreferred)
        }
        @Bindable get() = field


    var email: String = String()
        set(value) {
            field = value
            notifyPropertyChanged(BR.email)
        }
        @Bindable get() = field


}

class AlternateContactsDetail : BaseObservable(), Serializable {
    companion object {
        fun build(): AlternateContactsDetail {
            val alternateContactsDetail = AlternateContactsDetail()
            alternateContactsDetail.name = String()
            alternateContactsDetail.relationship = String()
            alternateContactsDetail.address = String()
            alternateContactsDetail.phonePreferred = String()
            alternateContactsDetail.phoneAlternate = String()
            alternateContactsDetail.email = String()
            return alternateContactsDetail
        }
    }


    var name: String = String()
        set(value) {
            field = value
            notifyPropertyChanged(BR.name)
        }
        @Bindable get() = field


    var relationship: String = String()
        set(value) {
            field = value
            notifyPropertyChanged(BR.relationship)
        }
        @Bindable get() = field


    var address: String = String()
        set(value) {
            field = value
            notifyPropertyChanged(BR.address)
        }
        @Bindable get() = field


    var phonePreferred: String = String()
        set(value) {
            field = value
            notifyPropertyChanged(BR.phonePreferred)
        }
        @Bindable get() = field


    var phoneAlternate: String = String()
        set(value) {
            field = value
            notifyPropertyChanged(BR.phoneAlternate)
        }
        @Bindable get() = field


    var email: String = String()
        set(value) {
            field = value
            notifyPropertyChanged(BR.email)
        }
        @Bindable get() = field
}


class AddressX : BaseObservable(), Serializable {
    companion object {
        fun build(): AddressX {
            val address = AddressX()
            address.street = String()
            address.country = String()
            address.locality = String()
            address.postcode = String()
            return address
        }
    }


    var street: String = String()
        set(value) {
            field = value
            notifyPropertyChanged(BR.street)
        }
        @Bindable get() = field


    var country: String = String()
        set(value) {
            field = value
            notifyPropertyChanged(BR.country)
        }
        @Bindable get() = field


    var locality: String = String()
        set(value) {
            field = value
            notifyPropertyChanged(BR.locality)
        }
        @Bindable get() = field


    var postcode: String = String()
        set(value) {
            field = value
            notifyPropertyChanged(BR.postcode)
        }
        @Bindable get() = field
}

class Age : BaseObservable(), Serializable {
    companion object {
        fun build(): Age {
            val age = Age()
            age.dob = String()
            age.ageInYears = String()
            return age
        }
    }


    var dob: String = String()
        set(value) {
            field = value
            notifyPropertyChanged(BR.dob)
        }
        @Bindable get() = field


    var ageInYears: String = String()
        set(value) {
            field = value
            notifyPropertyChanged(BR.ageInYears)
        }
        @Bindable get() = field

}
