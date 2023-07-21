package org.southasia.ghru.ui.samplecollection.fast


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
import org.southasia.ghru.databinding.FastFragmentBinding
import org.southasia.ghru.di.Injectable
import org.southasia.ghru.ui.samplecollection.fast.reshedule.ResheduleDialogFragment
import org.southasia.ghru.util.autoCleared
import org.southasia.ghru.util.hideKeyboard
import org.southasia.ghru.util.singleClick
import org.southasia.ghru.vo.request.ParticipantRequest
import javax.inject.Inject

class FastFragment : Fragment(), Injectable {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    var binding by autoCleared<FastFragmentBinding>()


    var dataBindingComponent: DataBindingComponent = FragmentDataBindingComponent(this)

    @Inject
    lateinit var fastViewModel: FastViewModel


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
        val dataBinding = DataBindingUtil.inflate<FastFragmentBinding>(
            inflater,
            R.layout.fast_fragment,
            container,
            false
        )
        binding = dataBinding
        setHasOptionsMenu(true)
        val appCompatActivity = requireActivity() as AppCompatActivity
        appCompatActivity.setSupportActionBar(binding.detailToolbar)
        appCompatActivity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.root.hideKeyboard()
        return dataBinding.root
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.setLifecycleOwner(this)

        binding.buttonCancel.singleClick {

            val resheduleDialogFragment = ResheduleDialogFragment()
            val bundle = Bundle()
            resheduleDialogFragment.arguments = bundle
            resheduleDialogFragment.show(fragmentManager!!)
        }

        binding.buttonSubmit.singleClick({
            var bundle = bundleOf("participant" to participant)
            navController().navigate(R.id.action_fastFragment_to_fastedFragment, bundle)
        })

        binding.participant = participant
    }

    /**
     * Created to be able to override in tests
     */
    fun navController() = findNavController()
}
