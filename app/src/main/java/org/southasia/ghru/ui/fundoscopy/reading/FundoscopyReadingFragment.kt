package org.southasia.ghru.ui.fundoscopy.reading


import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
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
import androidx.recyclerview.widget.LinearLayoutManager
import com.birbit.android.jobqueue.JobManager
import com.crashlytics.android.Crashlytics
import kotlinx.android.synthetic.main.check_list_fragment.*
import kotlinx.android.synthetic.main.fundos_reading.view.*
import kotlinx.android.synthetic.main.setting_fragment.*
import org.southasia.ghru.AppExecutors
import org.southasia.ghru.R
import org.southasia.ghru.binding.FragmentDataBindingComponent
import org.southasia.ghru.databinding.FundosReadingBinding
import org.southasia.ghru.di.Injectable
import org.southasia.ghru.jobs.SyncFundoscopyJob
import org.southasia.ghru.ui.fundoscopy.reading.completed.CompletedDialogFragment
import org.southasia.ghru.ui.fundoscopy.reading.reason.ReasonDialogFragment
import org.southasia.ghru.util.autoCleared
import org.southasia.ghru.util.getLocalTimeString
import org.southasia.ghru.util.singleClick
import org.southasia.ghru.vo.Measurements
import org.southasia.ghru.vo.StationDeviceData
import org.southasia.ghru.vo.Status
import org.southasia.ghru.vo.request.ParticipantRequest
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject


class FundoscopyReadingFragment : Fragment(), Injectable {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var appExecutors: AppExecutors


    var binding by autoCleared<FundosReadingBinding>()

    var dataBindingComponent: DataBindingComponent = FragmentDataBindingComponent(this)

    @Inject
    lateinit var fundoscopyReadingViewModel: FundoscopyReadingViewModel

    private var participant: ParticipantRequest? = null

    private var adapter by autoCleared<AssetAdapter>()

    private var deviceListName: MutableList<String> = arrayListOf()
    private var deviceListObject: List<StationDeviceData> = arrayListOf()
    private var selectedDeviceID: String? = null

    @Inject
    lateinit var jobManager: JobManager

    private var didDilation: Boolean? = null
    private var cataractObservation : String = ""

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
        val dataBinding = DataBindingUtil.inflate<FundosReadingBinding>(
            inflater,
            R.layout.fundos_reading,
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
        binding.viewModel = fundoscopyReadingViewModel
        binding.participant = participant

        val adapter = AssetAdapter(dataBindingComponent, appExecutors) { homeemumerationlistItem ->

        }

        this.adapter = adapter
        binding.assetList.adapter = adapter
        val layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
        binding.assetList.setLayoutManager(layoutManager)
        fundoscopyReadingViewModel.asserts?.observe(this, Observer { assertsResource ->
            if (assertsResource?.status == Status.SUCCESS) {
                println(assertsResource.data?.data)
                if (assertsResource.data != null) {
                    adapter.submitList(assertsResource.data.data)
                    binding.icSync.visibility = View.GONE
                    binding.icText.visibility = View.GONE

                } else {
                    adapter.submitList(emptyList())
                    binding.icSync.visibility = View.VISIBLE
                    binding.icText.visibility = View.VISIBLE
                }
            }
        })
        binding.syncLayout.singleClick {
            fundoscopyReadingViewModel.setParticipant(
                participant!!,
                binding.comment.text.toString(),
                selectedDeviceID!!,
                didDilation!!,
                cataractObservation
            )

        }

        fundoscopyReadingViewModel.fundoscopyComplete?.observe(this, Observer { participant ->

            if (participant?.status == Status.SUCCESS) {
                val completedDialogFragment = CompletedDialogFragment()
                completedDialogFragment.arguments = bundleOf("is_cancel" to false)
                completedDialogFragment.show(fragmentManager!!)
            } else if (participant?.status == Status.ERROR) {
                Crashlytics.setString("comment", binding.comment.text.toString())
                Crashlytics.setString("participant", participant.toString())
                Crashlytics.logException(Exception("fundoscopyComplete " + participant.message.toString()))
                binding.executePendingBindings()
            }
        })
        binding.nextButton.singleClick {
            //print(participant.toString())
            if(selectedDeviceID==null)
            {
                binding.textViewDeviceError.visibility = View.VISIBLE
            }
            else if (validateFundoscopy()) {

                val endTime: String = convertTimeTo24Hours()
                val endDate: String = getDate()
                val endDateTime:String = endDate + " " + endTime

                participant?.meta?.endTime =  endDateTime
//                if (isNetworkAvailable()) {
                    fundoscopyReadingViewModel.setParticipantComplete(
                        participant!!,
                        binding.comment.text.toString(),
                        selectedDeviceID!!,
                        didDilation!!,isNetworkAvailable(),
                        cataractObservation
                    )
//                } else {
//                    jobManager.addJobInBackground(
//                        SyncFundoscopyJob(
//                            participant,
//                            binding.comment.text.toString(),
//                            selectedDeviceID!!,
//                            didDilation!!
//                        )
//                    )
//                    val completedDialogFragment = CompletedDialogFragment()
//                    completedDialogFragment.arguments = bundleOf("is_cancel" to false)
//                    completedDialogFragment.show(fragmentManager!!)
//                }

            } else {

            }
        }
        binding.buttonCancel.singleClick {

            val reasonDialogFragment = ReasonDialogFragment()
            reasonDialogFragment.arguments = bundleOf("participant" to participant)
            reasonDialogFragment.show(fragmentManager!!)
        }

        deviceListName.clear()
        deviceListName.add(getString(R.string.unknown))
        val adapter_Device_list = ArrayAdapter(context!!, R.layout.basic_spinner_dropdown_item, deviceListName)
        binding.deviceIdSpinner.setAdapter(adapter_Device_list);

        fundoscopyReadingViewModel.setStationName(Measurements.FUNDOSCOPY)
        fundoscopyReadingViewModel.stationDeviceList?.observe(this, Observer {
            if (it.status.equals(Status.SUCCESS)) {
                deviceListObject = it.data!!

                deviceListObject.iterator().forEach {
                    deviceListName.add(it.device_name!!)
                }
                adapter_Device_list.notifyDataSetChanged()
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

        binding.radioGroupAbove.setOnCheckedChangeListener({ radioGroup, i ->
            if (radioGroup.checkedRadioButtonId == R.id.no) {
                binding.radioGroupAboveValue = false
                didDilation = false

            } else {
                binding.radioGroupAboveValue = false
                didDilation = true

            }
            binding.executePendingBindings()
        })
        binding.radioGroupCataractObserved.setOnCheckedChangeListener({ radioGroup, i ->
             if (radioGroup.checkedRadioButtonId == R.id.cataractLeft)
            {
                cataractObservation = "Left"
                binding.radioGroupCataractValue = false;
            }
            else if (radioGroup.checkedRadioButtonId == R.id.cataractRight)
            {
                cataractObservation = "Right"
                binding.radioGroupCataractValue = false;
            }
            else if(radioGroup.checkedRadioButtonId == R.id.cataractBoth)
            {
                cataractObservation = "Both"
                binding.radioGroupCataractValue = false;
            }
            else
            {
                cataractObservation = "No"
                binding.radioGroupCataractValue = false;
            }
            binding.executePendingBindings()

        })


    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = activity?.getSystemService(Context.CONNECTIVITY_SERVICE)
        return if (connectivityManager is ConnectivityManager) {
            val networkInfo: NetworkInfo? = connectivityManager.activeNetworkInfo
            networkInfo?.isConnected ?: false
        } else false
    }


    private fun validateFundoscopy(): Boolean {
        if(didDilation == null)
        {
            binding.radioGroupAboveValue = true
            binding.executePendingBindings()
            return false

        }
        else if(cataractObservation == "")
        {
            binding.radioGroupCataractValue = true
            binding.executePendingBindings()
            return false
        }
        else {
            return true
        }
    }

    private fun convertTimeTo24Hours(): String
    {
        val now: Calendar = Calendar.getInstance()
        val inputFormat: DateFormat = SimpleDateFormat("MMM DD, yyyy HH:mm:ss")
        val outputformat: DateFormat = SimpleDateFormat("HH:mm")
        val date: Date
        val output: String
        try{
            date= inputFormat.parse(now.time.toLocaleString())
            output = outputformat.format(date)
            return output
        }catch(p: ParseException){
            return ""
        }
    }

    private fun getDate(): String
    {
        val inputFormat: DateFormat = SimpleDateFormat("yyyy-MM-dd hh:mm")
        val outputformat: DateFormat = SimpleDateFormat("yyyy-MM-dd")
        val date: Date
        val output: String
        try{
            date= inputFormat.parse(binding.root.getLocalTimeString())
            output = outputformat.format(date)

            return output
        }catch(p: ParseException){
            return ""
        }
    }

    /**
     * Created to be able to override in tests
     */
    fun navController() = findNavController()
}
