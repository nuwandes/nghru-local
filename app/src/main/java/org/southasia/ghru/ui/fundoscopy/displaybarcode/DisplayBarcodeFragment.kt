package org.southasia.ghru.ui.fundoscopy.displaybarcode


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingComponent
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import org.southasia.ghru.R
import org.southasia.ghru.binding.FragmentDataBindingComponent
import org.southasia.ghru.databinding.DisplayBarcodeBinding
import org.southasia.ghru.di.Injectable
import org.southasia.ghru.util.autoCleared
import org.southasia.ghru.util.singleClick
import org.southasia.ghru.vo.request.ParticipantRequest
import javax.inject.Inject

class DisplayBarcodeFragment : Fragment(), Injectable {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    var binding by autoCleared<DisplayBarcodeBinding>()
    var dataBindingComponent: DataBindingComponent = FragmentDataBindingComponent(this)
    @Inject
    lateinit var displayBarcodeViewModel: DisplayBarcodeViewModel

    private var participant: ParticipantRequest? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            participant = arguments?.getParcelable<ParticipantRequest>("participant")!!
        } catch (e: KotlinNullPointerException) {

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val dataBinding = DataBindingUtil.inflate<DisplayBarcodeBinding>(
            inflater,
            R.layout.display_barcode,
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
        binding.setLifecycleOwner(this)
        binding.participant = participant

        binding.nextButton.singleClick {

            if (validateNextButton()) {
                val bundle = Bundle()
                bundle.putParcelable("participant", participant)
                navController().navigate(R.id.action_displayBarcode_to_guideMainFragment, bundle)
            }
        }

        binding.checkBox.setOnCheckedChangeListener { _, isChecked ->

            displayBarcodeViewModel.setHasExplained(isChecked)
            validateNextButton()
        }

    }

    private fun validateNextButton(): Boolean {

//        if (displayBarcodeViewModel.hasGivenConsent.value != null && displayBarcodeViewModel.hasGivenConsent.value!!) {
//            binding.checkLayout.background = resources.getDrawable(R.drawable.ic_base_check, null)
//            binding.textViewError.visibility = View.GONE
//            return true
//        } else {
//            binding.checkLayout.background = resources.getDrawable(R.drawable.ic_base_check_error, null)
//            binding.textViewError.visibility = View.VISIBLE
//            return false
//
//        }
        return true
    }

    /**
     * Created to be able to override in tests
     */
    fun navController() = findNavController()
}
