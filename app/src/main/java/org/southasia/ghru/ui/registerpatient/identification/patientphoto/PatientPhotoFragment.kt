package org.southasia.ghru.ui.registerpatient.identification.patientphoto

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingComponent
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import io.reactivex.disposables.CompositeDisposable
import org.southasia.ghru.R
import org.southasia.ghru.binding.FragmentDataBindingComponent
import org.southasia.ghru.databinding.PatientPhotoFragmentBinding
import org.southasia.ghru.di.Injectable
import org.southasia.ghru.event.BitmapRxBus
import org.southasia.ghru.util.*
import org.southasia.ghru.vo.SavedBitMap
import org.southasia.ghru.vo.request.Member
import javax.inject.Inject


class PatientPhotoFragment : Fragment(), Injectable {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    var binding by autoCleared<PatientPhotoFragmentBinding>()
    var dataBindingComponent: DataBindingComponent = FragmentDataBindingComponent(this)

    private var member: Member? = null

    private val disposables = CompositeDisposable()

    private var savedBitmap: SavedBitMap? = null

    private var householdId: String? = null

    private var concentPhoto: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            concentPhoto = arguments?.getString("concentPhoto")!!
            member = arguments?.getParcelable<Member>("member")!!
            householdId = arguments?.getString("householdId")!!
        } catch (e: KotlinNullPointerException) {
            //Crashlytics.logException(e)
        }

        disposables.add(
            BitmapRxBus.getInstance().toObservable()
                .subscribe({ result ->
                    Log.d("Result", "Member ${result}")
                    savedBitmap = result
                    Log.d("Saved path", result.bitmapPath)
                }, { error ->
                    error.printStackTrace()
                })
        )
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val dataBinding = DataBindingUtil.inflate<PatientPhotoFragmentBinding>(
            inflater,
            R.layout.patient_photo_fragment,
            container,
            false
        )
        binding = dataBinding
        setHasOptionsMenu(true)
        val appCompatActivity = requireActivity() as AppCompatActivity
        appCompatActivity.setSupportActionBar(binding.detailToolbar)
        appCompatActivity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.expand = true
        return dataBinding.root
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.root.hideKeyboard()
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
            binding.executePendingBindings()
        })

        binding.member = member
        binding.nextButton.singleClick {
            if (savedBitmap != null) {
                navController().navigate(
                    R.id.action_patientPhotoFragment_to_reviewFragment,
                    bundleOf("member" to member, "householdId" to householdId, "concentPhotoPath" to concentPhoto)
                )
            }
        }
        if (savedBitmap != null) {
            // binding.profileView
            member?.profileImage = savedBitmap?.bitmapPath!!
            validateNextButton()
            binding.userprofile.setImageBitmap(savedBitmap?.bitmap?.bitmap)
            val rotationDegrees: Float? = savedBitmap?.bitmap?.rotationDegrees?.toFloat()
            binding.userprofile.setRotation(-rotationDegrees!!);
            binding.cameraButton.visibility = View.INVISIBLE
            binding.profileView.visibility = View.VISIBLE

            binding.executePendingBindings()
        } else {
            binding.profileView.visibility = View.INVISIBLE
            binding.cameraButton.visibility = View.VISIBLE
        }

        binding.cameraButton.singleClick {

            navController().navigate(R.id.action_global_cameraFragment)
        }

    }


    private fun validateNextButton() {
        if (member?.profileImage != null) {
            binding.nextButton.setTextColor(Color.parseColor("#0A1D53"))
            binding.nextButton.setDrawableRightColor("#0A1D53")
            binding.nextButton.isEnabled = true
        } else {
            binding.nextButton.setTextColor(Color.parseColor("#AED6F1"));
            binding.nextButton.setDrawableRightColor("#AED6F1")
            binding.nextButton.isEnabled = false
        }
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.getItemId()) {
            android.R.id.home -> {
                return navController().popBackStack()
            }
        }
        return super.onOptionsItemSelected(item)
    }


    /**
     * Created to be able to override in tests
     */
    fun navController() = findNavController()
}
