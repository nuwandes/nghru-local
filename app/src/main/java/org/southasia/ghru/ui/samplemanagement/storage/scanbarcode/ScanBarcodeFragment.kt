package org.southasia.ghru.ui.samplemanagement.storage.scanbarcode

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
import org.southasia.ghru.R
import org.southasia.ghru.binding.FragmentDataBindingComponent
import org.southasia.ghru.databinding.ScanBarcodeStorageFragmentBinding
import org.southasia.ghru.di.Injectable
import org.southasia.ghru.ui.registerpatient.scanqrcode.errordialog.ErrorDialogFragment
import org.southasia.ghru.util.*
import org.southasia.ghru.vo.Meta
import org.southasia.ghru.vo.Status
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject


class ScanBarcodeFragment : Fragment(), Injectable {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory


    var binding by autoCleared<ScanBarcodeStorageFragmentBinding>()

    var dataBindingComponent: DataBindingComponent = FragmentDataBindingComponent(this)

    @Inject
    lateinit var viewModel: ScanBarcodeViewModel

    private lateinit var codeScanner: CodeScanner

    var meta: Meta? = null


    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val dataBinding = DataBindingUtil.inflate<ScanBarcodeStorageFragmentBinding>(
                inflater,
                R.layout.scan_barcode_storage_fragment,
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
        viewModel.setUser("user")
        viewModel.user?.observe(this, Observer { userData ->
            if (userData?.data != null) {

                // commented due to storage no need to update start_time/end_time/collected by ------ 28.4.2020 -----
//                val sTime: String = convertTimeTo24Hours()
//                val sDate: String = getDate()
//                val sDateTime:String = sDate + " " + sTime
//
//                meta = Meta(collectedBy = userData.data?.id, startTime = sDateTime)

                // ----------------------------------------------------------------------------------------
                //  meta?.registeredBy = userData.data?.id
            }

        })

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
                    activity?.runOnUiThread {
                        //                        if (isNetworkAvailable()) {
                //                            val mSampleRequest = SampleRequest(sampleId = it.text, screeningId = "")
                //                            findNavController().navigate(R.id.action_QRcodeScanFragment_to_TransferFragment, bundleOf("SampleRequestResource" to mSampleRequest))
                //                        } else {
                //                            viewModel.setSampleIdOffline(it.text)
                //                        }

                        viewModel.setSampleIdOffline(it.text)
                    }
                } else {
                    codeScanner.startPreview()
                    val errorDialogFragment = org.southasia.ghru.ui.registerpatient.scanqrcode.errordialog.ErrorDialogFragment()
                    errorDialogFragment.setErrorMessage(getString(R.string.invalid_code))
                    errorDialogFragment.show(fragmentManager!!)
                    //Crashlytics.logException(Exception(getString(R.string.invalid_code)))
                }
            }
        }
        codeScanner.errorCallback = ErrorCallback {
            // or ErrorCallback.SUPPRESS
            activity?.runOnUiThread {
                Toast.makeText(activity!!, "Camera initialization error: ${it.message}",
                        Toast.LENGTH_LONG).show()
                codeScanner.startPreview()
            }
        }
        viewModel.sampleOffline?.observe(this, Observer { sampleRequestResource ->
            if (sampleRequestResource?.status == Status.SUCCESS) {
                // findNavController().navigate(R.id.action_QRcodeScanFragment_to_TransferFragment, bundleOf("SampleRequestResource" to sampleRequestResource.data))
                val sample = sampleRequestResource.data
                sample?.meta = meta
                Navigation.findNavController(activity!!, R.id.container).navigate(R.id.action_QRcodeScanFragment_to_TransferFragment, bundleOf("SampleRequestResource" to sample))

            } else if (sampleRequestResource?.status == Status.ERROR) {
                val errorDialogFragment = ErrorDialogFragment()
                codeScanner.startPreview()
                errorDialogFragment.setErrorMessage(getString(R.string.processing_error_id_not_valid))
                errorDialogFragment.show(fragmentManager!!)
                //Crashlytics.logException(Exception(sampleRequestResource.toString()))
            }
            binding.executePendingBindings()
        })
//
//        if (BuildConfig.DEBUG) {
//            viewModel.setSampleIdOffline("CAA-0150-8")
//        }

        binding.buttonManualEntry.singleClick {
            val bundle = bundleOf("meta" to meta)
            findNavController().navigate(R.id.action_scanbarCodeFragment_to_ManualEntryBarcodeFragment, bundle)
        }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.getItemId()) {
            android.R.id.home -> {
                return navController().popBackStack()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = activity?.getSystemService(Context.CONNECTIVITY_SERVICE)
        return if (connectivityManager is ConnectivityManager) {
            val networkInfo: NetworkInfo? = connectivityManager.activeNetworkInfo
            networkInfo?.isConnected ?: false
        } else false
    }

    override fun onResume() {
        super.onResume()
        codeScanner.startPreview()
    }

    override fun onPause() {
        super.onPause()
        viewModel.sampleOffline?.removeObservers(this)
        codeScanner.releaseResources()
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
