package org.southasia.ghru.ui.samplemanagement.hogtt


import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Bundle
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
import org.southasia.ghru.R
import org.southasia.ghru.binding.FragmentDataBindingComponent
import org.southasia.ghru.databinding.HogttFragmentBinding
import org.southasia.ghru.di.Injectable
import org.southasia.ghru.event.BusProvider
import org.southasia.ghru.event.HOGTTRxBus
import org.southasia.ghru.sync.JanaCareGlucoseRxBus
import org.southasia.ghru.util.Constants
import org.southasia.ghru.util.autoCleared
import org.southasia.ghru.util.hideKeyboard
import org.southasia.ghru.util.singleClick
import org.southasia.ghru.vo.*
import javax.inject.Inject

class HOGTTFragment : Fragment(), Injectable {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory


    var binding by autoCleared<HogttFragmentBinding>()


    var dataBindingComponent: DataBindingComponent = FragmentDataBindingComponent(this)

    @Inject
    lateinit var viewModel: HOGTTViewModel

    private val disposables = CompositeDisposable()

    private lateinit var hOGTT: HOGTT

    private lateinit var validator: Validator

    private var deviceListName: MutableList<String> = arrayListOf()
    private var deviceListObject: List<StationDeviceData> = arrayListOf()
    private var selectedDeviceID: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        disposables.add(
            JanaCareGlucoseRxBus.getInstance().toObservable()
                .subscribe({ result ->

//                    binding.textInputEditTextFBG.setText(result)
//                    binding.textviewFbgAinaValue.setText(result)
//                    viewModel.hogtt.value = result

                }, { error ->
                    error.printStackTrace()
                })
        )
        hOGTT = HOGTT.build()
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val dataBinding = DataBindingUtil.inflate<HogttFragmentBinding>(
            inflater,
            R.layout.hogtt_fragment,
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
        binding.hogtt = hOGTT

        binding.buttonSubmit.singleClick {
            binding.root.hideKeyboard()
            if(selectedDeviceID==null)
            {
                binding.textViewDeviceError.visibility = View.VISIBLE
            }
            else if (validator.validate() && isValidOGTTRange()) {

                binding.hogtt!!.deviceId = selectedDeviceID!!
                val hOGTTDto = HOGTTDto(
                    value = binding.hogtt!!.value + " mg/dL",
                    lot_id = binding.hogtt!!.probeId,
                    comment = binding.hogtt!!.comment,
                    device_id = binding.hogtt!!.deviceId
                )
                HOGTTRxBus.getInstance().post(hOGTTDto)
                BusProvider.getInstance().post(hOGTTDto);
                //navController().popBackStack()
            }
        }


        binding.buttonJanacare.singleClick {
            // navController().navigate(R.id.action_samplemangementhb1AcFragment_to_bagScanBarcodeFragment)
            startAina("com.janacare.aina.glucose", AINA_REQUEST_CODE_GLUCOSE);

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

            binding.textInputEditTextFBG.setText("")

            binding.ainaViewConnected.visibility = View.GONE
            binding.ainaViewNotConnected.visibility = View.VISIBLE

            binding.layoutFbgTextInput.visibility = View.VISIBLE
            binding.layoutFbgAinaInput.visibility = View.GONE
        }
//        if (isAinaPackageAvailable(activity!!.getApplicationContext())) {
//            binding.ainaViewNotConnected.visibility = View.GONE
//            binding.ainaViewConnected.visibility = View.VISIBLE
//
//            binding.layoutFbgTextInput.visibility = View.GONE
//            binding.layoutFbgAinaInput.visibility = View.VISIBLE
//        } else {
//            binding.ainaViewNotConnected.visibility = View.VISIBLE
//            binding.ainaViewConnected.visibility = View.GONE
//
//            binding.layoutFbgTextInput.visibility = View.VISIBLE
//            binding.layoutFbgAinaInput.visibility = View.GONE
//        }

        binding.buttonRunTest.singleClick {

            startAina("com.janacare.aina.glucose", AINA_REQUEST_CODE_GLUCOSE);

        }
        deviceListName.clear()
        deviceListName.add(getString(R.string.unknown))
        val adapter = ArrayAdapter(context!!, R.layout.basic_spinner_dropdown_item, deviceListName)
        binding.deviceIdSpinner.setAdapter(adapter);
        viewModel.setStationName(Measurements.H_OGTT)
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
        viewModel.hogtt.observe(this, Observer { hogtt ->
            isValidOGTTRange()
        })

    }


    val AINA_REQUEST_CODE_GLUCOSE = 10

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
          //  binding.ainaViewConnected.visibility = View.VISIBLE
          //  binding.ainaViewNotConnected.visibility = View.GONE
        } else {
            Toast.makeText(activity, "Aina app not installed", Toast.LENGTH_SHORT).show()
         //   binding.ainaViewConnected.visibility = View.VISIBLE
         //   binding.ainaViewNotConnected.visibility = View.GONE
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

    fun isValidOGTTRange(): Boolean {
        if (binding.textInputEditTextFBG.text != null) {
            val value = binding.textInputEditTextFBG.text.toString().toDouble()
            if (value >= Constants.OGTT_MIN_VAL && value <= Constants.OGTT_MAX_VAL) {
                binding.textInputLayoutFBG.error = ""
                return true
            } else {
                binding.textInputLayoutFBG.error = getString(R.string.error_invalid_input)
                return false

            }
        } else {
            return false
        }
    }

}
