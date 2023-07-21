package org.southasia.ghru.ui.samplemanagement.hb1ac


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
import org.southasia.ghru.databinding.HbAcFragmentBinding
import org.southasia.ghru.di.Injectable
import org.southasia.ghru.event.BusProvider
import org.southasia.ghru.event.Hb1AcRxBus
import org.southasia.ghru.sync.JanaCareGlucoseRxBus
import org.southasia.ghru.sync.JanaCareHb1AcRxBus
import org.southasia.ghru.util.*
import org.southasia.ghru.vo.*
import javax.inject.Inject


class Hb1AcFragment : Fragment(), Injectable {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    @Inject
    lateinit var appExecutors: AppExecutors

    @Inject
    lateinit var localeManager: LocaleManager


    var binding by autoCleared<HbAcFragmentBinding>()


    var dataBindingComponent: DataBindingComponent = FragmentDataBindingComponent(this)

    private val disposables = CompositeDisposable()

    private lateinit var validator: Validator

    @Inject
    lateinit var samplemangementhb1AcViewModel: Hb1AcViewModel
    private lateinit var hb1Ac: Hb1Ac

    private var deviceListName: MutableList<String> = arrayListOf()
    private var deviceListObject: List<StationDeviceData> = arrayListOf()
    private var selectedDeviceID: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        disposables.add(
            JanaCareHb1AcRxBus.getInstance().toObservable()
                .subscribe({ result ->
                    Log.d("Result", "household SyncCommentLifecycleObserver ${result}")
                    //handleSyncResponse(result)
                    binding.textInputEditTextHb1Ac.setText(result.result.result.toString())
                    binding.textviewFbgAinaValue.setText(result.result.result.toString())
                    binding.hb1Ac!!.value = result.result.result.toString()
                    binding.hb1Ac!!.probeId = result.result.lotNumber.toString()
                }, { error ->
                    error.printStackTrace()
                })
        )
        hb1Ac = Hb1Ac.build()
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val dataBinding = DataBindingUtil.inflate<HbAcFragmentBinding>(
            inflater,
            R.layout.hb_ac_fragment,
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
        binding.viewModel = samplemangementhb1AcViewModel
        binding.hb1Ac = hb1Ac
        binding.buttonSubmit.singleClick {
            binding.root.hideKeyboard()
            if(selectedDeviceID==null)
            {
                binding.textViewDeviceError.visibility = View.VISIBLE
            }
            else if (validator.validate() && validateHbac(binding.textInputEditTextHb1Ac.text.toString())) {

                binding.hb1Ac!!.deviceId = selectedDeviceID!!
                // Timber.d("ddce " + binding.hb1Ac!!.value + " " + binding.hb1Ac!!.probeId)
                val mhb1Ac = Hb1AcDto(
                    value = binding.hb1Ac!!.value + " %",
                    lot_id = binding.hb1Ac!!.probeId,
                    comment = binding.hb1Ac!!.comment,
                    device_id = binding.hb1Ac!!.deviceId
                )

                Hb1AcRxBus.getInstance().post(mhb1Ac)
                //navController().popBackStack()
            }
        }


        binding.buttonJanacare.singleClick {
            // navController().navigate(R.id.action_samplemangementhb1AcFragment_to_bagScanBarcodeFragment)
            startAina("com.janacare.aina.a1c", AINA_REQUEST_CODE_Hb1Ac);

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

            binding.textInputEditTextHb1Ac.setText("")

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

            startAina("com.janacare.aina.a1c", AINA_REQUEST_CODE_Hb1Ac);

        }

        samplemangementhb1AcViewModel.hb1Ac.observe(this, Observer { hbac ->
            validateHbac(hbac!!)
        })
        deviceListName.clear()
        deviceListName.add(getString(R.string.unknown))
        val adapter = ArrayAdapter(context!!, R.layout.basic_spinner_dropdown_item, deviceListName)
        binding.deviceIdSpinner.setAdapter(adapter);
        samplemangementhb1AcViewModel.setStationName(Measurements.HB1AC)
        samplemangementhb1AcViewModel.stationDeviceList?.observe(this, Observer {
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

    val AINA_REQUEST_CODE_Hb1Ac = 20

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
            binding.ainaViewConnected.visibility = View.VISIBLE
            binding.ainaViewNotConnected.visibility = View.GONE
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


    fun validateHbac(hb1ac: String) : Boolean {
        try {
            var triglycerold: Double = hb1ac.toDouble()
            if (triglycerold >= Constants.HB1AC_MIN_VAL && triglycerold <= Constants.HB1AC_MAX_VAL) {
                binding.textInputLayoutHbac.error = null
                samplemangementhb1AcViewModel.isValidateError = false
                hb1Ac.value = hb1ac
                return true

            } else {
                samplemangementhb1AcViewModel.isValidateError = true
                binding.textInputLayoutHbac.error = getString(R.string.error_not_in_range)
                binding.textInputLayoutHbac.requestFocus()
                Toast.makeText(activity, getString(R.string.error_blood_hba1c), Toast.LENGTH_SHORT).show()
                return false
            }

        } catch (e: Exception) {
            binding.textInputLayoutHbac.error = getString(R.string.error_invalid_input)

            Toast.makeText(activity, getString(R.string.error_blood_hba1c), Toast.LENGTH_SHORT).show()
            return false
        }
    }

}
