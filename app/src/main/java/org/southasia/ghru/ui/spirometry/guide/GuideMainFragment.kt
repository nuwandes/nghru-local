package org.southasia.ghru.ui.spirometry.guide

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.MediaController
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingComponent
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import org.southasia.ghru.R
import org.southasia.ghru.binding.FragmentDataBindingComponent
import org.southasia.ghru.databinding.SpirometryGuideMainFragmentBinding
import org.southasia.ghru.di.Injectable
import org.southasia.ghru.util.*
import org.southasia.ghru.vo.request.ParticipantRequest
import javax.inject.Inject

class GuideMainFragment : Fragment(), Injectable {

    lateinit var viewModelFactory: ViewModelProvider.Factory

    var binding by autoCleared<SpirometryGuideMainFragmentBinding>()
    var dataBindingComponent: DataBindingComponent = FragmentDataBindingComponent(this)
    @Inject
    lateinit var guideMainViewModel: GuideMainViewModel

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
        val dataBinding = DataBindingUtil.inflate<SpirometryGuideMainFragmentBinding>(
            inflater,
            R.layout.spirometry_guide_main_fragment,
            container,
            false
        )
        binding = dataBinding
        setHasOptionsMenu(true)
        val appCompatActivity = requireActivity() as AppCompatActivity
        appCompatActivity.setSupportActionBar(binding.detailToolbar)
        appCompatActivity.supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.expandProcedure = false
        binding.linearLayoutPrepContainer.collapse()
        binding.root.hideKeyboard()

        return dataBinding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.setLifecycleOwner(this)
        binding.viewModel = guideMainViewModel
        binding.participant = participant

        binding.vidoeView.setVideoPath("android.resource://" + activity?.getPackageName() + "/" + R.raw.nuvoair);
//        mediaController = new MediaController(TestActivity.this);
//        mediaController.setAnchorView(videoView);
//        videoView.setMediaController(mediaController);
//        videoView.start();
        var mediaController = MediaController(activity)
        mediaController.setAnchorView(binding.vidoeView);
        binding.vidoeView.setMediaController(mediaController);
        binding.vidoeView.singleClick {
            binding.vidoeView.start();
        }
        binding.previousButton.setOnClickListener {
            navController().popBackStack()
        }

        binding.nextButton.singleClick {

            if (validateNextButton()) {
                navController().navigate(
                    R.id.action_guideMainFragment_to_TestsFragment,
                    bundleOf("participant" to participant)
                )
            }
        }

        binding.checkbox.setOnCheckedChangeListener { _, isChecked ->

            guideMainViewModel.setHasExplained(isChecked)
            validateNextButton()
        }


        binding.prepEC.setOnClickListener {
            if (binding.expandProcedure!!) {

                //collapse(binding.linearLayoutEcContainer)
                binding.linearLayoutPrepContainer.collapse()
                binding.expandProcedure = false

            } else {
                //itexpand()
                binding.linearLayoutPrepContainer.expand()
                binding.expandProcedure = true
            }
            binding.executePendingBindings()
        }

    }

    private fun validateNextButton(): Boolean {

        if (guideMainViewModel.hasExplained.value != null && guideMainViewModel.hasExplained.value!!) {
            binding.checkLayout.background = resources.getDrawable(R.drawable.ic_base_check, null)
            binding.textViewError.visibility = View.GONE
            return true
        } else {

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