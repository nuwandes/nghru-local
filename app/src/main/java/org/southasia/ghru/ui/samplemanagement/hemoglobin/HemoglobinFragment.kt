package org.southasia.ghru.ui.samplemanagement.hemoglobin

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import br.com.ilhasoft.support.validation.Validator
import io.reactivex.disposables.CompositeDisposable

import org.southasia.ghru.R
import org.southasia.ghru.databinding.HemoglobinFragmentNewBinding
import org.southasia.ghru.di.Injectable
import org.southasia.ghru.event.BusProvider
import org.southasia.ghru.event.HemoglobinRxBus
import org.southasia.ghru.sync.CholesterolcomEventType
import org.southasia.ghru.sync.JanaCareCholesterolcomRxBus
import org.southasia.ghru.util.Constants
import org.southasia.ghru.util.autoCleared
import org.southasia.ghru.util.hideKeyboard
import org.southasia.ghru.util.singleClick
import org.southasia.ghru.vo.*
import javax.inject.Inject

class HemoglobinFragment : Fragment() , Injectable {

    var binding by autoCleared<HemoglobinFragmentNewBinding>()

    private val disposables = CompositeDisposable()

    private lateinit var validator: Validator

    @Inject
    lateinit var viewModel: HemoglobinViewModel
    private lateinit var hemoglobin: Hemoglobin

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
                    when (result.eventType) {
                        CholesterolcomEventType.HEMOGLOBIN -> {
                            if (result != null)
                            {
                                binding.textInputEditTextHemoglobin.setText(result.result.result.toString())
                                binding.textviewHemAinaValue.setText(result.result.result.toString())
                                binding.hemoglobin!!.value = result.result.result.toString()
                                binding.hemoglobin!!.probeId = result.result.lotNumber.toString()

                            }

                        }
                    }

                }, { error ->
                    error.printStackTrace()
                })
        )
        hemoglobin = Hemoglobin.build()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val dataBinding = DataBindingUtil.inflate<HemoglobinFragmentNewBinding>(
            inflater,
            R.layout.hemoglobin_fragment_new,
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

        // upto HemogRxBus

        binding.setLifecycleOwner(this)
        binding.viewModel = viewModel
        binding.hemoglobin = hemoglobin
        binding.buttonSubmit.singleClick {

            binding.root.hideKeyboard()
            if(selectedDeviceID==null)
            {
                binding.textViewDeviceError.visibility = View.VISIBLE
            }
            else if (validateHemoglobin(binding.textInputEditTextHemoglobin.text.toString())) {
                binding.hemoglobin!!.deviceId = selectedDeviceID!!

                // Timber.d("ddce " + binding.totalCholesterol!!.value + " " + binding.totalCholesterol!!.probeId)
                val mtotalHemoglobin = HemoglobinDto(
                    value = binding.hemoglobin!!.value + " g/dL",
                    lot_id = binding.hemoglobin!!.probeId,
                    comment = binding.hemoglobin!!.comment,
                    device_id = binding.hemoglobin!!.deviceId
                )

                HemoglobinRxBus.getInstance().post(mtotalHemoglobin)
                //navController().popBackStack()
            }
        }

        binding.buttonJanacare.singleClick {

            startAina("com.janacare.aina.total_hemoglobin", AINA_REQUEST_CODE_Hemoglobin);

        }
        binding.buttonConnect.singleClick {
            if (isAinaPackageAvailable(activity!!.getApplicationContext())) {
                binding.ainaViewConnected.visibility = View.VISIBLE
                binding.ainaViewNotConnected.visibility = View.GONE

                binding.layoutHemTextInput.visibility = View.GONE
                binding.layoutHemAinaInput.visibility = View.VISIBLE
            } else {
                Toast.makeText(activity, "Aina app not installed", Toast.LENGTH_SHORT).show()
            }
        }
        binding.buttonManualEntry.singleClick {

            binding.textInputEditTextHemoglobin.setText("")

            binding.ainaViewConnected.visibility = View.GONE
            binding.ainaViewNotConnected.visibility = View.VISIBLE

            binding.layoutHemTextInput.visibility = View.VISIBLE
            binding.layoutHemAinaInput.visibility = View.GONE
        }
        if (isAinaPackageAvailable(activity!!.getApplicationContext())) {
            binding.ainaViewNotConnected.visibility = View.GONE
            binding.ainaViewConnected.visibility = View.VISIBLE

            binding.layoutHemTextInput.visibility = View.GONE
            binding.layoutHemAinaInput.visibility = View.VISIBLE
        } else {
            binding.ainaViewNotConnected.visibility = View.VISIBLE
            binding.ainaViewConnected.visibility = View.GONE

            binding.layoutHemTextInput.visibility = View.VISIBLE
            binding.layoutHemAinaInput.visibility = View.GONE
        }

        // binding button run test up to bottom

        binding.buttonRunTest.singleClick {

            startAina("com.janacare.aina.total_hemoglobin", AINA_REQUEST_CODE_Hemoglobin)

        }
        viewModel.hemoglobin.observe(this, Observer { hemo ->
            validateHemoglobin(hemo!!)
        })
        deviceListName.clear()
        deviceListName.add(getString(R.string.unknown))
        val adapter = ArrayAdapter(context!!, R.layout.basic_spinner_dropdown_item, deviceListName)
        binding.deviceIdSpinner.setAdapter(adapter);
        viewModel.setStationName(Measurements.HEMOGLOBIN)
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

    val AINA_REQUEST_CODE_Hemoglobin = 60

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

    // startAina fun up to bottom

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


    fun validateHemoglobin(hemo: String) : Boolean {
        try {
            if(binding.hemoglobin!!.probeId == null || binding.hemoglobin!!.probeId == "")
            {
                viewModel.isValidateError = true
                binding.textInputLayoutLotID.error = getString(R.string.error_error_there_are_missing_inputs)
                return false
            }
            else {
                binding.textInputLayoutLotID.error = null
                var chold: Double = hemo.toDouble()
                if (chold >= Constants.HEM_MIN_VAL && chold <= Constants.HEM_MAX_VAL) {
                    binding.textInputLayoutHemoglobin.error = null
                    viewModel.isValidateError = false
                    hemoglobin.value = hemo
                    return true

                } else {
                    viewModel.isValidateError = true
                    binding.textInputLayoutHemoglobin.error = getString(R.string.error_not_in_range)
                    binding.textInputLayoutHemoglobin.requestFocus()
                    Toast.makeText(activity, getString(R.string.error_hemoglobin), Toast.LENGTH_SHORT)
                        .show()
                    return false
                }
            }

        } catch (e: Exception) {
            binding.textInputLayoutHemoglobin.error = getString(R.string.error_invalid_input)
            Toast.makeText(activity, getString(R.string.error_hemoglobin), Toast.LENGTH_SHORT).show()
            return false
        }
    }

}
