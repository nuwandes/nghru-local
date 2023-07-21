package org.southasia.ghru.ui.samplemanagement.storage.samplelist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingComponent
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import org.southasia.ghru.AppExecutors
import org.southasia.ghru.R
import org.southasia.ghru.binding.FragmentDataBindingComponent
import org.southasia.ghru.databinding.StorageSampleListFragmentBinding
import org.southasia.ghru.di.Injectable
import org.southasia.ghru.util.autoCleared
import org.southasia.ghru.util.singleClick
import org.southasia.ghru.vo.Status
import timber.log.Timber
import javax.inject.Inject


class PendingSampleListFragment : Fragment(), Injectable ,  SwipeRefreshLayout.OnRefreshListener {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    @Inject
    lateinit var appExecutors: AppExecutors

    var binding by autoCleared<StorageSampleListFragmentBinding>()
    var dataBindingComponent: DataBindingComponent = FragmentDataBindingComponent(this)

    @Inject
    lateinit var visitedHouseholdViewModel: PendingSampleListViewModel

    private var adapter by autoCleared<PendingSampleListAdapter>()

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val dataBinding = DataBindingUtil.inflate<StorageSampleListFragmentBinding>(
                inflater,
                R.layout.storage_sample_list_fragment,
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
        binding.viewModel = visitedHouseholdViewModel;

        val adapter = PendingSampleListAdapter(dataBindingComponent, appExecutors) { household ->
            Timber.d(household.toString())
        }

        binding.buttonNewHouseHold.singleClick {
            findNavController().navigate(R.id.action_PendingSampleListFragment_to_scanbarCodeFragment)
        }
        this.adapter = adapter
        binding.nghruList.adapter = adapter
        binding.nghruList.setLayoutManager(GridLayoutManager(activity, 1))
        visitedHouseholdViewModel.setId("en")

        visitedHouseholdViewModel.pendingSampleListOnline?.observe(this, Observer { resource ->
            binding.swiperefresh.isRefreshing = false
            if(resource.status == Status.SUCCESS) {
                if (resource?.data != null) {
                    adapter.submitList(resource.data)
                    //binding.empty = false
                    binding.emptyLayout.visibility = View.GONE
                    val count = resource.data.size
                    binding.textView9.setText("Pending samples list ($count)")
                    binding.executePendingBindings()
                } else {
                    binding.textView9.setText("No pending samples)")
                    adapter.submitList(emptyList())
                    //binding.empty = true
                    binding.emptyLayout.visibility = View.VISIBLE
                    binding.executePendingBindings()
                }
            }
        })
        binding.swiperefresh.setOnRefreshListener(this)

    }

    override fun onRefresh() {
        visitedHouseholdViewModel.retry()
    }
    /**
     * Created to be able to override in tests
     */
    fun navController() = findNavController()
}
