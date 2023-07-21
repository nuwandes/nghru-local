package org.southasia.ghru.ui.ecg.guide.main


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
import org.southasia.ghru.databinding.GuideMainFragmentBinding
import org.southasia.ghru.di.Injectable
import org.southasia.ghru.ui.ecg.guide.ElectrodeAdapter
import org.southasia.ghru.ui.ecg.guide.GuideAdapter
import org.southasia.ghru.ui.ecg.guide.PrepAdapter
import org.southasia.ghru.util.*
import org.southasia.ghru.vo.request.ParticipantRequest
import javax.inject.Inject

class GuideMainFragment : Fragment(), Injectable {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    var binding by autoCleared<GuideMainFragmentBinding>()
    var dataBindingComponent: DataBindingComponent = FragmentDataBindingComponent(this)
    @Inject
    lateinit var guideMainViewModel: GuideMainViewModel

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
        val dataBinding = DataBindingUtil.inflate<GuideMainFragmentBinding>(
            inflater,
            R.layout.guide_main_fragment,
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
        binding.viewModel = guideMainViewModel
        binding.viewPagerPro.adapter = pagerAdapter
        binding.dotsIndicatorPro.setViewPager(binding.viewPagerPro)
        binding.viewPagerPrep.adapter = prepAdapter
        binding.dotsIndicatorPrep.setViewPager(binding.viewPagerPrep)
        binding.viewPagerElectro.adapter = electrodeAdapter
        binding.dotsIndicatorElectro.setViewPager(binding.viewPagerElectro)
        binding.participant = participant

        // guideMainViewModel.hasExplained.observe(this, Observer { validationError -> //validateNextButton() })


        binding.previousButton.singleClick {
            navController().popBackStack()
        }

        binding.nextButton.singleClick {

            if (validateNextButton()) {
                navController().navigate(
                    R.id.action_guideMainFragment_to_InputFragment,
                    bundleOf("participant" to participant)
                )
            }
        }

        binding.checkbox.setOnCheckedChangeListener { _, isChecked ->

            guideMainViewModel.setHasExplained(isChecked)
            validateNextButton()
        }


        binding.prepEC.singleClick {
            if (binding.expandPreparation!!) {

                //collapse(binding.linearLayoutEcContainer)
                binding.linearLayoutPrepContainer.collapse()
                binding.expandPreparation = false

            } else {
                //itexpand()
                binding.linearLayoutPrepContainer.expand()
                binding.expandPreparation = true
            }
            binding.executePendingBindings()

        }

        binding.electrodeEC.singleClick {
            if (binding.expandElectrode!!) {

                //collapse(binding.linearLayoutEcContainer)
                binding.linearLayoutElectroContainer.collapse()
                binding.expandElectrode = false

            } else {
                //itexpand()
                binding.linearLayoutElectroContainer.expand()
                binding.expandElectrode = true
            }
            binding.executePendingBindings()
        }

        binding.procedureEC.singleClick {
            if (binding.expandProcedure!!) {

                //collapse(binding.linearLayoutEcContainer)
                binding.linearLayoutProContainer.collapse()
                binding.expandProcedure = false

            } else {
                //itexpand()
                binding.linearLayoutProContainer.expand()
                binding.expandProcedure = true
            }

            binding.executePendingBindings()

        }

    }


    private fun validateNextButton(): Boolean {

        if (guideMainViewModel.hasExplained.value != null && guideMainViewModel.hasExplained.value!!) {
            /*binding.nextButton.setTextColor(Color.parseColor("#0A1D53"))
            binding.nextButton.setDrawableRightColor("#0A1D53")
            binding.nextButton.isEnabled = true*/
            binding.checkLayout.background = resources.getDrawable(R.drawable.ic_base_check, null)
            binding.textViewError.visibility = View.GONE
            return true
        } else {

            /*binding.nextButton.setTextColor(Color.parseColor("#AED6F1"));
            binding.nextButton.setDrawableRightColor("#AED6F1")
            binding.nextButton.isEnabled = false*/
            binding.checkLayout.background = resources.getDrawable(R.drawable.ic_base_check_error, null)
            binding.textViewError.visibility = View.VISIBLE
            return false

        }

    }

    /**
     * Created to be able to override in tests
     */
    fun navController() = findNavController()
}
