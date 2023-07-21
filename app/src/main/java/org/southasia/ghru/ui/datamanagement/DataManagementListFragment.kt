package org.southasia.ghru.ui.datamanagement

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingComponent
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import cn.pedant.SweetAlert.SweetAlertDialog
import com.birbit.android.jobqueue.Job
import com.birbit.android.jobqueue.JobManager
import com.birbit.android.jobqueue.callback.JobManagerCallbackAdapter
import com.crashlytics.android.Crashlytics
import org.southasia.ghru.R
import org.southasia.ghru.binding.FragmentDataBindingComponent
import org.southasia.ghru.databinding.DataManagementFragmentBinding
import org.southasia.ghru.di.Injectable
import org.southasia.ghru.ui.enumeration.completed.CompletedDialogFragment
import org.southasia.ghru.util.autoCleared
import org.southasia.ghru.util.singleClick
import org.southasia.ghru.vo.*
import org.southasia.ghru.vo.request.Member
import org.southasia.ghru.vo.request.*
import javax.inject.Inject


class DataManagementListFragment : Fragment(), Injectable {

    var binding by autoCleared<DataManagementFragmentBinding>()
    var dataBindingComponent: DataBindingComponent = FragmentDataBindingComponent(this)

    private lateinit var linearLayoutManager: LinearLayoutManager
    private lateinit var adapter: DataManagementAdapter
    private var recordList :ArrayList<Any>  = ArrayList<Any>()
    private var currentRow : Int = 0
    @Inject
    lateinit var dataManagmentViewModel: DataManagmentViewModel

    @Inject
    lateinit var  jobManager: JobManager

    lateinit var pDialog :SweetAlertDialog

    private var householdRequestMeta: HouseholdRequestMeta? = null

//    var currentJob = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val dataBinding = DataBindingUtil.inflate<DataManagementFragmentBinding>(
            inflater,
            R.layout.data_management_fragment,
            container,
            false
        )
        binding = dataBinding
        setHasOptionsMenu(true)

        linearLayoutManager = LinearLayoutManager(context)
        binding.recyclerViewDataItem.layoutManager = linearLayoutManager

        adapter = DataManagementAdapter(recordList)
        binding.recyclerViewDataItem.adapter = adapter

        adapter.notifyDataSetChanged()



        return dataBinding.root
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        pDialog = SweetAlertDialog(context, SweetAlertDialog.PROGRESS_TYPE)
        pDialog.getProgressHelper().setBarColor(Color.parseColor("#00548F"))
        pDialog.setTitleText(getString(R.string.string_sync_inprogress))
        pDialog.setCancelable(false)
        jobManager.start()
        dataManagmentViewModel.setStationNameBP(Measurements.BLOOD_PRESSURE)

        dataManagmentViewModel.stationBPLocalList?.observe(this, Observer {
            if (it != null) {
                recordList.addAll(it)
                adapter.notifyDataSetChanged()
            }

            dataManagmentViewModel.setStationNameBM((Measurements.BODY_COMPOSITION))
        })

        dataManagmentViewModel.stationBMLocalList?.observe(this, Observer {

            if (it.data != null) {
                recordList.addAll(it.data!!)
                adapter.notifyDataSetChanged()
            }
            dataManagmentViewModel.setStationNameECG(Measurements.ECG)

        })
        dataManagmentViewModel.stationECGLocalList?.observe(this, Observer {

            if (it.data != null) {
                recordList.addAll(it.data!!)
                adapter.notifyDataSetChanged()
            }
            dataManagmentViewModel.setStationNameSpiro(Measurements.SPIROMETRY)
        })

        dataManagmentViewModel.stationSpiroLocalList?.observe(this, Observer {

            if(it.data != null)
            {
                recordList.addAll(it.data)
                adapter.notifyDataSetChanged()
            }
            dataManagmentViewModel.setStationNameFundoscopy(Measurements.FUNDOSCOPY)
        })

        dataManagmentViewModel.stationFundoscopyLocalList?.observe(this , Observer {

            if(it.data != null)
            {
                recordList.addAll(it.data)
                adapter.notifyDataSetChanged()
            }
            dataManagmentViewModel.setStationCancel(Measurements.CANCEL)
        })
        dataManagmentViewModel.stationCancelLocalList?.observe(this, Observer {

            if(it.data != null)
            {
                recordList.addAll(it.data)
                adapter.notifyDataSetChanged()
            }
            dataManagmentViewModel.setStationSample(Measurements.SAMPLE)
        })
        dataManagmentViewModel.stationSampleLocalList?.observe(this, Observer {
            if(it != null)
            {
                recordList.addAll(it)
                adapter.notifyDataSetChanged()
            }
            dataManagmentViewModel.setStationActivity(Measurements.AXIVITY)
        })

        dataManagmentViewModel.stationActivityLocalList?.observe(this, Observer {

            if(it.data != null)
            {
                recordList.addAll(it.data!!)
                adapter.notifyDataSetChanged()
            }
            dataManagmentViewModel.setStationEnumaration(Measurements.Enumaration)
        })

        dataManagmentViewModel.stationEnumarationList?.observe(this, Observer {

            if(it.data != null)
            {
                recordList.addAll(it.data!!)
                adapter.notifyDataSetChanged()
            }
            dataManagmentViewModel.setStationRegistration(Measurements.REGISTRATION)
        })

        dataManagmentViewModel.stationRegistration?.observe(this, Observer {

            if(it.data != null)
            {
                recordList.addAll(it.data)
                adapter.notifyDataSetChanged()
            }
        })

        binding.buttonSyncNow.singleClick {


//            activity?.runOnUiThread({
               pDialog.show()
//            })

            Thread(Runnable {
                println("test job count - " + jobManager.count())
                if(isNetworkAvailable())
                {
                    jobManager.stop()
                    jobManager.clear()
//
                    activity?.runOnUiThread({
                        syncNow(currentRow)
                    })
//
                }
                else
                {
                    activity?.runOnUiThread({
                        pDialog.hide()
                        Toast.makeText(context, getString(R.string.string_offline_not_available), Toast.LENGTH_SHORT)
                            .show()
                    })
                }
            }).start()

        }

//            var currentJob = 0
//            jobManager.addCallback(object : JobManagerCallbackAdapter() {
//                override fun onDone(@NonNull job: Job) {
//
//
//                    if(currentJob<recordList.size) {
//                        recordList.removeAt(currentJob)
//                        activity?.runOnUiThread({
//                            adapter.notifyDataSetChanged()
//                        })
//                    }
//                    else
//                    {
//                        jobManager.start()
//                    }
//                    if(currentJob >=recordList.size  ) {
//                        if(recordList.size >0)
//                        {
//                            recordList.clear()
//                            activity?.runOnUiThread({
//                                adapter.notifyDataSetChanged()
//                                Toast.makeText(context, getString(R.string.string_completed), Toast.LENGTH_SHORT)
//                                    .show()
//                            })
//                        }
//
//                    }
//                    currentJob++
//
//                }
//
//            })

        addSyncObservers()

    }

    private fun syncNow(index : Int)
    {
        if(index < recordList.size)
        {
            var record = recordList.get(index)
            recordList.removeAt(index)
            adapter.notifyDataSetChanged()
            if(record is BloodPressureMetaRequest)
            {
                dataManagmentViewModel.setRecordBloodPressureMetaRequest(record)
            }
            else if (record is BodyMeasurementMeta)
            {
                dataManagmentViewModel.setRecordBodyMeasurementMetaRequest(record)
            }
            else if (record is ECGStatus)
            {
                dataManagmentViewModel.setRecordECGStatus(record)
            }
            else if (record is SpirometryRequest)
            {
                dataManagmentViewModel.setSpirometryRequest(record)
            }
            else if (record is FundoscopyRequest)
            {
                dataManagmentViewModel.setFundoscopyRequest(record)
            }
            else if(record is CancelRequest)
            {
                dataManagmentViewModel.setCancelRequest(record)
            }
            else if(record is SampleRequest)
            {
                dataManagmentViewModel.setSampleRequest(record)
            }
            else if(record is Axivity)
            {
                dataManagmentViewModel.setAxivityRequest(record)
            }
            else if(record is HouseholdRequestMeta)
            {
                dataManagmentViewModel.setHouseholdRequestSyncRemote(record)
            }
//            else if(record is Measurements)
//            {
//                dataManagmentViewModel.setStationRegistration(record)
//            }
        }
        else
        {
            activity?.runOnUiThread({
                pDialog.hide()
            })
        }
    }
    private fun addSyncObservers()
    {
        dataManagmentViewModel.syncRecordBloodPressureMetaRequest?.observe(this, Observer {
            if(it.status != Status.LOADING)
            {
                currentRow ++
                syncNow(currentRow)
            }
        })
        dataManagmentViewModel.syncRecordBodyMeasurementMetaRequest?.observe(this, Observer {
            if(it.status != Status.LOADING)
            {
                currentRow ++
                syncNow(currentRow)
            }
        })
        dataManagmentViewModel.syncRecordECGStatus?.observe(this, Observer {
            if(it.status != Status.LOADING)
            {
                currentRow ++
                syncNow(currentRow)
            }
        })
        dataManagmentViewModel.syncRecordSpirometryRequest?.observe(this, Observer {
            if(it.status != Status.LOADING)
            {
                currentRow ++
                syncNow(currentRow)
            }
        })
        dataManagmentViewModel.syncFundoscopyRequest?.observe(this, Observer {
            if(it.status != Status.LOADING)
            {
                currentRow ++
                syncNow(currentRow)
            }
        })
        dataManagmentViewModel.syncCancelRequest?.observe(this, Observer {
            if(it.status != Status.LOADING)
            {
                currentRow ++
                syncNow(currentRow)
            }
        })
        dataManagmentViewModel.syncSampleRequest?.observe(this, Observer {
            if(it.status != Status.LOADING)
            {
                currentRow ++
                syncNow(currentRow)
            }
        })
        dataManagmentViewModel.syncAxivityRequest?.observe(this, Observer {
            if(it.status != Status.LOADING)
            {
                currentRow ++
                syncNow(currentRow)
            }
        })
        dataManagmentViewModel.householdRequestSyncRemote?.observe(this, Observer { householdRequest ->
            if (householdRequest?.status == Status.SUCCESS) {

                householdRequestMeta =   householdRequest.data as HouseholdRequestMeta
                dataManagmentViewModel.setMember(householdRequestMeta?.householdRequest?.enumerationId!!)

            } else if (householdRequest?.status == Status.ERROR) {
                currentRow ++
                syncNow(currentRow)
            }
        })
        dataManagmentViewModel.memberList?.observe(this, Observer { memberList ->
            if (memberList?.status == Status.SUCCESS) {
                val list = ArrayList<Member>()
                for (member in memberList?.data!!) {
                    list.add(member)
                }
                if(list.size >0)
                    dataManagmentViewModel.setMemberSyncedLocal(list, householdRequestMeta!!)

            } else if (memberList?.status == Status.ERROR) {
                currentRow ++
                syncNow(currentRow)
            }
        })
        dataManagmentViewModel.memberSyncRemote?.observe(this, Observer { memberSyncRemote ->
//            if (memberSyncRemote?.status == Status.SUCCESS) {
//
//            } else if (memberSyncRemote?.status == Status.ERROR) {
//
//            }
            currentRow ++
            syncNow(currentRow)
        })
//        dataManagmentViewModel.stationRegistration?.observe(this, Observer {
//
//            if(it.status != Status.LOADING)
//            {
//                currentRow ++
//                syncNow(currentRow)
//            }
//        })


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

}