package org.southasia.ghru.ui.spirometry.cancelchecklist

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
import org.southasia.ghru.R
import org.southasia.ghru.binding.FragmentDataBindingComponent
import org.southasia.ghru.databinding.SpirometryCancelchecklistDialogFragmentBinding
import org.southasia.ghru.di.Injectable
import org.southasia.ghru.jobs.SyncCancelrequestJob
import org.southasia.ghru.ui.ecg.trace.reason.ReasonDialogFragment
import org.southasia.ghru.ui.spirometry.tests.completed.CompletedDialogFragment
import org.southasia.ghru.util.autoCleared
import org.southasia.ghru.util.hideKeyboard
import org.southasia.ghru.util.shoKeyboard
import org.southasia.ghru.vo.Status
import org.southasia.ghru.vo.request.CancelRequest
import org.southasia.ghru.vo.request.ParticipantRequest
import javax.inject.Inject

class CancelDialogFragment : DialogFragment(), Injectable {

    val TAG = ReasonDialogFragment::class.java.getSimpleName()

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    var binding by autoCleared<SpirometryCancelchecklistDialogFragmentBinding>()
    var dataBindingComponent: DataBindingComponent = FragmentDataBindingComponent(this)
    @Inject
    lateinit var viewModel: CancelDialogViewModel

    lateinit var cancelRequest: CancelRequest

    private var participant: ParticipantRequest? = null
    @Inject
    lateinit var jobManager: JobManager

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
        val dataBinding = DataBindingUtil.inflate<SpirometryCancelchecklistDialogFragmentBinding>(
            inflater,
            R.layout.spirometry_cancelchecklist_dialog_fragment,
            container,
            false
        )
        binding = dataBinding
        return dataBinding.root
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        cancelRequest = CancelRequest(stationType = "spirometry")
        cancelRequest.reason = "Participant has active and productive cough."

        binding.buttonAcceptAndContinue.setOnClickListener {
            binding.root.hideKeyboard()
            cancelRequest.comment = binding.comment.text.toString()
            //println(cancelRequest.toString())
//            if (isNetworkAvailable()) {
                cancelRequest.screeningId = participant?.screeningId!!
                cancelRequest.syncPending = !isNetworkAvailable()
                viewModel.setLogin(participant, cancelRequest)
//            } else {
//                jobManager.addJobInBackground(SyncCancelrequestJob(participant!!, cancelRequest))
//                dismiss()
//                val completedDialogFragment = CompletedDialogFragment()
//                completedDialogFragment.arguments = bundleOf("is_cancel" to true)
//                completedDialogFragment.show(fragmentManager!!)
//            }
        }

        viewModel.cancelId?.observe(this, Observer { cancelObserver ->
            if (cancelObserver?.status == Status.SUCCESS) {
                dismiss()
                val completedDialogFragment = CompletedDialogFragment()
                completedDialogFragment.arguments = bundleOf("is_cancel" to true)
                completedDialogFragment.show(fragmentManager!!)
            } else if (cancelObserver?.status == Status.SUCCESS) {
                binding.textViewError.text = cancelObserver.message?.message.toString()
            }
        })

        binding.buttonCancel.setOnClickListener {
            binding.root.hideKeyboard()
            dismiss()
        }
    }


    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = activity?.getSystemService(Context.CONNECTIVITY_SERVICE)
        return if (connectivityManager is ConnectivityManager) {
            val networkInfo: NetworkInfo? = connectivityManager.activeNetworkInfo
            networkInfo?.isConnected ?: false
        } else false
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
        dialog.setCancelable(false)
        return dialog
    }

    /**
     * Created to be able to override in tests
     */
    fun navController() = findNavController()

    fun show(fragmentManager: FragmentManager) {
        super.show(fragmentManager, TAG)
    }

}