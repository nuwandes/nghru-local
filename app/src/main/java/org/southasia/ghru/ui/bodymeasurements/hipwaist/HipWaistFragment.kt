package org.southasia.ghru.ui.bodymeasurements.hipwaist


import android.graphics.Color
import android.os.Bundle
import android.text.InputFilter
import android.text.Spanned
import android.view.*
import android.widget.AdapterView
import android.widget.ArrayAdapter
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
import org.southasia.ghru.BuildConfig
import org.southasia.ghru.R
import org.southasia.ghru.binding.FragmentDataBindingComponent
import org.southasia.ghru.databinding.HipWaistFragmentBinding
import org.southasia.ghru.di.Injectable
import org.southasia.ghru.event.BodyMeasurementDataEventType
import org.southasia.ghru.event.BodyMeasurementDataResponse
import org.southasia.ghru.event.BodyMeasurementDataRxBus
import org.southasia.ghru.event.BusProvider
import org.southasia.ghru.network.ConnectivityReceiver
import org.southasia.ghru.ui.bodymeasurements.hipwaist.reason.ReasonDialogFragment
import org.southasia.ghru.util.*
import org.southasia.ghru.vo.Measurements
import org.southasia.ghru.vo.StationDeviceData
import org.southasia.ghru.vo.Status
import org.southasia.ghru.vo.request.BodyMeasurementData
import org.southasia.ghru.vo.request.BodyMeasurementValue
import org.southasia.ghru.vo.request.BodyMeasurementValueData
import org.southasia.ghru.vo.request.BodyMeasurementValueDto
import javax.inject.Inject


class HipWaistFragment : Fragment(), Injectable {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    @Inject
    lateinit var appExecutors: AppExecutors

    @Inject
    lateinit var localeManager: LocaleManager


    var binding by autoCleared<HipWaistFragmentBinding>()


    var dataBindingComponent: DataBindingComponent = FragmentDataBindingComponent(this)

    private val disposables = CompositeDisposable()

    private lateinit var validator: Validator

    @Inject
    lateinit var viewModel: HipWaistViewModel
    private lateinit var bodyMeasurementValue: BodyMeasurementValue
    private lateinit var bodyMeasurementValueHip: BodyMeasurementValue


    private var deviceListName: MutableList<String> = arrayListOf()
    private var deviceListObject: List<StationDeviceData> = arrayListOf()
    private var selectedDeviceID: String? = null
    private lateinit var bodyMeasurementData: BodyMeasurementData

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        bodyMeasurementValue = BodyMeasurementValue.build()
        bodyMeasurementValueHip = BodyMeasurementValue.build()
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val dataBinding = DataBindingUtil.inflate<HipWaistFragmentBinding>(
            inflater,
            R.layout.hip_waist_fragment,
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


        if (BuildConfig.DEBUG) {
            bodyMeasurementValue.unit = "cm"
//            bodyMeasurementValue.value = "80.2"
//            bodyMeasurementValue.comment = "Reason by Mujeeb create body Hip"

            bodyMeasurementValueHip.unit = "cm"
            //bodyMeasurementValueHip.value = "79.5"

        }
        binding.bodyMeasurementValue = bodyMeasurementValue
        binding.bodyMeasurementValueHip = bodyMeasurementValueHip
        bodyMeasurementData = BodyMeasurementData()
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

        binding.hipSizeEditText.filters = arrayOf<InputFilter>(filter)
        binding.waistSizeEditText.filters = arrayOf<InputFilter>(filter)


        deviceListName.clear()
        deviceListName.add(getString(R.string.unknown))
        val adapter = ArrayAdapter(context!!, R.layout.basic_spinner_dropdown_item, deviceListName)
        binding.deviceIdSpinner.setAdapter(adapter);

        viewModel.setStationName(Measurements.HIP_AND_WAIST)
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

        binding.buttonSubmit.singleClick {
            //println(bodyMeasurementData.toString())
            if(selectedDeviceID==null)
            {
                binding.textViewDeviceError.visibility = View.VISIBLE
            }
            else if (validateHipSize(bodyMeasurementValueHip.value) && validateWaistSize(bodyMeasurementValue.value)) {
                bodyMeasurementData.comment = bodyMeasurementValue.comment
                bodyMeasurementData.deviceId = selectedDeviceID
                bodyMeasurementData.data = BodyMeasurementValueData(
                    waist = BodyMeasurementValueDto(
                        value = bodyMeasurementValue.value.toDouble(),
                        unit = "cm"
                    ), hip = BodyMeasurementValueDto(value = bodyMeasurementValueHip.value.toDouble(), unit = "cm")
                )
                // println(bodyMeasurementData.toString())
                BodyMeasurementDataRxBus.getInstance()
                    .post(BodyMeasurementDataResponse(BodyMeasurementDataEventType.HIP_WAIST, bodyMeasurementData))
            }
        }
    }

    private fun validateHipSize(fat: String): Boolean {

        try {
            val fatVal: Double = fat.toDouble()
            if (fatVal >= Constants.BD_HIP_MIN_VAL && fatVal <= Constants.BD_HIP_MAX_VAL) {
                binding.hipSizeTextLayout.error = null
                viewModel.isValidHipSize = false
                return true

            } else {
                viewModel.isValidHipSize = true
                binding.hipSizeTextLayout.error = getString(R.string.error_not_in_range)
                return false
            }

            // validateNextButton()

        } catch (e: Exception) {
            binding.hipSizeTextLayout.error = getString(R.string.error_invalid_input)
            return false
        }

    }

    private fun validateWaistSize(fat: String): Boolean {

        try {
            val fatVal: Double = fat.toDouble()
            if (fatVal >= Constants.BD_WAIST_MIN_VAL && fatVal <= Constants.BD_WAIST_MAX_VAL) {
                binding.waistTextLayout.error = null
                viewModel.isValidWaistSize = false
                return true


            } else {
                viewModel.isValidWaistSize = true
                binding.waistTextLayout.error = getString(R.string.error_not_in_range)
                return false

            }

            //validateNextButton()

        } catch (e: Exception) {
            binding.waistTextLayout.error = getString(R.string.error_invalid_input)
            return false

        }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_skip -> {
                val reasonDialogFragment = ReasonDialogFragment()
                reasonDialogFragment.show(fragmentManager!!)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.bp_main, menu)
        checkConnection(menu!!)
    }

    private fun checkConnection(menu: Menu) {
        val isConnected = ConnectivityReceiver.isConnected(context)
        if (isConnected) {
            menu.findItem(R.id.menu_text).setTitleColor(Color.WHITE)
            menu.findItem(R.id.menu_text).setTitle("Online (Local)")
            menu.findItem(R.id.menu_online).setIcon(R.drawable.ic_icon_local_lan)
        } else {
            menu.findItem(R.id.menu_text).setTitleColor(Color.RED)
            menu.findItem(R.id.menu_text).setTitle("Offline")
            menu.findItem(R.id.menu_online).setIcon(R.drawable.ic_icon_wifi_disconnected)
        }
        activity!!.invalidateOptionsMenu();
    }


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


}
