package org.southasia.ghru.ui.visitedhouseholds

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingComponent
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import belka.us.androidtoggleswitch.widgets.BaseToggleSwitch
import io.reactivex.disposables.CompositeDisposable
import org.southasia.ghru.AppExecutors
import org.southasia.ghru.R
import org.southasia.ghru.binding.FragmentDataBindingComponent
import org.southasia.ghru.databinding.VisitedHouseholdsFragmentBinding
import org.southasia.ghru.db.MemberDao
import org.southasia.ghru.di.Injectable
import org.southasia.ghru.sync.SyncHouseholdRequestmetaRxBus
import org.southasia.ghru.util.autoCleared
import org.southasia.ghru.util.singleClick
import org.southasia.ghru.vo.Status
import org.southasia.ghru.vo.request.HouseholdRequest
import org.southasia.ghru.vo.request.HouseholdRequestMeta
import org.southasia.ghru.vo.request.Member
import timber.log.Timber
import javax.inject.Inject


class VisitedHouseholdFragment : Fragment(), Injectable {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    @Inject
    lateinit var appExecutors: AppExecutors

    var binding by autoCleared<VisitedHouseholdsFragmentBinding>()
    var dataBindingComponent: DataBindingComponent = FragmentDataBindingComponent(this)

    @Inject
    lateinit var viewModel: VisitedHouseholdViewModel

    private var adapter by autoCleared<VisitedHouseholdRequestAdapter>()
    @Inject
    lateinit var memberDao: MemberDao

    private val disposables = CompositeDisposable()

    val list: ArrayList<HouseholdRequest> = ArrayList<HouseholdRequest>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        disposables.add(
            SyncHouseholdRequestmetaRxBus.getInstance().toObservable()
                .subscribe({ result ->
                    // Log.d("Result", "household SyncHouseholdLifecycleObserver ${result.householdRequest}")
                    activity?.runOnUiThread({
                        viewModel.setId(result.householdRequestMeta.uuid.toString())
                    })
                }, { error ->
                    error.printStackTrace()
                })
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val dataBinding = DataBindingUtil.inflate<VisitedHouseholdsFragmentBinding>(
            inflater,
            R.layout.visited_households_fragment,
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
        binding.viewModel = viewModel;

        val adapter = VisitedHouseholdRequestAdapter(dataBindingComponent, appExecutors) { household ->
            Timber.d(household.toString())
        }

        binding.buttonNewHouseHold.singleClick {

            Navigation.findNavController(binding.root)
                .navigate(R.id.action_visitedHouseholdFragment_to_registerGeolocationFragment)
            binding.executePendingBindings()
        }
        this.adapter = adapter
        binding.nghruList.adapter = adapter
        binding.nghruList.setLayoutManager(GridLayoutManager(activity, 1))

        binding.statusSwitch.setOnToggleSwitchChangeListener(object : BaseToggleSwitch.OnToggleSwitchChangeListener {

            override fun onToggleSwitchChangeListener(position: Int, isChecked: Boolean) {
                // Write your code ...
                if (position == 0) {
                    getHouseHoldsSyncPending()
                } else if (position == 1) {
                    getAllHouseHoldsSynced()
                } else if (position == 2) {
                    getAllHouseHolds()
                }
            }
        });
        // visitedHouseholdViewModel.

        viewModel.visitedHouseholdItem?.observe(this, Observer { resource ->

            if (resource?.status == Status.SUCCESS) {

                val list: ArrayList<HouseholdRequest> = ArrayList<HouseholdRequest>()
                val householdRequestMetaList: ArrayList<HouseholdRequestMeta> = ArrayList<HouseholdRequestMeta>()
                val memberList: ArrayList<Member> = ArrayList<Member>()
                resource.data?.data?.forEach { household ->
                    val householdRequestMeta = HouseholdRequestMeta(
                        meta = household.meta,
                        uuid = household.uuid!!,
                        householdRequest = household.householdRequest
                    )

                    val element = household.householdRequest!!
                    //element.syncPending = false
                    list.add(element)
                    household.memberList?.forEach { member ->
                        member.householdId = household.householdRequest.enumerationId
                        member.registed = member.studyStatus?.registered
                    }
                    memberList.addAll(household.memberList!!)
                    householdRequestMetaList.add(householdRequestMeta)
                }
                viewModel.setHouseholdRequestMetas(householdRequestMetaList)
                viewModel.setMembers(memberList)

            } else {
                // adapter.submitList(emptyList())
            }
        })

        viewModel.MembersSave?.observe(this, Observer { resource ->

            if (resource?.data != null) {
                //L.d("data saved")
            }

        })

        viewModel.screeningIdCheckAll?.observe(this, Observer { householdId ->
            //L.d(householdId.toString())
            if (householdId.status == Status.SUCCESS) {
            }
        })
        viewModel.setSampleIdAll("ss")

        viewModel.participantMetas?.observe(this, Observer { userData ->
            if (userData?.data != null) {
                // setupNavigationDrawer(userData.data)
//                user = userData.data
//                meta = Meta(collectedBy = user?.id, startTime = binding.root.getLocalTimeString())
//                meta?.registeredBy = user?.id
            }

        })

        viewModel.householdRequestMetasSave?.observe(this, Observer { resource ->

            if (resource?.data != null) {

//                val list: ArrayList<HouseholdRequest> = ArrayList<HouseholdRequest>()
//
//                resource.data?.forEach {
//                    //val householdRequestMeta = HouseholdRequestMeta(meta = it.meta, uuid = it.uuid, householdRequest = it.householdRequest)
//
//                    val element = it.householdRequest!!
//                    //element.syncPending = false
//                    list.add(element)
//                }
//                adapter.submitList(list)
//                binding.emptyLayout.visibility = View.GONE
//                binding.executePendingBindings()

                binding.statusSwitch.setCheckedTogglePosition(0)
            } else {
                adapter.submitList(emptyList())
            }

        })

        viewModel.searchItems?.observe(this, Observer { resource ->


            if (resource?.data != null) {
                val list: ArrayList<HouseholdRequest> = ArrayList<HouseholdRequest>()
                resource.data?.forEach {
                    list.add(it.householdRequest!!)
                }
                adapter.submitList(list)
                //binding.empty = false
                binding.emptyLayout.visibility = View.GONE
                binding.executePendingBindings()
            } else {
                adapter.submitList(emptyList())
            }
        })
        binding.textInputEditTextSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                //println(s)
            }

            override fun afterTextChanged(s: Editable) {
                if (s.length > 0) {
                    viewModel.setSearch(s.toString())
                } else {
                    viewModel.setId(System.currentTimeMillis().toString())
                }
            }
        })

        viewModel.screeningIdCheckAll?.observe(this, Observer { householdId ->
            //L.d(householdId.toString())
            if (householdId.status == Status.SUCCESS) {
                //  viewModel.setSampleId(sampleId)
            }
        })

        binding.statusSwitch.setCheckedTogglePosition(0)


    }


    override fun onResume() {
        super.onResume()
        if (!isNetworkAvailable()) {
            viewModel.setIdOffline("en")
        } else {
            viewModel.setSampleIdAll("s")
            viewModel.setId("en")
        }

    }

    private fun getHouseHoldsSyncPending() {
        viewModel.setSyncStatus(true)
        viewModel.getHouseholdRequest?.observe(this, Observer { resource ->

            if (resource?.status == Status.SUCCESS) {

                val list: ArrayList<HouseholdRequest> = ArrayList<HouseholdRequest>()

                resource.data?.forEach {
                    //val householdRequestMeta = HouseholdRequestMeta(meta = it.meta, uuid = it.uuid, householdRequest = it.householdRequest)

                    val element = it.householdRequest!!
                    list.add(element)

                }
                adapter.submitList(list)
                adapter.notifyDataSetChanged()
                binding.emptyLayout.visibility = View.GONE
                binding.executePendingBindings()

            } else {
                adapter.submitList(emptyList())
            }

        })
    }

    private fun getAllHouseHoldsSynced() {
        viewModel.setIdOffline("1")
        viewModel.visitedHouseholdItemRead?.observe(this, Observer { resource ->

            if (resource?.status == Status.SUCCESS) {

                val list: ArrayList<HouseholdRequest> = ArrayList<HouseholdRequest>()
                resource.data?.forEach { household ->
                    val element = household.householdRequest!!
                    if (!element.syncPending)
                        list.add(element)
                }
                adapter.submitList(list)
                adapter.notifyDataSetChanged()
                binding.emptyLayout.visibility = View.GONE
                binding.executePendingBindings()

            } else {
                adapter.submitList(emptyList())
            }

        })
    }

    private fun getAllHouseHolds() {
        viewModel.setIdOffline("1")
        viewModel.visitedHouseholdItemRead?.observe(this, Observer { resource ->

            if (resource?.status == Status.SUCCESS) {

                val list: ArrayList<HouseholdRequest> = ArrayList<HouseholdRequest>()
                resource.data?.forEach { household ->
                    val element = household.householdRequest!!
                    list.add(element)
                }
                adapter.submitList(list)
                adapter.notifyDataSetChanged()
                binding.emptyLayout.visibility = View.GONE
                binding.executePendingBindings()

            } else {
                adapter.submitList(emptyList())
            }

        })
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = activity?.getSystemService(Context.CONNECTIVITY_SERVICE)
        return if (connectivityManager is ConnectivityManager) {
            val networkInfo: NetworkInfo? = connectivityManager.activeNetworkInfo
            networkInfo?.isConnected ?: false
        } else false
    }

    /**
     * Created to be able to override in tests
     */
    fun navController() = findNavController()
}
