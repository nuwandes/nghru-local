package org.southasia.ghru.ui.bodymeasurements.bodycomposition


import android.graphics.Color
import android.opengl.Visibility
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
import org.southasia.ghru.databinding.BodyCompositionFragmentBinding
import org.southasia.ghru.di.Injectable
import org.southasia.ghru.event.BodyMeasurementDataEventType
import org.southasia.ghru.event.BodyMeasurementDataResponse
import org.southasia.ghru.event.BodyMeasurementDataRxBus
import org.southasia.ghru.event.BusProvider
import org.southasia.ghru.network.ConnectivityReceiver
import org.southasia.ghru.ui.bodymeasurements.bodycomposition.reason.ReasonDialogFragment
import org.southasia.ghru.util.*
import org.southasia.ghru.vo.Measurements
import org.southasia.ghru.vo.StationDeviceData
import org.southasia.ghru.vo.Status
import org.southasia.ghru.vo.request.BodyMeasurementData
import org.southasia.ghru.vo.request.BodyMeasurementValue
import org.southasia.ghru.vo.request.BodyMeasurementValueData
import org.southasia.ghru.vo.request.BodyMeasurementValueDto
import javax.inject.Inject


class BodyCompositionFragment : Fragment(), Injectable {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    @Inject
    lateinit var appExecutors: AppExecutors

    @Inject
    lateinit var localeManager: LocaleManager


    var binding by autoCleared<BodyCompositionFragmentBinding>()


    var dataBindingComponent: DataBindingComponent = FragmentDataBindingComponent(this)

    private val disposables = CompositeDisposable()

    private lateinit var validator: Validator

    @Inject
    lateinit var viewModel: BodyCompositionViewModel
    private lateinit var bodyMeasurementValue: BodyMeasurementValue

    private lateinit var bodyMeasurementValueBodyFat: BodyMeasurementValue
    private lateinit var bodyMeasurementValueVisceralFat: BodyMeasurementValue
    private lateinit var bodyMeasurementValueMuscle: BodyMeasurementValue


    private var deviceListName: MutableList<String> = arrayListOf()
    private var deviceListObject: List<StationDeviceData> = arrayListOf()
    private var selectedDeviceID: String? = null

    private lateinit var bodyMeasurementData: BodyMeasurementData

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        bodyMeasurementValue = BodyMeasurementValue.build()
        bodyMeasurementValueBodyFat = BodyMeasurementValue.build()
        bodyMeasurementValueVisceralFat = BodyMeasurementValue.build()
        bodyMeasurementValueMuscle = BodyMeasurementValue.build()

    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val dataBinding = DataBindingUtil.inflate<BodyCompositionFragmentBinding>(
            inflater,
            R.layout.body_composition_fragment,
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
            bodyMeasurementValue.unit = "kg"
//            bodyMeasurementValue.value = "80.2"
//            bodyMeasurementValue.comment = "Reason by Mujeeb create body composition"

            bodyMeasurementValueBodyFat.unit = "%"
//            bodyMeasurementValueBodyFat.value = "30.1"

            bodyMeasurementValueVisceralFat.unit = "%"
//            bodyMeasurementValueVisceralFat.value = "30.1"

            bodyMeasurementValueMuscle.unit = "%"
//            bodyMeasurementValueMuscle.value = "30.1"
        }
        binding.bodyMeasurementValue = bodyMeasurementValue
        binding.bodyMeasurementValueBodyFat = bodyMeasurementValueBodyFat
        binding.bodyMeasurementValueVisceralFat = bodyMeasurementValueVisceralFat
        binding.bodyMeasurementValueMuscle = bodyMeasurementValueMuscle

        bodyMeasurementData = BodyMeasurementData()
        binding.viewModel = viewModel
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

        binding.bodycompositionEditText.filters = arrayOf<InputFilter>(filter)


        deviceListName.clear()
        deviceListName.add(getString(R.string.unknown))
        val adapter = ArrayAdapter(context!!, R.layout.basic_spinner_dropdown_item, deviceListName)
        binding.deviceIdSpinner.setAdapter(adapter)
        viewModel.setStationName(Measurements.BODY_COMPOSITION)
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
                }
                else
                {
                    binding.textViewDeviceError.visibility = View.GONE
                    selectedDeviceID = deviceListObject[position - 1].device_id
                }

            }

            override fun onNothingSelected(parentView: AdapterView<*>) {
                // your code here
            }

        }
        binding.checkbox.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                binding.linearLayoutMuscleSection.visibility = View.GONE
            } else {
                binding.linearLayoutMuscleSection.visibility = View.VISIBLE
            }
        }

        binding.buttonSubmit.singleClick {
            //println(bodyMeasurementData.toString())
            if(selectedDeviceID == null)
            {
                binding.textViewDeviceError.visibility = View.VISIBLE
            }
            else if (validateWeight(bodyMeasurementValue.value)) {

                    bodyMeasurementData.comment = bodyMeasurementValue.comment
                    bodyMeasurementData.deviceId = selectedDeviceID
                    if (binding.checkbox.isChecked) {
                        bodyMeasurementData.data = BodyMeasurementValueData(
                            fatComposition = null,
                            weight = BodyMeasurementValueDto(
                                value = bodyMeasurementValue.value.toDouble(),
                                unit = "kg"
                            ),
                            visceral = null,
                            muscle = null
                        )
                        // println(bodyMeasurementData.toString())
                        BodyMeasurementDataRxBus.getInstance().post(
                            BodyMeasurementDataResponse(
                                BodyMeasurementDataEventType.BODY_COMOSITION,
                                bodyMeasurementData
                            )
                        )
                    } else {
                        if (validateFatCom(bodyMeasurementValueBodyFat.value) && validateVisceral(
                                bodyMeasurementValueVisceralFat.value
                            ) && validateMuscle(bodyMeasurementValueMuscle.value)
                        ) {

                            bodyMeasurementData.data = BodyMeasurementValueData(
                                fatComposition = BodyMeasurementValueDto(
                                    value = bodyMeasurementValueBodyFat.value.toDouble(),
                                    unit = "%"
                                ),
                                weight = BodyMeasurementValueDto(
                                    value = bodyMeasurementValue.value.toDouble(),
                                    unit = "kg"
                                ),
                                visceral = BodyMeasurementValueDto(
                                    value = bodyMeasurementValueVisceralFat.value.toDouble(),
                                    unit = "%"
                                ),
                                muscle = BodyMeasurementValueDto(
                                    value = bodyMeasurementValueMuscle.value.toDouble(),
                                    unit = "%"
                                )
                            )
                            // println(bodyMeasurementData.toString())
                            BodyMeasurementDataRxBus.getInstance().post(
                                BodyMeasurementDataResponse(
                                    BodyMeasurementDataEventType.BODY_COMOSITION,
                                    bodyMeasurementData
                                )
                            )
                        }
                    }

            }
        }
    }


    private fun validateWeight(weight: String): Boolean {

        try {
            val weightVal: Double = weight.toDouble()
            if (weightVal >= Constants.BD_WEIGHT_MIN_VAL && weightVal <= Constants.BD_WEIGHT_MAX_VAL) {
                binding.weightTextLayout.error = null
                viewModel.isValidWeight = false
                return true


            } else {
                viewModel.isValidWeight = true
                binding.weightTextLayout.error = getString(R.string.error_not_in_range)
                return false
            }


        } catch (e: Exception) {
            binding.weightTextLayout.error = getString(R.string.error_invalid_input)
            return false
        }

    }


    private fun validateFatCom(fat: String): Boolean {

        try {
            val fatVal: Double = fat.toDouble()
            if (fatVal >= Constants.BD_FATCOM_MIN_VAL && fatVal <= Constants.BD_FATCOM_MAX_VAL) {
                binding.fatcomTextLayout.error = null
                viewModel.isValifFatComp = false
                return true

            } else {
                viewModel.isValifFatComp = true
                binding.fatcomTextLayout.error = getString(R.string.error_not_in_range)
                return false
            }

        } catch (e: Exception) {
            binding.fatcomTextLayout.error = getString(R.string.error_invalid_input)
            return false
        }

    }

    private fun validateVisceral(fat: String): Boolean {

        try {
            val fatVal: Double = fat.toDouble()
            if (fatVal >= Constants.BD_VISCERAL_MIN_VAL && fatVal <= Constants.BD_VISCERAL_MAX_VAL) {
                binding.visceralTextLayout.error = null
                viewModel.isValidVisceralFat = false
                return true


            } else {
                viewModel.isValidVisceralFat = true
                binding.visceralTextLayout.error = getString(R.string.error_not_in_range)
                return false
            }

        } catch (e: Exception) {
            binding.visceralTextLayout.error = getString(R.string.error_invalid_input)
            return false
        }

    }

    private fun validateMuscle(fat: String): Boolean {

        try {
            val fatVal: Double = fat.toDouble()
            if (fatVal >= Constants.BD_MUSCLE_MIN_VAL && fatVal <= Constants.BD_MUSCLE_MAX_VAL) {
                binding.muscleTextLayout.error = null
                viewModel.isValidMuscle = false
                return true

            } else {
                viewModel.isValidMuscle = true
                binding.muscleTextLayout.error = getString(R.string.error_not_in_range)
                return false
            }

        } catch (e: Exception) {
            binding.muscleTextLayout.error = getString(R.string.error_invalid_input)
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
