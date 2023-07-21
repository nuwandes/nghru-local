package org.southasia.ghru.ui.samplecollection.fasted


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingComponent
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import org.southasia.ghru.R
import org.southasia.ghru.binding.FragmentDataBindingComponent
import org.southasia.ghru.databinding.FastedFragmentBinding
import org.southasia.ghru.di.Injectable
import org.southasia.ghru.util.autoCleared
import org.southasia.ghru.util.hideKeyboard
import org.southasia.ghru.util.singleClick
import org.southasia.ghru.vo.request.ParticipantRequest
import javax.inject.Inject

class FastedFragment : Fragment(), Injectable {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    var binding by autoCleared<FastedFragmentBinding>()


    var dataBindingComponent: DataBindingComponent = FragmentDataBindingComponent(this)

    private var participant: ParticipantRequest? = null

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
        val dataBinding = DataBindingUtil.inflate<FastedFragmentBinding>(
            inflater,
            R.layout.fasted_fragment,
            container,
            false
        )
        binding = dataBinding
        setHasOptionsMenu(true)
        val appCompatActivity = requireActivity() as AppCompatActivity
        appCompatActivity.setSupportActionBar(binding.detailToolbar)
        appCompatActivity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.root.hideKeyboard()

        binding.participant = participant
        return dataBinding.root
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.setLifecycleOwner(this)

        binding.buttonSubmit.singleClick {
            val bundle = bundleOf("participant" to participant)
            navController().navigate(R.id.action_fastedFragment_to_bagScanBarcodeFragment, bundle)
        }
    }

    /**
     * Created to be able to override in tests
     */
    fun navController() = findNavController()
}
