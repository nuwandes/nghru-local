package org.southasia.ghru.ui.registerpatient.basicdetails

import android.app.DatePickerDialog
import android.content.Context
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.EditText
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingComponent
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import br.com.ilhasoft.support.validation.Validator
import org.southasia.ghru.R
import org.southasia.ghru.binding.FragmentDataBindingComponent
import org.southasia.ghru.databinding.BasicDetailsFragmentBinding
import org.southasia.ghru.di.Injectable
import org.southasia.ghru.util.*
import org.southasia.ghru.util.Constants.Companion.RELATIONSHIP_AUNT_UNCLE
import org.southasia.ghru.util.Constants.Companion.RELATIONSHIP_COLLEAGUE
import org.southasia.ghru.util.Constants.Companion.RELATIONSHIP_COUSIN
import org.southasia.ghru.util.Constants.Companion.RELATIONSHIP_FRIEND
import org.southasia.ghru.util.Constants.Companion.RELATIONSHIP_GRANDPARENT
import org.southasia.ghru.util.Constants.Companion.RELATIONSHIP_NEIGHBOUR
import org.southasia.ghru.util.Constants.Companion.RELATIONSHIP_OTHER
import org.southasia.ghru.util.Constants.Companion.RELATIONSHIP_PARENT
import org.southasia.ghru.util.Constants.Companion.RELATIONSHIP_SIBLING
import org.southasia.ghru.util.Constants.Companion.RELATIONSHIP_SPOUSE
import org.southasia.ghru.vo.Date
import org.southasia.ghru.vo.Meta
import org.southasia.ghru.vo.User
import org.southasia.ghru.vo.UserConfig
import org.southasia.ghru.vo.request.*
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class BasicDetailsFragment : Fragment(), Injectable {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    var binding by autoCleared<BasicDetailsFragmentBinding>()

    var dataBindingComponent: DataBindingComponent = FragmentDataBindingComponent(this)

    @Inject
    lateinit var basicdetailsViewModel: BasicDetailsViewModel

    private var member: Member? = null

    private var householdId: String? = null

    val sdf = SimpleDateFormat(Constants.dataFormat, Locale.US)

    var cal = Calendar.getInstance()

    var user: User? = null
    var userConfig: UserConfig? = null

    var meta: Meta? = null
    var hoursFasted: String? = null
    var memberRequest: MemberRequest = MemberRequest.build()

    lateinit var participantMeta: ParticipantMeta

    var household: HouseholdRequest? = null

    private var concentPhoto: String? = null

    private var selectedRelationShip: String? = null

    private lateinit var validator: Validator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            member = arguments?.getParcelable<Member>("member")!!
        } catch (e: KotlinNullPointerException) {
            print(e)
        }
        try {
            concentPhoto = arguments?.getString("concentPhotoPath")!!
            householdId = arguments?.getString("householdId")!!
            meta = arguments?.getParcelable<Meta>("meta")!!
            hoursFasted = arguments?.getString("hours_fasted")
            household = arguments?.getParcelable("household")
        } catch (e: KotlinNullPointerException) {
            print(e)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val dataBinding = DataBindingUtil.inflate<BasicDetailsFragmentBinding>(
            inflater,
            R.layout.basic_details_fragment,
            container,
            false
        )
        binding = dataBinding
        setHasOptionsMenu(true)
        validator = Validator(binding)
        val appCompatActivity = requireActivity() as AppCompatActivity
        appCompatActivity.setSupportActionBar(binding.detailToolbar)
        appCompatActivity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.root.hideKeyboard()
        return dataBinding.root
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.setLifecycleOwner(this)

        binding.memberRequest = memberRequest
        if (member != null) {
            binding.member = member
            memberRequest.firstName = member?.name!!
            memberRequest.lastName = member?.familyName!!
            memberRequest.nickName = if (member?.nickName == null) "" else member?.nickName!!
            memberRequest.gender = member?.gender!!
            memberRequest.hoursFasted = hoursFasted?.toInt()!!
            memberRequest.contactDetails.phoneNumberPreferred =
                    if (member?.contactNo == null) "" else member?.contactNo!!
            memberRequest.age.ageInYears = member?.age!!
            memberRequest.age.dob = member?.birthDate?.year.toString() + "-" +
                    member?.birthDate?.month.toString().format(2) + "-" + member?.birthDate?.day.toString().format(2)
            memberRequest.address.street = household?.address?.street!!
            memberRequest.address.country = household?.address?.country!!
            memberRequest.address.locality = household?.address?.locality!!
            memberRequest.address.postcode =
                    if (household?.address?.postcode == null) "" else household?.address?.postcode!!


//            if (BuildConfig.DEBUG) {
//                val addressX = AddressX()
//                addressX.street = "16/1 S weetasena silva mawatha"
//                addressX.country = "LK"
//                addressX.locality = "Ratmalana"
//                addressX.postcode = "10390"
//                memberRequest.address = addressX
//                val alternateContactsDetail = AlternateContactsDetail()
//                alternateContactsDetail.address = "16/1 S weetasena silva mawatha"
//                alternateContactsDetail.email = "shanukagaya@gail.com"
//                alternateContactsDetail.name = "shanuka"
//                alternateContactsDetail.phoneAlternate = "177229618"
//                alternateContactsDetail.phonePreferred = "112638295"
//                memberRequest.alternateContactsDetails = alternateContactsDetail
//                val age = Age()
//                age.ageInYears = "36"
//                age.dob = "1982-03-01"
//
//                memberRequest.age = age
//                val contactsDetail = ContactsDetail()
//                contactsDetail.email = "shanukagaya@gail.com"
//                contactsDetail.phoneNumberAlternate = "1772296180"
//                contactsDetail.phoneNumberPreferred = "1126382950"
//                memberRequest.contactDetails = contactsDetail
//            }


            binding.viewModel = basicdetailsViewModel
            binding.viewModel?.gender?.postValue(member?.gender)
            if (member?.birthDate != null) {
                val c = Calendar.getInstance();
                c.set(member?.birthDate!!.year, member?.birthDate!!.month - 1, member?.birthDate!!.day)
                val format = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                binding.viewModel?.birthDate?.postValue(format.format(c.time))
            }
        } else {
            binding.viewModel = basicdetailsViewModel
            binding.memberRequest?.gender = Gender.MALE.gender.toString()
            binding.viewModel?.gender?.postValue("male")
        }


        binding.executePendingBindings()
        binding.nextButton.singleClick {
            //memberRequest.gender = basicdetailsViewModel.gender.value.toString().toLowerCase()
            //Timber.d("${memberRequest.lastName} ${memberRequest.gender}")

            val gender = memberRequest.gender.toLowerCase()
            val newStr = gender.toLowerCase()
            val memberId: String? = if (member != null) {
                if (isNetworkAvailable()) member?.memberId!! else member?.uuid!!
            } else {
                null
            }

            val householdIdX: String? = if (member != null) {
                householdId
            } else {
                null
            }
            participantMeta = ParticipantMeta(
                meta = meta!!,
                body = ParticipantX(
                    consentObtained = true,
                    isEligible = true,
                    firstName = memberRequest.firstName,
                    lastName = memberRequest.lastName,
                    preferredName = memberRequest.nickName,
                    gender = newStr,
                    hoursFasted = hoursFasted!!,
                    enumerationId = householdIdX,
                    memberId = memberId,
                    idType = "NID",
                    videoWatched = false,
                    alternateContactsDetails = ParticipantAlternateContactsDetails(
                        name = memberRequest.alternateContactsDetails.name,
                        relationship = selectedRelationShip.toString(),
                        address = memberRequest.alternateContactsDetails.address,
                        email = if (memberRequest.alternateContactsDetails.email.isEmpty()) {
                            null
                        } else {
                            memberRequest.alternateContactsDetails.email
                        },
                        phone_preferred = memberRequest.alternateContactsDetails.phonePreferred,
                        phone_alternate = if (memberRequest.alternateContactsDetails.phoneAlternate.isEmpty()) {
                            null
                        } else memberRequest.alternateContactsDetails.phoneAlternate
                    ),
                    age = ParticipantAge(
                        dob = memberRequest.age.dob,
                        ageInYears = memberRequest.age.ageInYears,
                        dobComputed = true
                    ),
                    address = ParticipantAddress(
                        street = memberRequest.address.street,
                        country = memberRequest.address.country,
                        locality = memberRequest.address.locality,
                        postcode = memberRequest.address.postcode
                    ),
                    contactDetails = ParticipantContactDetails(
                        phoneNumberAlternate = if (memberRequest.contactDetails.phoneNumberAlternate.isEmpty()) null else memberRequest.contactDetails.phoneNumberAlternate,
                        phoneNumberPreferred = memberRequest.contactDetails.phoneNumberPreferred,
                        email = if (memberRequest.contactDetails.email.isEmpty()) {
                            null
                        } else memberRequest.contactDetails.email
                    ),
                    comment = null
                )
            )
            participantMeta.phoneCountryCode = userConfig?.mobileCode
            participantMeta.countryCode = userConfig?.countryCode
            //  Timber.d("par", participantMeta.toString())
            findNavController().navigate(
                R.id.action_global_IdentificationFragment,
                bundleOf("participantMeta" to participantMeta, "concentPhotoPath" to concentPhoto)
            )
        }

        val dateSetListener = DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
            basicdetailsViewModel.birthYear = year
            cal.set(Calendar.YEAR, year)
            cal.set(Calendar.MONTH, monthOfYear)
            cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)

            val birthDate: Date = Date(cal.get(Calendar.DAY_OF_MONTH), cal.get(Calendar.YEAR), cal.get(Calendar.MONTH))
            basicdetailsViewModel.birthDate.postValue(sdf.format(cal.time))
            basicdetailsViewModel.birthDateVal.postValue(birthDate)
            binding.member?.birthDate = birthDate
            binding.memberRequest?.age?.dob = view.toSimpleDateString(cal.time)

            val years = UserConfig.getAge(
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            )  //Calendar.getInstance().get(Calendar.YEAR) - year

            basicdetailsViewModel.age.value = years

            binding.memberRequest?.age?.ageInYears = years

            binding.textViewYears.text = getString(R.string.string_years)
            binding.executePendingBindings()
        }

        binding.birthDate.singleClick {
            var datepicker = DatePickerDialog(
                activity!!, R.style.datepicker, dateSetListener,
                1998,
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            )
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.YEAR, -80)
            datepicker.datePicker.minDate = calendar.timeInMillis
            datepicker.show()
        }


        val itemsRelationship = arrayOf(
            getString(R.string.registraion_contact_relationship_grandparent),
            getString(R.string.registraion_contact_relationship_parent),
            getString(R.string.registraion_contact_relationship_spouse),
            getString(R.string.registraion_contact_relationship_sibling),
            getString(R.string.registraion_contact_relationship_aunt),
            getString(R.string.registraion_contact_relationship_cousin),
            getString(R.string.registraion_contact_relationship_friend),
            getString(R.string.registraion_contact_relationship_colleague),
            getString(R.string.registraion_contact_relationship_neighbour),
            getString(R.string.registraion_contact_relationship_other)
        )
        val adapter = ArrayAdapter(context!!, R.layout.basic_spinner_dropdown_item, itemsRelationship)
        binding.contactRelationshipSpinner.setAdapter(adapter);
        //       selectedRelationShip = RELATIONSHIP_GRANDPARENT
        binding.contactRelationshipSpinner.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(
                parentView: AdapterView<*>, @NonNull selectedItemView: View?,
                position: Int,
                id: Long
            ) {

                if (position == 0)
                    selectedRelationShip = RELATIONSHIP_GRANDPARENT
                else if (position == 1)
                    selectedRelationShip = RELATIONSHIP_PARENT
                else if (position == 2)
                    selectedRelationShip = RELATIONSHIP_SPOUSE
                else if (position == 3)
                    selectedRelationShip = RELATIONSHIP_SIBLING
                else if (position == 4)
                    selectedRelationShip = RELATIONSHIP_AUNT_UNCLE
                else if (position == 5)
                    selectedRelationShip = RELATIONSHIP_COUSIN
                else if (position == 6)
                    selectedRelationShip = RELATIONSHIP_FRIEND
                else if (position == 7)
                    selectedRelationShip = RELATIONSHIP_COLLEAGUE
                else if (position == 8)
                    selectedRelationShip = RELATIONSHIP_NEIGHBOUR
                else if (position == 9)
                    selectedRelationShip = RELATIONSHIP_OTHER


                binding.memberRequest?.alternateContactsDetails?.relationship = selectedRelationShip.toString()

                validateNextButton()
            }

            override fun onNothingSelected(parentView: AdapterView<*>) {
                // your code here
            }

        }
        //  binding.textInputEditTextFathersName.addTextChangedListener(watcher)
        //  binding.contacNoEditText.addTextChangedListener(watcherContact)

        basicdetailsViewModel.setUser("user")
        basicdetailsViewModel.user?.observe(this, Observer { userData ->
            if (userData?.data != null) {

                user = userData.data
                val countryCode = user?.team?.country
                userConfig = UserConfig.getUserConfig(countryCode)

                binding.contactNumberPrimaryCodeEditText.setText(userConfig?.mobileCode + " - ")
                binding.contactNumberSecondryCodeEditText.setText(userConfig?.mobileCode + " - ")
                binding.contactPersonContactNumberPrimaryCodeEditText.setText(userConfig?.mobileCode + " - ")
                binding.contactPersonContactNumberSecondryCodeEditText.setText(userConfig?.mobileCode + " - ")

                binding.contactNumberPrimaryEditText.filters =
                        arrayOf<InputFilter>(InputFilter.LengthFilter(userConfig?.mobileMaxLength!!))
                binding.contactNumberSecondryEditText.filters =
                        arrayOf<InputFilter>(InputFilter.LengthFilter(userConfig?.mobileMaxLength!!))
                binding.contactPersonContactNumberSecondryEditText.filters =
                        arrayOf<InputFilter>(InputFilter.LengthFilter(userConfig?.mobileMaxLength!!))
                binding.contactPersonContactNumberPrimaryEditText.filters =
                        arrayOf<InputFilter>(InputFilter.LengthFilter(userConfig?.mobileMaxLength!!))

                binding
                memberRequest.address.country = user?.team?.country!!

            }
        })

        onTextChanges(binding.fullNameEditText)
        onTextChanges(binding.familyNameEditText)
        onTextChanges(binding.birthDate)
        onTextChanges(binding.contactNumberPrimaryEditText)
        onTextChanges(binding.addressEditText)
        onTextChanges(binding.areaEditText)
        onTextChanges(binding.postcodeEditText)
        onTextChanges(binding.contactPersonNameEditText)
        onTextChanges(binding.contactAddressEditText)
        onTextChanges(binding.contactPersonContactNumberPrimaryEditText)
        onTextChanges(binding.emailEditText)
        onTextChanges(binding.contactPersonEmailEditText)
        onTextChanges(binding.birthDate)

    }

    private fun onTextChanges(editText: EditText) {
        editText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                if(editText == binding.birthDate){
                    validateDOB()
                }
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                validateNextButton()
            }
        })
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                return navController().popBackStack()
            }
        }
        return super.onOptionsItemSelected(item)
    }


    private fun validateNextButton() {

        if (!binding.memberRequest?.firstName.isNullOrBlank()
            && !binding.memberRequest?.lastName.isNullOrBlank()
            && !binding.memberRequest?.gender.isNullOrBlank()
            && !binding.memberRequest?.age?.ageInYears.isNullOrBlank()
            && !binding.memberRequest?.contactDetails?.phoneNumberPreferred.isNullOrBlank()
            && !binding.memberRequest?.address?.street.isNullOrBlank()
            && !binding.memberRequest?.address?.locality.isNullOrBlank()
            && !binding.memberRequest?.alternateContactsDetails?.name.isNullOrBlank()
            && !binding.memberRequest?.alternateContactsDetails?.relationship.isNullOrBlank()
            && !binding.memberRequest?.alternateContactsDetails?.address.isNullOrBlank()
            && !binding.memberRequest?.alternateContactsDetails?.phonePreferred.isNullOrBlank()
            && validatePrimaryMobile()
            && validateSecondaryMobile()
            && validateContactPrimaryMobile()
            && validateContactSecondaryMobile()
            && validatePrimaryEmail()
            && validateContactPersonEmail()
            && validateDOB()
            && validator.validate()
        ) {
            binding.nextButton.setTextColor(Color.parseColor("#0A1D53"))
            binding.nextButton.setDrawableRightColor("#0A1D53")
            binding.nextButton.isEnabled = true
        } else {
            binding.nextButton.setTextColor(Color.parseColor("#AED6F1"));
            binding.nextButton.setDrawableRightColor("#AED6F1")
            binding.nextButton.isEnabled = false
        }

    }
    private fun validateDOB() : Boolean
    {
         if(binding.memberRequest?.age?.ageInYears != null && binding.memberRequest?.age?.ageInYears != "" && binding.memberRequest?.age?.ageInYears!!.toInt() > 17)
         {
             binding.inputLayoutBirthDate.error = null
             return true
         }
        else
         {
             binding.inputLayoutBirthDate.error = getString(R.string.error_age)
             return false
         }
    }
    private fun validatePrimaryMobile(): Boolean {

        if (!binding.contactNumberPrimaryEditText.text.isNullOrEmpty()) {
            if (UserConfig.isValidPhoneNumber(binding.contactNumberPrimaryEditText.text.toString(), userConfig!!)) {
                binding.contactNumberPrimaryTextLayout.error = null
//                binding.contactNumberPrimaryTextLayout.clearFocus()
//                binding.contactNumberSecondryEditText.requestFocus()
                return true
            } else {

                //binding.contactNumberPrimaryTextLayout.requestFocus();
                binding.contactNumberPrimaryTextLayout.error = getString(R.string.app_error_valid_phone)
                return false
            }

        } else {
            return false
        }
    }

    private fun validateSecondaryMobile(): Boolean {

        if (!binding.contactNumberSecondryEditText.text.isNullOrEmpty()) {
            if (UserConfig.isValidPhoneNumber(binding.contactNumberSecondryEditText.text.toString(), userConfig!!)) {
                binding.contactNumberSecondryTextLayout.error = null
                // binding.contactNumberSecondryTextLayout.clearFocus()
                return true
            } else {

                //   binding.contactNumberSecondryTextLayout.requestFocus();
                binding.contactNumberSecondryTextLayout.error = getString(R.string.app_error_valid_phone)
                return false
            }

        } else {
            return true // bcz optional
        }
    }

    private fun validateContactSecondaryMobile(): Boolean {

        if (!binding.contactPersonContactNumberSecondryEditText.text.isNullOrEmpty()) {
            if (UserConfig.isValidPhoneNumber(
                    binding.contactPersonContactNumberSecondryEditText.text.toString(),
                    userConfig!!
                )
            ) {
                binding.contactPersonContactNumberSecondryTextLayout.error = null
                // binding.contactPersonContactNumberSecondryTextLayout.clearFocus()
                //binding.contactPersonEmailEditText.requestFocus()

                return true
            } else {

                //binding.contactPersonContactNumberSecondryTextLayout.requestFocus();
                binding.contactPersonContactNumberSecondryTextLayout.error = getString(R.string.app_error_valid_phone)
                return false
            }

        } else {
            return true // bcz optional
        }
    }

    private fun validateContactPrimaryMobile(): Boolean {

        if (!binding.contactPersonContactNumberPrimaryEditText.text.isNullOrEmpty()) {
            if (UserConfig.isValidPhoneNumber(
                    binding.contactPersonContactNumberPrimaryEditText.text.toString(),
                    userConfig!!
                )
            ) {
                binding.contactPersonContactNumberPrimaryTextLayout.error = null
                // binding.contactPersonContactNumberPrimaryTextLayout.clearFocus()
                // binding.contactPersonContactNumberSecondryEditText.requestFocus()
                return true
            } else {

                // binding.contactPersonContactNumberPrimaryTextLayout.requestFocus();
                binding.contactPersonContactNumberPrimaryTextLayout.error = getString(R.string.app_error_valid_phone)
                return false
            }

        } else {
            return false
        }
    }

    private fun validatePrimaryEmail(): Boolean {
        if (!binding.emailEditText.text.isNullOrEmpty()) {
            if (UserConfig.isValidEMail(memberRequest.contactDetails.email)) {
                binding.emailTextLayout.error = ""
                //binding.emailTextLayout.clearFocus()
                return true
            } else {
                binding.emailTextLayout.error = getString(R.string.app_error_valid_email)
                // binding.emailTextLayout.requestFocus()
                return false
            }
        } else {
            return true // bcz optional
        }
    }

    private fun validateContactPersonEmail(): Boolean {
        if (!binding.contactPersonEmailEditText.text.isNullOrEmpty()) {
            if (UserConfig.isValidEMail(memberRequest.alternateContactsDetails.email)) {
                binding.contactPersonEmailTextLayout.error = ""
                //binding.contactPersonEmailTextLayout.clearFocus()
                // binding.nextButton.requestFocus()
                return true
            } else {
                binding.contactPersonEmailTextLayout.error = getString(R.string.app_error_valid_email)
                // binding.contactPersonEmailTextLayout.requestFocus()
                return false
            }
        } else {
            return true // bcz optional
        }
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = activity?.getSystemService(Context.CONNECTIVITY_SERVICE)
        return if (connectivityManager is ConnectivityManager) {
            val networkInfo: NetworkInfo? = connectivityManager.activeNetworkInfo
            networkInfo?.isConnected ?: false
        } else false
    }

    /**
     * Created to be able to override in tests
     */
    fun navController() = findNavController()
}





