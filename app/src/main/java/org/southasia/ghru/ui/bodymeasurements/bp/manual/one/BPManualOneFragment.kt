package org.southasia.ghru.ui.bodymeasurements.bp.manual.one


import android.content.Context
import android.graphics.Color
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
import io.reactivex.disposables.CompositeDisposable
import org.southasia.ghru.BuildConfig
import org.southasia.ghru.R
import org.southasia.ghru.binding.FragmentDataBindingComponent
import org.southasia.ghru.databinding.BPManualOneFragmentBinding
import org.southasia.ghru.di.Injectable
import org.southasia.ghru.event.BPRecordRxBus
import org.southasia.ghru.jobs.SyncBloodPresureRequestJob
import org.southasia.ghru.ui.bodymeasurements.bp.reason.ReasonDialogFragment
import org.southasia.ghru.ui.bodymeasurements.review.completed.CompletedDialogFragment
import org.southasia.ghru.util.*
import org.southasia.ghru.vo.*
//import org.southasia.ghru.vo.Date
import org.southasia.ghru.vo.request.BloodPressureMetaRequest
import org.southasia.ghru.vo.request.BloodPresureItemRequest
import org.southasia.ghru.vo.request.BloodPresureRequest
import org.southasia.ghru.vo.request.ParticipantRequest
import timber.log.Timber
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import java.util.Date
import javax.inject.Inject
import kotlin.collections.ArrayList


class BPManualOneFragment : Fragment(), Injectable {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    // private var measurement: BodyMeasurement? = null

    var binding by autoCleared<BPManualOneFragmentBinding>()
    var dataBindingComponent: DataBindingComponent = FragmentDataBindingComponent(this)
    @Inject
    lateinit var bPManualOneViewModel: BPManualOneViewModel


    private var participantRequest: ParticipantRequest? = null

    private lateinit var linearLayoutManager: LinearLayoutManager
    private lateinit var adapter: BPRecordAdapter
    private val disposables = CompositeDisposable()
    private var recordList: ArrayList<BloodPressure> = ArrayList()
    var isCriticalRecordFound: Boolean = false

    private var deviceListName: MutableList<String> = arrayListOf()
    private var deviceListObject: List<StationDeviceData> = arrayListOf()
    private var selectedDeviceID: String? = null
    var user: User? = null
    var meta: Meta? = null

    @Inject
    lateinit var jobManager: JobManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            //   measurement = arguments?.getParcelable<BodyMeasurement>(Constants.ARG_BODY_MEASURMENT)!!
            participantRequest = arguments?.getParcelable<ParticipantRequest>("ParticipantRequest")!!

            //   Log.d("measurement", measurement?.height?.value)
        } catch (e: KotlinNullPointerException) {
            //Crashlytics.logException(e)
        }
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val dataBinding = DataBindingUtil.inflate<BPManualOneFragmentBinding>(
                inflater,
                R.layout.b_p_manual_one_fragment,
                container,
                false
        )
        binding = dataBinding
        setHasOptionsMenu(true)
        val appCompatActivity = requireActivity() as AppCompatActivity
        appCompatActivity.setSupportActionBar(binding.detailToolbar)
        appCompatActivity.supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.expandProcedure = false
        binding.linearLayoutPrepContainer.collapse()
        binding.linearLayoutMessageContainer.collapse()

        linearLayoutManager = LinearLayoutManager(context)
        binding.recyclerView.layoutManager = linearLayoutManager

        adapter = BPRecordAdapter(recordList)
        binding.recyclerView.adapter = adapter

        disposables.add(
                BPRecordRxBus.getInstance().toObservable()
                        .subscribe({ result ->

                            Timber.d(result.toString())
                            if (!recordList.contains(result)) {
                                recordList.add(result)
                                adapter.notifyDataSetChanged()

                                if (result.systolic.value?.toInt()!! > 180 || result.diastolic.value?.toInt()!! > 120) {
                                    isCriticalRecordFound = true
                                }

                            }

                        }, { error ->
                            print(error)
                            error.printStackTrace()
                        }))
        disposables.add(
                BPRecordRxBus.getInstance().toObservableReset()
                        .subscribe({ result ->

                            if (result == 1) {
                                recordList.clear()
                                adapter.notifyDataSetChanged()
                                isCriticalRecordFound = false

                            }

                        }, { error ->
                            print(error)
                            error.printStackTrace()
                        }))

        return dataBinding.root
    }

    var mBloodPressureMetaRequest: BloodPressureMetaRequest? = null

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.setLifecycleOwner(this)

        bPManualOneViewModel.setUser("user")
        bPManualOneViewModel.user?.observe(this, Observer { userData ->
            if (userData?.data != null) {
                // setupNavigationDrawer(userData.data)

                val sTime: String = convertTimeTo24Hours()
                val sDate: String = getDate()
                val sDateTime:String = sDate + " " + sTime

                user = userData.data
                meta = Meta(collectedBy = user?.id, startTime = sDateTime)
                //meta?.registeredBy = user?.id
            }

        })

        binding.participant = participantRequest
        // bPManualOneViewModel.setBodyMeasurement(measurement!!)
        binding.bloodPressure = bPManualOneViewModel.getBloodPressure().value

        if (recordList.count() > 0) {
            binding.linearLayoutMessageContainer.expand()
        } else {
            binding.linearLayoutMessageContainer.collapse()
        }
        validateNextButton()
        binding.executePendingBindings()
        binding.nextButton.singleClick {

            if(selectedDeviceID==null)
            {
                binding.textViewDeviceError.visibility = View.VISIBLE
            }
            else {


                val bloodPresureRequestList: ArrayList<BloodPresureItemRequest> = ArrayList()
                recordList?.forEach {
                    val mBloodPressureItemRequest: BloodPresureItemRequest = BloodPresureItemRequest(
                        systolic = it.systolic.value?.toInt()!!,
                        diastolic = it.diastolic.value?.toInt()!!,
                        pulse = it.pulse.value?.toInt()!!,
                        arm = it.arm.value?.toString()!!
                    )
                    bloodPresureRequestList.add(mBloodPressureItemRequest)
                }
                val mBloodPressureRequest: BloodPresureRequest = BloodPresureRequest(
                    comment = binding.comment.text.toString(),
                    device_id = selectedDeviceID.toString()
                )
                mBloodPressureRequest.syncPending = !isNetworkAvailable()
                mBloodPressureRequest.screeningId = participantRequest?.screeningId!!
                mBloodPressureRequest.bloodPresureRequestList = bloodPresureRequestList

                val eTime: String = convertTimeTo24Hours()
                val eDate: String = getDate()
                val eDateTime:String = eDate + " " + eTime

                meta?.endTime = eDateTime

                mBloodPressureMetaRequest = BloodPressureMetaRequest(meta = meta!!, body = mBloodPressureRequest)

//                if (isNetworkAvailable()) {
                    mBloodPressureMetaRequest?.syncPending = !isNetworkAvailable()

                    bPManualOneViewModel.setBloodPressureMetaRequestRemote(
                        mBloodPressureMetaRequest!!,
                        participantRequest!!
                    )
                    binding.progressBar.visibility = View.VISIBLE
                    binding.textViewError.setText("")
                    binding.textViewError.visibility = View.GONE
//                } else {
//                    mBloodPressureMetaRequest?.syncPending = true
//                    jobManager.addJobInBackground(
//                        SyncBloodPresureRequestJob(
//                            participantRequest?.screeningId!!,
//                            mBloodPressureMetaRequest!!
//                        )
//                    )
//                    val completedDialogFragment = CompletedDialogFragment()
//                    completedDialogFragment.show(fragmentManager!!)
//                }
            }

        }

        bPManualOneViewModel.bloodPressureRequestRemote?.observe(this, Observer {
            binding.progressBar.visibility = View.GONE
            if (it.status.equals(Status.SUCCESS)) {
                val completedDialogFragment = CompletedDialogFragment()
                completedDialogFragment.show(fragmentManager!!)
            } else if (it?.status == Status.ERROR) {
                Crashlytics.setString("mBloodPressureMetaRequest", mBloodPressureMetaRequest.toString())
                Crashlytics.setString("participant", participantRequest.toString())
                Crashlytics.logException(Exception("bloodPressureRequestRemote " + it.message.toString()))
                binding.textViewError.visibility = View.VISIBLE
                binding.textViewError.setText(it.message?.message)

            }
        })

        binding.textViewSkip.singleClick {
            val reasonDialogFragment = ReasonDialogFragment()
            reasonDialogFragment.arguments = bundleOf("participant" to participantRequest)
            reasonDialogFragment.show(fragmentManager!!)
        }
        if (BuildConfig.DEBUG) {
//            bPManualOneViewModel.getBloodPressure().value?.systolic?.value = "120"
//            bPManualOneViewModel.getBloodPressure().value?.diastolic?.value = "90"
//            bPManualOneViewModel.getBloodPressure().value?.pulse?.value = "80"
        }

//        binding.previousButton.singleClick {
//            navController().popBackStack()
//        }
//
//
//        binding.textViewSkip.singleClick {
//
//            val skipDialogFragment = SkipDialogFragment()
//            val bundle = bundleOf("ParticipantRequest" to participantRequest, Constants.ARG_BODY_MEASURMENT to bPManualOneViewModel.getBodyMeasurement().value)
//            skipDialogFragment.arguments = bundle
//            skipDialogFragment.show(fragmentManager!!)
//        }

        binding.prepEC.setOnClickListener {
            if (binding.expandProcedure!!) {

                binding.linearLayoutPrepContainer.collapse()
                binding.expandProcedure = false

            } else {

                binding.linearLayoutPrepContainer.expand()
                binding.expandProcedure = true
            }
            binding.executePendingBindings()

        }

        binding.buttonAddTest.singleClick {

            val bundle = bundleOf("ParticipantRequest" to participantRequest, Constants.ARG_BODY_MEASURMENT to bPManualOneViewModel.getBodyMeasurement().value)
            navController().navigate(R.id.action_pPManualOneFragment_to_bPManualTwoFragment, bundle)

        }

        deviceListName.clear()
        deviceListName.add(getString(R.string.unknown))
        val adapter = ArrayAdapter(context!!, R.layout.basic_spinner_dropdown_item, deviceListName)
        binding.deviceIdSpinner.setAdapter(adapter);

        bPManualOneViewModel.setStationName(Measurements.BLOOD_PRESSURE)
        bPManualOneViewModel.stationDeviceList?.observe(this, Observer {
            if (it.status.equals(Status.SUCCESS)) {
                deviceListObject = it.data!!

                deviceListObject.iterator().forEach {
                    deviceListName.add(it.device_name!!)
                }
                adapter.notifyDataSetChanged()
            }
        })
        binding.deviceIdSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parentView: AdapterView<*>, @NonNull selectedItemView: View?, position: Int, id: Long) {
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

    private fun validateNextButton() {

        if (recordList.count() > 2) {
            binding.nextButton.setTextColor(Color.parseColor("#0A1D53"))
            binding.nextButton.setDrawableRightColor("#0A1D53")
            binding.nextButton.isEnabled = true
        } else {
            binding.nextButton.setTextColor(Color.parseColor("#AED6F1"));
            binding.nextButton.setDrawableRightColor("#AED6F1")
            binding.nextButton.isEnabled = false
        }

    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = activity?.getSystemService(Context.CONNECTIVITY_SERVICE)
        return if (connectivityManager is ConnectivityManager) {
            val networkInfo: NetworkInfo? = connectivityManager.activeNetworkInfo
            networkInfo?.isConnected ?: false
        } else false
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
