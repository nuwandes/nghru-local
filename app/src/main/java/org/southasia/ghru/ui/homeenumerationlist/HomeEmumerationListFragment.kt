package org.southasia.ghru.ui.homeenumerationlist

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingComponent
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.birbit.android.jobqueue.JobManager
import com.pixplicity.easyprefs.library.Prefs
import org.southasia.ghru.*
import org.southasia.ghru.binding.FragmentDataBindingComponent
import org.southasia.ghru.databinding.HomeEmumerationListFragmentBinding
import org.southasia.ghru.di.Injectable
import org.southasia.ghru.util.autoCleared
import org.southasia.ghru.util.showToast
import timber.log.Timber
import javax.inject.Inject


class HomeEmumerationListFragment : Fragment(), Injectable {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    @Inject
    lateinit var appExecutors: AppExecutors

    var binding by autoCleared<HomeEmumerationListFragmentBinding>()
    var dataBindingComponent: DataBindingComponent = FragmentDataBindingComponent(this)

    @Inject
    lateinit var homeemumerationlistViewModel: HomeEmumerationListViewModel

    private var adapter by autoCleared<HomeEmumerationListAdapter>()

    @Inject
    lateinit var  jobManager: JobManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val dataBinding = DataBindingUtil.inflate<HomeEmumerationListFragmentBinding>(
            inflater,
            R.layout.home_emumeration_list_fragment,
            container,
            false
        )
        binding = dataBinding
        setHasOptionsMenu(true)
        return dataBinding.root
    }

    override fun onStart() {

        super.onStart()
        jobManager.start()
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


        binding.homeViewModel = homeemumerationlistViewModel

        homeemumerationlistViewModel.user?.observe(this, Observer { userData ->
            if (userData?.data != null) {

                Prefs.putString("COUNTRY", userData.data.team?.country.toString())

                Log.d("HOME_ENUM_FRAG","COUNTRY_IS: " + Prefs.getString("COUNTRY", null))
            }

        })

//       //L.d("luhnCheckDigit PAA-9576 expeceted 8 = " + calculateCheckDigit(convertIDToDigits("PAA-9576")!!))
//       //L.d("luhnCheckDigit PAA-9578 expeceted 4 = " + calculateCheckDigit(convertIDToDigits("PAA-9578")!!))
//       //L.d("luhnCheckDigit PAA-9579 expeceted 2 = " + calculateCheckDigit(convertIDToDigits("PAA-9579")!!))
//
//
//       //L.d("luhnCheckDigit EAA-0005-0 expeceted 0 = " + calculateCheckDigit(convertIDToDigits("EAA-0005")!!))
//       //L.d("luhnCheckDigit EAA-0010-0 expeceted 0 = " + calculateCheckDigit(convertIDToDigits("EAA-0010")!!))
//       //L.d("luhnCheckDigit EAA-0029-0 expeceted 0 = " + calculateCheckDigit(convertIDToDigits("EAA-0029")!!))


        val adapter = HomeEmumerationListAdapter(dataBindingComponent, appExecutors) { homeemumerationlistItem ->

            Timber.d(homeemumerationlistItem.toString())
            if (homeemumerationlistItem.id == 1) {
                val intent = Intent(activity, EnumerationActivity::class.java)
                startActivity(intent)
            }

            if (homeemumerationlistItem.id == 2) {
                // navController().navigate(R.id.action_homeEmumerationListFragment_to_homeFragment)
                val intent = Intent(activity, ScreeningHomeActivity::class.java)
                startActivity(intent)
            }

            if (homeemumerationlistItem.id == 6) {
                // navController().navigate(R.id.sampleMangementFragment)
                val intent = Intent(activity, SampleHomeActivity::class.java)
                startActivity(intent)
            }

            if (homeemumerationlistItem.id == 3) {
                if (isNetworkAvailable()) {
                    val intent = Intent(activity, ReportViewActivity::class.java)
                    startActivity(intent)
                } else {
                    activity!!.showToast("This feature is not available offline")
                }
            }


        }
        this.adapter = adapter
        binding.nghruList.adapter = adapter
        binding.nghruList.setLayoutManager(GridLayoutManager(activity, 1))
        homeemumerationlistViewModel.setId("en")
        homeemumerationlistViewModel.homeemumerationlistItem.observe(this, Observer { listResource ->


            if (listResource?.data != null) {
                adapter.submitList(listResource.data)
            } else {
                adapter.submitList(emptyList())
            }
        })
        homeemumerationlistViewModel.setUser("Login")
        homeemumerationlistViewModel.user?.observe(this, Observer { user ->
            if (user?.data != null) {
                binding.user = user.data
                binding.executePendingBindings()
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
