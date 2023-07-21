package org.southasia.ghru.ui.samplemanagement.storage.reasonc

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
import org.southasia.ghru.databinding.StorageReasonDialogFragmentBinding
import org.southasia.ghru.di.Injectable
import org.southasia.ghru.jobs.SyncCancelProcesRequestJob
import org.southasia.ghru.jobs.SyncCancelStorageRequestJob
import org.southasia.ghru.ui.ecg.trace.reason.ReasonDialogFragment
import org.southasia.ghru.ui.samplemanagement.storage.completed.CompletedDialogFragment
import org.southasia.ghru.util.autoCleared
import org.southasia.ghru.util.hideKeyboard
import org.southasia.ghru.util.shoKeyboard
import org.southasia.ghru.util.singleClick
import org.southasia.ghru.vo.Status
import org.southasia.ghru.vo.request.CancelRequest
import org.southasia.ghru.vo.request.SampleRequest
import javax.inject.Inject


class ReasonDialogFragment : DialogFragment(), Injectable {

    val TAG = ReasonDialogFragment::class.java.getSimpleName()

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    var binding by autoCleared<StorageReasonDialogFragmentBinding>()
    var dataBindingComponent: DataBindingComponent = FragmentDataBindingComponent(this)
    @Inject
    lateinit var confirmationdialogViewModel: ReasonDialogViewModel


    @Inject
    lateinit var viewModel: ReasonDialogViewModel

    lateinit var cancelRequest: CancelRequest

    @Inject
    lateinit var jobManager: JobManager

    private var sampleRequest: SampleRequest? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            sampleRequest = arguments?.getParcelable("SampleRequestResource")!!
        } catch (e: KotlinNullPointerException) {

        }
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val dataBinding = DataBindingUtil.inflate<StorageReasonDialogFragmentBinding>(
                inflater,
                R.layout.storage_reason_dialog_fragment,
                container,
                false
        )
        binding = dataBinding
        return dataBinding.root
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        cancelRequest = CancelRequest(stationType = "sample")

        binding.radioGroup.setOnCheckedChangeListener { radioGroup, i ->
            // println("i $i" + radioGroup.checkedRadioButtonId)
            when (radioGroup.checkedRadioButtonId) {
                R.id.radioButtonTubeBroken -> cancelRequest.reason = getString(R.string.processing_failure_broken)
                R.id.radioButtonTubeMissing -> cancelRequest.reason = getString(R.string.processing_failure_missing)
                R.id.radioButtonWrongParticapant -> cancelRequest.reason = getString(R.string.wrong_participant_linked)
                else -> {
                    // binding.textViewError.text = getString(R.string.app_error_please_select_one)
                    binding.textViewError.visibility = View.GONE
                }
            }
            if (radioGroup.checkedRadioButtonId == R.id.radioButtonOther) {
                binding.textInputEditTextOther.visibility = View.VISIBLE
                binding.textInputEditTextOther.shoKeyboard()
            } else {
                binding.textInputEditTextOther.visibility = View.GONE
            }
            binding.executePendingBindings()
        }


        binding.buttonAcceptAndContinue.singleClick {
            binding.root.hideKeyboard()
            if (binding.radioGroup.checkedRadioButtonId == -1) {
                binding.textViewError.text = getString(R.string.app_error_please_select_one)
                binding.textViewError.visibility = View.VISIBLE

            } else {
                binding.textViewError.text = "";
                binding.textViewError.visibility = View.GONE

                if(binding.radioButtonTubeBroken.isChecked)
                    cancelRequest.reason = binding.radioButtonTubeBroken.text.toString()
                else if(binding.radioButtonTubeMissing.isChecked)
                    cancelRequest.reason = binding.radioButtonTubeMissing.text.toString()
                else
                    cancelRequest.reason = binding.radioButtonWrongParticapant.text.toString()

                cancelRequest.comment = binding.comment.text.toString()
                cancelRequest.syncPending =!isNetworkAvailable()
                if(sampleRequest?.screeningId != null)
                {
                    cancelRequest.screeningId = sampleRequest?.screeningId!!
                }
                else if (sampleRequest?.storageId != null)
                {
                    cancelRequest.screeningId = sampleRequest?.storageId!!
                }
                else
                {
                    cancelRequest.screeningId = sampleRequest?.sampleId!!
                }
                viewModel.setLogin(sampleRequest, cancelRequest)
            }


        }

        viewModel.deleteId?.observe(this, Observer { householdResource ->
            if (householdResource?.status == Status.SUCCESS) {
                dismiss()
                val completedDialogFragment = CompletedDialogFragment()
                completedDialogFragment.arguments = bundleOf("is_cancel" to true)
                completedDialogFragment.show(fragmentManager!!)
            }
        })

        viewModel.cancelId?.observe(this, Observer { householdResource ->
            if (householdResource?.status == Status.SUCCESS) {
                viewModel.setDelete(sampleRequest)
            }
        })
        binding.buttonCancel.singleClick {
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
                ViewGroup.LayoutParams.WRAP_CONTENT)

        // creating the fullscreen dialog
        val dialog = Dialog(context!!)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(root)
        if (dialog.window != null) {
            dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.window!!.setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT)
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
