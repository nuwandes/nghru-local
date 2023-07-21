package org.southasia.ghru.ui.samplecollection.bagscanbarcode

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
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
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.budiyev.android.codescanner.*
import io.reactivex.disposables.CompositeDisposable
import org.southasia.ghru.R
import org.southasia.ghru.binding.FragmentDataBindingComponent
import org.southasia.ghru.databinding.BagScanBarcodeFragmentBinding
import org.southasia.ghru.di.Injectable
import org.southasia.ghru.ui.codeheck.CodeCheckDialogFragment
import org.southasia.ghru.ui.registerpatient.scanqrcode.errordialog.ErrorDialogFragment
import org.southasia.ghru.util.Constants
import org.southasia.ghru.util.autoCleared
import org.southasia.ghru.util.singleClick
import org.southasia.ghru.util.validateChecksum
import org.southasia.ghru.vo.Status
import org.southasia.ghru.vo.request.ParticipantRequest
import javax.inject.Inject


class BagScanBarcodeFragment : Fragment(), Injectable {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    var binding by autoCleared<BagScanBarcodeFragmentBinding>()

    var dataBindingComponent: DataBindingComponent = FragmentDataBindingComponent(this)
    @Inject
    lateinit var viewModel: BagScanBarcodeViewModel

    private val disposables = CompositeDisposable()

    private var participant: ParticipantRequest? = null

    private lateinit var codeScanner: CodeScanner

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
        val dataBinding = DataBindingUtil.inflate<BagScanBarcodeFragmentBinding>(
            inflater,
            R.layout.bag_scan_barcode_fragment,
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
        codeScanner.formats = CodeScanner.ONE_DIMENSIONAL_FORMATS // list of type BarcodeFormat,
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

                val checkSum = validateChecksum(it.text, Constants.TYPE_SAMPLE)
                if (!checkSum.error) {
                    //mFullScannerFragment?.stop()
                    sampleId = it.text
                    if (isNetworkAvailable()) {
                        viewModel.setSampleIdAll(sampleId)
                    } else {
                        viewModel.setSampleId(sampleId)
                    }
                } else {
                    codeScanner.startPreview()
                    val errorDialogFragment = ErrorDialogFragment()
                    // errorDialogFragment.setErrorMessage(checkSum.message)
                    errorDialogFragment.setErrorMessage(getString(R.string.laboratory_ID_not_found))
                    errorDialogFragment.show(fragmentManager!!)
                    //  //Crashlytics.logException(Exception(checkSum.message))
                    //Crashlytics.logException(Exception(getString(R.string.laboratory_ID_not_found)))
                    //mFullScannerFragment?.start()
                }

            }
        }
        codeScanner.errorCallback = ErrorCallback {
            // or ErrorCallback.SUPPRESS
            activity?.runOnUiThread {
                Toast.makeText(
                    activity!!, "Camera initialization error: ${it.message}",
                    Toast.LENGTH_LONG
                ).show()
                codeScanner.startPreview()
            }
        }

        viewModel.screeningIdCheck?.observe(this, Observer { householdId ->
            //L.d(householdId.toString())
            if (householdId?.status == Status.SUCCESS) {
                codeScanner.startPreview()
                val codeCheckDialogFragment = CodeCheckDialogFragment()
                codeCheckDialogFragment.show(fragmentManager!!)
            } else if (householdId?.status == Status.ERROR) {
                val bundle = bundleOf("participant" to participant, "sample_id" to sampleId)
                Navigation.findNavController(activity!!, R.id.container)
                    .navigate(R.id.action_bagScanBarcodeFragment_to_bagScannedFragment, bundle)
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
//            if(isNetworkAvailable()) {
//                viewModel.setSampleIdAll(sampleId)
//            }else{
//                viewModel.setSampleId(sampleId)
//            }
//        }

        binding.buttonManualEntry.singleClick {
            val bundle = bundleOf("participant" to participant)
            findNavController().navigate(R.id.action_bagScanBarcodeFragment_to_maualBagScannedFragment, bundle)
        }


    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = activity?.getSystemService(Context.CONNECTIVITY_SERVICE)
        return if (connectivityManager is ConnectivityManager) {
            val networkInfo: NetworkInfo? = connectivityManager.activeNetworkInfo
            networkInfo?.isConnected ?: false
        } else false
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
        disposables.clear()
        codeScanner.releaseResources()
        disposables.clear()
    }


    /**
     * Created to be able to override in tests
     */
    fun navController() = findNavController()
}
