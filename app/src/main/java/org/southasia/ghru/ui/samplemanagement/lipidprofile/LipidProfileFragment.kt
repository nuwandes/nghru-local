package org.southasia.ghru.ui.samplemanagement.lipidprofile


import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingComponent
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import br.com.ilhasoft.support.validation.Validator
import io.reactivex.disposables.CompositeDisposable
import org.southasia.ghru.BuildConfig
import org.southasia.ghru.R
import org.southasia.ghru.binding.FragmentDataBindingComponent
import org.southasia.ghru.databinding.LipidProfileFragmentBinding
import org.southasia.ghru.di.Injectable
import org.southasia.ghru.event.BusProvider
import org.southasia.ghru.event.LipidProfileRxBus
import org.southasia.ghru.sync.CholesterolcomEventType
import org.southasia.ghru.sync.JanaCareCholesterolcomRxBus
import org.southasia.ghru.util.Constants
import org.southasia.ghru.util.autoCleared
import org.southasia.ghru.util.hideKeyboard
import org.southasia.ghru.util.singleClick
import org.southasia.ghru.vo.LipidProfile
import org.southasia.ghru.vo.LipidProfileDto
import javax.inject.Inject


class LipidProfileFragment : Fragment(), Injectable {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    var binding by autoCleared<LipidProfileFragmentBinding>()

    var dataBindingComponent: DataBindingComponent = FragmentDataBindingComponent(this)

    @Inject
    lateinit var samplemangementlipidprofileViewModel: LipidProfileViewModel

    private lateinit var lipidProfile: LipidProfile

    private val disposables = CompositeDisposable()

    private lateinit var validator: Validator


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        disposables.add(
            JanaCareCholesterolcomRxBus.getInstance().toObservable()
                .subscribe({ result ->
                    Log.d("Result", "household SyncCommentLifecycleObserver ${result}")
                    //handleSyncResponse(result)
                    // janacareResult = result
                    when (result.eventType) {
//                        CholesterolcomEventType.TOTAL_CHOLESTEROLHDL -> {
//                            if (result != null) {
//                                binding.textInputEditTextTotalCholesterol.setText(result.result)
//                            }
//
//                        }
//                        CholesterolcomEventType.HDL -> {
//                            if (result != null) {
//                                binding.textInputEditTextHDL.setText(result.result)
//                            }
//
//                        }
//                        CholesterolcomEventType.TRIGLYCERIDESCODE -> {
//                            if (result != null) {
//                                binding.textInputEditTextTriglycerol.setText(result.result)
//                            }
//
//                        }
                    }

                }, { error ->
                    error.printStackTrace()
                })
        )
        lipidProfile = LipidProfile.build()
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val dataBinding = DataBindingUtil.inflate<LipidProfileFragmentBinding>(
            inflater,
            R.layout.lipid_profile_fragment,
            container,
            false
        )
        binding = dataBinding
        setHasOptionsMenu(true)
        val appCompatActivity = requireActivity() as AppCompatActivity
        appCompatActivity.setSupportActionBar(binding.detailToolbar)
        appCompatActivity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        validator = Validator(binding)
        binding.root.hideKeyboard()
        return dataBinding.root
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.setLifecycleOwner(this)
        if (BuildConfig.DEBUG) {
//            lipidProfile.hDL = "7"
//            lipidProfile.totalCholesterol = "4"
//            lipidProfile.triglycerol = "7"
//            lipidProfile.lDLC = "7"
        }
        binding.lipidProfile = lipidProfile
        binding.lipidViewModel = samplemangementlipidprofileViewModel
        binding.buttonSubmit.singleClick {
            if (!samplemangementlipidprofileViewModel.isValidateError && validator.validate()) {
                // navController().navigate(R.id.action_samplemangementlipidprofileFragment_to_bagScanBarcodeFragment)
                binding.root.hideKeyboard()
                //Timber.d("lipidProfile " + binding.lipidProfile!!.totalCholesterol + " " + binding.lipidProfile!!.probeId)
                val lipidProfileDto = LipidProfileDto(
                    totalCholesterol = binding.lipidProfile!!.totalCholesterol + " mg/dL",
                    lot_id = binding.lipidProfile!!.probeId,
                    hDL = binding.lipidProfile!!.hDL + " mg/dL",
                    lDLC = binding.lipidProfile!!.lDLC + " mg/dL",
                    triglycerol = binding.lipidProfile!!.triglycerol + " mg/dL"
                )
                LipidProfileRxBus.getInstance().post(lipidProfileDto)
                //  navController().popBackStack()
            }
        }

        binding.buttonJanacareTotalCholesterol.singleClick {
            // navController().navigate(R.id.action_samplemangementhb1AcFragment_to_bagScanBarcodeFragment)
            startAina("com.janacare.aina.total_cholesterol", AINA_REQUEST_CODE_TotalCholesterol);
        }

        binding.buttonJanacareHDL.singleClick {
            // navController().navigate(R.id.action_samplemangementhb1AcFragment_to_bagScanBarcodeFragment)
            startAina("com.janacare.aina.hdl", AINA_REQUEST_CODE_HDL);
        }

        binding.buttonJanacareTriglycerol.singleClick {
            // navController().navigate(R.id.action_samplemangementhb1AcFragment_to_bagScanBarcodeFragment)
            startAina("com.janacare.aina.triglycerides", AINA_REQUEST_CODE_Triglycerol);
        }

        samplemangementlipidprofileViewModel.totalCholesterol.observe(this, Observer { chol ->
            validateTotalChol(chol!!)
        })

        samplemangementlipidprofileViewModel.hDL.observe(this, Observer { hdl ->
            validateHDL(hdl!!)
        })

        samplemangementlipidprofileViewModel.lDLC.observe(this, Observer { ldlc ->
            validateLDLC(ldlc!!)
        })

        samplemangementlipidprofileViewModel.triglycerol.observe(this, Observer { triglycerol ->
            validateTriglycerol(triglycerol!!)
        })
    }

    val AINA_REQUEST_CODE_HDL = 30
    val AINA_REQUEST_CODE_TotalCholesterol = 40
    val AINA_REQUEST_CODE_Triglycerol = 50

    fun isAinaPackageAvailable(context: Context): Boolean {
        val packages: List<ApplicationInfo>
        val pm: PackageManager
        pm = context.getPackageManager()
        packages = pm.getInstalledApplications(0)
        for (packageInfo in packages) {
            if (packageInfo.packageName.contains("com.janacare.aina")) return true
        }
        return false
    }

    private fun startAina(action: String, requestCode: Int) {
        var ainaIntent: Intent? = null
        if (isAinaPackageAvailable(activity!!.getApplicationContext())) {
            ainaIntent = Intent(action)
            activity!!.startActivityForResult(ainaIntent, requestCode)
        } else {
            Toast.makeText(activity, "Aina app not installed", Toast.LENGTH_SHORT).show()
        }
    }// Lines of code to invoke Aina launch for a specific test

    /**
     * Created to be able to override in tests
     */
    fun navController() = findNavController()

    override fun onResume() {
        super.onResume()
        BusProvider.getInstance().register(this)
    }

    override fun onPause() {
        super.onPause()
        BusProvider.getInstance().unregister(this)
    }


    fun validateLDLC(ldlc: String) {
        try {
            val ldlcd: Double = ldlc.toDouble()
            if (ldlcd >= Constants.LDLC_MIN_VAL && ldlcd <= Constants.LDLC_MAX_VAL) {
                binding.inputTextLayoutLDLC.error = null
                samplemangementlipidprofileViewModel.isValidateError = false
                lipidProfile.lDLC = ldlc

            } else {
                samplemangementlipidprofileViewModel.isValidateError = true
                binding.inputTextLayoutLDLC.error = getString(R.string.error_not_in_range)
                binding.inputTextLayoutLDLC.requestFocus()
            }

        } catch (e: Exception) {
            binding.inputTextLayoutLDLC.error = getString(R.string.error_invalid_input)
        }
    }


    fun validateTriglycerol(triglycerol: String) {
        try {
            val triglycerold: Double = triglycerol.toDouble()
            if (triglycerold >= Constants.TRIGLYCEROL_MIN_VAL && triglycerold <= Constants.TRIGLYCEROL_MAX_VAL) {
                binding.textInputLayoutTriglycerol.error = null
                samplemangementlipidprofileViewModel.isValidateError = false
                lipidProfile.triglycerol = triglycerol

            } else {
                samplemangementlipidprofileViewModel.isValidateError = true
                binding.textInputLayoutTriglycerol.error = getString(R.string.error_not_in_range)
                binding.textInputLayoutTriglycerol.requestFocus()
            }

        } catch (e: Exception) {
            binding.textInputLayoutTriglycerol.error = getString(R.string.error_invalid_input)
        }
    }


    fun validateHDL(hdl: String) {
        try {
            val hdld: Double = hdl.toDouble()
            if (hdld >= Constants.HDL_CHOL_MIN_VAL && hdld <= Constants.HDL_CHOL_MAX_VAL) {
                binding.textInputLayoutHDL.error = null
                samplemangementlipidprofileViewModel.isValidateError = false
                lipidProfile.hDL = hdl

            } else {
                samplemangementlipidprofileViewModel.isValidateError = true
                binding.textInputLayoutHDL.error = getString(R.string.error_not_in_range)
                binding.textInputLayoutHDL.requestFocus()
            }

            //validateNextButton()

        } catch (e: Exception) {
            binding.textInputLayoutHDL.error = getString(R.string.error_invalid_input)
        }
    }


    private fun validateTotalChol(chol: String) {

        try {
            val chold: Double = chol.toDouble()
            if (chold >= Constants.TOTAL_CHOL_MIN_VAL && chold <= Constants.TOTAL_CHOL_MAX_VAL) {
                binding.totalcholTextLayout.error = null
                samplemangementlipidprofileViewModel.isValidateError = false
                lipidProfile.totalCholesterol = chol

            } else {
                binding.totalcholTextLayout.requestFocus()
                samplemangementlipidprofileViewModel.isValidateError = true
                binding.totalcholTextLayout.error = getString(R.string.error_not_in_range)
            }

            //validateNextButton()

        } catch (e: Exception) {
            binding.totalcholTextLayout.error = getString(R.string.error_invalid_input)
        }

    }


}
