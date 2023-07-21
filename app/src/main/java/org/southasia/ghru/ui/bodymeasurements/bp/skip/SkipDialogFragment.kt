package org.southasia.ghru.ui.bodymeasurements.bp.skip

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.RelativeLayout
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingComponent
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import org.southasia.ghru.R
import org.southasia.ghru.binding.FragmentDataBindingComponent
import org.southasia.ghru.databinding.SkipDialogFragmentBinding
import org.southasia.ghru.di.Injectable
import org.southasia.ghru.util.*
import org.southasia.ghru.vo.BodyMeasurement
import org.southasia.ghru.vo.request.ParticipantRequest
import javax.inject.Inject


class SkipDialogFragment : DialogFragment(), Injectable {

    val TAG = SkipDialogFragment::class.java.getSimpleName()

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    var binding by autoCleared<SkipDialogFragmentBinding>()
    var dataBindingComponent: DataBindingComponent = FragmentDataBindingComponent(this)
    @Inject
    lateinit var skipdialogViewModel: SkipDialogViewModel

    private var measurement: BodyMeasurement? = null

    private var participant: ParticipantRequest? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            measurement = arguments?.getParcelable<BodyMeasurement>(Constants.ARG_BODY_MEASURMENT)!!
            participant = arguments?.getParcelable<ParticipantRequest>("ParticipantRequest")!!
            Log.d("measurement", measurement?.height?.value)
        } catch (e: KotlinNullPointerException) {

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val dataBinding = DataBindingUtil.inflate<SkipDialogFragmentBinding>(
            inflater,
            R.layout.skip_dialog_fragment,
            container,
            false
        )
        binding = dataBinding
        return dataBinding.root
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)


        binding.radioGroup.setOnCheckedChangeListener { radioGroup, _ ->
            // println("i $i" + radioGroup.checkedRadioButtonId)
            if (radioGroup.checkedRadioButtonId == R.id.radioButtonOther) {
                binding.textInputLayoutOther.expand()
                binding.textInputLayoutOther.shoKeyboard()
            } else {
                binding.textInputLayoutOther.collapse()
            }
            binding.executePendingBindings()
        }


        binding.buttonCancel.singleClick {
            dismiss()
        }

        binding.buttonNext.singleClick {

            measurement?.skip?.value = true
            // if(binding.radioGroup.checkedRadioButtonId)
            var reason: String? = null
            when (binding.radioGroup.checkedRadioButtonId) {
                R.id.radioButtonNoArm -> {
                    binding.textViewError.visibility = View.GONE
                    reason = getString(R.string.bp_skip_reason_1)
                }
                R.id.radioButtonArmBroken -> {
                    binding.textViewError.visibility = View.GONE

                    reason = getString(R.string.bp_skip_reason_2)
                }
                R.id.radioButtonOther -> {
                    binding.textViewError.visibility = View.GONE
                    reason = binding.textInputEditTextOther.text.toString()
                }
                -1 -> {
                    binding.textViewError.text = getString(R.string.app_error_please_select_one)
                    binding.textViewError.visibility = View.VISIBLE
                }
            }
            if (reason != null) {
                dismiss()
                measurement?.reson?.value = reason
                //  measurement?.bloodPressures?.value = Array(1) { i -> BloodPressure(0) }

                val bundle = bundleOf("ParticipantRequest" to participant, Constants.ARG_BODY_MEASURMENT to measurement)
                navController().navigate(R.id.action_global_reviewFragment2, bundle)
            }
        }
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.getItemId()) {
            android.R.id.home -> {
                return navController().navigateUp()
            }
        }
        return super.onOptionsItemSelected(item)
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

}
