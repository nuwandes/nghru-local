package org.southasia.ghru.ui.enumeration.concent.reasondialog

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
import com.birbit.android.jobqueue.JobManager
import org.southasia.ghru.R
import org.southasia.ghru.binding.FragmentDataBindingComponent
import org.southasia.ghru.databinding.ReasonDialogFragmentBinding
import org.southasia.ghru.di.Injectable
import org.southasia.ghru.jobs.SyncHouseholdRequestMetaJob
import org.southasia.ghru.util.autoCleared
import org.southasia.ghru.util.singleClick
import org.southasia.ghru.vo.Meta
import org.southasia.ghru.vo.Status
import org.southasia.ghru.vo.request.CancelRequest
import org.southasia.ghru.vo.request.Consent
import org.southasia.ghru.vo.request.HouseholdRequest
import org.southasia.ghru.vo.request.HouseholdRequestMeta
import javax.inject.Inject


class ReasonDialogFragment : DialogFragment(), Injectable {

    val TAG = ReasonDialogFragment::class.java.getSimpleName()

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    var binding by autoCleared<ReasonDialogFragmentBinding>()
    var dataBindingComponent: DataBindingComponent = FragmentDataBindingComponent(this)

    lateinit var household: HouseholdRequest
    lateinit var meta: Meta

    @Inject
    lateinit var viewModel: ReasonDialogViewModel

    private lateinit var consent: Consent

    private lateinit var cancelRequest: CancelRequest


    @Inject
    lateinit var jobManager: JobManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val dataBinding = DataBindingUtil.inflate<ReasonDialogFragmentBinding>(
            inflater,
            R.layout.reason_dialog_fragment,
            container,
            false
        )
        binding = dataBinding
        return dataBinding.root
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        binding.buttonCancel.singleClick {
            dismiss()
        }

        viewModel.householdSave?.observe(this, Observer { householdResource ->
            if (householdResource?.status == Status.SUCCESS) {
                jobManager.addJobInBackground(SyncHouseholdRequestMetaJob(householdResource.data!!))
                dismiss()
                activity?.finish()
            }
        })

        viewModel.householdRequestSyncRemote?.observe(this, Observer { householdResource ->
            if (householdResource?.status == Status.SUCCESS) {
                dismiss()
                activity?.finish()
            }
        })

        binding.radioGroup.setOnCheckedChangeListener { radioGroup, _ ->

            if (radioGroup.id == R.id.radioButtonNoHome) {
                // household.unavailable = true
                consent = Consent(status = false, reason = "not_home")
                household.consent = consent
            }

            if (radioGroup.id == R.id.radioButtonNoAdult) {
                // household.unavailable = true
                consent = Consent(status = false, reason = "under_age")
                household.consent = consent
            }

            if (radioGroup.id == R.id.radioButtonConsentNotProvied) {
                //household.consent = false
                consent = Consent(status = false, reason = "not_provided")
                household.consent = consent

            }
        }

        binding.buttonAcceptAndContinue.singleClick {
            if (binding.radioGroup.checkedRadioButtonId == -1) {
                binding.textViewError.text = getString(R.string.app_error_please_select_one)
                binding.textViewError.visibility = View.VISIBLE

            } else {
                binding.textViewError.visibility = View.GONE
                val household1 = HouseholdRequestMeta(meta = meta, householdRequest = household)
                if (isNetworkAvailable()) {
                    household1.syncPending = false
                    viewModel.setHouseholdRequestSyncRemote(household1)
                } else {
                    household1.syncPending = true
                    viewModel.setHouseholdRequest(household1)

                }
            }
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

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = activity?.getSystemService(Context.CONNECTIVITY_SERVICE)
        return if (connectivityManager is ConnectivityManager) {
            val networkInfo: NetworkInfo? = connectivityManager.activeNetworkInfo
            networkInfo?.isConnected ?: false
        } else false
    }

    fun show(fragmentManager: FragmentManager) {
        super.show(fragmentManager, TAG)
    }

}
