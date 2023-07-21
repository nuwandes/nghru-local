package org.southasia.ghru.ui.enumeration.createhousehold

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingComponent
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import br.com.ilhasoft.support.validation.Validator
import com.birbit.android.jobqueue.JobManager
import com.crashlytics.android.Crashlytics
import org.southasia.ghru.EnumerationActivity
import org.southasia.ghru.R
import org.southasia.ghru.binding.FragmentDataBindingComponent
import org.southasia.ghru.databinding.CreateHouseholdFragmentBinding
import org.southasia.ghru.di.Injectable
import org.southasia.ghru.jobs.SyncHouseholdMemberJob
import org.southasia.ghru.jobs.SyncHouseholdRequestMetaJob
import org.southasia.ghru.ui.enumeration.completed.CompletedDialogFragment
import org.southasia.ghru.util.Constants
import org.southasia.ghru.util.autoCleared
import org.southasia.ghru.util.getLocalTimeString
import org.southasia.ghru.util.singleClick
import org.southasia.ghru.vo.Date
import org.southasia.ghru.vo.Meta
import org.southasia.ghru.vo.Status
import org.southasia.ghru.vo.request.HouseholdRequest
import org.southasia.ghru.vo.request.HouseholdRequestMeta
import org.southasia.ghru.vo.request.Member
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject


class CreateHouseholdFragment : Fragment(), Injectable {


    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var validator: Validator


    var binding by autoCleared<CreateHouseholdFragmentBinding>()
    var dataBindingComponent: DataBindingComponent = FragmentDataBindingComponent(this)
    @Inject
    lateinit var enumerationViewModel: CreateHouseholdViewModel

    @Inject
    lateinit var jobManager: JobManager

    private var memberList: ArrayList<Member>? = null

    private var household: HouseholdRequest? = null

    private var householdRequestMeta: HouseholdRequestMeta? = null

    private lateinit var meta: Meta

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            household = arguments?.getParcelable<HouseholdRequest>("HouseholdRequest")
            memberList = arguments?.getParcelableArrayList<Member>("memberList")
            meta = arguments?.getParcelable<Meta>("meta")!!

        } catch (e: KotlinNullPointerException) {
            //Crashlytics.logException(e);
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val dataBinding = DataBindingUtil.inflate<CreateHouseholdFragmentBinding>(
            inflater,
            R.layout.create_household_fragment,
            container,
            false
        )
        binding = dataBinding
        validator = Validator(binding)
        setHasOptionsMenu(true)
        val appCompatActivity = requireActivity() as AppCompatActivity
        appCompatActivity.setSupportActionBar(binding.detailToolbar)
        appCompatActivity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.household = household
        return dataBinding.root
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        enumerationViewModel.memberLocal?.observe(this, Observer { members ->
            if (members?.status == Status.SUCCESS) {
                //Crashlytics.log(200, "SUCCESS", "member SUCCESS");
                //Crashlytics.log("member SUCCESS");
                if (!isNetworkAvailable()) {
                    jobManager.addJobInBackground(SyncHouseholdMemberJob(household!!, members.data!!))
                    //Navigation.findNavController(binding.root).navigate(R.id.action_CreateHouseholdFragment_to_visitedHouseholdFragment)
                    val completedDialogFragment = CompletedDialogFragment()
                    completedDialogFragment.arguments = bundleOf("is_cancel" to false)
                    completedDialogFragment.show(fragmentManager!!)
                } else {
                    enumerationViewModel.setMemberSyncedRemote(
                        members = memberList!!,
                        household = householdRequestMeta!!
                    )
                }

            }
        })


        enumerationViewModel.householdRequestSyncRemote?.observe(this, Observer { householdRequest ->
            if (householdRequest?.status == Status.SUCCESS) {
                enumerationViewModel.setMemberSyncedLocal(memberList, householdRequestMeta!!)
            } else if (householdRequest?.status == Status.ERROR) {
                Crashlytics.setString("householdRequestMeta", householdRequestMeta.toString())
                Crashlytics.logException(Exception(householdRequest.toString()))
                binding.textViewError.visibility = View.VISIBLE
                binding.textViewError.setText(householdRequest.message?.message)
                binding.progressBar.visibility = View.GONE
                enumerationViewModel.householdRequestLocal?.removeObservers(this)
                enumerationViewModel.setDelete(householdRequestMeta!!)
            }
        })

        enumerationViewModel.householdRequestDelete?.observe(this, Observer { householdRequest ->
            //L.d("delete " + householdRequest?.status)
        })



        enumerationViewModel.memberSyncRemote?.observe(this, Observer { memberSyncRemote ->
            if (memberSyncRemote?.status == Status.SUCCESS) {
                val completedDialogFragment = CompletedDialogFragment()
                completedDialogFragment.arguments = bundleOf("is_cancel" to false)
                completedDialogFragment.show(fragmentManager!!)
            } else if (memberSyncRemote?.status == Status.ERROR) {
                Crashlytics.setString("householdRequestMeta", householdRequestMeta.toString())
                Crashlytics.setString("householdRequestMeta", memberList.toString())
                Crashlytics.logException(Exception("memberSyncRemote " + memberSyncRemote.message.toString()))
                //Crashlytics.logException(Exception(memberSyncRemote.toString()))
                binding.textViewError.visibility = View.VISIBLE
                binding.textViewError.setText(memberSyncRemote.message?.message)
                binding.progressBar.visibility = View.GONE
            }
        })


        enumerationViewModel.householdRequestLocal?.observe(this, Observer { householdRequest ->

            if (householdRequest?.status == Status.SUCCESS) {
                //Crashlytics.log(100, "SUCCESS", "householdRequest SUCCESS");
                Crashlytics.log("householdRequest SUCCESS");
                if (householdRequest.data != null) {
                    householdRequestMeta = householdRequest.data
                    householdRequestMeta?.id = householdRequest.data.id
                    memberList?.forEachIndexed { index, it ->
                        if (householdRequestMeta != null) {
                            it.householdId = householdRequestMeta?.householdRequest?.enumerationId!!
                            val myFormat = Constants.dataFormat // mention the format you need
                            val sdf = SimpleDateFormat(myFormat, Locale.US)
                            val date = sdf.parse(it.dateOfBirth)
                            val cal = Calendar.getInstance();
                            cal.setTime(date);
                            it.memberId = (index + 1).toString()
                            it.uuid = UUID.randomUUID().toString();
                            it.birthDate = Date(
                                day = cal.get(Calendar.DAY_OF_MONTH),
                                month = cal.get(Calendar.MONTH) + 1,
                                year = cal.get(Calendar.YEAR)
                            )
                            if (this.isNetworkAvailable()) {
                                it.syncPending = false
                            } else {
                                it.syncPending = true
                            }
                        }
                    }
                    if (!isNetworkAvailable()) {
                        enumerationViewModel.setMemberSyncedLocal(memberList, householdRequestMeta!!)
                        jobManager.addJobInBackground(SyncHouseholdRequestMetaJob(householdRequest.data))
                    } else {
                        enumerationViewModel.setHouseholdRequestSyncRemote(householdRequestMeta!!)
                    }
                    binding.executePendingBindings()
                }
            }

        })

        val eTime: String = convertTimeTo24Hours()
        val eDate: String = getDate()
        val eDateTime:String = eDate + " " + eTime

        binding.buttonFinish.singleClick {
            household?.syncPending = true
            binding.progressBar.visibility = View.VISIBLE
            binding.buttonFinish.visibility = View.GONE
            meta.endTime = eDateTime
            val x = HouseholdRequestMeta(meta = meta, householdRequest = household!!)
            if (this.isNetworkAvailable()) {
                household?.syncPending = false
                x.syncPending = false
            } else {
                household?.syncPending = true
                x.syncPending = true

            }
            enumerationViewModel.setHouseholdRequestLocal(x)
        }
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = activity?.getSystemService(Context.CONNECTIVITY_SERVICE)
        return if (connectivityManager is ConnectivityManager) {
            val networkInfo: NetworkInfo? = connectivityManager.activeNetworkInfo
            networkInfo?.isConnected ?: false
        } else false
    }

    private fun convertTimeTo24Hours(): String
    {
        val now: Calendar = Calendar.getInstance()
        val inputFormat: DateFormat = SimpleDateFormat("MMM DD, yyyy HH:mm:ss")
        val outputformat: DateFormat = SimpleDateFormat("HH:mm")
        val date: java.util.Date
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
        val date: java.util.Date
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
