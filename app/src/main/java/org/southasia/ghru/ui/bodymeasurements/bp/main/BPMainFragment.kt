package org.southasia.ghru.ui.bodymeasurements.bp.main


import android.os.Bundle
import android.util.Log
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
import org.southasia.ghru.databinding.BPMainFragmentBinding
import org.southasia.ghru.di.Injectable
import org.southasia.ghru.ui.bodymeasurements.bp.info.InfoAdapter
import org.southasia.ghru.ui.bodymeasurements.bp.skip.SkipDialogFragment
import org.southasia.ghru.util.*
import org.southasia.ghru.vo.BodyMeasurement
import javax.inject.Inject

class BPMainFragment : Fragment(), Injectable {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private var measurement: BodyMeasurement? = null


    var binding by autoCleared<BPMainFragmentBinding>()

    var dataBindingComponent: DataBindingComponent = FragmentDataBindingComponent(this)

    private lateinit var pagerAdapter: InfoAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        measurement = arguments?.getParcelable<BodyMeasurement>("bodymeasurement")!!
        Log.d("measurement", measurement?.height?.value)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val dataBinding = DataBindingUtil.inflate<BPMainFragmentBinding>(
            inflater,
            R.layout.b_p_main_fragment,
            container,
            false
        )
        binding = dataBinding
        setHasOptionsMenu(true)
        val appCompatActivity = requireActivity() as AppCompatActivity
        appCompatActivity.setSupportActionBar(binding.detailToolbar)
        appCompatActivity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.expand = true
        // val info = arrayOf("a", "b", "c")
        pagerAdapter = InfoAdapter(childFragmentManager)
        binding.root.hideKeyboard()
        return dataBinding.root
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.viewPager.adapter = pagerAdapter
        binding.dotsIndicator.setViewPager(binding.viewPager)
        binding.buttonManual.singleClick {
            val bundle = Bundle()
            bundle.putParcelable("bodymeasurement", measurement)
            navController().navigate(R.id.action_bPMainFragment_to_pPManualOneFragment, bundle)
        }

        binding.previousButton.singleClick {
            navController().popBackStack()
        }


        binding.textViewSkip.singleClick {
            val skipDialogFragment = SkipDialogFragment()
//            val bundle = Bundle()
//            bundle.putInt("step", 0)
//
//            bundle.putParcelable(Constants.ARG_BODY_MEASURMENT, measurement)
            skipDialogFragment.arguments = bundleOf("step" to 0, Constants.ARG_BODY_MEASURMENT to measurement)
            skipDialogFragment.show(fragmentManager!!)
        }

        binding.imageButtonEC.singleClick({
            if (binding.expand!!) {

                //collapse(binding.linearLayoutEcContainer)
                binding.linearLayoutEcContainer.collapse()
                binding.expand = false

            } else {
                //itexpand()
                binding.linearLayoutEcContainer.expand()
                binding.expand = true
            }
        })

    }

    /**
     * Created to be able to override in tests
     */
    fun navController() = findNavController()
}
