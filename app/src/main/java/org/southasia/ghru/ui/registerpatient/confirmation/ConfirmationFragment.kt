package org.southasia.ghru.ui.registerpatient.confirmation

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingComponent
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.birbit.android.jobqueue.JobManager
import com.crashlytics.android.Crashlytics
import com.google.gson.GsonBuilder
import org.southasia.ghru.R
import org.southasia.ghru.binding.FragmentDataBindingComponent
import org.southasia.ghru.databinding.ConfirmationFragmentBinding
import org.southasia.ghru.di.Injectable
import org.southasia.ghru.jobs.SyncImageConcentUploadJob
import org.southasia.ghru.jobs.SyncImageUploadJob
import org.southasia.ghru.jobs.SyncparticipantMetaJob
import org.southasia.ghru.ui.registerpatient.confirmation.completed.CompletedDialogFragment
import org.southasia.ghru.util.autoCleared
import org.southasia.ghru.util.getLocalTimeString
import org.southasia.ghru.util.hideKeyboard
import org.southasia.ghru.util.singleClick
import org.southasia.ghru.vo.Status
import org.southasia.ghru.vo.request.ParticipantMeta
import org.southasia.ghru.vo.request.ParticipantRequest
import java.io.File
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject


class ConfirmationFragment : Fragment(), Injectable {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory


    var binding by autoCleared<ConfirmationFragmentBinding>()
    var dataBindingComponent: DataBindingComponent = FragmentDataBindingComponent(this)
    @Inject
    lateinit var confirmationViewModel: ConfirmationViewModel

    @Inject
    lateinit var jobManager: JobManager

    private var participantMeta: ParticipantMeta? = null
    private var concentPhoto: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            concentPhoto = arguments?.getString("concentPhotoPath")!!
            participantMeta = arguments?.getParcelable<ParticipantMeta>("participantMeta")!!
        } catch (e: KotlinNullPointerException) {

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val dataBinding = DataBindingUtil.inflate<ConfirmationFragmentBinding>(
            inflater,
            R.layout.confirmation_fragment,
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


    private lateinit var participantRequest: ParticipantRequest

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        //participantMeta?.body?.gender?.toLowerCase()

        val memberId = if (participantMeta?.body?.memberId != null) participantMeta?.body?.memberId!! else ""
        val householdIdX =
            if (participantMeta?.body?.enumerationId != null) participantMeta?.body?.enumerationId!! else ""
        binding.participantMeta = participantMeta


        binding.screeningId = participantMeta?.body?.screeningId!!

        val gender: String = participantMeta?.body?.gender.toString().toLowerCase()

        val endTime: String = convertTimeTo24Hours()
        val endDate: String = getDate()
        val endDateTime:String = endDate + " " + endTime

        participantMeta?.meta?.endTime = endDateTime
        participantMeta?.body?.gender = gender

        confirmationViewModel.participantRequestSaveLocal?.observe(this, Observer { members ->
            if (members?.status == Status.SUCCESS) {

                if (!isNetworkAvailable()) {

                    jobManager.addJobInBackground(SyncparticipantMetaJob(participantMeta!!))
                    if (!members.data?.identityImage.isNullOrEmpty()) {
                        jobManager.addJobInBackground(SyncImageUploadJob(participantRequest = members.data!!))
                    }
                    jobManager.addJobInBackground(
                        SyncImageConcentUploadJob(
                            concentPhoto = concentPhoto,
                            screeningId = participantMeta?.body?.screeningId
                        )
                    )
                    val completedDialogFragment = CompletedDialogFragment()
                    completedDialogFragment.show(fragmentManager!!)
                } else {

                    //val gender: String = participantMeta?.body?.gender.toString().toLowerCase()

                    val eTime: String = convertTimeTo24Hours()
                    val eDate: String = getDate()
                    val eDateTime:String = eDate + " " + eTime

                    participantMeta?.meta?.endTime = eDateTime
                    participantMeta?.body?.gender = gender
                    confirmationViewModel.setParticipantMetaRemote(participantMeta!!)
                }
            }
        })

        confirmationViewModel.participantMemberupdateLocal?.observe(this, Observer { members ->
            if (members?.status == Status.SUCCESS) {
                ////L.d(members.toString())
            }
        })




        confirmationViewModel.participantMetaSaveRemote?.observe(this, Observer { participant ->

            if (participant?.status == Status.SUCCESS) {
                jobManager.addJobInBackground(
                    SyncImageConcentUploadJob(
                        concentPhoto = concentPhoto,
                        screeningId = participantMeta?.body?.screeningId
                    )
                )
                binding.progressBar.visibility = View.GONE
                binding.confirmButton.visibility = View.VISIBLE
                val completedDialogFragment = CompletedDialogFragment()
                completedDialogFragment.show(fragmentManager!!)
            } else if (participant?.status == Status.ERROR) {
                //val gson = GsonBuilder().setPrettyPrinting().create()
                //val dataS = gson.toJson(participantMeta)
               // Crashlytics.setString("participantMeta", dataS)
                Crashlytics.setString("participantRequest", participantRequest.toString())
                Crashlytics.setString("participantMeta", participantMeta.toString())
                Crashlytics.logException(Exception("participantMetaSaveRemote " + participant.message?.error.toString()))
                Crashlytics.logException(Exception("participant.message?.data?.message " + participant.message?.data?.message))
                binding.progressBar.visibility = View.GONE
                binding.textViewError.setText(participant.message?.data?.message)
                binding.textViewError.visibility = View.VISIBLE
                binding.executePendingBindings()
            }

        })




        confirmationViewModel.uploadConcent?.observe(this, Observer { upload ->
            //println(upload)
            if (upload?.status == Status.SUCCESS) {
                Crashlytics.logException(Exception("uploadConcent sucess"))
                if (!participantRequest.identityImage.isNullOrEmpty()) {

                    confirmationViewModel.setUploadId(participantMeta?.body?.identityImage!!, participantRequest)
                } else {
                    binding.progressBar.visibility = View.GONE
                    binding.confirmButton.visibility = View.VISIBLE
                    val completedDialogFragment = CompletedDialogFragment()
                    completedDialogFragment.show(fragmentManager!!)
                }

            } else if (upload?.status == Status.ERROR) {
                Crashlytics.logException(Exception("uploadConcent" + upload.message.toString()))
                binding.progressBar.visibility = View.GONE
                binding.textViewError.setText(upload.message?.message)
                binding.textViewError.visibility = View.VISIBLE
                binding.executePendingBindings()

            }
        })



        confirmationViewModel.uploadIdCardRemote?.observe(this, Observer { upload ->
            //println(upload)
            if (upload?.status == Status.SUCCESS) {
//                val myFile = File(participantMeta?.body?.identityImage!!)
//                if (myFile.exists())
//                    myFile.delete()
//
//                val myFilex = File(concentPhoto)
//                if (myFilex.exists())
//                    myFilex.delete()
                Crashlytics.logException(Exception("uploadIdCardRemote success"))
                binding.progressBar.visibility = View.GONE
                binding.confirmButton.visibility = View.VISIBLE
                val completedDialogFragment = CompletedDialogFragment()
                completedDialogFragment.show(fragmentManager!!)
            } else if (upload?.status == Status.ERROR) {
                Crashlytics.logException(Exception("uploadConcent" + upload.message.toString()))
                binding.progressBar.visibility = View.GONE
                binding.textViewError.setText(upload.message?.message)
                binding.textViewError.visibility = View.VISIBLE
                binding.executePendingBindings()

            }
        })

//        confirmationViewModel.uploadProfileRemote?.observe(this, Observer { upload ->
//            //println(upload)
//            if (upload?.status == Status.SUCCESS) {
//                // confirmationViewModel.setUploadId(member!!, participantRequest)
//            } else if (upload?.status == Status.ERROR) {
//                binding.progressBar.visibility = View.GONE
//                binding.textViewError.setText(upload.message?.message)
//                binding.textViewError.visibility = View.VISIBLE
//                binding.executePendingBindings()
//            }
//        })

        binding.confirmButton.singleClick {
            binding.progressBar.visibility = View.VISIBLE
            binding.confirmButton.visibility = View.GONE
            participantMeta?.body?.comment = binding.comment.text.toString()

            participantRequest = ParticipantRequest(
                firstName = participantMeta?.body?.firstName!!,
                lastName = participantMeta?.body?.lastName!!,
                age = participantMeta?.body?.age!!,
                gender = participantMeta?.body?.gender!!.toString().toLowerCase(),
                idNumber = participantMeta?.body?.idNumber!!,
                fatherName = "fathers name",
                idType = "NID",
                screeningId = participantMeta?.body?.screeningId!!,
                householdId = householdIdX,
                memberId = memberId,
                profileImage = "",
                identityImage = participantMeta?.body?.identityImage,
                contactNumber = participantMeta?.body?.contactDetails?.phoneNumberPreferred!!,
                comment = participantMeta?.body?.comment

            )

            val sTime: String = convertTimeTo24Hours()
            val sDate: String = getDate()
            val sDateTime:String = sDate + " " + sTime

            participantRequest.createdDateTime = sDateTime
            if (isNetworkAvailable()) {
                participantRequest.syncPending = false
            } else {
                participantRequest.syncPending = true
            }
            confirmationViewModel.setParticipantRequestLocal(participantRequest)
        }

    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = activity?.getSystemService(Context.CONNECTIVITY_SERVICE)
        return if (connectivityManager is ConnectivityManager) {
            val networkInfo: NetworkInfo? = connectivityManager.activeNetworkInfo
            networkInfo?.isConnected ?: false
        } else false
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.getItemId()) {
            android.R.id.home -> {
                return navController().popBackStack()

            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun convertTimeTo24Hours(): String
    {
        val now: Calendar = Calendar.getInstance()
        val inputFormat: DateFormat = SimpleDateFormat("MMM DD, yyyy HH:mm:ss")
        val outputformat: DateFormat = SimpleDateFormat("HH:mm")
        val date: Date
        val output: String
        try{
            date= inputFormat.parse(now.time.toLocaleString())
            output = outputformat.format(date)
            return output
        }catch(p: ParseException){
            return ""
        }
    }

    private fun getDate(): String
    {
        val inputFormat: DateFormat = SimpleDateFormat("yyyy-MM-dd hh:mm")
        val outputformat: DateFormat = SimpleDateFormat("yyyy-MM-dd")
        val date: Date
        val output: String
        try{
            date= inputFormat.parse(binding.root.getLocalTimeString())
            output = outputformat.format(date)

            return output
        }catch(p: ParseException){
            return ""
        }
    }

    /**
     * Created to be able to override in tests
     */
    fun navController() = findNavController()
}
