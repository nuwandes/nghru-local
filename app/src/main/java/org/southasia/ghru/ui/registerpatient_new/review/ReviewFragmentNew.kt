package org.southasia.ghru.ui.registerpatient_new.review

import android.app.DatePickerDialog
import android.graphics.BitmapFactory
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
import org.southasia.ghru.R
import org.southasia.ghru.binding.FragmentDataBindingComponent
import org.southasia.ghru.databinding.ReviewPatientFragmentNewBinding
import org.southasia.ghru.databinding.ReviewPatientFragmentSgBinding
import org.southasia.ghru.di.Injectable
import org.southasia.ghru.util.Constants
import org.southasia.ghru.util.autoCleared
import org.southasia.ghru.util.hideKeyboard
import org.southasia.ghru.util.singleClick
import org.southasia.ghru.vo.Date
import org.southasia.ghru.vo.request.ParticipantMeta
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject


class ReviewFragmentNew : Fragment(), Injectable {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    var binding by autoCleared<ReviewPatientFragmentNewBinding>()
    var dataBindingComponent: DataBindingComponent = FragmentDataBindingComponent(this)
    @Inject
    lateinit var reviewViewModelNew: ReviewViewModelNew

//    lateinit var participantMeta: ParticipantMeta

    var participantMeta: ParticipantMeta? = null

//    private var participantMeta: ParticipantMeta? = null

    val sdf = SimpleDateFormat(Constants.dataFormatOLD, Locale.US)

    var cal = Calendar.getInstance()

    private var concentPhoto: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            participantMeta = arguments?.getParcelable<ParticipantMeta>("participantMeta")!!
            concentPhoto = arguments?.getString("concentPhotoPath")!!
            //participantMeta = arguments?.getString("participantMeta")!!

        } catch (e: KotlinNullPointerException) {
            Log.d("EXCEPTION", "IS: " + e.toString())
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val dataBinding = DataBindingUtil.inflate<ReviewPatientFragmentNewBinding>(
            inflater,
            R.layout.review_patient_fragment_new,
            container,
            false
        )
        binding = dataBinding
        setHasOptionsMenu(true)
        val appCompatActivity = requireActivity() as AppCompatActivity
        appCompatActivity.setSupportActionBar(binding.detailToolbar)
        appCompatActivity.supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.member = participantMeta
        return dataBinding.root
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.setLifecycleOwner(this)
        binding.root.hideKeyboard()
        binding.viewModel = reviewViewModelNew



        //val options = BitmapFactory.Options()
       // options.inSampleSize = 8
      //  val b = BitmapFactory.decodeFile(participantMeta?.body?.identityImage, options)

        // binding.userPhoto.setImageBitmap(b)
        binding.viewModel?.gender?.postValue(participantMeta?.body?.gender)

        if (participantMeta?.body?.age?.dob != null) {
            val date = SimpleDateFormat(Constants.dataFormatOLD, Locale.US).parse(participantMeta?.body?.age?.dob!!)
            reviewViewModelNew.birthDateVal.postValue(Date(date?.year!!, date.month, date?.date))
            binding.viewModel?.birthDate?.postValue(participantMeta?.body?.age?.dob!!)
        }

        if (participantMeta?.body?.age != null) {
            binding.viewModel?.age?.postValue(participantMeta?.body?.age?.ageInYears)
        }

        reviewViewModelNew.gender.observe(this, androidx.lifecycle.Observer { gender ->
            participantMeta?.body?.gender = gender.toString().toUpperCase()
            // Log.d("Gender >>",gender.toString().toUpperCase())
        })


        binding.nextButton.singleClick {
            binding.root.hideKeyboard()
            Timber.d(participantMeta.toString())
            navController().navigate(
                R.id.action_reviewFragmentNew_to_scanBarcodeFragmentNew,
                bundleOf("participantMeta" to participantMeta)
            )
        }

        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
            reviewViewModelNew.birthYear = year
            cal.set(Calendar.YEAR, year)
            cal.set(Calendar.MONTH, monthOfYear)
            cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)

            val birthDate: Date =
                Date(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH))
            reviewViewModelNew.birthDate.postValue(sdf.format(cal.time))
            reviewViewModelNew.birthDateVal.postValue(birthDate)
            val years = Calendar.getInstance().get(Calendar.YEAR) - year
            reviewViewModelNew.age.value = years.toString()

            binding.executePendingBindings()
        }

        binding.linearLayoutDob.singleClick {
            var datepicker = DatePickerDialog(
                activity!!, R.style.datepicker, dateSetListener,
                1998,
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            )
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.YEAR, -80)
            datepicker.datePicker.minDate = calendar.timeInMillis
            datepicker.show()
        }

        binding.birthDate.singleClick {
            var datepicker = DatePickerDialog(
                activity!!, R.style.datepicker, dateSetListener,
                1998,
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            )
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.YEAR, -80)
            datepicker.datePicker.minDate = calendar.timeInMillis
            datepicker.show()
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

//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        when (item.getItemId()) {
//            android.R.id.home -> {
//                findNavController().navigate(
//                    R.id.action_global_BasicDetailsFragmentNew, bundleOf("participantMeta" to participantMeta, "concentPhotoPath" to concentPhoto)
//                )
//            }
//        }
//        return true
//    }


    /**
     * Created to be able to override in tests
     */
    fun navController() = findNavController()
}
