package org.southasia.ghru.ui.samplemanagement.fastingbloodglucose


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
import org.southasia.ghru.databinding.FastingBloodGlucoseFragmentBinding
import org.southasia.ghru.di.Injectable
import org.southasia.ghru.event.BusProvider
import org.southasia.ghru.event.FastingBloodGlucoseRxBus
import org.southasia.ghru.sync.JanaCareGlucoseRxBus
import org.southasia.ghru.util.Constants.Companion.FBG_MAX_VAL
import org.southasia.ghru.util.Constants.Companion.FBG_MIN_VAL
import org.southasia.ghru.util.autoCleared
import org.southasia.ghru.util.hideKeyboard
import org.southasia.ghru.util.singleClick
import org.southasia.ghru.vo.*
import javax.inject.Inject

class FastingBloodGlucoseFragment : Fragment(), Injectable {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory


    var binding by autoCleared<FastingBloodGlucoseFragmentBinding>()


    var dataBindingComponent: DataBindingComponent = FragmentDataBindingComponent(this)

    @Inject
    lateinit var samplemangementfastingbloodglucoseViewModel: FastingBloodGlucoseViewModel

    private val disposables = CompositeDisposable()

    private lateinit var fastingBloodGlucose: FastingBloodGlucose

    private lateinit var validator: Validator

    private var deviceListName: MutableList<String> = arrayListOf()
    private var deviceListObject: List<StationDeviceData> = arrayListOf()
    private var selectedDeviceID: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        disposables.add(
            JanaCareGlucoseRxBus.getInstance().toObservable()
                .subscribe({ result ->
                    if (result != null) {
                        binding.textInputEditTextFBG.setText(result.result.result)
                        binding.textviewFbgAinaValue.setText(result.result.result.toString())
                        samplemangementfastingbloodglucoseViewModel.fastingBloodGlucose.value = result.result.result.toString()
                    }

                }, { error ->
                    error.printStackTrace()
                })
        )
        fastingBloodGlucose = FastingBloodGlucose.build()
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val dataBinding = DataBindingUtil.inflate<FastingBloodGlucoseFragmentBinding>(
            inflater,
            R.layout.fasting_blood_glucose_fragment,
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
        binding.fastingBloodGlucose = fastingBloodGlucose
        binding.buttonSubmit.singleClick {
            //  Timber.d("ddce " + binding.fastingBloodGlucose!!.value + " " + binding.fastingBloodGlucose!!.probeId)
            if(selectedDeviceID==null)
            {
                binding.textViewDeviceError.visibility = View.VISIBLE
            }
            else if (validator.validate() && isValidFBGRange()) {
                binding.root.hideKeyboard()

                binding.fastingBloodGlucose?.deviceId = selectedDeviceID!!
                val mFastingBloodGlucoseDto = FastingBloodGlucoseDto(
                    value = binding.fastingBloodGlucose!!.value + " mg/dL",
                    device_id = binding.fastingBloodGlucose?.deviceId,
                    lot_id = binding.fastingBloodGlucose!!.lotId,
                    comment = binding.fastingBloodGlucose!!.comment
                )
                FastingBloodGlucoseRxBus.getInstance().post(mFastingBloodGlucoseDto)
                BusProvider.getInstance().post(mFastingBloodGlucoseDto);
                navController().popBackStack()
                //navController().popBackStack()
            }
        }

        samplemangementfastingbloodglucoseViewModel.fastingBloodGlucose.observe(this, Observer { fbg ->
            isValidFBGRange()
        })
        binding.buttonJanacare.singleClick {
            // navController().navigate(R.id.action_samplemangementhb1AcFragment_to_bagScanBarcodeFragment)
            startAina("com.janacare.ainamini.openAinaMini", AINA_REQUEST_CODE_GLUCOSE);

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

            startAina("com.janacare.ainamini.openAinaMini", AINA_REQUEST_CODE_GLUCOSE);

        }
        deviceListName.clear()
        deviceListName.add(getString(R.string.unknown))
        val adapter = ArrayAdapter(context!!, R.layout.basic_spinner_dropdown_item, deviceListName)
        binding.deviceIdSpinner.setAdapter(adapter);
        samplemangementfastingbloodglucoseViewModel.setStationName(Measurements.BLOOD_GLUCOSE)
        samplemangementfastingbloodglucoseViewModel.stationDeviceList?.observe(this, Observer {
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


    val AINA_REQUEST_CODE_GLUCOSE = 10

    fun isAinaPackageAvailable(context: Context): Boolean {
        val packages: List<ApplicationInfo>
        val pm: PackageManager
        pm = context.getPackageManager()
        packages = pm.getInstalledApplications(0)
        for (packageInfo in packages) {
            if (packageInfo.packageName.contains("com.janacare.ainamini")) return true
        }
        return false
    }

    private fun startAina(action: String, requestCode: Int) {
        var ainaIntent: Intent? = null
        if (isAinaPackageAvailable(activity!!.getApplicationContext())) {
            ainaIntent = Intent(action)
            activity!!.startActivityForResult(ainaIntent, requestCode)
//            binding.ainaViewConnected.visibility = View.VISIBLE
//            binding.ainaViewNotConnected.visibility = View.GONE
        } else {
            Toast.makeText(activity, "Aina app not installed", Toast.LENGTH_SHORT).show()
//            binding.ainaViewConnected.visibility = View.GONE
//            binding.ainaViewNotConnected.visibility = View.VISIBLE
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

    fun isValidFBGRange(): Boolean {
        if(binding.textInputEditTextFBG.text!=null) {
            val value = binding.textInputEditTextFBG.text.toString().toFloat()
            if (value >= FBG_MIN_VAL && value <= FBG_MAX_VAL) {
                binding.textInputLayoutFBG.error = ""
                return true
            } else {
                binding.textInputLayoutFBG.error = getString(R.string.error_invalid_input)

                Toast.makeText(activity, getString(R.string.error_blood_glucose_message), Toast.LENGTH_SHORT).show()
                return false

            }
        }else{
            return false
        }

    }

}
