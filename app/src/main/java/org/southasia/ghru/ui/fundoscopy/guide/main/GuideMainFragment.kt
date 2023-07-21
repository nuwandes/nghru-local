package org.southasia.ghru.ui.fundoscopy.guide.main


import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ScrollView
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingComponent
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import org.southasia.ghru.R
import org.southasia.ghru.binding.FragmentDataBindingComponent
import org.southasia.ghru.databinding.FundosGuideMainFragmentBinding
import org.southasia.ghru.di.Injectable
import org.southasia.ghru.ui.fundoscopy.guide.ElectrodeAdapter
import org.southasia.ghru.ui.fundoscopy.guide.GuideAdapter
import org.southasia.ghru.ui.fundoscopy.guide.PrepAdapter
import org.southasia.ghru.util.*
import org.southasia.ghru.vo.request.ParticipantRequest
import javax.inject.Inject

class GuideMainFragment : Fragment(), Injectable {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory


    var binding by autoCleared<FundosGuideMainFragmentBinding>()
    var dataBindingComponent: DataBindingComponent = FragmentDataBindingComponent(this)
    @Inject
    lateinit var verifyIDViewModel: GuideMainViewModel

    private lateinit var pagerAdapter: GuideAdapter

    private lateinit var electrodeAdapter: ElectrodeAdapter

    private lateinit var prepAdapter: PrepAdapter


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
        val dataBinding = DataBindingUtil.inflate<FundosGuideMainFragmentBinding>(
            inflater,
            R.layout.fundos_guide_main_fragment,
            container,
            false
        )
        binding = dataBinding
        setHasOptionsMenu(true)
        val appCompatActivity = requireActivity() as AppCompatActivity
        appCompatActivity.setSupportActionBar(binding.detailToolbar)
        appCompatActivity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.expandElectrode = false
        binding.expandPreparation = false
        binding.expandProcedure = false
        binding.linearLayoutProContainer.collapse()
        binding.linearLayoutPrepContainer.collapse()
        binding.linearLayoutElectroContainer.collapse()
        // val info = arrayOf("a", "b", "c")
        pagerAdapter = GuideAdapter(childFragmentManager)
        electrodeAdapter = ElectrodeAdapter(childFragmentManager)
        prepAdapter = PrepAdapter(childFragmentManager)
        binding.root.hideKeyboard()
        return dataBinding.root
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.setLifecycleOwner(this)
        binding.viewModel = verifyIDViewModel
        binding.viewPagerPro.adapter = pagerAdapter
        binding.dotsIndicatorPro.setViewPager(binding.viewPagerPro)
        binding.viewPagerPrep.adapter = prepAdapter
        binding.dotsIndicatorPrep.setViewPager(binding.viewPagerPrep)
        binding.viewPagerElectro.adapter = electrodeAdapter
        binding.dotsIndicatorElectro.setViewPager(binding.viewPagerElectro)
        binding.participant = participant

        binding.previousButton.singleClick {
            navController().popBackStack()
        }

        binding.nextButton.singleClick {

            if (validateNext()) {
                val bundle = Bundle()
                bundle.putParcelable("participant", participant)
                navController().navigate(R.id.action_guideMainFragment_to_readingFragment, bundle)
            }
        }


        /* binding.textViewSkip.singleClick {
             //
             val skipDialogFragment = SkipDialogFragment()
             val bundle = Bundle()
             //bundle.putParcelableArrayList("memberList", ArrayList(membersResource.data?.data))
             skipDialogFragment.arguments = bundle
             skipDialogFragment.show(fragmentManager!!)
         }*/

        binding.prepEC.singleClick {
            if (binding.expandPreparation!!) {
                binding.linearLayoutPrepContainer.collapse()
                binding.expandPreparation = false

            } else {
                binding.linearLayoutPrepContainer.expand()
                binding.expandPreparation = true
            }
            binding.executePendingBindings()

        }

        binding.electrodeEC.singleClick {
            if (binding.expandElectrode!!) {
                binding.linearLayoutElectroContainer.collapse()
                binding.expandElectrode = false

            } else {
                binding.linearLayoutElectroContainer.expand()
                binding.expandElectrode = true
            }
            binding.executePendingBindings()
        }

        binding.procedureEC.singleClick {
            if (binding.expandProcedure!!) {
                binding.linearLayoutProContainer.collapse()
                binding.expandProcedure = false

            } else {
                binding.linearLayoutProContainer.expand()
                binding.expandProcedure = true
            }

            binding.executePendingBindings()

        }

        binding.checkbox.setOnCheckedChangeListener { _, isChecked ->

            verifyIDViewModel.setHasExplained(isChecked)
            validateNext()
        }


    }


    private fun validateNext(): Boolean {
        if (verifyIDViewModel.isChecked == true) {
            binding.textViewError.visibility = View.GONE
            verifyIDViewModel.validationError.postValue(false)
            binding.checkLayout.background = resources.getDrawable(R.drawable.ic_base_check, null)

            return true
        } else {
            verifyIDViewModel.validationError.postValue(true)
            scrolToUp()
            binding.executePendingBindings()
            binding.textViewError.visibility = View.VISIBLE
            binding.checkLayout.background = resources.getDrawable(R.drawable.ic_base_check_error, null)

            return false
        }

    }

    private fun scrolToUp() {
        Handler().postDelayed({
            binding.scrollView.fullScroll(ScrollView.FOCUS_UP);
        }, 600)
    }

    /**
     * Created to be able to override in tests
     */
    fun navController() = findNavController()
}
