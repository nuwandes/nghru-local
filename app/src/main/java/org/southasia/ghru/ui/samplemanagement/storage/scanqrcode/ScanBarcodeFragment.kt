package org.southasia.ghru.ui.samplemanagement.storage.scanqrcode

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingComponent
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.budiyev.android.codescanner.*
import org.southasia.ghru.R
import org.southasia.ghru.binding.FragmentDataBindingComponent
import org.southasia.ghru.databinding.BagscanStorageFragmentBinding
import org.southasia.ghru.di.Injectable
import org.southasia.ghru.event.QRcodeRxBus
import org.southasia.ghru.ui.codeheck.CodeCheckDialogFragment
import org.southasia.ghru.ui.registerpatient.scanqrcode.errordialog.ErrorDialogFragment
import org.southasia.ghru.util.Constants
import org.southasia.ghru.util.autoCleared
import org.southasia.ghru.util.singleClick
import org.southasia.ghru.util.validateChecksum
import org.southasia.ghru.vo.Status
import javax.inject.Inject


class ScanBarcodeFragment : Fragment(), Injectable {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory


    var binding by autoCleared<BagscanStorageFragmentBinding>()

    var dataBindingComponent: DataBindingComponent = FragmentDataBindingComponent(this)

    @Inject
    lateinit var viewModel: ScanBarcodeViewModel

    private lateinit var codeScanner: CodeScanner

    var storageId: String? = null

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val dataBinding = DataBindingUtil.inflate<BagscanStorageFragmentBinding>(
                inflater,
                R.layout.bagscan_storage_fragment,
                container,
                false
        )
        binding = dataBinding
        setHasOptionsMenu(true)
        val appCompatActivity = requireActivity() as AppCompatActivity
        appCompatActivity.setSupportActionBar(binding.detailToolbar)
        appCompatActivity.supportActionBar?.setDisplayHomeAsUpEnabled(false)
        return dataBinding.root
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        codeScanner = CodeScanner(context!!, binding.scannerView)

        // Parameters (default values)
        codeScanner.camera = CodeScanner.CAMERA_BACK // or CAMERA_FRONT or specific camera id
        codeScanner.formats = CodeScanner.TWO_DIMENSIONAL_FORMATS // list of type BarcodeFormat,
        // ex. listOf(BarcodeFormat.QR_CODE)
        codeScanner.autoFocusMode = AutoFocusMode.SAFE // or CONTINUOUS
        codeScanner.scanMode = ScanMode.SINGLE // or CONTINUOUS or PREVIEW
        codeScanner.isAutoFocusEnabled = true // Whether to enable auto focus or not
        codeScanner.isFlashEnabled = false // Whether to enable flash or not
        codeScanner.startPreview()
        // Callbacks
        codeScanner.decodeCallback = DecodeCallback {
            activity?.runOnUiThread {
                Toast.makeText(activity!!, getString(R.string.scan_result) + ": ${it.text}", Toast.LENGTH_LONG).show()

                val checkSum = validateChecksum(it.text, Constants.TYPE_STORAGE)
                if (!checkSum.error) {
                    // mFullScannerFragment.stop()
                    storageId = it.text
                    viewModel.setStorageId(storageId)
                } else {
                    // mFullScannerFragment.start()
                    codeScanner.startPreview()
                    val errorDialogFragment = ErrorDialogFragment()
                    errorDialogFragment.setErrorMessage(getString(R.string.invalid_code))
                    errorDialogFragment.show(fragmentManager!!)
                    //Crashlytics.logException(Exception(getString(R.string.invalid_code)))
                }
            }
        }

        viewModel.storageIdCheck?.observe(this, Observer { householdId ->
           //L.d(householdId.toString())
            if (householdId?.status == Status.SUCCESS) {
                codeScanner.startPreview()
                val codeCheckDialogFragment = CodeCheckDialogFragment()
                codeCheckDialogFragment.show(fragmentManager!!)
            } else if (householdId?.status == Status.ERROR) {
                QRcodeRxBus.getInstance().post(storageId!!)
            }

        })
        codeScanner.errorCallback = ErrorCallback {
            // or ErrorCallback.SUPPRESS
            activity?.runOnUiThread {
                Toast.makeText(activity!!, "Camera initialization error: ${it.message}",
                        Toast.LENGTH_LONG).show()
                codeScanner.startPreview()
            }
        }
        binding.buttonManualEntry.singleClick {
            val bundle = bundleOf()
            findNavController().navigate(R.id.action_QRFragment_to_manualEntryFragment, bundle)
        }
//        if (BuildConfig.DEBUG) {
//            storageId = "CAA-1022-8"
//            codeScanner.releaseResources()
//            viewModel.setStorageId(storageId)
//        }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.getItemId()) {
            android.R.id.home -> {
                return navController().popBackStack()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        codeScanner.startPreview()
    }

    override fun onPause() {
        super.onPause()
        codeScanner.releaseResources()
    }


    /**
     * Created to be able to override in tests
     */
    fun navController() = findNavController()
}
