package org.southasia.ghru.ui.enumeration.member


import android.app.DatePickerDialog
import android.os.Bundle
import android.os.Handler
import android.text.InputFilter
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ScrollView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingComponent
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import br.com.ilhasoft.support.validation.Validator
import org.southasia.ghru.R
import org.southasia.ghru.binding.FragmentDataBindingComponent
import org.southasia.ghru.databinding.AddHouseHoldMemberFragmentBinding
import org.southasia.ghru.di.Injectable
import org.southasia.ghru.sync.SyncHouseholdMemberRxBus
import org.southasia.ghru.sync.SyncResponseEventType
import org.southasia.ghru.util.*
import org.southasia.ghru.vo.Meta
import org.southasia.ghru.vo.UserConfig
import org.southasia.ghru.vo.request.HouseholdRequest
import org.southasia.ghru.vo.request.Member
import org.southasia.ghru.vo.request.Reason
import java.text.SimpleDateFormat
import java.util.*
import java.util.Locale
import javax.inject.Inject


class AddHouseHoldMemberFragment : Fragment(), Injectable, Validator.ValidationListener {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var validator: Validator

    @Inject
    lateinit var addHouseHoldMemberViewModel: AddHouseHoldMemberViewModel

    var dataBindingComponent: DataBindingComponent = FragmentDataBindingComponent(this)

    var binding by autoCleared<AddHouseHoldMemberFragmentBinding>()

    private var household: HouseholdRequest? = null

    private var more: Boolean = false
    private var isDOB: Boolean = false
    private var isAppointmentDate: Boolean = false
    private var isAbleToAttend: Boolean = false

    var cal = Calendar.getInstance()

    private lateinit var meta: Meta

    var countryCode: String? = ""

    var userConfig: UserConfig? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            more = arguments?.getBoolean("more", false)!!
            household = arguments?.getParcelable<HouseholdRequest>("HouseholdRequest")!!
            meta = arguments?.getParcelable<Meta>("meta")!!
            countryCode = arguments?.getString("countryCode")

            userConfig = UserConfig.getUserConfig(countryCode)

        } catch (e: KotlinNullPointerException) {
            //Crashlytics.logException(e)
        }
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.setLifecycleOwner(this)
        if (more) {
            addHouseHoldMemberViewModel.setISSelf(false)
            binding.radioRespondent.isChecked = true
            binding.radioSelf.isChecked = false

        } else {
            addHouseHoldMemberViewModel.setISSelf(true)
            binding.radioRespondent.isChecked = false
            binding.radioSelf.isChecked = true
        }

        addHouseHoldMemberViewModel.setIsHouseHoldHead(true)
        binding.radioYesHhHead.isChecked = true
        binding.radioNoHhHead.isChecked = false

        binding.viewModel = addHouseHoldMemberViewModel
        binding.member = addHouseHoldMemberViewModel.getHouseHoldMember().value

        addHouseHoldMemberViewModel.getHouseHoldMember().value?.fullName?.observe(
            this,
            Observer { echo -> println(" Gender " + echo) })

        addHouseHoldMemberViewModel.member?.observe(this, Observer { member -> println("Member updated $member") })

        binding.editTextContactPrefix.setText(userConfig?.mobileCode + " - ")
        binding.hhHeadContacNoEditText.filters =
                arrayOf<InputFilter>(InputFilter.LengthFilter(userConfig?.mobileMaxLength!!))
        binding.ageEditText.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(2))

        binding.finishButton.singleClick {
            binding.root.hideKeyboard()
            if (validator.validate() && validateWillStay() && validateGender() && validateMbile() && validateAge() && validateAttending() && validateAppointmentDate()) {
                binding.root.hideKeyboard()
                val member = constructMember()
                if (!more) {
                    val bundle = bundleOf(
                        "HouseholdRequest" to household,
                        "member" to member,
                        "meta" to meta,
                        "countryCode" to countryCode
                    )
                    Navigation.findNavController(binding.root)
                        .navigate(R.id.action_addHouseHoldMember_to_householdMembersFragment, bundle)
                } else {
                    SyncHouseholdMemberRxBus.getInstance().post(SyncResponseEventType.SUCCESS, member)
                    navController().popBackStack()
                }
            }
        }

        validator = Validator(binding)


        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
            if (isDOB) {
                cal.set(Calendar.YEAR, year)
                cal.set(Calendar.MONTH, monthOfYear)
                cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                val myFormat = Constants.dataFormat // mention the format you need
                val sdf = SimpleDateFormat(myFormat, Locale.US)
                // textView.text = sdf.format(cal.time)
                binding.member?.dOB?.value = sdf.format(cal.time)
                val years = UserConfig.getAge(
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH)
                )  // Calendar.getInstance().get(Calendar.YEAR) - year
                binding.member?.age?.value = years.toString()
            } else if (isAppointmentDate) {
                cal.set(Calendar.YEAR, year)
                cal.set(Calendar.MONTH, monthOfYear)
                cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                val myFormat = Constants.dataFormat // mention the format you need
                val sdf = SimpleDateFormat(myFormat, Locale.US)
                // textView.text = sdf.format(cal.time)
                binding.member?.appointment_date?.value = sdf.format(cal.time)
            }

            binding.executePendingBindings()
        }
        binding.dobEditText.singleClick {
            isAppointmentDate = false
            isDOB = true
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
        binding.appointmentDateEditText.singleClick {

            isAppointmentDate = true
            isDOB = false
            var cal_appoinment = Calendar.getInstance()
            var datepicker = DatePickerDialog(
                activity!!, R.style.datepicker, dateSetListener,
                cal_appoinment.get(Calendar.YEAR),
                cal_appoinment.get(Calendar.MONTH),
                cal_appoinment.get(Calendar.DATE)
            )
            datepicker.datePicker.minDate = System.currentTimeMillis()
            datepicker.show()

        }

        binding.participantAttendRadioGroup.setOnCheckedChangeListener { radioGroup, _ ->
            // println("i $i" + radioGroup.checkedRadioButtonId)
            if (radioGroup.checkedRadioButtonId == R.id.radio_attend_no) {
                binding.linearLayoutAttented.expand()
                binding.appointmentDateTextLayout.collapse()
                isAbleToAttend = false
            } else {
                binding.linearLayoutAttented.collapse()
                binding.appointmentDateTextLayout.expand()
                isAbleToAttend = true
            }
            binding.executePendingBindings()
        }

        binding.memberHhHeadRadioGroup.setOnCheckedChangeListener { radioGroup, _ ->
            // println("i $i" + radioGroup.checkedRadioButtonId)
            if (radioGroup.checkedRadioButtonId == R.id.radio_yes_hh_head) {
                // binding.hhHeadContacNoEditText.hint = getString(R.string.string_contact_no)
            } else {

                binding.hhHeadContacNoLayout.error = null
                binding.hhHeadContacNoLayout.clearFocus()
                // binding.hhHeadContacNoEditText.hint = getString(R.string.app_contact_no_optional)
            }
            binding.executePendingBindings()
        }


    }

    private fun validateAge(): Boolean {
        if (!binding.ageEditText.text.isNullOrEmpty() && binding.ageEditText.text.toString().toInt() > 17 && binding.ageEditText.text.toString().toInt() < 81 ) {
            binding.ageTextLayout.error = null
            binding.ageTextLayout.clearFocus()

            return true
        } else {
            addHouseHoldMemberViewModel.memberValidationError?.postValue(true)
            scrolToUp()
            binding.ageTextLayout.requestFocus()
            binding.ageTextLayout.error = getString(R.string.error_age)
            return false
        }

    }

    private fun validateMbile(): Boolean {
        if (!binding.hhHeadContacNoEditText.text.toString().isEmpty()) {
            if (binding.member?.isPrimaryContact?.value!!) {

                if (UserConfig.isValidPhoneNumber(binding.hhHeadContacNoEditText.text.toString(), userConfig!!)) {
                    binding.hhHeadContacNoLayout.error = null
                    binding.hhHeadContacNoLayout.clearFocus()
                    return true
                } else {
                    addHouseHoldMemberViewModel.memberValidationError?.postValue(true)
                    scrolToUp()
                    binding.dobTextLayout.requestFocus()
                    binding.hhHeadContacNoLayout.error = getString(R.string.app_error_valid_phone)
                    return false
                }

            } else {
                return true // optional number
            }
        } else {
            if (binding.member?.isPrimaryContact?.value!!) {
                addHouseHoldMemberViewModel.memberValidationError?.postValue(true)
                scrolToUp()
                binding.hhHeadContacNoLayout.requestFocus()
                binding.hhHeadContacNoLayout.error = getString(R.string.app_error_valid_phone)
                return false
            } else {
                binding.hhHeadContacNoLayout.error = null
                binding.hhHeadContacNoLayout.clearFocus()
                return true
            }

        }
    }


    private fun validateGender(): Boolean {
        if (binding.member?.gender?.value != "") {
            binding.lbParticipantAbleAttend.clearFocus()
            binding.textViewErrorGender.visibility = View.GONE

            return true
        } else {
            addHouseHoldMemberViewModel.memberValidationError?.postValue(true)
            scrolToUp()
            binding.executePendingBindings()
            binding.lbParticipantAbleAttend.requestFocus()
            binding.textViewErrorGender.visibility = View.VISIBLE
            binding.textViewErrorGender.text = getString(R.string.app_error_gender)

            return false
        }

    }


    private fun validateAttending(): Boolean {
        if (addHouseHoldMemberViewModel.isScreeningSelected) {
            binding.participantAttendRadioGroup.clearFocus()
            binding.textViewErrorAttend.visibility = View.GONE
            val isScreening = binding.member?.isAttending?.value as Boolean
            val reason = binding.member?.reasonForNotAttending?.value as String
            if (binding.reasonRadioGroup.checkedRadioButtonId == -1 && !isScreening) {
                binding.textViewErrorAttend.requestFocus()
                binding.textViewErrorAttend.visibility = View.VISIBLE
                binding.textViewErrorAttend.text = getString(R.string.app_button_please_select_reason)
                return false
            } else {
                if (reason == Reason.OTHER.toString()) {
                    if (binding.absentReasonEditText.text.toString().isEmpty()) {
                        binding.textViewErrorAttend.requestFocus()
                        binding.textViewErrorAttend.visibility = View.VISIBLE
                        binding.textViewErrorAttend.text = getString(R.string.app_error_reason)
                        return false
                    }

                }
            }

            return true
        } else {

            addHouseHoldMemberViewModel.memberValidationError?.postValue(true)
            // scrolToUp()
            binding.participantAttendRadioGroup.requestFocus()
            binding.textViewErrorAttend.visibility = View.VISIBLE
            binding.textViewErrorAttend.text = getString(R.string.app_error_attent_or_not)
            return false
        }

    }


    private fun validateWillStay(): Boolean {
        if (addHouseHoldMemberViewModel.hasStaySelected) {
            binding.hhMemberStayRadioGroup.clearFocus()
            binding.textViewErrorWillStay.visibility = View.GONE

            return true
        } else {
            addHouseHoldMemberViewModel.memberValidationError?.postValue(true)
            scrolToUp()
            binding.hhMemberStayRadioGroup.requestFocus()
            binding.textViewErrorWillStay.visibility = View.VISIBLE
            binding.textViewErrorWillStay.text = getString(R.string.app_error_yes_or_no)
            return false
        }
    }

    private fun validateAppointmentDate(): Boolean {
        if (isAbleToAttend) {
            if (binding.appointmentDateEditText.text.isNullOrBlank()) {
                binding.appointmentDateTextLayout.error = getString(R.string.error_invalid_input)
                binding.appointmentDateTextLayout.requestFocus()
                return false
            } else {
                binding.appointmentDateTextLayout.error = null
                return true
            }
        } else {
            return true
        }

    }

    private fun scrolToUp() {
        Handler().postDelayed({
            binding.scrollView.fullScroll(ScrollView.FOCUS_UP)
        }, 600)
    }


    private fun constructMember(): Member {

        val fullName = binding.member?.fullName?.value?.trim() as String
        val familyName = binding.member?.familyName?.value as String
        val nickName = binding.member?.nickName?.value as String
        val gender = binding.member?.gender?.value as String
        val isPrimaryContact = binding.member?.isPrimaryContact?.value as Boolean
        val contactNo = binding.member?.contactNo?.value as String
        val age = binding.member?.age?.value as String
        var dob = binding.member?.dOB?.value as String


        if (binding.member?.dOB?.value.isNullOrBlank()) {
            try {
                val dobyear = Calendar.getInstance().get(Calendar.YEAR) - age.toInt()
                cal.set(Calendar.YEAR, dobyear)
                cal.set(Calendar.MONTH, 0)
                cal.set(Calendar.DAY_OF_MONTH, 1)

                val myFormat = Constants.dataFormat
                val sdf = SimpleDateFormat(myFormat, Locale.US)
                dob = sdf.format(cal.time)
                Log.d("DOB", dob)

            } catch (e: Exception) {

            }

        }

        val isStay = binding.member?.hasStayed?.value as Boolean
        val isSelf = binding.member?.infoProvider?.value as Boolean
        val isScreening = binding.member?.isAttending?.value as Boolean
        val reason = binding.member?.reasonForNotAttending?.value as String

        val reasonText = if (reason == Reason.OTHER.toString()) {
            binding.absentReasonEditText.text.toString()
        } else if (reason == Reason.UNAVAILABLE.toString()) {
            getString(R.string.enumeration_unavailable)
        } else if (reason == Reason.SERIOUS_ILLNESS.toString()) {
            getString(R.string.enumeration_serious_illness)
        } else {
            reason
        }
        println(reasonText)

        var appointment_date: String = ""

        if (isAbleToAttend) {
            val formatter = SimpleDateFormat(Constants.dataFormat, Locale.US)
            val date = formatter.parse(binding.member?.appointment_date?.value.toString())

            val myFormat = Constants.dateFormat_appointmentDB
            val sdf = SimpleDateFormat(myFormat, Locale.US)
            appointment_date = sdf.format(date)
        }

        val member = Member(
            fullName,
            familyName,
            nickName,
            gender,
            isPrimaryContact,
            contactNo,
            age,
            dob,
            isStay,
            isSelf,
            isScreening,
            reasonText,
            appointment_date
        )

        return member
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val dataBinding = DataBindingUtil.inflate<AddHouseHoldMemberFragmentBinding>(
            inflater,
            R.layout.add_house_hold_member_fragment,
            container,
            false
        )
        binding = dataBinding
        validator = Validator(binding)
        setHasOptionsMenu(true)
        val appCompatActivity = requireActivity() as AppCompatActivity
        appCompatActivity.setSupportActionBar(binding.detailToolbar)
        appCompatActivity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        return dataBinding.root
    }

    override fun onValidationError() {

    }

    override fun onValidationSuccess() {

    }

    /**
     * Created to be able to override in tests
     */
    fun navController() = findNavController()
}
