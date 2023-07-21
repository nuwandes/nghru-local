package org.southasia.ghru.ui.bodymeasurements.measurements.second


import android.graphics.Color
import android.os.Bundle
import android.text.InputFilter
import android.text.Spanned
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
import br.com.ilhasoft.support.validation.Validator
import org.southasia.ghru.AppExecutors
import org.southasia.ghru.BuildConfig
import org.southasia.ghru.R
import org.southasia.ghru.binding.FragmentDataBindingComponent
import org.southasia.ghru.databinding.MeasurementsSecondFragmentBinding
import org.southasia.ghru.di.Injectable
import org.southasia.ghru.util.*
import org.southasia.ghru.vo.BodyMeasurement
import org.southasia.ghru.vo.request.ParticipantRequest
import javax.inject.Inject

class MeasurementsSecondFragment : Fragment(), Injectable {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var appExecutors: AppExecutors

    @Inject
    lateinit var localeManager: LocaleManager

    private lateinit var validator: Validator

    private var measurement: BodyMeasurement? = null


    var binding by autoCleared<MeasurementsSecondFragmentBinding>()

    var dataBindingComponent: DataBindingComponent = FragmentDataBindingComponent(this)

    @Inject
    lateinit var verifyIDViewModel: MeasurementsSecondViewModel

    private var participantRequest: ParticipantRequest? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {

            measurement = arguments?.getParcelable<BodyMeasurement>(Constants.ARG_BODY_MEASURMENT)!!
            participantRequest = arguments?.getParcelable<ParticipantRequest>("ParticipantRequest")!!
            Log.d("measurement", measurement?.height?.value)
        } catch (e: KotlinNullPointerException) {

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val dataBinding = DataBindingUtil.inflate<MeasurementsSecondFragmentBinding>(
            inflater,
            R.layout.measurements_second_fragment,
            container,
            false
        )
        binding = dataBinding
        setHasOptionsMenu(true)
        val appCompatActivity = requireActivity() as AppCompatActivity
        appCompatActivity.setSupportActionBar(binding.detailToolbar)
        appCompatActivity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        validateNextButton()

        val filter = object : InputFilter {
            val maxDigitsBeforeDecimalPoint = 10
            val maxDigitsAfterDecimalPoint = 1

            override fun filter(
                source: CharSequence, start: Int, end: Int,
                dest: Spanned, dstart: Int, dend: Int
            ): CharSequence? {
                val builder = StringBuilder(dest)
                builder.replace(
                    dstart, dend, source
                        .subSequence(start, end).toString()
                )
                return if (!builder.toString().matches(("(([1-9]{1})([0-9]{0," + (maxDigitsBeforeDecimalPoint - 1) + "})?)?(\\.[0-9]{0," + maxDigitsAfterDecimalPoint + "})?").toRegex())) {
                    if (source.length == 0) dest.subSequence(dstart, dend) else ""
                } else null

            }
        }
        binding.editTextHipSize.filters = arrayOf<InputFilter>(filter)
        binding.editTextWaistSize.filters = arrayOf<InputFilter>(filter)
        return dataBinding.root
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.setLifecycleOwner(this)
        verifyIDViewModel.setBodyMeasurement(measurement!!)
        binding.mesurement = verifyIDViewModel.getBodyMeasurement().value
        //bPManualTwoViewModel.bodyMeasurement?.postValue(measurement)
        verifyIDViewModel.bodyMeasurement?.value?.hipSize?.observe(
            this,
            Observer { mesurement -> validateHipSize(mesurement!!) })
        verifyIDViewModel.bodyMeasurement?.value?.waistSize?.observe(
            this,
            Observer { mesurement -> validateWaistSize(mesurement!!) })
        validator = Validator(binding)
        binding.nextButton.singleClick {
            if (validator.validate()) {
                val bundle = bundleOf(
                    "ParticipantRequest" to participantRequest,
                    Constants.ARG_BODY_MEASURMENT to binding.mesurement
                )
                binding.root.hideKeyboard()
                navController().navigate(R.id.action_measurementsSecondFragment_to_pPManualOneFragment, bundle)
            }
        }
        if (BuildConfig.DEBUG) {
//            verifyIDViewModel.bodyMeasurement?.value?.hipSize?.value = "60"
//            verifyIDViewModel.bodyMeasurement?.value?.waistSize?.value = "80"
        }

        binding.previousButton.singleClick {
            navController().popBackStack()
        }

    }


    private fun validateHipSize(fat: String) {

        try {
            val fatVal: Double = fat.toDouble()
            if (fatVal >= Constants.BD_HIP_MIN_VAL && fatVal <= Constants.BD_HIP_MAX_VAL) {
                binding.heightTextLayout.error = null
                verifyIDViewModel.isValidHipSize = false

            } else {
                verifyIDViewModel.isValidHipSize = true
                binding.heightTextLayout.error = getString(R.string.error_not_in_range)
            }

            validateNextButton()

        } catch (e: Exception) {
            binding.heightTextLayout.error = getString(R.string.error_invalid_input)
        }

    }

    private fun validateWaistSize(fat: String) {

        try {
            val fatVal: Double = fat.toDouble()
            if (fatVal >= Constants.BD_WAIST_MIN_VAL && fatVal <= Constants.BD_WAIST_MAX_VAL) {
                binding.waistTextLayout.error = null
                verifyIDViewModel.isValidWaistSize = false

            } else {
                verifyIDViewModel.isValidWaistSize = true
                binding.waistTextLayout.error = getString(R.string.error_not_in_range)
            }

            validateNextButton()

        } catch (e: Exception) {
            binding.waistTextLayout.error = getString(R.string.error_invalid_input)
        }

    }


    private fun validateNextButton() {

        if (!binding.mesurement?.hipSize?.value.isNullOrBlank()
            && !binding.mesurement?.waistSize?.value.isNullOrBlank()
            && !verifyIDViewModel.isValidHipSize
            && !verifyIDViewModel.isValidWaistSize
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


    /**
     * Created to be able to override in tests
     */
    fun navController() = findNavController()
}
