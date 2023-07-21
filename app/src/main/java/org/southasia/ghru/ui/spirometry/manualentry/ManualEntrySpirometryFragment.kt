package org.southasia.ghru.ui.spirometry.manualentry

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
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
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import io.reactivex.disposables.CompositeDisposable
import org.southasia.ghru.R
import org.southasia.ghru.binding.FragmentDataBindingComponent
import org.southasia.ghru.databinding.SpirometryManualBarcodeEntryFragmentBinding
import org.southasia.ghru.di.Injectable
import org.southasia.ghru.event.StationCheckRxBus
import org.southasia.ghru.ui.registerpatient.scanqrcode.errordialog.ErrorDialogFragment
import org.southasia.ghru.ui.spirometry.scanbarcode.ScanBarcodeViewModel
import org.southasia.ghru.ui.stationcheck.StationCheckDialogFragment
import org.southasia.ghru.util.*
import org.southasia.ghru.vo.Meta
import org.southasia.ghru.vo.Status
import org.southasia.ghru.vo.request.ParticipantRequest
import javax.inject.Inject

class ManualEntrySpirometryFragment : Fragment(), Injectable {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    var dataBindingComponent: DataBindingComponent = FragmentDataBindingComponent(this)
    var binding by autoCleared<SpirometryManualBarcodeEntryFragmentBinding>()

    @Inject
    lateinit var viewModel: ScanBarcodeViewModel

    private val disposables = CompositeDisposable()

    private var participantRequest: ParticipantRequest? = null

    var meta: Meta? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            meta = arguments?.getParcelable<Meta>("meta")!!
        } catch (e: KotlinNullPointerException) {
            //Crashlytics.logException(e)
        }
        disposables.add(
            StationCheckRxBus.getInstance().toObservable()
                .subscribe({ result ->
                    participantRequest?.meta = meta
                    val bundle = bundleOf("participant" to participantRequest)
                    Navigation.findNavController(activity!!, R.id.container)
                        .navigate(R.id.action_global_CheckListFragment, bundle)
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
        val dataBinding = DataBindingUtil.inflate<SpirometryManualBarcodeEntryFragmentBinding>(
            inflater,
            R.layout.spirometry_manual_barcode_entry_fragment,
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


        viewModel.participant.observe(this, Observer { participantResource ->
            if (participantResource?.status == Status.SUCCESS) {

                participantRequest = participantResource.data?.data
                participantRequest?.meta = meta
                if (!participantResource.data?.stationStatus!!) {
                    participantRequest?.meta = meta
                    val bundle = bundleOf("participant" to participantRequest)
                    Navigation.findNavController(activity!!, R.id.container)
                        .navigate(R.id.action_global_CheckListFragment, bundle)
                } else {
                    val stationCheckDialogFragment = StationCheckDialogFragment()
                    stationCheckDialogFragment.show(fragmentManager!!)
                }
            } else if (participantResource?.status == Status.ERROR) {
                val errorDialogFragment = ErrorDialogFragment()
                errorDialogFragment.setErrorMessage(participantResource.message?.message!!)
                errorDialogFragment.show(fragmentManager!!)
                //Crashlytics.logException(Exception(participantResource.toString()))
            }
        })


        viewModel.participantOffline?.observe(this, Observer { participantResource ->
            if (participantResource?.status == Status.SUCCESS) {
                participantRequest?.meta = meta
                val bundle = Bundle()
                participantRequest = participantResource.data
                participantRequest?.meta = meta
                bundle.putParcelable("participant", participantRequest)
                findNavController().navigate(R.id.action_global_CheckListFragment, bundle)
            } else if (participantResource?.status == Status.ERROR) {
                val errorDialogFragment = ErrorDialogFragment()
                errorDialogFragment.setErrorMessage("The Paticipant ID is not found")
                errorDialogFragment.show(fragmentManager!!)
                //Crashlytics.logException(Exception(participantResource.toString()))
            }
            binding.executePendingBindings()
        })

        binding.editTextCode.filters = binding.editTextCode.filters + InputFilter.AllCaps()
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, InputMethodManager.HIDE_IMPLICIT_ONLY)
        binding.editTextCode.requestFocus()


    }

    fun handleContinue() {
        var checkSum = validateChecksum(binding.editTextCode.text.toString(), Constants.TYPE_PARTICIPANT)
        if (!checkSum.error) {

            if (isNetworkAvailable()) {
                viewModel.setScreeningId(binding.editTextCode.text.toString())
            } else {
                viewModel.setScreeningIdOffline(binding.editTextCode.text.toString())
            }

        } else {

            binding.textLayoutCode.error = getString(R.string.invalid_code) //checkSum.message
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