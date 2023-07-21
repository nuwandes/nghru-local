package org.southasia.ghru.ui.samplemanagement.hdl


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
import org.southasia.ghru.databinding.HDLFragmentBinding
import org.southasia.ghru.di.Injectable
import org.southasia.ghru.event.BusProvider
import org.southasia.ghru.event.HDLRxBus
import org.southasia.ghru.sync.CholesterolcomEventType
import org.southasia.ghru.sync.JanaCareCholesterolcomRxBus
import org.southasia.ghru.util.*
import org.southasia.ghru.vo.*
import javax.inject.Inject


class HDLFragment : Fragment(), Injectable {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    @Inject
    lateinit var appExecutors: AppExecutors

    @Inject
    lateinit var localeManager: LocaleManager


    var binding by autoCleared<HDLFragmentBinding>()


    var dataBindingComponent: DataBindingComponent = FragmentDataBindingComponent(this)

    private val disposables = CompositeDisposable()

    private lateinit var validator: Validator

    @Inject
    lateinit var viewModel: HDLViewModel
    private lateinit var hDl: HDL

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
                        CholesterolcomEventType.HDL -> {
                            if (result != null) {
                                binding.textInputEditTextHDL.setText(result.result.result.toString())
                                binding.textviewFbgAinaValue.setText(result.result.result.toString())
                                binding.hDl!!.value = result.result.result.toString()
                                binding.hDl!!.probeId = result.result.lotNumber.toString()

                            }

                        }
                    }

                }, { error ->
                    error.printStackTrace()
                })
        )
        hDl = HDL.build()
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val dataBinding = DataBindingUtil.inflate<HDLFragmentBinding>(
            inflater,
            R.layout.h_d_l_fragment,
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
        binding.hDl = hDl
        binding.buttonSubmit.singleClick {

            binding.root.hideKeyboard()
            if(selectedDeviceID==null)
            {
                binding.textViewDeviceError.visibility = View.VISIBLE
            }
            else if (validator.validate() && validateHbac(binding.textInputEditTextHDL.text.toString())) {
                binding.hDl!!.deviceId = selectedDeviceID!!

                // Timber.d("ddce " + binding.hDl!!.value + " " + binding.hDl!!.probeId)
                val mhDl = HDLDto(
                    value = binding.hDl!!.value + " mg/dL",
                    lot_id = binding.hDl!!.probeId,
                    comment = binding.hDl!!.comment,
                    device_id = binding.hDl!!.deviceId
                )

                HDLRxBus.getInstance().post(mhDl)
                //navController().popBackStack()
            }
        }


        binding.buttonJanacare.singleClick {
            // navController().navigate(R.id.action_samplemangementhb1AcFragment_to_bagScanBarcodeFragment)
            startAina("com.janacare.aina.hdl", AINA_REQUEST_CODE_HDL);

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

            binding.textInputEditTextHDL.setText("")

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

            startAina("com.janacare.aina.hdl", AINA_REQUEST_CODE_HDL);

        }
        viewModel.hDl.observe(this, Observer { hbac ->
            validateHbac(hbac!!)
        })
        deviceListName.clear()
        deviceListName.add(getString(R.string.unknown))
        val adapter = ArrayAdapter(context!!, R.layout.basic_spinner_dropdown_item, deviceListName)
        binding.deviceIdSpinner.setAdapter(adapter);
        viewModel.setStationName(Measurements.HDL)
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

    val AINA_REQUEST_CODE_HDL = 30

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


    fun validateHbac(hdlS: String) :  Boolean {
        try {
            val hdld: Double = hdlS.toDouble()
            if (hdld >= Constants.HDL_CHOL_MIN_VAL && hdld <= Constants.HDL_CHOL_MAX_VAL) {
                binding.textInputLayoutHbac.error = null
                viewModel.isValidateError = false
                hDl.value = hdlS
                return true

            } else {
                viewModel.isValidateError = true
                binding.textInputLayoutHbac.error = getString(R.string.error_not_in_range)
                binding.textInputLayoutHbac.requestFocus()
                Toast.makeText(activity, getString(R.string.error_blood_hdl_cholesterol), Toast.LENGTH_SHORT).show()
                return false
            }

        } catch (e: Exception) {
            binding.textInputLayoutHbac.error = getString(R.string.error_invalid_input)
            Toast.makeText(activity, getString(R.string.error_blood_hdl_cholesterol), Toast.LENGTH_SHORT).show()
            return false
        }
    }

}
