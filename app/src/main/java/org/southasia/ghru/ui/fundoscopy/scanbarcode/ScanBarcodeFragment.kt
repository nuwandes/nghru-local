package org.southasia.ghru.ui.fundoscopy.scanbarcode

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
import org.southasia.ghru.BuildConfig
import org.southasia.ghru.R
import org.southasia.ghru.binding.FragmentDataBindingComponent
import org.southasia.ghru.databinding.FundoscopyScanBarcodeBinding
import org.southasia.ghru.di.Injectable
import org.southasia.ghru.event.StationCheckRxBus
import org.southasia.ghru.ui.registerpatient.scanqrcode.errordialog.ErrorDialogFragment
import org.southasia.ghru.ui.stationcheck.StationCheckDialogFragment
import org.southasia.ghru.util.*
import org.southasia.ghru.vo.Meta
import org.southasia.ghru.vo.Status
import org.southasia.ghru.vo.request.ParticipantRequest
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject


class ScanBarcodeFragment : Fragment(), Injectable {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    var binding by autoCleared<FundoscopyScanBarcodeBinding>()
    var dataBindingComponent: DataBindingComponent = FragmentDataBindingComponent(this)
    @Inject
    lateinit var viewModel: ScanBarcodeViewModel


    private lateinit var codeScanner: CodeScanner

    private val disposables = CompositeDisposable()

    private var participantRequest: ParticipantRequest? = null

    var meta: Meta? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        disposables.add(
            StationCheckRxBus.getInstance().toObservable()
                .subscribe({ result ->
                    val bundle = bundleOf("participant" to participantRequest)
                    Navigation.findNavController(activity!!, R.id.container)
                        .navigate(R.id.action_scanBarcodeFragment_to_displayBarcode, bundle)
                }, { error ->
                    print(error)
                    error.printStackTrace()
                })
        )
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val dataBinding = DataBindingUtil.inflate<FundoscopyScanBarcodeBinding>(
            inflater,
            R.layout.fundoscopy_scan_barcode,
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

                val sTime: String = convertTimeTo24Hours()
                val sDate: String = getDate()
                val sDateTime:String = sDate + " " + sTime

                meta = Meta(collectedBy = userData.data?.id, startTime = sDateTime)
                //meta?.registeredBy = userData.data?.id
            }

        })
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

        codeScanner.decodeCallback = DecodeCallback {
            activity?.runOnUiThread {
                Toast.makeText(activity!!, getString(R.string.scan_result) + ": ${it.text}", Toast.LENGTH_LONG).show()

                val checkSum = validateChecksum(it.text, Constants.TYPE_PARTICIPANT)
                if (!checkSum.error) {

                    if (isNetworkAvailable()) {
                        viewModel.setScreeningId(it.text)
                    } else {
                        viewModel.setScreeningIdOffline(it.text)
                    }

                } else {
                    codeScanner.startPreview()
                    val errorDialogFragment = ErrorDialogFragment()
                    errorDialogFragment.setErrorMessage(getString(R.string.invalid_code))
                    errorDialogFragment.show(fragmentManager!!)
                    //Crashlytics.logException(Exception(getString(R.string.invalid_code)))
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
            }
        }
        viewModel.participant.observe(this, Observer { participantResource ->
            if (participantResource?.status == Status.SUCCESS) {
                participantRequest = participantResource.data?.data
                participantRequest?.meta = meta
                if (!participantResource.data?.stationStatus!!) {
                    val bundle = bundleOf("participant" to participantRequest)
                    Navigation.findNavController(activity!!, R.id.container)
                        .navigate(R.id.action_scanBarcodeFragment_to_displayBarcode, bundle)
                } else {
                    val stationCheckDialogFragment = StationCheckDialogFragment()
                    stationCheckDialogFragment.show(fragmentManager!!)
                }

            } else if (participantResource?.status == Status.ERROR) {
                codeScanner.startPreview()
                val errorDialogFragment = ErrorDialogFragment()
                errorDialogFragment.setErrorMessage(participantResource.message?.message!!)
                errorDialogFragment.show(fragmentManager!!)
            }
        })

        viewModel.participantOffline?.observe(this, Observer { participantResource ->
            if (participantResource?.status == Status.SUCCESS) {
                participantRequest = participantResource.data
                participantRequest?.meta = meta
                val bundle = Bundle()
                bundle.putParcelable("participant", participantRequest)
                // findNavController().navigate(R.id.action_scanBarcodeFragment_to_displayBarcode, bundle)
                Navigation.findNavController(activity!!, R.id.container)
                    .navigate(R.id.action_scanBarcodeFragment_to_displayBarcode, bundle)

            } else if (participantResource?.status == Status.ERROR) {
                codeScanner.startPreview()
                val errorDialogFragment = ErrorDialogFragment()
                errorDialogFragment.setErrorMessage("The Paticipant ID is not found")
                errorDialogFragment.show(fragmentManager!!)
                //Crashlytics.logException(Exception(participantResource.toString()))
            }
            binding.executePendingBindings()
        })
//        if (BuildConfig.DEBUG) {
//            val screeningId = "PAA-1042-9"
//            if (isNetworkAvailable()) {
//                viewModel.setScreeningId(screeningId)
//            } else {
//                viewModel.setScreeningIdOffline(screeningId)
//            }
//        }

        binding.buttonManualEntry.singleClick {
            val bundle = bundleOf("meta" to meta)
            findNavController().navigate(R.id.action_scanBarcodeFragment_to_manualFragment, bundle)
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
        viewModel.participant.removeObservers(this)
        viewModel.participantOffline?.removeObservers(this)
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
