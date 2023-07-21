package org.southasia.ghru.ui.samplemanagement

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingComponent
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import org.southasia.ghru.*
import org.southasia.ghru.binding.FragmentDataBindingComponent
import org.southasia.ghru.databinding.SamplemangementFragmentBinding
import org.southasia.ghru.di.Injectable
import org.southasia.ghru.network.ConnectivityReceiver
import org.southasia.ghru.util.autoCleared
import org.southasia.ghru.util.setTitleColor
import timber.log.Timber
import javax.inject.Inject


class SampleMangementFragment : Fragment(), Injectable {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    @Inject
    lateinit var appExecutors: AppExecutors

    var binding by autoCleared<SamplemangementFragmentBinding>()
    var dataBindingComponent: DataBindingComponent = FragmentDataBindingComponent(this)

    @Inject
    lateinit var samplemangementViewModel: SampleMangementViewModel

    private var adapter by autoCleared<SampleMangementAdapter>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val dataBinding = DataBindingUtil.inflate<SamplemangementFragmentBinding>(
            inflater,
            R.layout.samplemangement_fragment,
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_setting -> {
                val intent = Intent(activity, SettingActivity::class.java)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.homeViewModel = samplemangementViewModel;

        val adapter = SampleMangementAdapter(dataBindingComponent, appExecutors) { samplemangementItem ->

            Timber.d(samplemangementItem.toString())

            if (samplemangementItem.id == 1) {
                val intent = Intent(activity, SampleProcessingActivity::class.java)
                startActivity(intent)
            } else if (samplemangementItem.id == 2) {
                val intent = Intent(activity, SampleStorageActivity::class.java)
                startActivity(intent)
            }

        }
        this.adapter = adapter
        binding.nghruList.adapter = adapter
        binding.nghruList.setLayoutManager(GridLayoutManager(activity, 3))
        samplemangementViewModel.setId("en")

        samplemangementViewModel.homeItem.observe(this, Observer { listResource ->


            if (listResource?.data != null) {
                adapter.submitList(listResource.data)
            } else {
                adapter.submitList(emptyList())
            }
        })
    }


    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.menu_main, menu)
        checkConnection(menu!!)
    }

    /**
     * Created to be able to override in tests
     */
    fun navController() = findNavController()


    // Method to manually check connection status
    private fun checkConnection(menu: Menu) {
        val isConnected = ConnectivityReceiver.isConnected(context)
        if (isConnected) {
            menu.findItem(R.id.menu_text).setTitleColor(Color.WHITE)
            menu.findItem(R.id.menu_text).setTitle(getString(R.string.network_status_online))
            menu.findItem(R.id.menu_online).setIcon(R.drawable.ic_icon_local_lan)
        } else {
            menu.findItem(R.id.menu_text).setTitleColor(Color.RED)
            menu.findItem(R.id.menu_text).setTitle(getString(R.string.network_status_offline))
            menu.findItem(R.id.menu_online).setIcon(R.drawable.ic_icon_wifi_disconnected)
        }
        activity!!.invalidateOptionsMenu();
    }

}
