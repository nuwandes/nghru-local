package org.southasia.ghru.ui.bodymeasurements.hipwaist.reason

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
import org.southasia.ghru.databinding.HipwaistReasonDialogFragmentBinding
import org.southasia.ghru.di.Injectable
import org.southasia.ghru.event.BodyMeasurementDataEventType
import org.southasia.ghru.event.BodyMeasurementDataResponse
import org.southasia.ghru.event.BodyMeasurementDataRxBus
import org.southasia.ghru.jobs.SyncCancelrequestJob
import org.southasia.ghru.util.autoCleared
import org.southasia.ghru.util.hideKeyboard
import org.southasia.ghru.util.shoKeyboard
import org.southasia.ghru.util.singleClick
import org.southasia.ghru.vo.Status
import org.southasia.ghru.vo.request.BodyMeasurementData
import org.southasia.ghru.vo.request.CancelRequest
import org.southasia.ghru.vo.request.ParticipantRequest
import javax.inject.Inject


class ReasonDialogFragment : DialogFragment(), Injectable {

    val TAG = ReasonDialogFragment::class.java.getSimpleName()

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    var binding by autoCleared<HipwaistReasonDialogFragmentBinding>()

    var dataBindingComponent: DataBindingComponent = FragmentDataBindingComponent(this)

    @Inject
    lateinit var viewModel: ReasonDialogViewModel


    lateinit var cancelRequest: CancelRequest

    private var participant: ParticipantRequest? = null

    @Inject
    lateinit var jobManager: JobManager

    private lateinit var bodyMeasurementData: BodyMeasurementData


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
        val dataBinding = DataBindingUtil.inflate<HipwaistReasonDialogFragmentBinding>(
            inflater,
            R.layout.hipwaist_reason_dialog_fragment,
            container,
            false
        )
        binding = dataBinding
        return dataBinding.root
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        cancelRequest = CancelRequest()

        bodyMeasurementData = BodyMeasurementData()


        binding.radioGroup.setOnCheckedChangeListener { radioGroup, _ ->
            // println("i $i" + radioGroup.checkedRadioButtonId)
            when (radioGroup.checkedRadioButtonId) {
                R.id.radioButtonParticipantFactor -> cancelRequest.reason =
                    getString(R.string.participant_factor)
                R.id.radioButtonTechnicalProblem -> cancelRequest.reason = getString(R.string.technical_problem)
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

            } else if (binding.radioGroup.checkedRadioButtonId == R.id.radioButtonOther) {
                cancelRequest.reason = binding.textInputEditTextOther.text.toString()
            }
            if (binding.radioGroup.checkedRadioButtonId != -1) {
                cancelRequest.comment = binding.comment.text.toString()
                //println(cancelRequest.toString())

                bodyMeasurementData.skip = cancelRequest

                BodyMeasurementDataRxBus.getInstance()
                    .post(BodyMeasurementDataResponse(BodyMeasurementDataEventType.HIP_WAIST, bodyMeasurementData))
                dismiss()
            }


        }
        viewModel.cancelId?.observe(this, Observer { householdResource ->
            if (householdResource?.status == Status.SUCCESS) {
                dismiss()
                activity?.finish()
            }
        })
        binding.buttonCancel.singleClick {
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
