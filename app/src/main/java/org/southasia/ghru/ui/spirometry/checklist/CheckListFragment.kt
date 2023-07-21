package org.southasia.ghru.ui.spirometry.checklist

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingComponent
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.check_list_fragment.*
import org.southasia.ghru.R
import org.southasia.ghru.binding.FragmentDataBindingComponent
import org.southasia.ghru.databinding.SpiroCheckListFragmentBinding
import org.southasia.ghru.di.Injectable
import org.southasia.ghru.ui.spirometry.cancelchecklist.CancelDialogFragment
import org.southasia.ghru.util.autoCleared
import org.southasia.ghru.util.getLocalTimeString
import org.southasia.ghru.util.hideKeyboard
import org.southasia.ghru.util.singleClick
import org.southasia.ghru.vo.Meta
import org.southasia.ghru.vo.User
import org.southasia.ghru.vo.request.ParticipantRequest
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject


class CheckListFragment : Fragment(), Injectable {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    var binding by autoCleared<SpiroCheckListFragmentBinding>()

    var dataBindingComponent: DataBindingComponent = FragmentDataBindingComponent(this)

    @Inject
    lateinit var viewModel: CheckListViewModel
    var user: User? = null
    var meta: Meta? = null

    private var participant: ParticipantRequest? = null

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
        val dataBinding = DataBindingUtil.inflate<SpiroCheckListFragmentBinding>(
            inflater,
            R.layout.spiro_check_list_fragment,
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


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.setLifecycleOwner(this)

        viewModel.setUser("user")
        viewModel.user?.observe(this, Observer { userData ->
            if (userData?.data != null) {

                val sTime: String = convertTimeTo24Hours()
                val sDate: String = getDate()
                val sDateTime:String = sDate + " " + sTime

                // setupNavigationDrawer(userData.data)
                user = userData.data
                meta = Meta(collectedBy = user?.id, startTime = sDateTime)
                meta?.registeredBy = user?.id
            }

        })

        viewModel.participantMetas?.observe(this, Observer { userData ->
            if (userData?.data != null) {
                // setupNavigationDrawer(userData.data)
//                user = userData.data
//                meta = Meta(collectedBy = user?.id, startTime = binding.root.getLocalTimeString())
//                meta?.registeredBy = user?.id
            }

        })

        binding.buttonSubmit.singleClick {
            //            var bundle = bundleOf("member" to member, "householdId" to householdId)
            binding.root.hideKeyboard()
            if (!isCheckListNotCompleted()) {
                if (!validateContinue()) {
                    findNavController().navigate(
                        R.id.action_CheckListFragment_to_guideMainFragment, bundleOf("participant" to participant)
                    )
                } else {
                    Toast.makeText(
                        context,
                        getString(R.string.registration_preregistration_nid_fail),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                Toast.makeText(
                    context,
                    getString(R.string.registration_preregistration_check_list_complete_error),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        binding.radioGroupAbove.setOnCheckedChangeListener { radioGroup, i ->
            if (radioGroup.checkedRadioButtonId == R.id.no) {
                binding.radioGroupAboveValue = false
                radioButtonEnable(
                    textViewAbove
                )

            } else {
                binding.radioGroupAboveValue = true
                radioButtonDisable(

                    textViewAbove
                )
            }
            binding.executePendingBindings()
        }

        binding.buttonBackToHomeFive.singleClick {
            val cancelDialogFragment = CancelDialogFragment()
            cancelDialogFragment.arguments = bundleOf("participant" to participant)
            cancelDialogFragment.show(fragmentManager!!)
        }

    }

    fun radioButtonDisable(
        textView: TextView
    ) {
        textView.setTextColor(resources.getColor(R.color.gray))
        binding.buttonSubmit.setBackgroundResource(R.drawable.ic_button_disable_primary)

        binding.buttonSubmit.isEnabled = false
    }

    fun radioButtonEnable(
        textView: TextView
    ) {
        textView.setTextColor(resources.getColor(R.color.primary_material_dark))
        binding.buttonSubmit.setBackgroundResource(R.drawable.ic_button_fill_primary)

        binding.buttonSubmit.isEnabled = true
    }


    private fun validateContinue(): Boolean {
        return binding.radioGroupAboveValue!!
    }

    private fun isCheckListNotCompleted(): Boolean {
        return binding.radioGroupAboveValue == null
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
