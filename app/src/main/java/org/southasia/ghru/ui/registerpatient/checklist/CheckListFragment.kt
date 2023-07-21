package org.southasia.ghru.ui.registerpatient.checklist

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingComponent
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.check_list_fragment.*
import org.southasia.ghru.R
import org.southasia.ghru.binding.FragmentDataBindingComponent
import org.southasia.ghru.databinding.CheckListFragmentBinding
import org.southasia.ghru.di.Injectable
import org.southasia.ghru.util.autoCleared
import org.southasia.ghru.util.getLocalTimeString
import org.southasia.ghru.util.hideKeyboard
import org.southasia.ghru.util.singleClick
import org.southasia.ghru.vo.Meta
import org.southasia.ghru.vo.User
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject


class CheckListFragment : Fragment(), Injectable {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    var binding by autoCleared<CheckListFragmentBinding>()

    var dataBindingComponent: DataBindingComponent = FragmentDataBindingComponent(this)

    @Inject
    lateinit var viewModel: CheckListViewModel
    var user: User? = null
    var meta: Meta? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val dataBinding = DataBindingUtil.inflate<CheckListFragmentBinding>(
            inflater,
            R.layout.check_list_fragment,
            container,
            false
        )
        binding = dataBinding
        setHasOptionsMenu(true)
        val appCompatActivity = requireActivity() as AppCompatActivity
        appCompatActivity.setSupportActionBar(binding.detailToolbar)
        appCompatActivity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.root.hideKeyboard()

        return dataBinding.root
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.setLifecycleOwner(this)

        viewModel.setUser("user")
        viewModel.user?.observe(this, Observer { userData ->
            if (userData?.data != null) {
                // setupNavigationDrawer(userData.data)
                user = userData.data

                val sTime: String = convertTimeTo24Hours()
                val sDate: String = getDate()
                val sDateTime:String = sDate + " " + sTime

                meta = Meta(collectedBy = user?.id, startTime = sDateTime)
                meta?.registeredBy = user?.id
            }

        })

        viewModel.participantMetas?.observe(this, Observer { userData ->
            if (userData?.data != null) {
                // setupNavigationDrawer(userData.data)
//                user = userData.data
//                meta = Meta(collectedBy = user?.id, startTime = binding.root.getLocalTimeString())
//                meta?.registeredBy = user?.id
            }

        })

        binding.buttonSubmit.singleClick {
            //            var bundle = bundleOf("member" to member, "householdId" to householdId)
            binding.root.hideKeyboard()
            if (!isCheckListNotCompleted()) {
                if (validateContinue()) {
                    findNavController().navigate(
                        R.id.action_checkListFragment_to_scanQRCodeFragment,
                        bundleOf("hours_fasted" to binding.durationEditText.text.toString(), "meta" to meta)
                    )
                } else {
                    Toast.makeText(
                        context,
                        getString(R.string.registration_preregistration_nid_fail),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                Toast.makeText(
                    context,
                    getString(R.string.registration_preregistration_check_list_complete_error),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        binding.radioGroupAbove.setOnCheckedChangeListener({ radioGroup, i ->
            if (radioGroup.checkedRadioButtonId == R.id.no) {
                binding.radioGroupAboveValue = true
                radioButtonDisable(binding.yesresident, binding.noresident, null, textViewResident)
                radioButtonDisable(binding.yesfast, binding.nofast, null, textViewFast)
                radioButtonDisable(binding.yesNational, binding.noNational, null, textViewNationalID)
                radioButtonDisable(
                    binding.yesmedications,
                    binding.nomedications,
                    binding.notApplicableMedications,
                    textViewMedications
                )


            } else {
                binding.radioGroupAboveValue = false
                radioButtonEnable(binding.yesresident, binding.noresident, null, textViewResident)
                radioButtonEnable(binding.yesfast, binding.nofast, null, textViewFast)
                radioButtonEnable(binding.yesNational, binding.noNational, null, textViewNationalID)
                radioButtonEnable(
                    binding.yesmedications,
                    binding.nomedications,
                    binding.notApplicableMedications,
                    textViewMedications
                )

            }
            binding.executePendingBindings()
        })

        binding.radioGroupResident.setOnCheckedChangeListener({ radioGroup, i ->
            if (radioGroup.checkedRadioButtonId == R.id.noresident) {
                binding.radioGroupResidentValue = true
                radioButtonDisable(binding.yes, binding.no, null, textViewAbove)
                radioButtonDisable(binding.yesfast, binding.nofast, null, textViewFast)
                radioButtonDisable(binding.yesNational, binding.noNational, null, textViewNationalID)
                radioButtonDisable(
                    binding.yesmedications,
                    binding.nomedications,
                    binding.notApplicableMedications,
                    textViewMedications
                )

            } else {
                binding.radioGroupResidentValue = false
                radioButtonEnable(binding.yes, binding.no, null, textViewAbove)
                radioButtonEnable(binding.yesfast, binding.nofast, null, textViewFast)
                radioButtonEnable(binding.yesNational, binding.noNational, null, textViewNationalID)
                radioButtonEnable(
                    binding.yesmedications,
                    binding.nomedications,
                    binding.notApplicableMedications,
                    textViewMedications
                )
            }
            binding.executePendingBindings()
        })

        binding.radioGroupFast.setOnCheckedChangeListener({ radioGroup, i ->
            if (radioGroup.checkedRadioButtonId == R.id.nofast) {
                binding.radioGroupFastValue = true
                binding.radioGroupFastDurationValue = false

                radioButtonDisable(binding.yes, binding.no, null, textViewAbove)
                radioButtonDisable(binding.yesresident, binding.noresident, null, textViewResident)
                radioButtonDisable(binding.yesNational, binding.noNational, null, textViewNationalID)
                radioButtonDisable(
                    binding.yesmedications,
                    binding.nomedications,
                    binding.notApplicableMedications,
                    textViewMedications
                )

            } else if (radioGroup.checkedRadioButtonId == R.id.yesfast) {

                binding.radioGroupFastDurationValue = true
                binding.radioGroupFastValue = false

                radioButtonEnable(binding.yes, binding.no, null, textViewAbove)
                radioButtonEnable(binding.yesresident, binding.noresident, null, textViewResident)
                radioButtonEnable(binding.yesNational, binding.noNational, null, textViewNationalID)
                radioButtonEnable(
                    binding.yesmedications,
                    binding.nomedications,
                    binding.notApplicableMedications,
                    textViewMedications
                )

            } else {
                binding.radioGroupFastValue = false
            }
            binding.executePendingBindings()
        })

        binding.radioGroupNationalID.setOnCheckedChangeListener({ radioGroup, i ->
            binding.root.hideKeyboard()
            if (radioGroup.checkedRadioButtonId == R.id.noNational) {
                binding.radioGroupNationalIDValue = true

                radioButtonDisable(binding.yes, binding.no, null, textViewAbove)
                radioButtonDisable(binding.yesresident, binding.noresident, null, textViewResident)
                radioButtonDisable(binding.yesfast, binding.nofast, null, textViewFast)
                radioButtonDisable(
                    binding.yesmedications,
                    binding.nomedications,
                    binding.notApplicableMedications,
                    textViewMedications
                )

            } else {
                binding.radioGroupNationalIDValue = false

                radioButtonEnable(binding.yes, binding.no, null, textViewAbove)
                radioButtonEnable(binding.yesresident, binding.noresident, null, textViewResident)
                radioButtonEnable(binding.yesfast, binding.nofast, null, textViewFast)
                radioButtonEnable(
                    binding.yesmedications,
                    binding.nomedications,
                    binding.notApplicableMedications,
                    textViewMedications
                )
            }
            binding.executePendingBindings()
        })

        binding.radioGroupMedications.setOnCheckedChangeListener({ radioGroup, i ->
            if (radioGroup.checkedRadioButtonId == R.id.nomedications) {
                binding.radioGroupMedicationsValue = true

                radioButtonDisable(binding.yes, binding.no, null, textViewAbove)
                radioButtonDisable(binding.yesresident, binding.noresident, null, textViewResident)
                radioButtonDisable(binding.yesfast, binding.nofast, null, textViewFast)
                radioButtonDisable(binding.yesNational, binding.noNational, null, textViewNationalID)

            } else {
                binding.radioGroupMedicationsValue = false

                radioButtonEnable(binding.yes, binding.no, null, textViewAbove)
                radioButtonEnable(binding.yesresident, binding.noresident, null, textViewResident)
                radioButtonEnable(binding.yesfast, binding.nofast, null, textViewFast)
                radioButtonEnable(binding.yesNational, binding.noNational, null, textViewNationalID)

            }
            binding.executePendingBindings()
        })

        binding.durationEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (s.toString().length > 0) {
                    validateFasted(s)
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }
        })


        binding.buttonBackToHomeOne.singleClick {
            val appCompatActivity = requireActivity() as AppCompatActivity
            appCompatActivity.finish()
        }
        binding.buttonBackToHomeTwo.singleClick {
            val appCompatActivity = requireActivity() as AppCompatActivity
            appCompatActivity.finish()
        }
        binding.buttonBackToHomeThree.singleClick {
            val appCompatActivity = requireActivity() as AppCompatActivity
            appCompatActivity.finish()
        }
        binding.buttonBackToHomeFour.singleClick {
            val appCompatActivity = requireActivity() as AppCompatActivity
            appCompatActivity.finish()
        }
        binding.buttonBackToHomeFive.singleClick {
            val appCompatActivity = requireActivity() as AppCompatActivity
            appCompatActivity.finish()
        }
        binding.buttonBackToHomeSix.singleClick {
            val appCompatActivity = requireActivity() as AppCompatActivity
            appCompatActivity.finish()
        }

    }

    fun radioButtonDisable(
        radioYesButton: RadioButton,
        radioNoButton: RadioButton,
        radioNoApplicableButton: RadioButton?,
        textView: TextView
    ) {
        radioYesButton.setTextColor(resources.getColor(R.color.gray))
        radioNoButton.setTextColor(resources.getColor(R.color.gray))
        radioYesButton.isEnabled = false
        radioNoButton.isEnabled = false

        textView.setTextColor(resources.getColor(R.color.gray))

        radioNoApplicableButton?.setTextColor(resources.getColor(R.color.gray))
        radioNoApplicableButton?.isEnabled = false

        binding.buttonSubmit.setBackgroundResource(R.drawable.ic_button_disable_primary)
        binding.buttonSubmit.isEnabled = false
    }

    fun radioButtonEnable(
        radioYesButton: RadioButton,
        radioNoButton: RadioButton,
        radioNoApplicableButton: RadioButton?,
        textView: TextView
    ) {
        radioYesButton.setTextColor(resources.getColor(R.color.primary_material_dark))
        radioNoButton.setTextColor(resources.getColor(R.color.primary_material_dark))
        radioYesButton.isEnabled = true
        radioNoButton.isEnabled = true

        radioNoApplicableButton?.setTextColor(resources.getColor(R.color.primary_material_dark))
        radioNoApplicableButton?.isEnabled = true

        textView.setTextColor(resources.getColor(R.color.primary_material_dark))

        binding.buttonSubmit.setBackgroundResource(R.drawable.ic_button_fill_primary)
        binding.buttonSubmit.isEnabled = true
    }

    fun validateFasted(s: CharSequence?) {
        if (s.toString().toInt() < 8) {

            binding.radioGroupFastDurationMinValue = true

            radioButtonDisable(binding.yes, binding.no, null, textViewAbove)
            radioButtonDisable(binding.yesresident, binding.noresident, null, textViewResident)
            radioButtonDisable(
                binding.yesmedications,
                binding.nomedications,
                binding.notApplicableMedications,
                textViewMedications
            )
            radioButtonDisable(binding.yesNational, binding.noNational, null, textViewNationalID)

        } else {

            binding.radioGroupFastDurationMinValue = false

            radioButtonEnable(binding.yes, binding.no, null, textViewAbove)
            radioButtonEnable(binding.yesresident, binding.noresident, null, textViewResident)
            radioButtonEnable(
                binding.yesmedications,
                binding.nomedications,
                binding.notApplicableMedications,
                textViewMedications
            )
            radioButtonEnable(binding.yesNational, binding.noNational, null, textViewNationalID)


        }
    }

    private fun validateContinue(): Boolean {
        return !binding.radioGroupAboveValue!! &&
                !binding.radioGroupResidentValue!! &&
                binding.radioGroupFastDurationValue!! &&
                !binding.radioGroupNationalIDValue!! &&
                !binding.radioGroupMedicationsValue!! &&
                !binding.radioGroupFastDurationMinValue!!
    }

    private fun isCheckListNotCompleted(): Boolean {
        return binding.radioGroupAboveValue == null ||
                binding.radioGroupResidentValue == null ||
                binding.radioGroupFastDurationValue == null ||
                binding.radioGroupNationalIDValue == null ||
                binding.radioGroupMedicationsValue == null ||
                binding.radioGroupFastDurationMinValue == null


    }

    private fun convertTimeTo24Hours(): String
    {
        val now: Calendar = Calendar.getInstance()
        val inputFormat: DateFormat = SimpleDateFormat("MMM DD, yyyy HH:mm:ss")
        val outputformat: DateFormat = SimpleDateFormat("HH:mm")
        val date: Date
        val output: String
        try{
            date= inputFormat.parse(now.time.toLocaleString())
            output = outputformat.format(date)
            return output
        }catch(p: ParseException){
            return ""
        }
    }

    private fun getDate(): String
    {
        val inputFormat: DateFormat = SimpleDateFormat("yyyy-MM-dd hh:mm")
        val outputformat: DateFormat = SimpleDateFormat("yyyy-MM-dd")
        val date: Date
        val output: String
        try{
            date= inputFormat.parse(binding.root.getLocalTimeString())
            output = outputformat.format(date)

            return output
        }catch(p: ParseException){
            return ""
        }
    }


    /**
     * Created to be able to override in tests
     */
    fun navController() = findNavController()
}
