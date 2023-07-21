package org.southasia.ghru.ui.bodymeasurements.measurements


import android.graphics.Color
import android.os.Bundle
import android.text.InputFilter
import android.text.Spanned
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
import org.southasia.ghru.databinding.MeasurementsFragmentBinding
import org.southasia.ghru.di.Injectable
import org.southasia.ghru.util.*
import org.southasia.ghru.vo.request.ParticipantRequest
import javax.inject.Inject


class MeasurementsFragment : Fragment(), Injectable {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var appExecutors: AppExecutors

    @Inject
    lateinit var localeManager: LocaleManager

    private lateinit var validator: Validator

    var binding by autoCleared<MeasurementsFragmentBinding>()

    var dataBindingComponent: DataBindingComponent = FragmentDataBindingComponent(this)

    @Inject
    lateinit var measurementViewModel: MeasurementsViewModel

    private var participant: ParticipantRequest? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            participant = arguments?.getParcelable<ParticipantRequest>("ParticipantRequest")!!
        } catch (e: KotlinNullPointerException) {

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val dataBinding = DataBindingUtil.inflate<MeasurementsFragmentBinding>(
            inflater,
            R.layout.measurements_fragment,
            container,
            false
        )
        binding = dataBinding

        setHasOptionsMenu(true)
        val appCompatActivity = requireActivity() as AppCompatActivity
        appCompatActivity.setSupportActionBar(binding.detailToolbar)
        appCompatActivity.supportActionBar?.setDisplayHomeAsUpEnabled(true)

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

        binding.heightEditText.filters = arrayOf<InputFilter>(filter)
        binding.weightEditText.filters = arrayOf<InputFilter>(filter)
        binding.fatcomEditText.filters = arrayOf<InputFilter>(filter)
        binding.visceralEditText.filters = arrayOf<InputFilter>(filter)
        binding.muscleEditText.filters = arrayOf<InputFilter>(filter)


        return dataBinding.root
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.setLifecycleOwner(this)
        binding.mesurement = measurementViewModel.getBodyMeasurement().value
        binding.participant = participant
        validator = Validator(binding)
        measurementViewModel.bodyMeasurement?.value?.height?.observe(
            this,
            Observer { mesurement -> validateHight(mesurement!!) })
        measurementViewModel.bodyMeasurement?.value?.weight?.observe(
            this,
            Observer { mesurement -> validateWeight(mesurement!!) })
        measurementViewModel.bodyMeasurement?.value?.fatComposition?.observe(
            this,
            Observer { mesurement -> validateFatCom(mesurement!!) })
        measurementViewModel.bodyMeasurement?.value?.visceralFat?.observe(
            this,
            Observer { mesurement -> validateVisceral(mesurement!!) })
        measurementViewModel.bodyMeasurement?.value?.muscle?.observe(
            this,
            Observer { mesurement -> validateMuscle(mesurement!!) })
        measurementViewModel.bodyMeasurement?.value?.hipSize?.observe(
            this,
            Observer { mesurement -> validateHipSize(mesurement!!) })
        measurementViewModel.bodyMeasurement?.value?.waistSize?.observe(
            this,
            Observer { mesurement -> validateWaistSize(mesurement!!) })
        binding.nextButton.singleClick {
            if (validator.validate()) {
                val bundle =
                    bundleOf("ParticipantRequest" to participant, Constants.ARG_BODY_MEASURMENT to binding.mesurement)
                binding.root.hideKeyboard()
                navController().navigate(R.id.action_measurementsSecondFragment_to_pPManualOneFragment, bundle)
            }
        }
        if (BuildConfig.DEBUG) {
//            measurementViewModel.bodyMeasurement?.value?.height?.value = "160.0"
//            measurementViewModel.bodyMeasurement?.value?.weight?.value = "80.0"
//            measurementViewModel.bodyMeasurement?.value?.fatComposition?.value = "30.0"
//            measurementViewModel.bodyMeasurement?.value?.visceralFat?.value = "5.0"
//            measurementViewModel.bodyMeasurement?.value?.muscle?.value = "5.0"
//            measurementViewModel.bodyMeasurement?.value?.hipSize?.value = "55"
//            measurementViewModel.bodyMeasurement?.value?.waistSize?.value = "55"
        }
        binding.previousButton.singleClick {
            navController().popBackStack()
        }
        validateNextButton()
    }


    private fun validateHight(hight: String) {

        try {

            val hightVal: Double = hight.toDouble()
            if (hightVal >= Constants.BD_HEIGHT_MIN_VAL && hightVal <= Constants.BD_HEIGHT_MAX_VAL) {

                binding.heightTextLayout.error = null
                measurementViewModel.isValidHeight = false

            } else {
                measurementViewModel.isValidHeight = true
                binding.heightTextLayout.error = getString(R.string.error_not_in_range)
            }

            validateNextButton()

        } catch (e: Exception) {
            binding.heightTextLayout.error = getString(R.string.error_invalid_input)
        }

    }


    private fun validateWeight(weight: String) {

        try {
            val weightVal: Double = weight.toDouble()
            if (weightVal >= Constants.BD_WEIGHT_MIN_VAL && weightVal <= Constants.BD_WEIGHT_MAX_VAL) {
                binding.weightTextLayout.error = null
                measurementViewModel.isValidWeight = false

            } else {
                measurementViewModel.isValidWeight = true
                binding.weightTextLayout.error = getString(R.string.error_not_in_range)
            }

            validateNextButton()

        } catch (e: Exception) {
            binding.weightTextLayout.error = getString(R.string.error_invalid_input)
        }

    }


    private fun validateFatCom(fat: String) {

        try {
            val fatVal: Double = fat.toDouble()
            if (fatVal >= Constants.BD_FATCOM_MIN_VAL && fatVal <= Constants.BD_FATCOM_MAX_VAL) {
                binding.fatcomTextLayout.error = null
                measurementViewModel.isValifFatComp = false

            } else {
                measurementViewModel.isValifFatComp = true
                binding.fatcomTextLayout.error = getString(R.string.error_not_in_range)
            }

            validateNextButton()

        } catch (e: Exception) {
            binding.fatcomTextLayout.error = getString(R.string.error_invalid_input)
        }

    }

    private fun validateVisceral(fat: String) {

        try {
            val fatVal: Double = fat.toDouble()
            if (fatVal >= Constants.BD_VISCERAL_MIN_VAL && fatVal <= Constants.BD_VISCERAL_MAX_VAL) {
                binding.visceralTextLayout.error = null
                measurementViewModel.isValidVisceralFat = false

            } else {
                measurementViewModel.isValidVisceralFat = true
                binding.visceralTextLayout.error = getString(R.string.error_not_in_range)
            }

            validateNextButton()

        } catch (e: Exception) {
            binding.visceralTextLayout.error = getString(R.string.error_invalid_input)
        }

    }

    private fun validateMuscle(fat: String) {

        try {
            val fatVal: Double = fat.toDouble()
            if (fatVal >= Constants.BD_MUSCLE_MIN_VAL && fatVal <= Constants.BD_MUSCLE_MAX_VAL) {
                binding.muscleTextLayout.error = null
                measurementViewModel.isValidMuscle = false

            } else {
                measurementViewModel.isValidMuscle = true
                binding.muscleTextLayout.error = getString(R.string.error_not_in_range)
            }

            validateNextButton()

        } catch (e: Exception) {
            binding.visceralTextLayout.error = getString(R.string.error_invalid_input)
        }

    }

    private fun validateHipSize(fat: String) {

        try {
            val fatVal: Double = fat.toDouble()
            if (fatVal >= Constants.BD_HIP_MIN_VAL && fatVal <= Constants.BD_HIP_MAX_VAL) {
                binding.hipSizeTextLayout.error = null
                measurementViewModel.isValidHipSize = false

            } else {
                measurementViewModel.isValidHipSize = true
                binding.hipSizeTextLayout.error = getString(R.string.error_not_in_range)
            }

            validateNextButton()

        } catch (e: Exception) {
            binding.hipSizeTextLayout.error = getString(R.string.error_invalid_input)
        }

    }

    private fun validateWaistSize(fat: String) {

        try {
            val fatVal: Double = fat.toDouble()
            if (fatVal >= Constants.BD_WAIST_MIN_VAL && fatVal <= Constants.BD_WAIST_MAX_VAL) {
                binding.waistTextLayout.error = null
                measurementViewModel.isValidWaistSize = false

            } else {
                measurementViewModel.isValidWaistSize = true
                binding.waistTextLayout.error = getString(R.string.error_not_in_range)
            }

            validateNextButton()

        } catch (e: Exception) {
            binding.waistTextLayout.error = getString(R.string.error_invalid_input)
        }

    }

    private fun validateNextButton() {

        if (!binding.mesurement?.height?.value.isNullOrBlank()
            && !binding.mesurement?.weight?.value.isNullOrBlank()
            && !binding.mesurement?.fatComposition?.value.isNullOrBlank()
            && !measurementViewModel.isValidHeight
            && !measurementViewModel.isValidWeight
            && !measurementViewModel.isValifFatComp
            && !measurementViewModel.isValidVisceralFat
            && !measurementViewModel.isValidMuscle
            && !measurementViewModel.isValidWaistSize
            && !measurementViewModel.isValidHipSize
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
