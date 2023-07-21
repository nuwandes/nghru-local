package org.southasia.ghru.ui.samplemanagement.storage.manualentry

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
import androidx.databinding.DataBindingComponent
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import org.southasia.ghru.R
import org.southasia.ghru.binding.FragmentDataBindingComponent
import org.southasia.ghru.databinding.StorageManualQrcodeEntryFragmentBinding
import org.southasia.ghru.di.Injectable
import org.southasia.ghru.event.QRcodeRxBus
import org.southasia.ghru.ui.codeheck.CodeCheckDialogFragment
import org.southasia.ghru.util.*
import org.southasia.ghru.vo.Status
import org.southasia.ghru.vo.request.SampleRequest
import javax.inject.Inject

class ManualEntryFragment : Fragment(), Injectable {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    var dataBindingComponent: DataBindingComponent = FragmentDataBindingComponent(this)

    var binding by autoCleared<StorageManualQrcodeEntryFragmentBinding>()

    private var sampleRequest: SampleRequest? = null

    @Inject
    lateinit var viewModel: ManualEntryViewModel

    var storageId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        try {
//            sampleRequest = arguments?.getParcelable("SampleRequestResource")!!
//        } catch (e: KotlinNullPointerException) {
//            //Crashlytics.logException(e)
//        }

    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val dataBinding = DataBindingUtil.inflate<StorageManualQrcodeEntryFragmentBinding>(
                inflater,
                R.layout.storage_manual_qrcode_entry_fragment,
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

        binding.codeEditText.addTextChangedListener(object : TextWatcher {
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

        viewModel.storageIdCheck?.observe(this, Observer { householdId ->
            //L.d(householdId.toString())
            if (householdId?.status == Status.SUCCESS) {
                val codeCheckDialogFragment = CodeCheckDialogFragment()
                codeCheckDialogFragment.show(fragmentManager!!)
            } else if (householdId?.status == Status.ERROR) {
                QRcodeRxBus.getInstance().post(binding.codeEditText.text.toString())
                navController().popBackStack()
            }

        })

        binding.codeEditText.filters = binding.codeEditText.filters + InputFilter.AllCaps()
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, InputMethodManager.HIDE_IMPLICIT_ONLY)
        binding.codeEditText.requestFocus()

        binding.root.shoKeyboard()
        //view?.shoKeyboard()

    }

    fun handleContinue() {
        val checkSum = validateChecksum(binding.codeEditText.text.toString(), Constants.TYPE_STORAGE)
        if (!checkSum.error) {
            activity?.runOnUiThread({

                storageId = binding.codeEditText.text.toString()
                viewModel.setStorageId(storageId)
            })
        } else {

            binding.textLayoutCode.error = getString(R.string.invalid_code) //checkSum.message
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