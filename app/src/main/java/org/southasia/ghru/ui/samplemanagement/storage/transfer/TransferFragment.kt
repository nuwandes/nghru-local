package org.southasia.ghru.ui.samplemanagement.storage.transfer

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
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
import br.com.ilhasoft.support.validation.Validator
import com.birbit.android.jobqueue.JobManager
import com.crashlytics.android.Crashlytics
import org.southasia.ghru.R
import org.southasia.ghru.binding.FragmentDataBindingComponent
import org.southasia.ghru.databinding.TransferFragmentBinding
import org.southasia.ghru.di.Injectable
import org.southasia.ghru.jobs.SyncSampledStorageFreezeIDJob
import org.southasia.ghru.ui.samplemanagement.storage.completed.CompletedDialogFragment
import org.southasia.ghru.ui.samplemanagement.storage.reasonc.ReasonDialogFragment
import org.southasia.ghru.util.*
import org.southasia.ghru.vo.Status
import org.southasia.ghru.vo.Storage
import org.southasia.ghru.vo.StorageDto
import org.southasia.ghru.vo.request.SampleRequest
import timber.log.Timber
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class TransferFragment : Fragment(), Injectable {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    var dataBindingComponent: DataBindingComponent = FragmentDataBindingComponent(this)
    var binding by autoCleared<TransferFragmentBinding>()
    private var sampleRequest: SampleRequest? = null

    val storage = Storage.build()

    @Inject
    lateinit var viewModel: TransferViewModel

    @Inject
    lateinit var jobManager: JobManager


    private lateinit var validator: Validator
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            sampleRequest = arguments?.getParcelable("SampleRequestResource")!!
        } catch (e: KotlinNullPointerException) {
            //Crashlytics.logException(e)
        }

    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val dataBinding = DataBindingUtil.inflate<TransferFragmentBinding>(
                inflater,
                R.layout.transfer_fragment,
                container,
                false
        )
        binding = dataBinding
        validator = Validator(binding)
        setHasOptionsMenu(true)
        val appCompatActivity = requireActivity() as AppCompatActivity
        appCompatActivity.setSupportActionBar(binding.detailToolbar)
        appCompatActivity.supportActionBar?.setDisplayHomeAsUpEnabled(true)



        return dataBinding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.sample = sampleRequest
        binding.storage = storage

        binding.editTextFreezerId.filters = binding.editTextFreezerId.filters + InputFilter.AllCaps()
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, InputMethodManager.HIDE_IMPLICIT_ONLY)

        viewModel.sampleMangementPocess?.observe(this, Observer { sampleMangementPocess ->
            Timber.d(sampleMangementPocess.toString())
            if (sampleMangementPocess?.status == Status.SUCCESS) {
                viewModel.setDelete(sampleRequest)
            } else if (sampleMangementPocess?.status == Status.ERROR) {
                binding.progressBar.visibility = View.GONE
                binding.buttonComplete.visibility = View.VISIBLE
                //Crashlytics.logException(Exception(sampleMangementPocess.message?.message))
                //var error = accessToken.dat
            }
        })

        viewModel.sampleMangementPocessDelete?.observe(this, Observer { sampleMangementPocess ->
            Timber.d(sampleMangementPocess.toString())
            if (sampleMangementPocess?.status == Status.SUCCESS) {
                val staorage = StorageDto(freezerId = storage.freezerId)
                staorage.meta = sampleRequest?.meta
//                if (isNetworkAvailable()) {
                sampleRequest?.syncPending = !isNetworkAvailable()
                sampleRequest?.freezerId = storage.freezerId
                viewModel.setSync(staorage, sampleRequest)
//                } else {
//                    jobManager.addJobInBackground(SyncSampledStorageFreezeIDJob(storageDto = staorage, sampleStorageRequest = sampleRequest))
//
//                }
               // activity!!.finish()
                //viewModel.setDelete(sampleRequest)
                val completedDialogFragment = CompletedDialogFragment()
                completedDialogFragment.arguments = bundleOf("is_cancel" to false)
                completedDialogFragment.show(fragmentManager!!)
            } else if (sampleMangementPocess?.status == Status.ERROR) {
                Crashlytics.setString("sampleRequest", sampleRequest.toString())
                Crashlytics.setString("freezerId", StorageDto(freezerId = storage.freezerId).toString())
                Crashlytics.logException(Exception("sample collection " + sampleMangementPocess.message.toString()))
                binding.progressBar.visibility = View.GONE
                binding.buttonComplete.visibility = View.VISIBLE
                //Crashlytics.logException(Exception(sampleMangementPocess.message?.message))
                //var error = accessToken.dat
            }
        })
        binding.buttonComplete.singleClick {
            handleContinue()
        }
        binding.buttonCancel.singleClick {
            val reasonDialog = ReasonDialogFragment()
            reasonDialog.arguments = bundleOf("SampleRequestResource" to sampleRequest)
            reasonDialog.show(fragmentManager!!)
            //  navController().popBackStack()
        }
        binding.checkbox.setOnCheckedChangeListener { _, isChecked ->

            viewModel.setHasChecked(isChecked)
            validateChecked()

        }
    }
    fun handleContinue() {
        val checkSum = validateChecksum(binding.editTextFreezerId.text.toString(), Constants.TYPE_FREEZER_BOX)
        if (!checkSum.error) {
            activity?.runOnUiThread({

                view?.hideKeyboard()

                // commented due to storage no need to update start_time/end_time/collected by ------ 28.4.2020 -----

//                val endTime: String = convertTimeTo24Hours()
//                val endDate: String = getDate()
//                val endDateTime:String = endDate + " " + endTime
//
//                sampleRequest?.meta?.endTime = endDateTime

                // ----------------------------------------------------------------------------------------------------
                if (validator.validate()) {
                    if (viewModel.isChecked) {
                        println(storage.freezerId)
                        storage.freezerId = binding.editTextFreezerId.text.toString()
                        viewModel.setDelete(sampleRequest)

                    } else {
                        validateChecked()
                    }
                }
            })
        } else {

            binding.textLayoutCode.error = getString(R.string.invalid_code) //checkSum.message
        }
    }
    private fun validateChecked() {
        if (viewModel.isChecked) {
            binding.checkLayout.background = resources.getDrawable(R.drawable.ic_base_check, null)

        } else {
            binding.checkLayout.background = resources.getDrawable(R.drawable.ic_base_check_error, null)

        }

    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = activity?.getSystemService(Context.CONNECTIVITY_SERVICE)
        return if (connectivityManager is ConnectivityManager) {
            val networkInfo: NetworkInfo? = connectivityManager.activeNetworkInfo
            networkInfo?.isConnected ?: false
        } else false
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

    fun navController() = findNavController()
}