package org.southasia.ghru.ui.ecg.trace.complete

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.RelativeLayout
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingComponent
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.birbit.android.jobqueue.JobManager
import com.crashlytics.android.Crashlytics
import org.southasia.ghru.R
import org.southasia.ghru.binding.FragmentDataBindingComponent
import org.southasia.ghru.databinding.EcgCompleteDialogFragmentBinding
import org.southasia.ghru.di.Injectable
import org.southasia.ghru.jobs.SyncECGJob
import org.southasia.ghru.ui.ecg.trace.completed.CompletedDialogFragment
import org.southasia.ghru.util.autoCleared
import org.southasia.ghru.util.getLocalTimeString
import org.southasia.ghru.util.singleClick
import org.southasia.ghru.vo.ECGStatus
import org.southasia.ghru.vo.Status
import org.southasia.ghru.vo.request.BodyMeasurementMeta
import org.southasia.ghru.vo.request.ParticipantRequest
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class CompleteDialogFragment : DialogFragment(), Injectable {

    val TAG = CompleteDialogFragment::class.java.getSimpleName()

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    var binding by autoCleared<EcgCompleteDialogFragmentBinding>()
    var dataBindingComponent: DataBindingComponent = FragmentDataBindingComponent(this)
    @Inject
    lateinit var confirmationdialogViewModel: CompleteDialogViewModel

    @Inject
    lateinit var jobManager: JobManager
    private var participant: ParticipantRequest? = null
    private var comment: String? = null
    private var device_id: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            participant = arguments?.getParcelable<ParticipantRequest>("participant")!!
            comment = arguments?.getString("comment")
            device_id = arguments?.getString("deviceId")
        } catch (e: KotlinNullPointerException) {

        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val dataBinding = DataBindingUtil.inflate<EcgCompleteDialogFragmentBinding>(
            inflater,
            R.layout.ecg_complete_dialog_fragment,
            container,
            false
        )
        binding = dataBinding
        return dataBinding.root
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        confirmationdialogViewModel.eCGSaveRemote?.observe(this, Observer { participant ->

            if (participant?.status == Status.SUCCESS) {
                dismiss()
                val completedDialogFragment = CompletedDialogFragment()
                completedDialogFragment.arguments = bundleOf("is_cancel" to false)
                completedDialogFragment.show(fragmentManager!!)
            } else if (participant?.status == Status.ERROR) {

                Crashlytics.setString("comment", comment.toString())
                Crashlytics.setString("participant", participant.toString())
                Crashlytics.logException(Exception("eCGSaveRemote " + participant.message.toString()))
                binding.progressBar.visibility = View.GONE
                binding.textViewError.setText(participant.message?.message)
                binding.textViewError.visibility = View.VISIBLE
                binding.executePendingBindings()
            }
        })
        binding.buttonAcceptAndContinue.singleClick {
            // if(binding,)
            val status = if (binding.radioGroup.checkedRadioButtonId == R.id.normal) {
                getString(R.string.ecg_check_normal)
            } else {
                getString(R.string.ecg_check_abnormal)
            }

            val endTime: String = convertTimeTo24Hours()
            val endDate: String = getDate()
            val endDateTime:String = endDate + " " + endTime

            participant?.meta?.endTime = endDateTime
//            if (isNetworkAvailable()) {
            confirmationdialogViewModel.setECGRemote(participant!!, status, comment, device_id!!,isNetworkAvailable())
//            } else {
//                val mECGStatus = ECGStatus(status, comment, device_id, meta= participant?.meta)
//                jobManager.addJobInBackground(SyncECGJob(participant, mECGStatus))
//                val completedDialogFragment = CompletedDialogFragment()
//                completedDialogFragment.arguments = bundleOf("is_cancel" to false)
//                completedDialogFragment.show(fragmentManager!!)
//            }
        }
        binding.buttonCancel.singleClick {
            dismiss()
        }
    }


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // the content
        val root = RelativeLayout(activity)
        root.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        // creating the fullscreen dialog
        val dialog = Dialog(context!!)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(root)
        if (dialog.window != null) {
            dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.window!!.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
        dialog.setCanceledOnTouchOutside(false)

        return dialog
    }

    /**
     * Created to be able to override in tests
     */
    fun navController() = findNavController()

    fun show(fragmentManager: FragmentManager) {
        super.show(fragmentManager, TAG)
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

}
