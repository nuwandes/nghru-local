package org.southasia.ghru.ui.samplemanagement.storage

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingComponent
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.birbit.android.jobqueue.JobManager
import io.reactivex.disposables.CompositeDisposable
import org.southasia.ghru.AppExecutors
import org.southasia.ghru.R
import org.southasia.ghru.binding.FragmentDataBindingComponent
import org.southasia.ghru.databinding.SampleStorageFragmentBinding
import org.southasia.ghru.di.Injectable
import org.southasia.ghru.event.QRcodeRxBus
import org.southasia.ghru.ui.samplemanagement.storage.reason.ReasonDialogFragment
import org.southasia.ghru.util.autoCleared
import org.southasia.ghru.util.collapse
import org.southasia.ghru.util.expand
import org.southasia.ghru.util.singleClick
import org.southasia.ghru.vo.request.SampleRequest
import javax.inject.Inject


class StorageFragment : Fragment(), Injectable {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    @Inject
    lateinit var appExecutors: AppExecutors

    var binding by autoCleared<SampleStorageFragmentBinding>()

    var dataBindingComponent: DataBindingComponent = FragmentDataBindingComponent(this)

    @Inject
    lateinit var viewModel: StorageViewModel

    private val disposables = CompositeDisposable()

    private var sampleRequest: SampleRequest? = null
    // private var storageId: String? = null

    @Inject
    lateinit var jobManager: JobManager


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            sampleRequest = arguments?.getParcelable("SampleRequestResource")!!
        } catch (e: KotlinNullPointerException) {
            //Crashlytics.logException(e)
        }

        disposables.add(
            QRcodeRxBus.getInstance().toObservable()
                .subscribe({ result ->
                    Log.d("oqrcode", "oqrcode ${result}")
                    sampleRequest?.storageId = result
                    navController().popBackStack()
                    // val bundle = Bundle()
                    //bundle.putString("sample_id_qr", result)
                    //findNavController().navigate(R.id.action_QRFragment_to_storageFragment, bundle)

                }, { error ->
                    error.printStackTrace()
                })
        )
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val dataBinding = DataBindingUtil.inflate<SampleStorageFragmentBinding>(
            inflater,
            R.layout.sample_storage_fragment,
            container,
            false
        )
        binding = dataBinding
        val appCompatActivity = requireActivity() as AppCompatActivity
        appCompatActivity.setSupportActionBar(binding.detailToolbar)
        appCompatActivity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        return dataBinding.root
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.viewModel = viewModel;
        binding.sample = sampleRequest
        binding.buttonScan.singleClick {
            findNavController().navigate(R.id.action_storageFragment_to_scanFragment)
        }


        binding.buttonComplete.singleClick {

            if (validateCompleteButton()) {
                findNavController().navigate(
                    R.id.action_sampleStorageFragment_to_sampleMangementHomeViewModel,
                    bundleOf("SampleRequestResource" to sampleRequest)
                )
            }


        }

        binding.checkbox.setOnCheckedChangeListener { buttonView, isChecked ->

            viewModel.setHasLinked(isChecked)
            binding.checkLayout.background = resources.getDrawable(R.drawable.ic_base_check, null)
        }

        binding.buttonCancel.singleClick {

            val reasonDialog = ReasonDialogFragment()
            reasonDialog.arguments = bundleOf("SampleRequestResource" to sampleRequest)
            reasonDialog.show(fragmentManager!!)
        }
        binding.buttonResetId.singleClick {
            binding.buttonScan.visibility = View.VISIBLE
            binding.linearLayoutLinkedStorageID.collapse()
            sampleRequest?.storageId = ""

        }

        binding.linearLayoutLinkedStorageID.collapse()
        if (!sampleRequest?.storageId.isNullOrEmpty()) {
            binding.buttonScan.visibility = View.GONE
            binding.linearLayoutLinkedStorageID.expand()
            binding.storageID = sampleRequest?.storageId

        } else {
            binding.buttonScan.visibility = View.VISIBLE
            binding.linearLayoutLinkedStorageID.collapse()
            binding.storageID = ""
        }
        binding.executePendingBindings()

    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = activity?.getSystemService(Context.CONNECTIVITY_SERVICE)
        return if (connectivityManager is ConnectivityManager) {
            val networkInfo: NetworkInfo? = connectivityManager.activeNetworkInfo
            networkInfo?.isConnected ?: false
        } else false
    }

    private fun validateCompleteButton(): Boolean {

        if (!sampleRequest?.storageId.isNullOrEmpty()) {
            if (viewModel.hasLinked.value != null && viewModel.hasLinked.value!!) {

                binding.checkLayout.background = resources.getDrawable(R.drawable.ic_base_check, null)
                return true

            } else {

                binding.checkLayout.background = resources.getDrawable(R.drawable.ic_base_check_error, null)
                return false

            }
        } else {
            Toast.makeText(context, getString(R.string.string_storage_scan_qr_message), Toast.LENGTH_SHORT).show()
            return false
        }

    }


    /**
     * Created to be able to override in tests
     */
    fun navController() = findNavController()
}
