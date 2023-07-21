package org.southasia.ghru.ui.samplecollection.manualentry

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingComponent
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import org.southasia.ghru.R
import org.southasia.ghru.binding.FragmentDataBindingComponent
import org.southasia.ghru.databinding.SamplecollectionManualBagBarcodeFragmentBinding
import org.southasia.ghru.di.Injectable
import org.southasia.ghru.ui.codeheck.CodeCheckDialogFragment
import org.southasia.ghru.util.*
import org.southasia.ghru.vo.Status
import org.southasia.ghru.vo.request.ParticipantRequest
import javax.inject.Inject

class ManualEntrySampleBagBarcodeFragment : Fragment(), Injectable {

    var binding by autoCleared<SamplecollectionManualBagBarcodeFragmentBinding>()

    var dataBindingComponent: DataBindingComponent = FragmentDataBindingComponent(this)

    @Inject
    lateinit var viewModel: ManualEntrySampleBagBarcodeViewModel

    private var participant: ParticipantRequest? = null

    var sampleId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            participant = arguments?.getParcelable<ParticipantRequest>("participant")!!
        } catch (e: KotlinNullPointerException) {
            //Crashlytics.logException(e)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val dataBinding = DataBindingUtil.inflate<SamplecollectionManualBagBarcodeFragmentBinding>(
            inflater,
            R.layout.samplecollection_manual_bag_barcode_fragment,
            container,
            false
        )
        binding = dataBinding
        setHasOptionsMenu(true)
        val appCompatActivity = requireActivity() as AppCompatActivity
        appCompatActivity.setSupportActionBar(binding.detailToolbar)
        appCompatActivity.supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.buttonContinue.singleClick {
            view?.hideKeyboard()
            handleContinue()

        }
        binding.buttonBack.singleClick {
            it?.hideKeyboard()
            navController().popBackStack()
        }
        binding.editTextCode.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                binding.textLayoutCode.error = ""
            }
        })

        viewModel.screeningIdCheck?.observe(this, Observer { householdId ->
            ////L.d(householdId.toString())
            if (householdId?.status == Status.SUCCESS) {
                val codeCheckDialogFragment = CodeCheckDialogFragment()
                codeCheckDialogFragment.show(fragmentManager!!)
            } else if (householdId?.status == Status.ERROR) {
                val bundle = bundleOf("participant" to participant, "sample_id" to binding.editTextCode.text.toString())
                findNavController().navigate(R.id.action_manualBagScanBarcodeFragment_to_bagScannedFragment, bundle)
            }

        })

        viewModel.screeningIdCheckAll?.observe(this, Observer { householdId ->
            //L.d(householdId.toString())
            if (householdId.status == Status.SUCCESS) {
                viewModel.setSampleId(sampleId)
            }
        })
//        if (BuildConfig.DEBUG) {
//            sampleId = "SAA-1048-0"
//            viewModel.setSampleIdAll(sampleId)
//        }
        return dataBinding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)


        binding.editTextCode.filters = binding.editTextCode.filters + InputFilter.AllCaps()
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, InputMethodManager.HIDE_IMPLICIT_ONLY)
        binding.editTextCode.requestFocus()

    }

    fun handleContinue() {
        val checkSum = validateChecksum(binding.editTextCode.text.toString(), Constants.TYPE_SAMPLE)
        if (!checkSum.error) {
            sampleId = binding.editTextCode.text.toString()
            viewModel.setSampleId(sampleId)
        } else {

            binding.textLayoutCode.error = getString(R.string.laboratory_ID_not_found) //checkSum.message
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                binding.root.hideKeyboard()
                navController().popBackStack()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    fun navController() = findNavController()
}