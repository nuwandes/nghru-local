package org.southasia.ghru.ui.enumeration.householdmembers

import android.os.Bundle
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
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import io.reactivex.disposables.CompositeDisposable
import org.southasia.ghru.AppExecutors
import org.southasia.ghru.R
import org.southasia.ghru.binding.FragmentDataBindingComponent
import org.southasia.ghru.databinding.HouseholdMembersFragmentBinding
import org.southasia.ghru.di.Injectable
import org.southasia.ghru.sync.SyncHouseholdMemberResponse
import org.southasia.ghru.sync.SyncHouseholdMemberRxBus
import org.southasia.ghru.sync.SyncResponseEventType
import org.southasia.ghru.ui.enumeration.householdmembers.asigndialog.AsignDialogFragment
import org.southasia.ghru.util.autoCleared
import org.southasia.ghru.util.hideKeyboard
import org.southasia.ghru.util.singleClick
import org.southasia.ghru.vo.Meta
import org.southasia.ghru.vo.request.HouseholdRequest
import org.southasia.ghru.vo.request.Member
import timber.log.Timber
import java.util.*
import javax.inject.Inject


class HouseholdMembersFragment : Fragment(), Injectable {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var appExecutors: AppExecutors

    var binding by autoCleared<HouseholdMembersFragmentBinding>()

    var dataBindingComponent: DataBindingComponent = FragmentDataBindingComponent(this)

    private var household: HouseholdRequest? = null

    private val disposables = CompositeDisposable()

    private var adapter by autoCleared<HouseholdMembersAdapter>()

    val memberList: ArrayList<Member> = arrayListOf()

    private var meta: Meta? = null
    var countryCode: String? = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            meta = arguments?.getParcelable<Meta>("meta")!!
            val member = arguments?.getParcelable<Member>("member")!!
            memberList.add(member)
            household = arguments?.getParcelable<HouseholdRequest>("HouseholdRequest")!!

            countryCode = arguments?.getString("countryCode")

        } catch (e: KotlinNullPointerException) {
            //Crashlytics.logException(e);
        }
        //data from member add
        disposables.add(
            SyncHouseholdMemberRxBus.getInstance().toObservable()
                .subscribe({ result ->
                    handleSyncResponse(result)
                    // //L.d("Result", "household SyncHouseholdMemberRxBus ${result.member}")
                }, { error ->
                    error.printStackTrace()
                })
        )

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val dataBinding = DataBindingUtil.inflate<HouseholdMembersFragmentBinding>(
            inflater,
            R.layout.household_members_fragment,
            container,
            false
        )
        binding = dataBinding
        setHasOptionsMenu(true)
        val appCompatActivity = requireActivity() as AppCompatActivity
        appCompatActivity.setSupportActionBar(binding.detailToolbar)
        appCompatActivity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.root.hideKeyboard()
        //  household = arguments?.getParcelable<Household>("household")
        binding.household = household
        return dataBinding.root
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val adapter = HouseholdMembersAdapter(dataBindingComponent, appExecutors) { member ->
            Timber.d(member.toString())
        }
        adapter.submitList(memberList)
        this.adapter = adapter
        binding.membersList.adapter = adapter
        binding.membersList.setHasFixedSize(false);
        val linearLayoutManager = LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        binding.membersList.setLayoutManager(linearLayoutManager)
        binding.buttonNemMember.singleClick {
            val bundle = bundleOf("HouseholdRequest" to household, "meta" to meta, "countryCode" to countryCode)
            bundle.putBoolean("more", true)
            Navigation.findNavController(binding.root).navigate(R.id.action_global_addHouseHoldMember, bundle)

        }


        binding.buttonConfirmAndAsign.singleClick {
            val mAsignDialogFragment = AsignDialogFragment()
            val bundle = bundleOf(
                "HouseholdRequest" to household,
                "memberList" to memberList,
                "meta" to meta,
                "countryCode" to countryCode
            )
            mAsignDialogFragment.arguments = bundle
            mAsignDialogFragment.show(fragmentManager!!)
        }

        adapter.notifyDataSetChanged()

    }

    private fun handleSyncResponse(result: SyncHouseholdMemberResponse?) {
        if (result?.eventType === SyncResponseEventType.SUCCESS) {
            activity?.runOnUiThread {
                memberList.add(result.member)
            };

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
