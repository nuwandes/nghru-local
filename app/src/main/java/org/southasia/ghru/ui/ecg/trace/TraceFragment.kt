package org.southasia.ghru.ui.ecg.trace


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import org.southasia.ghru.R
import org.southasia.ghru.binding.FragmentDataBindingComponent
import org.southasia.ghru.databinding.TraceFragmentBinding
import org.southasia.ghru.di.Injectable
import org.southasia.ghru.ui.ecg.trace.complete.CompleteDialogFragment
import org.southasia.ghru.ui.ecg.trace.reason.ReasonDialogFragment
import org.southasia.ghru.util.autoCleared
import org.southasia.ghru.util.singleClick
import org.southasia.ghru.vo.Measurements
import org.southasia.ghru.vo.StationDeviceData
import org.southasia.ghru.vo.Status
import org.southasia.ghru.vo.request.ParticipantRequest
import javax.inject.Inject

class TraceFragment : Fragment(), Injectable {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    var binding by autoCleared<TraceFragmentBinding>()
    var dataBindingComponent: DataBindingComponent = FragmentDataBindingComponent(this)
    @Inject
    lateinit var verifyIDViewModel: TraceViewModel

    private var participant: ParticipantRequest? = null

    private var deviceListName: MutableList<String> = arrayListOf()
    private var deviceListObject: List<StationDeviceData> = arrayListOf()
    private var selectedDeviceID: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            participant = arguments?.getParcelable<ParticipantRequest>("participant")!!
        } catch (e: KotlinNullPointerException) {

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val dataBinding = DataBindingUtil.inflate<TraceFragmentBinding>(
            inflater,
            R.layout.trace_fragment,
            container,
            false
        )
        binding = dataBinding

        setHasOptionsMenu(true)
        val appCompatActivity = requireActivity() as AppCompatActivity
        appCompatActivity.setSupportActionBar(binding.detailToolbar)
        appCompatActivity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        return dataBinding.root
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.setLifecycleOwner(this)
        binding.participant = participant
        // binding.viewModel = verifyIDViewModel
        binding.buttonCancel.singleClick {
            val reasonDialogFragment = ReasonDialogFragment()
            reasonDialogFragment.arguments = bundleOf("participant" to participant)
            reasonDialogFragment.show(fragmentManager!!)
        }


        binding.buttonSubmit.singleClick {

            if(selectedDeviceID==null)
            {
                binding.textViewDeviceError.visibility = View.VISIBLE
            }
            else {
                val completeDialogFragment = CompleteDialogFragment()
//            val bundle = Bundle()
//            bundle.putParcelable("participant", participant)
//            bundle.putString("comment", binding.comment.text.toString())
//            bundle.putString("deviceId",selectedDeviceID)
                completeDialogFragment.arguments = bundleOf(
                    "participant" to participant,
                    "comment" to binding.comment.text.toString(),
                    "deviceId" to selectedDeviceID
                )
                completeDialogFragment.show(fragmentManager!!)
            }
        }

        deviceListName.clear()
        deviceListName.add(getString(R.string.unknown))
        val adapter = ArrayAdapter(context!!, R.layout.basic_spinner_dropdown_item, deviceListName)
        binding.deviceIdSpinner.setAdapter(adapter);

        verifyIDViewModel.setStationName(Measurements.ECG)
        verifyIDViewModel.stationDeviceList?.observe(this, Observer {
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

    /**
     * Created to be able to override in tests
     */
    fun navController() = findNavController()
}
