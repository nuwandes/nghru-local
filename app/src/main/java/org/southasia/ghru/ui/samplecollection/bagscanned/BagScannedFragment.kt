package org.southasia.ghru.ui.samplecollection.bagscanned

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingComponent
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.birbit.android.jobqueue.JobManager
import com.crashlytics.android.Crashlytics
import org.southasia.ghru.R
import org.southasia.ghru.binding.FragmentDataBindingComponent
import org.southasia.ghru.databinding.BagScannedFragmentBinding
import org.southasia.ghru.di.Injectable
import org.southasia.ghru.jobs.SyncSampledRequestJob
import org.southasia.ghru.ui.samplecollection.bagscanned.completed.CompletedDialogFragment
import org.southasia.ghru.ui.samplecollection.bagscanned.reason.ReasonDialogFragment
import org.southasia.ghru.util.*
import org.southasia.ghru.vo.Comment
import org.southasia.ghru.vo.Status
import org.southasia.ghru.vo.User
import org.southasia.ghru.vo.request.ParticipantRequest
import org.southasia.ghru.vo.request.SampleCreateRequest
import org.southasia.ghru.vo.request.SampleRequest
import timber.log.Timber
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class BagScannedFragment : Fragment(), Injectable {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    var binding by autoCleared<BagScannedFragmentBinding>()

    var dataBindingComponent: DataBindingComponent = FragmentDataBindingComponent(this)

    private var participant: ParticipantRequest? = null
    private var sampleId: String? = null

    @Inject
    lateinit var viewModel: BagScannedViewModel
    @Inject
    lateinit var jobManager: JobManager

    var allSampleCollected: Boolean = false

    var user: User? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            participant = arguments?.getParcelable<ParticipantRequest>("participant")!!
            sampleId = arguments?.getString("sample_id")!!

        } catch (e: KotlinNullPointerException) {
            //Crashlytics.logException(e)
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val dataBinding = DataBindingUtil.inflate<BagScannedFragmentBinding>(
            inflater,
            R.layout.bag_scanned_fragment,
            container,
            false
        )
        binding = dataBinding
        setHasOptionsMenu(true)
        val appCompatActivity = requireActivity() as AppCompatActivity
        appCompatActivity.setSupportActionBar(binding.detailToolbar)
        appCompatActivity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.root.hideKeyboard()
        return dataBinding.root
    }


    @SuppressLint("SetTextI18n")
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.setLifecycleOwner(this)
        binding.expand = true
        binding.linearLayoutEcContainer.collapse()
        binding.buttonCancel.singleClick {

            val reasonDialogFragment = ReasonDialogFragment()
            reasonDialogFragment.arguments = bundleOf("participant" to participant)
            reasonDialogFragment.show(fragmentManager!!)
        }


        viewModel.setUser("user")
        viewModel.user?.observe(this, Observer { userData ->
            if (userData?.data != null) {
                user = userData.data
            }
        })

        binding.buttonSubmit.singleClick {

            if (allSampleCollected) {
                Timber.d("participant $participant sample_id $sampleId")
                val sampleRequest = SampleRequest(
                    screeningId = participant?.screeningId!!,
                    sampleId = sampleId!!,
                    comment = Comment(comment = binding.comment.text.toString())
                )

                val endTime: String = convertTimeTo24Hours()
                val endDate: String = getDate()
                val endDateTime:String = endDate + " " + endTime

                participant?.meta?.endTime = endDateTime
                sampleRequest.meta = participant?.meta
                sampleRequest.syncPending = !isNetworkAvailable()
                sampleRequest.collectedBy = user?.name
                sampleRequest.createdAt = binding.root.getLocalDateString()
                sampleRequest.statusCode = 1
                sampleRequest.syncPending = !isNetworkAvailable()
                binding.progressBar.visibility = View.VISIBLE
                binding.buttonSubmit.visibility = View.GONE
                binding.textViewError.visibility = View.GONE
                viewModel.setSampleLocal(sampleRequest)

                binding.checkLayout.background = resources.getDrawable(R.drawable.ic_base_check, null)
            } else {
                binding.checkLayout.background = resources.getDrawable(R.drawable.ic_base_check_error, null)
            }

        }


        viewModel.sampleRequestLocal?.observe(this, Observer { sampleResource ->
            if (sampleResource?.status == Status.SUCCESS) {

                val eTime: String = convertTimeTo24Hours()
                val eDate: String = getDate()
                val eDateTime:String = eDate + " " + eTime

                //println(user)
                sampleResource.data?.meta = participant?.meta
                sampleResource.data?.meta?.endTime = eDateTime
                var mSampleCreateRequest = SampleCreateRequest(
                    meta =  sampleResource.data?.meta,
                    comment  = binding.comment.text.toString())
                if (!isNetworkAvailable()) {
                    jobManager.addJobInBackground(SyncSampledRequestJob(sampleRequest = sampleResource.data!!,sampleCreateRequest = mSampleCreateRequest))
                    val completedDialogFragment = CompletedDialogFragment()
                    completedDialogFragment.arguments = bundleOf("is_cancel" to false)
                    completedDialogFragment.show(fragmentManager!!)
                } else {
                    viewModel.setSample(participant, sampleId!!,mSampleCreateRequest)
                }

            } else if (sampleResource?.status == Status.ERROR) {
                //Crashlytics.logException(Exception(sampleResource.message?.message))
            }
        })

        viewModel.sample?.observe(this, Observer { sampleResource ->


            if (sampleResource?.status == Status.SUCCESS) {
                //println(user)
                val completedDialogFragment = CompletedDialogFragment()
                completedDialogFragment.arguments = bundleOf("is_cancel" to false)
                completedDialogFragment.show(fragmentManager!!)
            } else if (sampleResource?.status == Status.ERROR) {
                Crashlytics.setString("sampleId", sampleId.toString())
                Crashlytics.setString("participant", participant.toString())
                Crashlytics.logException(Exception("sample collection " + sampleResource.message.toString()))
                binding.buttonSubmit.visibility = View.GONE
                binding.textViewError.visibility = View.VISIBLE
                binding.progressBar.visibility = View.GONE
                binding.textViewError.text = sampleResource.message?.message
                //Crashlytics.logException(Exception(sampleResource.message?.message))
            }
        })


        binding.imageButtonEC.singleClick {
            if (binding.expand!!) {

                //collapse(binding.linearLayoutEcContainer)
                binding.linearLayoutEcContainer.collapse()
                binding.expand = false

            } else {
                //itexpand()
                binding.linearLayoutEcContainer.expand()
                binding.expand = true
            }
        }
        binding.checkbox.setOnCheckedChangeListener { buttonView, isChecked ->

            binding.checkLayout.background = resources.getDrawable(R.drawable.ic_base_check, null)
            allSampleCollected = isChecked
        }

        binding.participant = participant

//        if (BuildConfig.DEBUG) {
//            BarcodeRxBus.getInstance().post("SAA-0146-3")
//
//        }

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
