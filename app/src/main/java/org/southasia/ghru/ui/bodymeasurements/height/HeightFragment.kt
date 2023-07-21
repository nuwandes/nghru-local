package org.southasia.ghru.ui.bodymeasurements.height


import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.InputFilter
import android.text.Spanned
import android.view.*
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.annotation.NonNull
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
import org.southasia.ghru.SettingActivity
import org.southasia.ghru.binding.FragmentDataBindingComponent
import org.southasia.ghru.databinding.HeightFragmentBinding
import org.southasia.ghru.di.Injectable
import org.southasia.ghru.event.BodyMeasurementDataEventType
import org.southasia.ghru.event.BodyMeasurementDataResponse
import org.southasia.ghru.event.BodyMeasurementDataRxBus
import org.southasia.ghru.event.BusProvider
import org.southasia.ghru.network.ConnectivityReceiver
import org.southasia.ghru.ui.bodymeasurements.height.reason.ReasonDialogFragment
import org.southasia.ghru.util.*
import org.southasia.ghru.vo.Measurements
import org.southasia.ghru.vo.StationDeviceData
import org.southasia.ghru.vo.Status
import org.southasia.ghru.vo.request.BodyMeasurementData
import org.southasia.ghru.vo.request.BodyMeasurementValue
import org.southasia.ghru.vo.request.BodyMeasurementValueData
import org.southasia.ghru.vo.request.BodyMeasurementValueDto
import javax.inject.Inject


class HeightFragment : Fragment(), Injectable {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    @Inject
    lateinit var appExecutors: AppExecutors

    @Inject
    lateinit var localeManager: LocaleManager


    var binding by autoCleared<HeightFragmentBinding>()


    var dataBindingComponent: DataBindingComponent = FragmentDataBindingComponent(this)


    private lateinit var validator: Validator

    @Inject
    lateinit var viewModel: HeightViewModel

    private lateinit var bodyMeasurementValue: BodyMeasurementValue

    private var deviceListName: MutableList<String> = arrayListOf()
    private var deviceListObject: List<StationDeviceData> = arrayListOf()
    private var selectedDeviceID: String? = null

    private lateinit var bodyMeasurementData: BodyMeasurementData

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        bodyMeasurementValue = BodyMeasurementValue.build()
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val dataBinding = DataBindingUtil.inflate<HeightFragmentBinding>(
            inflater,
            R.layout.height_fragment,
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
//        if (BuildConfig.DEBUG) {
//            bodyMeasurementValue.unit = "cm"
//            bodyMeasurementValue.value = "180.2"
//            bodyMeasurementValue.comment = "Reason by Mujeeb create body height"
//
//        }
        binding.bodyMeasurementValue = bodyMeasurementValue
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

        binding.heightEditText.filters = arrayOf<InputFilter>(filter)


        deviceListName.clear()
        deviceListName.add(getString(R.string.unknown))
        val adapter = ArrayAdapter(context!!, R.layout.basic_spinner_dropdown_item, deviceListName)
        binding.deviceIdSpinner.setAdapter(adapter);

        viewModel.setStationName(Measurements.HEIGHT)
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
            // println(bodyMeasurementData.toString())
            if(selectedDeviceID==null)
            {
                binding.textViewDeviceError.visibility = View.VISIBLE
            }
            else if (validateHight(bodyMeasurementValue.value)) {
                bodyMeasurementData.comment = bodyMeasurementValue.comment
                bodyMeasurementData.deviceId = selectedDeviceID
                bodyMeasurementData.data = BodyMeasurementValueData(
                    height = BodyMeasurementValueDto(
                        value = bodyMeasurementValue.value.toDouble(),
                        unit = "cm"
                    )
                )
                // println(bodyMeasurementData.toString())
                BodyMeasurementDataRxBus.getInstance()
                    .post(BodyMeasurementDataResponse(BodyMeasurementDataEventType.HEIGHT, bodyMeasurementData))
            }
        }
    }

    private fun validateHight(hight: String): Boolean {

        try {

            val hightVal: Double = hight.toDouble()
            if (hightVal >= Constants.BD_HEIGHT_MIN_VAL && hightVal <= Constants.BD_HEIGHT_MAX_VAL) {

                binding.heightTextLayout.error = null
                viewModel.isValidHeight = false

                return true

            } else {
                viewModel.isValidHeight = true
                binding.heightTextLayout.error = getString(R.string.error_not_in_range)
                return false

            }

            //validateNextButton()

        } catch (e: Exception) {
            binding.heightTextLayout.error = getString(R.string.error_invalid_input)
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
