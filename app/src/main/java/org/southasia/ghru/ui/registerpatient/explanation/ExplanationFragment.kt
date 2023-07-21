package org.southasia.ghru.ui.registerpatient.explanation

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
import org.southasia.ghru.databinding.ExplanationFragmentBinding
import org.southasia.ghru.di.Injectable
import org.southasia.ghru.event.BitmapRxBus
import org.southasia.ghru.ui.registerpatient.explanation.reasondialog.ExplanationDialogFragment
import org.southasia.ghru.util.autoCleared
import org.southasia.ghru.util.hideKeyboard
import org.southasia.ghru.util.showToast
import org.southasia.ghru.util.singleClick
import org.southasia.ghru.vo.Meta
import org.southasia.ghru.vo.SavedBitMap
import org.southasia.ghru.vo.request.HouseholdRequest
import org.southasia.ghru.vo.request.Member
import javax.inject.Inject


class ExplanationFragment : Fragment(), Injectable {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory


    var binding by autoCleared<ExplanationFragmentBinding>()

    var dataBindingComponent: DataBindingComponent = FragmentDataBindingComponent(this)


    private var member: Member? = null

    private var householdId: String? = null

    var meta: Meta? = null

    var hoursFasted: String? = null

    var household: HouseholdRequest? = null

    private val disposables = CompositeDisposable()

    private var savedBitmap: SavedBitMap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            member = arguments?.getParcelable<Member>("member")!!
        } catch (e: KotlinNullPointerException) {

        }
        try {
            householdId = arguments?.getString("householdId")!!
            meta = arguments?.getParcelable<Meta>("meta")!!
            hoursFasted = arguments?.getString("hours_fasted")
            household = arguments?.getParcelable("household")
        } catch (e: KotlinNullPointerException) {

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
        val dataBinding = DataBindingUtil.inflate<ExplanationFragmentBinding>(
            inflater,
            R.layout.explanation_fragment,
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



        binding.buttonAcceptAndContinue.singleClick {
            if (savedBitmap != null) {

                navController().navigate(
                    R.id.action_global_BasicDetailsFragment,
                    bundleOf(
                        "member" to member,
                        "householdId" to householdId,
                        "hours_fasted" to hoursFasted,
                        "meta" to meta,
                        "household" to household,
                        "concentPhotoPath" to savedBitmap?.bitmapPath
                    )
                )

            } else {
                activity!!.showToast(getString(R.string.please_take_image))
            }
        }

        binding.saveAndExitButton.singleClick {
            val mDeleteFragmentDialog = ExplanationDialogFragment()
            mDeleteFragmentDialog.show(fragmentManager!!)
        }


        if (savedBitmap != null) {
            val rotationDegrees: Float? = savedBitmap?.bitmap?.rotationDegrees?.toFloat()
            binding.userprofile.setRotation(-rotationDegrees!!);binding.userprofile.setImageBitmap(savedBitmap?.bitmap?.bitmap)
            binding.cameraButton.visibility = View.INVISIBLE
            binding.profileView.visibility = View.VISIBLE
            binding.executePendingBindings()
        } else {
            binding.profileView.visibility = View.INVISIBLE
            binding.cameraButton.visibility = View.VISIBLE
        }

        binding.cameraButton.singleClick {
            binding.root.hideKeyboard()
            navController().navigate(R.id.action_global_cameraFragment)

        }

        binding.retakeBtn.singleClick {
            binding.root.hideKeyboard()
            savedBitmap?.bitmapPath = ""
            // participantMeta.body.identityImage = ""
            binding.userprofile.setImageBitmap(null)
            binding.cameraButton.visibility = View.VISIBLE
            binding.profileView.visibility = View.INVISIBLE
            // validateNextButton()

        }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.getItemId()) {
            android.R.id.home -> {
                return navController().navigateUp()
            }
        }
        return super.onOptionsItemSelected(item)
    }


    /**
     * Created to be able to override in tests
     */
    fun navController() = findNavController()
}
