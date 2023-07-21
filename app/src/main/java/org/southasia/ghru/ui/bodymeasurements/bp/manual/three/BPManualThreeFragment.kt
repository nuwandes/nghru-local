package org.southasia.ghru.ui.bodymeasurements.bp.manual.three

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingComponent
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import belka.us.androidtoggleswitch.widgets.BaseToggleSwitch
import org.southasia.ghru.BuildConfig
import org.southasia.ghru.R
import org.southasia.ghru.binding.FragmentDataBindingComponent
import org.southasia.ghru.databinding.BPManualThreeFragmentBinding
import org.southasia.ghru.di.Injectable
import org.southasia.ghru.ui.bodymeasurements.bp.skip.SkipDialogFragment
import org.southasia.ghru.util.Constants
import org.southasia.ghru.util.autoCleared
import org.southasia.ghru.util.setDrawableRightColor
import org.southasia.ghru.util.singleClick
import org.southasia.ghru.vo.BodyMeasurement
import org.southasia.ghru.vo.request.ParticipantRequest
import javax.inject.Inject

class BPManualThreeFragment : Fragment(), Injectable {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory


    private var measurement: BodyMeasurement? = null

    var binding by autoCleared<BPManualThreeFragmentBinding>()

    var dataBindingComponent: DataBindingComponent = FragmentDataBindingComponent(this)

    @Inject
    lateinit var viewModel: BPManualThreeViewModel


    private var participant: ParticipantRequest? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            measurement = arguments?.getParcelable<BodyMeasurement>(Constants.ARG_BODY_MEASURMENT)!!
            participant = arguments?.getParcelable<ParticipantRequest>("ParticipantRequest")!!
            Log.d("measurement", measurement?.height?.value)
        } catch (e: KotlinNullPointerException) {

        }
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val dataBinding = DataBindingUtil.inflate<BPManualThreeFragmentBinding>(
                inflater,
                R.layout.b_p_manual_three_fragment,
                container,
                false
        )
        binding = dataBinding
        setHasOptionsMenu(true)
        val appCompatActivity = requireActivity() as AppCompatActivity
        appCompatActivity.setSupportActionBar(binding.detailToolbar)
        appCompatActivity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        return dataBinding.root
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.setLifecycleOwner(this)
        viewModel.setBodyMeasurement(measurement!!);
        binding.bloodPressure = viewModel.getBloodPressure().value

        viewModel.getBloodPressure().value?.systolic?.observe(this, Observer { systolic -> validateSystolicBp(systolic!!) })
        viewModel.getBloodPressure().value?.diastolic?.observe(this, Observer { diastolic -> validateDiatolicBp(diastolic!!) })
        viewModel.getBloodPressure().value?.pulse?.observe(this, Observer { validateNextButton() })

        binding.nextButton.singleClick {

            val bloodPressure = viewModel.getBodyMeasurement().value?.bloodPressures?.value
            bloodPressure!![2].systolic.value = binding.bloodPressure?.systolic?.value
            bloodPressure[2].diastolic.value = binding.bloodPressure?.diastolic?.value
            bloodPressure[2].pulse.value = binding.bloodPressure?.pulse?.value
            val selectedArm: String = if (binding.armSwitch.checkedTogglePosition == 0) {
                "left"
            } else if (binding.armSwitch.checkedTogglePosition == 1) {
                "right"
            } else {
                "left"
            }
            bloodPressure[2].arm.value = selectedArm

            val bundle = bundleOf("ParticipantRequest" to participant, Constants.ARG_BODY_MEASURMENT to viewModel.getBodyMeasurement().value)

            navController().navigate(R.id.action_bPManualThreeFragment_to_reviewFragment, bundle)

        }

        binding.armSwitch.setOnToggleSwitchChangeListener(object : BaseToggleSwitch.OnToggleSwitchChangeListener {

            override fun onToggleSwitchChangeListener(position: Int, isChecked: Boolean) {
                // Write your code ...
                if (isChecked) {
                    viewModel.getBloodPressure().value?.arm?.value = "left"
                } else {
                    viewModel.getBloodPressure().value?.arm?.value = "right"
                }
            }
        });

        binding.previousButton.singleClick {
            navController().popBackStack()
        }

        if (BuildConfig.DEBUG) {
//            viewModel.getBloodPressure().value?.systolic?.value = "120"
//            viewModel.getBloodPressure().value?.diastolic?.value = "90"
//            viewModel.getBloodPressure().value?.pulse?.value = "80"
        }


        binding.textViewSkip.singleClick {
            val skipDialogFragment = SkipDialogFragment()
            val bundle = bundleOf("ParticipantRequest" to participant, Constants.ARG_BODY_MEASURMENT to viewModel.getBodyMeasurement().value)
            skipDialogFragment.arguments = bundle
            skipDialogFragment.show(fragmentManager!!)
        }
    }


    private fun validateSystolicBp(systolic: String) {
        try {
            val systolicVal: Double = systolic.toDouble()
            if (systolicVal > Constants.BP_SYSTOLIC_MIN_VAL && systolicVal < Constants.BP_SYSTOLIC_MAX_VAL) {
                binding.systolicInputLayout.error = null
                viewModel.isValidSystolicBp = false

            } else {
                viewModel.isValidSystolicBp = true
                binding.systolicInputLayout.error = getString(R.string.error_not_in_range)
            }

            validateNextButton()

        } catch (e: Exception) {
            binding.systolicInputLayout.error = getString(R.string.error_invalid_input)
        }
    }

    private fun validateDiatolicBp(diatolic: String) {
        try {
            val diatolicVal: Double = diatolic.toDouble()
            if (diatolicVal > Constants.BP_DIATOLIC_MIN_VAL && diatolicVal < Constants.BP_DIATOLIC_MAX_VAL) {
                binding.diastolicInputLayout.error = null
                viewModel.isValidDiastolicBp = false
            } else {
                viewModel.isValidDiastolicBp = true
                binding.diastolicInputLayout.error = getString(R.string.error_not_in_range)
            }
            validateNextButton()
        } catch (e: Exception) {
            binding.diastolicInputLayout.error = getString(R.string.error_invalid_input)
        }
    }




    private fun validateNextButton() {
        if (!binding.bloodPressure?.systolic?.value.isNullOrBlank()
                && !binding.bloodPressure?.diastolic?.value.isNullOrBlank()
                && !binding.bloodPressure?.pulse?.value.isNullOrBlank()) {
            binding.nextButton.setTextColor(Color.parseColor("#0A1D53"))
            binding.nextButton.setDrawableRightColor("#0A1D53")
            binding.nextButton.isEnabled = true
        } else {
            binding.nextButton.setTextColor(Color.parseColor("#AED6F1"));
            binding.nextButton.setDrawableRightColor("#AED6F1")
            binding.nextButton.isEnabled = false
        }
    }

    /**
     * Created to be able to override in tests
     */
    fun navController() = findNavController()
}
