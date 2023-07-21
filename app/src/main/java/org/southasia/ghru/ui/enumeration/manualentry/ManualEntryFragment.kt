package org.southasia.ghru.ui.enumeration.manualentry

import android.content.Context
import android.os.Bundle
import android.text.InputFilter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingComponent
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import org.southasia.ghru.R
import org.southasia.ghru.binding.FragmentDataBindingComponent
import org.southasia.ghru.databinding.EnumerationManualCodeEntryFragmentBinding
import org.southasia.ghru.di.Injectable
import org.southasia.ghru.ui.codeheck.CodeCheckDialogFragment
import org.southasia.ghru.ui.registerpatient.scanqrcode.errordialog.ErrorDialogFragment
import org.southasia.ghru.util.*
import org.southasia.ghru.vo.Meta
import org.southasia.ghru.vo.Status
import org.southasia.ghru.vo.request.HouseholdRequest
import org.southasia.ghru.vo.request.Member
import java.util.*
import javax.inject.Inject

class ManualEntryFragment : Fragment(), Injectable {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    var binding by autoCleared<EnumerationManualCodeEntryFragmentBinding>()

    var dataBindingComponent: DataBindingComponent = FragmentDataBindingComponent(this)

    var meta: Meta? = null

    private var household: HouseholdRequest? = null

    private var memberList: ArrayList<Member>? = null

    @Inject
    lateinit var viewModel: ManualEntryViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            meta = arguments?.getParcelable<Meta>("meta")!!

            household = arguments?.getParcelable("HouseholdRequest")
            memberList = arguments?.getParcelableArrayList<Member>("memberList")
        } catch (e: KotlinNullPointerException) {
            //Crashlytics.logException(e);
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val dataBinding = DataBindingUtil.inflate<EnumerationManualCodeEntryFragmentBinding>(
            inflater,
            R.layout.enumeration_manual_code_entry_fragment,
            container,
            false
        )
        binding = dataBinding
        setHasOptionsMenu(true)
        val appCompatActivity = requireActivity() as AppCompatActivity
        appCompatActivity.setSupportActionBar(binding.detailToolbar)
        appCompatActivity.supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.buttonContinue.singleClick {
            it?.hideKeyboard()
            continueManualEntry(binding.editTextCode.text.toString())
        }

        return dataBinding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        binding.editTextCode.filters = binding.editTextCode.filters + InputFilter.AllCaps()
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, InputMethodManager.HIDE_IMPLICIT_ONLY)
        binding.editTextCode.requestFocus()

        binding.buttonBack.singleClick {

            navController().popBackStack()
        }

        viewModel.householdRequestCheck?.observe(this, Observer { householdId ->
            //L.d(householdId.toString())
            if (householdId?.status == Status.SUCCESS) {
                val codeCheckDialogFragment = CodeCheckDialogFragment()
                codeCheckDialogFragment.show(fragmentManager!!)
            } else if (householdId?.status == Status.ERROR) {
                val memberList = arguments?.getParcelableArrayList<Member>("memberList")
                val bundle = bundleOf("HouseholdRequest" to household, "memberList" to memberList, "meta" to meta)
                navController().navigate(R.id.action_global_CreateHouseholdFragment, bundle)
            }

        })
    }

    /**
     * Created to be able to override in tests
     */
    fun navController() = findNavController()

    fun continueManualEntry(result: String) {
        val checkSum = validateChecksum(result, Constants.TYPE_ENUMERATION)
        if (!checkSum.error) {
            household?.enumerationId = result
            viewModel.getItemId(household?.enumerationId)
        } else {
            val errorDialogFragment = ErrorDialogFragment()
            errorDialogFragment.setErrorMessage(getString(R.string.invalid_code))
            errorDialogFragment.show(fragmentManager!!)
            //Crashlytics.logException(Exception(getString(R.string.invalid_code)))
        }

    }
}