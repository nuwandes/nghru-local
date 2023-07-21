package org.southasia.ghru.ui.spirometry.record

import android.os.Bundle
import android.text.InputFilter
import android.text.Spanned
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
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
import org.southasia.ghru.databinding.SpirometryRecordTestFragmentBinding
import org.southasia.ghru.di.Injectable
import org.southasia.ghru.event.SpirometryRecordTestRxBus
import org.southasia.ghru.util.Constants.Companion.FEV_EVC_MAX_VAL
import org.southasia.ghru.util.Constants.Companion.FEV_EVC_MIN_VAL
import org.southasia.ghru.util.Constants.Companion.FEV_MAX_VAL
import org.southasia.ghru.util.Constants.Companion.FEV_MIN_VAL
import org.southasia.ghru.util.Constants.Companion.FVC_MAX_VAL
import org.southasia.ghru.util.Constants.Companion.FVC_MIN_VAL
import org.southasia.ghru.util.Constants.Companion.pEFR_MIN_VAL
import org.southasia.ghru.util.Constants.Companion.pEFR_MAX_VAL
import org.southasia.ghru.util.LocaleManager
import org.southasia.ghru.util.autoCleared
import org.southasia.ghru.util.hideKeyboard
import org.southasia.ghru.vo.SpirometryRecord
import javax.inject.Inject

class RecordTestFragment : Fragment(), Injectable {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    @Inject
    lateinit var appExecutors: AppExecutors

    @Inject
    lateinit var localeManager: LocaleManager

    var binding by autoCleared<SpirometryRecordTestFragmentBinding>()

    var dataBindingComponent: DataBindingComponent = FragmentDataBindingComponent(this)
    @Inject
    lateinit var recordTestViewModel: RecordTestViewModel

    private lateinit var validator: Validator

    var isValidRecord: Boolean = false


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val dataBinding = DataBindingUtil.inflate<SpirometryRecordTestFragmentBinding>(
            inflater,
            R.layout.spirometry_record_test_fragment,
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
            val maxDigitsAfterDecimalPoint = 2

            override fun filter(
                source: CharSequence, start: Int, end: Int,
                dest: Spanned, dstart: Int, dend: Int
            ): CharSequence? {
                val builder = StringBuilder(dest)
                builder.replace(
                    dstart, dend, source
                        .subSequence(start, end).toString()
                )
                return if (!builder.toString().matches(("(([0-9]{1})([0-9]{0," + (maxDigitsBeforeDecimalPoint - 1) + "})?)?(\\.[0-9]{0," + maxDigitsAfterDecimalPoint + "})?").toRegex())) {
                    if (source.length == 0) dest.subSequence(dstart, dend) else ""
                } else null

            }
        }

        binding.editTextFEV.filters = arrayOf<InputFilter>(filter)
        binding.editTextFVC.filters = arrayOf<InputFilter>(filter)
        binding.editTextRatio.filters = arrayOf<InputFilter>(filter)
        binding.editTextPEV.filters = arrayOf<InputFilter>(filter)


        return dataBinding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        if (BuildConfig.DEBUG) {
//            recordTestViewModel.spirometryRecord().value?.fev?.value = "10"
//            recordTestViewModel.spirometryRecord().value?.fvc?.value = "10"
//            recordTestViewModel.spirometryRecord().value?.ratio?.value = "10"
//            recordTestViewModel.spirometryRecord().value?.pEFR?.value = "10"
        }
        recordTestViewModel.isValidFEV = false
        recordTestViewModel.isValidFVC = false
        recordTestViewModel.isValidRatio = false
        recordTestViewModel.isValidpEFR = false

        binding.setLifecycleOwner(this)
        binding.mesurement = recordTestViewModel.spirometryRecord().value
        validator = Validator(binding)
        recordTestViewModel.spirometryRecord?.value?.fev?.observe(this, Observer { mesurement ->
            validateFEV(mesurement!!)

        })
        recordTestViewModel.spirometryRecord?.value?.fvc?.observe(this, Observer { mesurement ->
            validateFVC(mesurement!!)

        })
        recordTestViewModel.spirometryRecord?.value?.ratio?.observe(
            this,
            Observer { mesurement -> validateRatio(mesurement!!) })


        recordTestViewModel.spirometryRecord?.value?.pEFR?.observe(
            this,
            Observer { mesurement -> validatepEFR(mesurement!!) })


        binding.buttonRecord.setOnClickListener {

            validateFEV(binding.mesurement?.fev?.value.toString())
            validateFVC(binding.mesurement?.fvc?.value.toString())
            validateRatio(binding.mesurement?.ratio?.value.toString())
            validatepEFR(binding.mesurement?.pEFR?.value.toString())
            validateAddButton()
            if (isValidRecord) {
                val record = SpirometryRecord()
                record.fev.postValue(binding.mesurement?.fev?.value.toString())
                record.fvc.postValue(binding.mesurement?.fvc?.value.toString())
                record.ratio.postValue(binding.mesurement?.ratio?.value.toString())
                record.pEFR.postValue(binding.mesurement?.pEFR?.value.toString())

                SpirometryRecordTestRxBus.getInstance().post(record)
                binding.root.hideKeyboard()
                navController().popBackStack()
            }

        }
        validateAddButton()


    }

    private fun calculateRatio(): String {

        val fev: Double = if (binding.mesurement?.fev != null) {
            binding.mesurement?.fev?.value?.toDouble()!!
        } else {
            1.0
        }
        val fvc: Double = if (binding.mesurement?.fvc != null) {
            binding.mesurement?.fvc?.value?.toDouble()!!
        } else {
            1.0
        }
        val ratio: Double = fev / fvc
        return String.format("%.2f", ratio);
    }

    private fun validateFEV(FEV: String) {

        try {

            val fevVal: Double = FEV.toDouble()
//            if (fevVal >= FEV_MIN_VAL && fevVal <= FEV_MAX_VAL) {
                if (fevVal > 0) {
                    if (!binding.editTextFVC.text.isNullOrEmpty())
                        recordTestViewModel.spirometryRecord?.value?.ratio?.postValue(calculateRatio().toString())
                }
                recordTestViewModel.isValidFEV = true
                binding.textLayoutFEV.error = null
                validateAddButton()
//            } else {
//                binding.textLayoutFEV.error = getString(R.string.error_invalid_input)
//                recordTestViewModel.isValidFEV = false
//            }

        } catch (e: Exception) {
            binding.textLayoutFEV.error = getString(R.string.error_invalid_input)
            recordTestViewModel.isValidFEV = false
        }
    }

    private fun validateFVC(FVC: String) {

        try {

            val fvcVal: Double = FVC.toDouble()
//            if (fvcVal >= FVC_MIN_VAL && fvcVal <= FVC_MAX_VAL) {
                if (fvcVal > 0) {
                    if (!binding.editTextFEV.text.isNullOrEmpty())
                        recordTestViewModel.spirometryRecord?.value?.ratio?.postValue(calculateRatio().toString())
                }
                recordTestViewModel.isValidFVC = true
                binding.textLayoutFVC.error = null
                validateAddButton()
//            } else {
//                binding.textLayoutFVC.error = getString(R.string.error_invalid_input)
//                recordTestViewModel.isValidFVC = false
//            }

        } catch (e: Exception) {
            binding.textLayoutFVC.error = getString(R.string.error_invalid_input)
            recordTestViewModel.isValidFVC = false
        }
    }



    private fun validateRatio(ratio: String) {
        try {

            var ratioVal: Double = ratio.toDouble()
//            if (ratioVal >= FEV_EVC_MIN_VAL && ratioVal <= FEV_EVC_MAX_VAL) {
//                recordTestViewModel.isValidRatio = true
//                binding.textLayoutRatio.error = null
//                validateAddButton()
//            } else {
//                binding.textLayoutRatio.error = getString(R.string.error_invalid_input)
//                recordTestViewModel.isValidRatio = false
//            }
            binding.textLayoutRatio.error = null
            recordTestViewModel.isValidRatio = true

        } catch (e: Exception) {
            binding.textLayoutRatio.error = getString(R.string.error_invalid_input)
            recordTestViewModel.isValidRatio = false
        }
    }

    private fun validatepEFR(pEFR: String) {

        try {

            val pEFRVal: Double = pEFR.toDouble()
//            if (pEFRVal >= pEFR_MIN_VAL && pEFRVal <= pEFR_MAX_VAL) {
                recordTestViewModel.isValidpEFR = true
                binding.textLayoutpEFR.error = null
                validateAddButton()
//            } else {
//                binding.textLayoutpEFR.error = getString(R.string.error_invalid_input)
//                recordTestViewModel.isValidpEFR = false
//            }

        } catch (e: Exception) {
            binding.textLayoutpEFR.error = getString(R.string.error_invalid_input)
            recordTestViewModel.isValidpEFR = false
        }
    }

    private fun validateAddButton() {

        if (recordTestViewModel.isValidFEV && recordTestViewModel.isValidFVC && recordTestViewModel.isValidRatio && recordTestViewModel.isValidpEFR) {
            isValidRecord = true
        } else {
            isValidRecord = false
        }
    }

    fun navController() = findNavController()
}