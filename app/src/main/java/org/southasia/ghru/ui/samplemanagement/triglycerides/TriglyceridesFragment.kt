package org.southasia.ghru.ui.samplemanagement.triglycerides


import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingComponent
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import br.com.ilhasoft.support.validation.Validator
import io.reactivex.disposables.CompositeDisposable
import org.southasia.ghru.AppExecutors
import org.southasia.ghru.R
import org.southasia.ghru.binding.FragmentDataBindingComponent
import org.southasia.ghru.databinding.TriglyceridesFragmentBinding
import org.southasia.ghru.di.Injectable
import org.southasia.ghru.event.BusProvider
import org.southasia.ghru.event.TriglyceridesRxBus
import org.southasia.ghru.sync.CholesterolcomEventType
import org.southasia.ghru.sync.JanaCareCholesterolcomRxBus
import org.southasia.ghru.util.*
import org.southasia.ghru.vo.*
import javax.inject.Inject


class TriglyceridesFragment : Fragment(), Injectable {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    @Inject
    lateinit var appExecutors: AppExecutors

    @Inject
    lateinit var localeManager: LocaleManager


    var binding by autoCleared<TriglyceridesFragmentBinding>()


    var dataBindingComponent: DataBindingComponent = FragmentDataBindingComponent(this)

    private val disposables = CompositeDisposable()

    private lateinit var validator: Validator

    @Inject
    lateinit var viewModel: TriglyceridesViewModel
    private lateinit var triglyceridesX: Triglycerides

    private var deviceListName: MutableList<String> = arrayListOf()
    private var deviceListObject: List<StationDeviceData> = arrayListOf()
    private var selectedDeviceID: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        disposables.add(
            JanaCareCholesterolcomRxBus.getInstance().toObservable()
                .subscribe({ result ->
                    Log.d("Result", "household SyncCommentLifecycleObserver ${result}")
                    //handleSyncResponse(result)
                    // janacareResult = result
                            when (result.eventType) {
                                CholesterolcomEventType.TRIGLYCERIDESCODE -> {
                                    if (result != null) {
                                        binding.textInputEditTextTriglycerides.setText(result.result.result.toString())
                                        binding.textviewFbgAinaValue.setText(result.result.result.toString())
                                        viewModel.triglycerides!!.value = result.result.result.toString()
                                        binding.triglycerides!!.probeId = result.result.lotNumber.toString()}

                                }
                            }
                    if (result != null) {

                    }

                }, { error ->
                    error.printStackTrace()
                })
        )
        triglyceridesX = Triglycerides.build()
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val dataBinding = DataBindingUtil.inflate<TriglyceridesFragmentBinding>(
            inflater,
            R.layout.triglycerides_fragment,
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
        binding.viewModel = viewModel
        binding.triglycerides = triglyceridesX
        binding.buttonSubmit.singleClick {

            binding.root.hideKeyboard()
            if(selectedDeviceID==null)
            {
                binding.textViewDeviceError.visibility = View.VISIBLE
            }
            else if (validator.validate() && validateHbac(binding.textInputEditTextTriglycerides.text.toString())) {

                binding.triglycerides!!.deviceId = selectedDeviceID!!
                // Timber.d("ddce " + binding.triglycerides!!.value + " " + binding.triglycerides!!.probeId)
                val mtriglycerides = TriglyceridesDto(
                    value = binding.triglycerides!!.value + " mg/dL",
                    lot_id = binding.triglycerides!!.probeId,
                    comment = binding.triglycerides!!.comment,
                    device_id = binding.triglycerides!!.deviceId
                )

                TriglyceridesRxBus.getInstance().post(mtriglycerides)
                //navController().popBackStack()
            }
        }

        binding.buttonJanacare.singleClick {
            // navController().navigate(R.id.action_samplemangementhb1AcFragment_to_bagScanBarcodeFragment)
            startAina("com.janacare.aina.triglycerides", AINA_REQUEST_CODE_Triglycerol);

        }
        binding.buttonConnect.singleClick {
            if (isAinaPackageAvailable(activity!!.getApplicationContext())) {
                binding.ainaViewConnected.visibility = View.VISIBLE
                binding.ainaViewNotConnected.visibility = View.GONE

                binding.layoutFbgTextInput.visibility = View.GONE
                binding.layoutFbgAinaInput.visibility = View.VISIBLE
            } else {
                Toast.makeText(activity, "Aina app not installed", Toast.LENGTH_SHORT).show()
            }
        }
        binding.buttonManualEntry.singleClick {

            binding.textInputEditTextTriglycerides.setText("")

            binding.ainaViewConnected.visibility = View.GONE
            binding.ainaViewNotConnected.visibility = View.VISIBLE

            binding.layoutFbgTextInput.visibility = View.VISIBLE
            binding.layoutFbgAinaInput.visibility = View.GONE
        }
        if (isAinaPackageAvailable(activity!!.getApplicationContext())) {
            binding.ainaViewNotConnected.visibility = View.GONE
            binding.ainaViewConnected.visibility = View.VISIBLE

            binding.layoutFbgTextInput.visibility = View.GONE
            binding.layoutFbgAinaInput.visibility = View.VISIBLE
        } else {
            binding.ainaViewNotConnected.visibility = View.VISIBLE
            binding.ainaViewConnected.visibility = View.GONE

            binding.layoutFbgTextInput.visibility = View.VISIBLE
            binding.layoutFbgAinaInput.visibility = View.GONE
        }

        binding.buttonRunTest.singleClick {

            startAina("com.janacare.aina.triglycerides", AINA_REQUEST_CODE_Triglycerol);

        }
        viewModel.triglycerides.observe(this, Observer { hbac ->
            validateHbac(hbac!!)
        })
        deviceListName.clear()
        deviceListName.add(getString(R.string.unknown))
        val adapter = ArrayAdapter(context!!, R.layout.basic_spinner_dropdown_item, deviceListName)
        binding.deviceIdSpinner.setAdapter(adapter);
        viewModel.setStationName(Measurements.TRIGLYCERIDES)
        viewModel.stationDeviceList?.observe(this, Observer {
            if (it.status.equals(Status.SUCCESS)) {
                deviceListObject = it.data!!

                deviceListObject.iterator().forEach {
                    deviceListName.add(it.device_name!!)
                }
                adapter.notifyDataSetChanged()
            }
        })
        binding.deviceIdSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parentView: AdapterView<*>, @NonNull selectedItemView: View?,
                position: Int,
                id: Long
            ) {
                if (position == 0) {
                    selectedDeviceID = null
                } else {
                    binding.textViewDeviceError.visibility = View.GONE
                    selectedDeviceID = deviceListObject[position - 1].device_id
                }

            }

            override fun onNothingSelected(parentView: AdapterView<*>) {
                // your code here
            }

        }
    }

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
            binding.ainaViewConnected.visibility = View.VISIBLE
            binding.ainaViewNotConnected.visibility = View.GONE
        } else {
            Toast.makeText(activity, "Aina app not installed", Toast.LENGTH_SHORT).show()
            binding.ainaViewConnected.visibility = View.GONE
            binding.ainaViewNotConnected.visibility = View.VISIBLE
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


    fun validateHbac(triglycerides: String) : Boolean {
        try {
            var triglycerold: Double = triglycerides.toDouble()
            if (triglycerold >= Constants.TRIGLYCEROL_MIN_VAL && triglycerold <= Constants.TRIGLYCEROL_MAX_VAL) {
                binding.textInputLayoutHbac.error = null
                viewModel.isValidateError = false
                triglyceridesX.value = triglycerides
                return true

            } else {
                viewModel.isValidateError = true
                binding.textInputLayoutHbac.error = getString(R.string.error_not_in_range)
                binding.textInputLayoutHbac.requestFocus()
                Toast.makeText(activity, getString(R.string.error_blood_triglycerides), Toast.LENGTH_SHORT).show()
            return false
            }

        } catch (e: Exception) {
            binding.textInputLayoutHbac.error = getString(R.string.error_invalid_input)
            Toast.makeText(activity, getString(R.string.error_blood_triglycerides), Toast.LENGTH_SHORT).show()
            return false
        }
    }

}
