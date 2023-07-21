package org.southasia.ghru.ui.registerpatient.scanqrcode

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingComponent
import androidx.databinding.DataBindingUtil.inflate
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.budiyev.android.codescanner.*
import org.southasia.ghru.R
import org.southasia.ghru.binding.FragmentDataBindingComponent
import org.southasia.ghru.databinding.RegisterScanQrCodeFragmentBinding
import org.southasia.ghru.di.Injectable
import org.southasia.ghru.ui.registerpatient.scanqrcode.errordialog.ErrorDialogFragment
import org.southasia.ghru.ui.registerpatient.scanqrcode.membersdialog.MembersDialogFragment
import org.southasia.ghru.util.*
import org.southasia.ghru.vo.Meta
import org.southasia.ghru.vo.Status
import org.southasia.ghru.vo.request.HouseholdRequest
import org.southasia.ghru.vo.request.Member
import javax.inject.Inject


class ScanQRCodeFragment : Fragment(), Injectable {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    var binding by autoCleared<RegisterScanQrCodeFragmentBinding>()
    var dataBindingComponent: DataBindingComponent = FragmentDataBindingComponent(this)

    @Inject
    lateinit var viewModel: ScanQRCodeViewModel

    var householdId: String = ""

    var meta: Meta? = null

    var hoursFasted: String? = null

    var memberList: java.util.ArrayList<Member>? = null

    var household: HouseholdRequest? = null

    var membersResourceList: List<Member>? = null

    private var membersDialogFragment: MembersDialogFragment = MembersDialogFragment()
    private lateinit var codeScanner: CodeScanner
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            meta = arguments?.getParcelable<Meta>("meta")!!
            hoursFasted = arguments?.getString("hours_fasted")
        } catch (e: KotlinNullPointerException) {
            //Crashlytics.logException(e)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val dataBinding = inflate<RegisterScanQrCodeFragmentBinding>(
            inflater,
            R.layout.register_scan_qr_code_fragment,
            container,
            false
        )
        binding = dataBinding
        setHasOptionsMenu(true)
        val appCompatActivity = requireActivity() as AppCompatActivity
        appCompatActivity.setSupportActionBar(binding.detailToolbar)
        appCompatActivity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
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
                val checkSum = validateChecksum(it.text, Constants.TYPE_ENUMERATION)
                if (!checkSum.error) {
                    householdId = it.text
                    if (isNetworkAvailable()) {
                        viewModel.setHouseholdId(it.text)
                    } else {
                        viewModel.setHouseholdIdOffline(it.text)
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
        viewModel.members?.observe(this, Observer { membersResource ->
            binding.resource = membersResource
            if (membersResource?.status == Status.SUCCESS) {
                if (membersResource.data?.data?.isNotEmpty()!!) {
                    memberList = ArrayList(membersResource.data.data)
                } else {
                    memberList = ArrayList()
                }
                viewModel.setEnumarationId(householdId)
            } else if (membersResource?.status == Status.ERROR) {
                val errorDialogFragment = ErrorDialogFragment()
                errorDialogFragment.setErrorMessage(membersResource.message?.message!!)
                if (!errorDialogFragment.isVisible) {
                    errorDialogFragment.show(fragmentManager!!)
                }
            }
            binding.executePendingBindings()

        })
        viewModel.houseHoldBody?.observe(this, Observer {
            if (it?.status == Status.SUCCESS) {
                household = it.data?.data?.household
                if (household != null) {
                    if (!membersDialogFragment.isAdded) {
                        membersDialogFragment.arguments = bundleOf(
                            "householdId" to householdId,
                            "memberList" to memberList,
                            "hours_fasted" to hoursFasted,
                            "meta" to meta,
                            "household" to household
                        )
                        membersDialogFragment.show(fragmentManager!!)
                    }
                }
            }
        })

        viewModel.membersOfline?.observe(this, Observer {
            binding.resource = it
            if (it?.status == Status.SUCCESS) {
                if (it.data != null) {
                    if (it.data.isNotEmpty()) {
                        membersResourceList = it.data;
                    } else if (it.data.isEmpty()) {
                        membersResourceList = ArrayList()
                    } else {
                        membersResourceList = ArrayList()
                    }
                } else {
                    membersResourceList = ArrayList()
                }

                viewModel.setEnumarationIdOffline(householdId)


            } else if (it?.status == Status.ERROR) {
                val errorDialogFragment = ErrorDialogFragment()
                errorDialogFragment.setErrorMessage("The Paticipant ID is not found")
                errorDialogFragment.show(fragmentManager!!)
            }
            binding.executePendingBindings()

        })

        viewModel.houseHoldBodyOffline?.observe(this, Observer { membersResource ->
            if (membersResource?.status == Status.SUCCESS) {
                household = membersResource.data?.householdRequest
                if (household != null) {
                    if (!membersDialogFragment.isAdded) {
                        membersDialogFragment.arguments = bundleOf(
                            "householdId" to householdId,
                            "memberList" to ArrayList(membersResourceList!!),
                            "hours_fasted" to hoursFasted,
                            "meta" to meta,
                            "household" to household
                        )
                        membersDialogFragment.setCancelable(false);
                        membersDialogFragment.show(fragmentManager!!)
                    }
                }
            }
        })
        binding.buttonManualEntry.singleClick {

            binding.root.shoKeyboard()
            findNavController().navigate(
                R.id.action_scanCodeFragment_to_scanQRcodeManualFragment,
                bundleOf("hours_fasted" to hoursFasted, "meta" to meta)
            )
        }

        binding.buttonNewPaticipant.singleClick {
            findNavController().navigate(
                R.id.action_scanCodeFragment_to_explanationFragment,
                bundleOf(
                    "householdId" to householdId,
                    "hours_fasted" to hoursFasted,
                    "meta" to meta,
                    "household" to household
                )
            )
        }

//        if (BuildConfig.DEBUG) {
//            householdId = "EAA-1049-7"
//            if (isNetworkAvailable()) {
//                viewModel.setHouseholdId(householdId)
//            } else {
//                viewModel.setHouseholdIdOffline(householdId)
//
//            }
//        }
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
        if (membersDialogFragment.isVisible) {
            membersDialogFragment.dismiss()
        }
        //L.d("onPause")
        viewModel.houseHoldBody?.removeObservers(this)
        codeScanner.releaseResources()
    }


    /**
     * Created to be able to override in tests
     */
    fun navController() = findNavController()
}
