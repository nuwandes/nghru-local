package org.southasia.ghru.ui.bodymeasurements.review


import android.content.Context
import android.content.DialogInterface
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingComponent
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.birbit.android.jobqueue.JobManager
import org.southasia.ghru.AppExecutors
import org.southasia.ghru.R
import org.southasia.ghru.binding.FragmentDataBindingComponent
import org.southasia.ghru.databinding.ReviewBodyMeasurmentFragmentBinding
import org.southasia.ghru.di.Injectable
import org.southasia.ghru.event.BPRecordRxBus
import org.southasia.ghru.ui.bodymeasurements.review.completed.BloodPressureAdapter
import org.southasia.ghru.ui.bodymeasurements.review.completed.CompletedDialogFragment
import org.southasia.ghru.util.*
import org.southasia.ghru.vo.BodyMeasurement
import org.southasia.ghru.vo.Status
import org.southasia.ghru.vo.request.BloodPresureItemRequest
import org.southasia.ghru.vo.request.BodyMeasurementItemRequest
import org.southasia.ghru.vo.request.BodyMeasurementRequest
import org.southasia.ghru.vo.request.ParticipantRequest
import timber.log.Timber
import javax.inject.Inject

class ReviewFragment : Fragment(), Injectable {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    @Inject
    lateinit var appExecutors: AppExecutors

    private var measurement: BodyMeasurement? = null


    var binding by autoCleared<ReviewBodyMeasurmentFragmentBinding>()

    private var adapter by autoCleared<BloodPressureAdapter>()

    var dataBindingComponent: DataBindingComponent = FragmentDataBindingComponent(this)

    @Inject
    lateinit var reviewViewModel: ReviewViewModel

    @Inject
    lateinit var jobManager: JobManager

    @Inject
    lateinit var tokenManager: TokenManager


    private var participant: ParticipantRequest? = null

    private lateinit var mBodyMeasurementRequest: BodyMeasurementRequest
    var isCriticalRecordFound: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            participant = arguments?.getParcelable<ParticipantRequest>("ParticipantRequest")!!

            measurement = arguments?.getParcelable<BodyMeasurement>(Constants.ARG_BODY_MEASURMENT)!!

            isCriticalRecordFound = arguments?.getBoolean("isCriticalRecordFound")!!

            val height: BodyMeasurementItemRequest =
                BodyMeasurementItemRequest(value = measurement?.height?.value?.toDouble()!!, unit = "cm")
            val weight: BodyMeasurementItemRequest =
                BodyMeasurementItemRequest(value = measurement?.weight?.value?.toDouble()!!, unit = "kg")
            val fatComposition: BodyMeasurementItemRequest =
                BodyMeasurementItemRequest(value = measurement?.fatComposition?.value?.toDouble()!!, unit = "%")
            val hipSize: BodyMeasurementItemRequest =
                BodyMeasurementItemRequest(value = measurement?.hipSize?.value?.toDouble()!!, unit = "cm")
            val waistSize: BodyMeasurementItemRequest =
                BodyMeasurementItemRequest(value = measurement?.waistSize?.value?.toDouble()!!, unit = "cm")
            val muscle: BodyMeasurementItemRequest =
                BodyMeasurementItemRequest(value = measurement?.muscle?.value?.toDouble()!!, unit = "%")
            val visceralFat: BodyMeasurementItemRequest =
                BodyMeasurementItemRequest(value = measurement?.visceralFat?.value?.toDouble()!!, unit = "%")
            val bloodPresureRequestList: ArrayList<BloodPresureItemRequest> = ArrayList()

            measurement?.bloodPressures?.value?.forEach {
                val mBloodPresureRequest: BloodPresureItemRequest = BloodPresureItemRequest(
                    systolic = it.systolic.value?.toInt()!!,
                    diastolic = it.diastolic.value?.toInt()!!,
                    pulse = it.pulse.value?.toInt()!!,
                    arm = it.arm.value?.toString()!!
                )
                bloodPresureRequestList.add(mBloodPresureRequest)
            }
            mBodyMeasurementRequest = BodyMeasurementRequest(
                height = height,
                weight = weight,
                fatComposition = fatComposition,
                hipSize = hipSize,
                waistSize = waistSize,
                visceralFat = visceralFat,
                muscle = muscle
            )
            mBodyMeasurementRequest.bloodPresureRequestList = bloodPresureRequestList
            Log.d("measurement", measurement?.height?.value)
        } catch (e: KotlinNullPointerException) {
            //Crashlytics.logException(e)
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val dataBinding = DataBindingUtil.inflate<ReviewBodyMeasurmentFragmentBinding>(
            inflater,
            R.layout.review_body_measurment_fragment,
            container,
            false
        )
        binding = dataBinding
        setHasOptionsMenu(true)
        val appCompatActivity = requireActivity() as AppCompatActivity
        appCompatActivity.setSupportActionBar(binding.detailToolbar)
        appCompatActivity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        // binding.root.hideKeyboard()
        return dataBinding.root
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.setLifecycleOwner(this)
        binding.root.hideKeyboard()
        binding.mesurement = measurement
        binding.participant = participant
        binding.isCriticalRecordFound = isCriticalRecordFound
        val adapter = BloodPressureAdapter(dataBindingComponent, appExecutors) { bloodPressure ->
            Timber.d(bloodPressure.toString())
        }
        adapter.submitList(measurement?.bloodPressures?.value?.toList())
        this.adapter = adapter
        binding.bpList.adapter = adapter
        binding.bpList.setHasFixedSize(false);
        val linearLayoutManager = LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        binding.bpList.setLayoutManager(linearLayoutManager)

        reviewViewModel.bodyMeasurementRequestRemote?.observe(this, Observer { memberSyncRemote ->
            Timber.d("bodyMeasurementRequestRemote" + memberSyncRemote.toString())
            if (memberSyncRemote?.status == Status.SUCCESS) {
                //Navigation.findNavController(binding.root).navigate(R.id.action_CreateHouseholdFragment_to_visitedHouseholdFragment)
                val completedDialogFragment = CompletedDialogFragment()
                completedDialogFragment.show(fragmentManager!!)
            } else if (memberSyncRemote?.status == Status.ERROR) {
                //Crashlytics.logException(Exception(memberSyncRemote.toString()))
                binding.textViewError.visibility = View.VISIBLE
                binding.textViewError.setText(memberSyncRemote.message?.message)
                binding.progressBar.visibility = View.GONE


            }

        })

//        reviewViewModel.bodyMeasurementRequestLocalBP?.observe(this, Observer { mBodyMeasurementRequestX ->
//            Timber.d("bodyMeasurementRequestLocal" + mBodyMeasurementRequest.toString())
//            if (mBodyMeasurementRequestX?.status == Status.SUCCESS) {
//                //Navigation.findNavController(binding.root).navigate(R.id.action_CreateHouseholdFragment_to_visitedHouseholdFragment)
//
//                if (!isNetworkAvailable()) {
//                    jobManager.addJobInBackground(SyncBodyMeasurementRequestJob(bodyMeasurementRequest = mBodyMeasurementRequest!!, screeningId = participant?.screeningId!!))
//                    val completedDialogFragment = CompletedDialogFragment()
//                    completedDialogFragment.show(fragmentManager!!)
//                } else {
//                    reviewViewModel.setBodyMeasurementRequestRemote(mBodyMeasurementRequest, participant!!)
//                }
//
//            } else if (mBodyMeasurementRequestX?.status == Status.ERROR) {
//                //Crashlytics.logException(Exception(mBodyMeasurementRequest.toString()))
//            }
//
//        })

        reviewViewModel.bodyMeasurementRequestLocal?.observe(this, Observer { mBodyMeasurementRequest ->
            Timber.d("bodyMeasurementRequestLocal" + mBodyMeasurementRequest.toString())
            if (mBodyMeasurementRequest?.status == Status.SUCCESS) {
                //Navigation.findNavController(binding.root).navigate(R.id.action_CreateHouseholdFragment_to_visitedHouseholdFragment)
                val bloodPresureRequestList: ArrayList<BloodPresureItemRequest> = ArrayList()
                measurement?.bloodPressures?.value?.forEach {
                    val mBloodPresureRequest: BloodPresureItemRequest = BloodPresureItemRequest(
                        systolic = it.systolic.value?.toInt()!!,
                        diastolic = it.diastolic.value?.toInt()!!,
                        pulse = it.pulse.value?.toInt()!!,
                        arm = it.arm.value?.toString()!!
                    )
                    //  mBloodPresureRequest.bodyMeasurementRequestId = mBodyMeasurementRequest.data?.id!!
                    bloodPresureRequestList.add(mBloodPresureRequest)
                }
                reviewViewModel.setBodyMeasurementRequestLocalBP(
                    bloodPresureRequestList,
                    mBodyMeasurementRequest.data!!
                )
            } else if (mBodyMeasurementRequest?.status == Status.ERROR) {
                //Crashlytics.logException(Exception(mBodyMeasurementRequest.toString()))
            }

        })

        binding.buttonSubmit.singleClick {

            Timber.d(measurement.toString())
            if (!isNetworkAvailable()) {
                mBodyMeasurementRequest.syncPending = true
            } else {
                mBodyMeasurementRequest.syncPending = false
            }
            binding.progressBar.visibility = View.VISIBLE
            binding.buttonSubmit.visibility = View.GONE
            reviewViewModel.setBodyMeasurementRequestLocal(mBodyMeasurementRequest)
        }

        binding.textViewRetake.singleClick {

            showConfirmationDialog()
        }

        if (isCriticalRecordFound) {
            binding.linearLayoutCriticalRecordFound.expand()
        } else {
            binding.linearLayoutCriticalRecordFound.collapse()
        }
        binding.executePendingBindings()
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = activity?.getSystemService(Context.CONNECTIVITY_SERVICE)
        return if (connectivityManager is ConnectivityManager) {
            val networkInfo: NetworkInfo? = connectivityManager.activeNetworkInfo
            networkInfo?.isConnected ?: false
        } else false
    }

    /**
     * Created to be able to override in tests
     */
    fun navController() = findNavController()

    private fun showConfirmationDialog() {

        lateinit var dialog: AlertDialog

        val builder = AlertDialog.Builder(context!!)

        builder.setTitle(getString(R.string.app_confirmation))

        builder.setMessage(getString(R.string.bp_retake_message))


        val dialogClickListener = DialogInterface.OnClickListener { _, which ->
            when (which) {
                DialogInterface.BUTTON_POSITIVE -> {
                    BPRecordRxBus.getInstance().post(1)
                    navController().popBackStack()
                }
                DialogInterface.BUTTON_NEGATIVE -> {
                    dialog.dismiss()
                }

            }
        }

        builder.setPositiveButton(getString(R.string.app_yes), dialogClickListener)
        builder.setNegativeButton(getString(R.string.app_no), dialogClickListener)
        dialog = builder.create()
        dialog.show()
    }

}
