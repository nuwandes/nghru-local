package org.southasia.ghru.ui.samplemanagement.tubescanbarcode.manualentry

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
import org.southasia.ghru.databinding.SampleManagementManualTubescanBinding
import org.southasia.ghru.di.Injectable
import org.southasia.ghru.ui.samplemanagement.tubescanbarcode.TubeScanBarcodeViewModel
import org.southasia.ghru.ui.samplemanagement.tubescanbarcode.errordialog.ErrorDialogFragment
import org.southasia.ghru.util.*
import org.southasia.ghru.vo.Meta
import org.southasia.ghru.vo.Status
import javax.inject.Inject

class TubeScanManualBarcodeFragment : Fragment(), Injectable {

    var binding by autoCleared<SampleManagementManualTubescanBinding>()
    var dataBindingComponent: DataBindingComponent = FragmentDataBindingComponent(this)
    @Inject
    lateinit var viewModel: TubeScanBarcodeViewModel

    var meta: Meta? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            meta = arguments?.getParcelable<Meta>("meta")!!
        } catch (e: KotlinNullPointerException) {
            //Crashlytics.logException(e)
        }
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val dataBinding = DataBindingUtil.inflate<SampleManagementManualTubescanBinding>(
                inflater,
                R.layout.sample_management_manual_tubescan,
                container,
                false
        )
        binding = dataBinding
        setHasOptionsMenu(true)
        val appCompatActivity = requireActivity() as AppCompatActivity
        appCompatActivity.setSupportActionBar(binding.detailToolbar)
        appCompatActivity.supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.buttonContinue.singleClick {

            handleContinue()
            view?.hideKeyboard()
        }
        binding.buttonBack.singleClick {
            binding.root.hideKeyboard()
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
        return dataBinding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel.sampleOffline?.observe(this, Observer { sampleRequestResource ->
            if (sampleRequestResource?.status == Status.SUCCESS) {
                val sample = sampleRequestResource.data
                sample?.meta = meta
                findNavController().navigate(R.id.action_global_sampleStorageFragment, bundleOf("SampleRequestResource" to sample))
            } else if (sampleRequestResource?.status == Status.ERROR) {
                val errorDialogFragment = ErrorDialogFragment()
                errorDialogFragment.setErrorMessage(getString(R.string.processing_error_id_not_valid))
                errorDialogFragment.show(fragmentManager!!)
                //Crashlytics.logException(Exception(sampleRequestResource.toString()))
            }
            binding.executePendingBindings()
        })

        binding.editTextCode.filters = binding.editTextCode.filters + InputFilter.AllCaps()
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, InputMethodManager.HIDE_IMPLICIT_ONLY)
        binding.editTextCode.requestFocus()

    }

    fun handleContinue() {
        val checkSum = validateChecksum(binding.editTextCode.text.toString(), Constants.TYPE_SAMPLE)
        if (!checkSum.error) {
            activity?.runOnUiThread({
                viewModel.setSampleIdOffline(binding.editTextCode.text.toString())
            })
        } else {

            binding.textLayoutCode.error = getString(R.string.invalid_code)//checkSum.message
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