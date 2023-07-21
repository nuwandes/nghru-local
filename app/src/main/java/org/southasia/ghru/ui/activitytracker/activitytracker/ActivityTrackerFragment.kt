package org.southasia.ghru.ui.activitytracker.activitytracker


import android.app.PendingIntent
import android.app.Service
import android.content.*
import android.content.Context.USB_SERVICE
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.Color
import android.hardware.usb.UsbManager
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.content.getSystemService
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingComponent
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.birbit.android.jobqueue.JobManager
import com.crashlytics.android.Crashlytics
import io.reactivex.disposables.CompositeDisposable
import org.southasia.ghru.AppExecutors
import org.southasia.ghru.R
import org.southasia.ghru.UsbSerialPort
import org.southasia.ghru.binding.FragmentDataBindingComponent
import org.southasia.ghru.databinding.ActivityTrackerFragmentBinding
import org.southasia.ghru.di.Injectable
import org.southasia.ghru.event.AxivityRxBus
import org.southasia.ghru.event.BPRecordRxBus
import org.southasia.ghru.jobs.SyncAxivityJob
import org.southasia.ghru.ui.activitytracker.activitytracker.completed.CompletedDialogFragment
import org.southasia.ghru.ui.activitytracker.activitytracker.reason.ReasonDialogFragment
import org.southasia.ghru.util.*
import org.southasia.ghru.vo.Axivity
import org.southasia.ghru.vo.Status
import org.southasia.ghru.vo.request.ParticipantRequest
import java.io.File
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.*
import javax.inject.Inject



class ActivityTackeFragment : Fragment(), Injectable {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var appExecutors: AppExecutors


    var binding by autoCleared<ActivityTrackerFragmentBinding>()

    var dataBindingComponent: DataBindingComponent = FragmentDataBindingComponent(this)

    @Inject
    lateinit var viewModel: ActivityTackeViewModel

    private var participant: ParticipantRequest? = null

    private val disposables = CompositeDisposable()

    private var existFileCount: Int = 0

    @Inject
    lateinit var jobManager: JobManager

    //    var axivity: Axivity? = Axivity("11111","222222","333333","44444")
    var axivity: Axivity? = null

    private var selectedDeviceID: String? = null

    var mUsbManager :UsbManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            participant = arguments?.getParcelable<ParticipantRequest>("ParticipantRequest")!!
        } catch (e: KotlinNullPointerException) {

        }

        mUsbManager = activity!!.getSystemService(Context.USB_SERVICE) as UsbManager


        disposables.add(
            AxivityRxBus.getInstance().toObservable()
                .subscribe({ result ->
                    // Log.d("Result", "household SyncCommentLifecycleObserver ${result}")
                    try {
                        if (isAinaPackageAvailable(activity!!.getApplicationContext()))
                        {
                            axivity = result
                            binding.ainaViewConnected.expand()
                            binding.ainaViewNotConnected.collapse()
                            binding.nextButton.isEnabled = false
                            binding.nextButton.setBackgroundColor(Color.GRAY)
                        }
                        else
                        {
                            binding.ainaViewConnected.collapse()
                            binding.ainaViewNotConnected.expand()
                            binding.nextButton.isEnabled = true
                            binding.nextButton.setBackground(resources.getDrawable(R.drawable.ic_button_fill_primary))
                        }
                    }
                    catch (ex: KotlinNullPointerException)
                    {
                        throw ex
                    }

                }, { error ->

                    try {
//                        binding.ainaViewConnected.collapse()
//                        binding.ainaViewNotConnected.expand()
                        error.printStackTrace()
                    }
                    catch (ex:IllegalStateException)
                    {
                        throw ex
                    }

                })
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val dataBinding = DataBindingUtil.inflate<ActivityTrackerFragmentBinding>(
            inflater,
            R.layout.activity_tracker_fragment,
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
        binding.setLifecycleOwner(this)
        binding.viewModel = viewModel
        binding.participant = participant

        val usbDevices = UsbSerialPort.getDevices(mUsbManager)

        if (usbDevices.isNotEmpty())
        {
            binding.nextButton.isEnabled = true
            binding.nextButton.setBackground(resources.getDrawable(R.drawable.ic_button_fill_primary))
            val usbDevice = usbDevices[0]
//            if (mUsbManager!!.hasPermission(usbDevice))
//            {
//                //deleteFile(activity!!, usbDevice!!.)
//            }
//            else
//            {
//
//            }
        }
        else
        {
            binding.nextButton.isEnabled = false
            binding.nextButton.setBackgroundColor(Color.GRAY)
            Log.d("ACTIVITY_TRACKER_FRAG","USB_DEVICES: " + usbDevices.size)
        }

        val itemsRelationship = arrayOf(
            "Left",
            "Right"
        )
        val adapter = ArrayAdapter(context!!, R.layout.basic_spinner_dropdown_item, itemsRelationship)
        binding.dominantWristSpinner.setAdapter(adapter);


        binding.dominantWristSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parentView: AdapterView<*>, @NonNull selectedItemView: View?,
                position: Int,
                id: Long
            ) {
                if (position == 0) {
                    selectedDeviceID = itemsRelationship.get(0)
                } else {
                    selectedDeviceID = itemsRelationship.get(1)
                }

            }

            override fun onNothingSelected(parentView: AdapterView<*>) {
                // your code here
            }

        }

        viewModel.axivitySync?.observe(this, Observer { participant ->

            if (participant?.status == Status.SUCCESS) {
                val completedDialogFragment = CompletedDialogFragment()
                completedDialogFragment.arguments = bundleOf("is_cancel" to false)
                completedDialogFragment.show(fragmentManager!!)
            } else if (participant?.status == Status.ERROR) {
                Crashlytics.setString("axivity", axivity.toString())
                Crashlytics.setString("participant", participant.toString())
                Crashlytics.logException(Exception("axivity " + participant.message.toString()))
                binding.executePendingBindings()

            }
        })
        binding.nextButton.singleClick {

            val gpath: String = Environment.getExternalStorageDirectory().absolutePath
            val spath = "USB storage 1/CWA-DATA.CWA"
            val fullpath = File(gpath + File.separator + spath)
            //Log.d("fullpath", "" + fullpath)
            Log.d("AXIVITY_TRACKER_FRAGMENT", "FULL_PATH: " + fullpath)
            fileReader(fullpath, it!!)
        }

        binding.submitButton.singleClick {

            if (axivity != null) {

                val endTime: String = convertTimeTo24Hours()
                val endDate: String = getDate()
                val endDateTime:String = endDate + " " + endTime

                participant?.meta?.endTime =  endDateTime
                axivity?.meta = participant?.meta
                axivity?.dominantWrist = selectedDeviceID?.toLowerCase()
                axivity?.syncPending = !isNetworkAvailable()
                axivity?.screeningId = participant?.screeningId!!
                axivity?.endTime =  endDateTime
                //if (isNetworkAvailable()) {
                if (axivity?.meta != null)
                {
                    viewModel.setAxivity(axivity = axivity!!, participantId = participant!!)
                }

//                } else {
//                    jobManager.addJobInBackground(SyncAxivityJob(participant?.screeningId!!, axivity!!))
//                    val completedDialogFragment = CompletedDialogFragment()
//                    completedDialogFragment.arguments = bundleOf("is_cancel" to false)
//                    completedDialogFragment.show(fragmentManager!!)
//                }
            } else {
                Toast.makeText(activity!!, "Please configure axivity", Toast.LENGTH_LONG).show()

            }
        }
        binding.buttonCancel.singleClick {

            val reasonDialogFragment = ReasonDialogFragment()
            reasonDialogFragment.arguments = bundleOf("participant" to participant)
            reasonDialogFragment.show(fragmentManager!!)
        }


    }

    val ACTIVITY_TRACKER_REQUEST_CODE = 1200

    private fun startAx3(action: String, requestCode: Int) {
        var ainaIntent: Intent? = null
        if (isAinaPackageAvailable(activity!!.getApplicationContext())) {

            try
            {
                ainaIntent = Intent(action)
                ainaIntent.putExtra("data", bundleOf("user" to participant?.screeningId))
                activity!!.startActivityForResult(ainaIntent, requestCode)

                Log.d("ACTIVITY_TRACKER_FRAG", "AX3_PACKAGE_OK: ")
                Toast.makeText(activity!!,"AX3_PACKAGE_OK", Toast.LENGTH_LONG).show()
            }
            catch (e: Exception)
            {
                Log.d("ACTIVITY_TRACKER_FRAG", "AX3_PACKAGE_NOT_OK: " + e)
                Toast.makeText(activity!!,"AX3_PACKAGE_NOT_OK: " + e.toString(), Toast.LENGTH_LONG).show()
            }

        } else {

//            binding.ainaViewConnected.collapse()
//            binding.ainaViewNotConnected.expand()

            Toast.makeText(activity, "Activity tracker not installed", Toast.LENGTH_SHORT).show()
        }
    }// Lines of code to invoke Aina launch for a specific test


    fun isAinaPackageAvailable(context: Context): Boolean {
        val packages: List<ApplicationInfo>
        val pm: PackageManager
        pm = context.getPackageManager()
        packages = pm.getInstalledApplications(0)
        for (packageInfo in packages) {
            if (packageInfo.packageName.contains("uk.ac.ncl.openlab.ax3config")) return true
        }
        return false
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
        //Format of the date defined in the input String
        val now: Calendar  = Calendar.getInstance()

        val inputFormat: DateFormat = SimpleDateFormat("MMM DD, yyyy HH:mm:ss")
        //Desired format: 24 hour format: Change the pattern as per the need
        val outputformat: DateFormat  = SimpleDateFormat("HH:mm")
        val date: Date
        val output: String
        try{
            //Converting the input String to Date
            date= inputFormat.parse(now.time.toLocaleString())
            //Changing the format of date and storing it in String
            output = outputformat.format(date)
            //Displaying the date

            return output
        }catch(p: ParseException){
            //p.printStackTrace()
            return ""
        }
    }

    private fun getDate(): String
    {
        val inputFormat: DateFormat = SimpleDateFormat("yyyy-MM-dd hh:mm")
        //Desired format: 24 hour format: Change the pattern as per the need
        val outputformat: DateFormat  = SimpleDateFormat("yyyy-MM-dd")
        val date: Date
        val output: String
        try{
            //Converting the input String to Date
            date= inputFormat.parse(binding.root.getLocalTimeString())
            //Changing the format of date and storing it in String
            output = outputformat.format(date)
            //Displaying the date

            return output
        }catch(p: ParseException){
            //p.printStackTrace()
            return ""
        }
    }

    private fun validateFundoscopy(): Boolean {
        return true;
    }

    private fun deleteFile(context: Context,  file: File): Boolean {
        val where: String = MediaStore.MediaColumns.DATA + "=?"
        val  selectionArgs =  arrayOf(file.getAbsolutePath())

        val contentResolver: ContentResolver = context.getContentResolver()
        val filesUri: Uri = MediaStore.Files.getContentUri("external")

        contentResolver.delete(filesUri, where, selectionArgs)

        if (file.exists()) {

            contentResolver.delete(filesUri, where, selectionArgs)
        }
        return !file.exists()
    }

    fun fileReader(root: File, view: View) {

//        Log.d("AXIVITY_TRACKER_FRAGMENT", "FILE: " + root)
//        val fileList: ArrayList<File> = ArrayList()
//        val listAllFiles = root.listFiles()

        Log.d("AXIVITY_TRACKER_FRAGMENT", "FILE: " + root.absoluteFile.name.endsWith("CWA"))

//        if (listAllFiles != null && listAllFiles.size > 0) {
//
//            Log.d("AXIVITY_TRACKER_FRAGMENT", "FILE_SIZE: " + listAllFiles.size)
//
//            for (currentFile in listAllFiles) {
//                if (currentFile.name.endsWith(".CWA")) {
//                    Log.d("AXIVITY_TRACKER_FRAGMENT", "HAS_FILE: " + currentFile.getName())
//
//                    existFileCount++
//                }
//                else
//                {
//                    Log.d("AXIVITY_TRACKER_FRAGMENT", "NO_FILE: " + currentFile.getName())
//                }
//            }
//            Log.d("AXIVITY_TRACKER_FRAGMENT", "FILE_LIST" + fileList.size + "FILE_COUNT: " + existFileCount)
//        }

        if (root.absoluteFile.name.endsWith("CWA")) {

            existFileCount++

            Log.d("AXIVITY_TRACKER_FRAGMENT", "FILE_SIZE: " + existFileCount)
        }

        if (existFileCount>=1)
        {
            showConfirmationDialog()
        }else
        {
            // start aina
            view.hideKeyboard()
            startAx3("uk.ac.ncl.openlab.ax3config", ACTIVITY_TRACKER_REQUEST_CODE)
        }
    }

    private fun showConfirmationDialog() {

        lateinit var dialog: AlertDialog

        val builder = AlertDialog.Builder(context!!)

        builder.setTitle(getString(R.string.app_confirmation))

        builder.setMessage(getString(R.string.axivity_exist_message))


        val dialogClickListener = DialogInterface.OnClickListener { _, which ->
            when (which) {
                DialogInterface.BUTTON_POSITIVE -> {
                    view!!.hideKeyboard()
                    startAx3("uk.ac.ncl.openlab.ax3config", ACTIVITY_TRACKER_REQUEST_CODE)
                }
                DialogInterface.BUTTON_NEGATIVE -> {
                    dialog.dismiss()
                }

            }
        }

        builder.setPositiveButton(getString(R.string.app_yes), dialogClickListener)
        builder.setNegativeButton(getString(R.string.app_no), dialogClickListener)

        dialog = builder.create()
        dialog.show()
    }

    /**
     * Created to be able to override in tests
     */
    fun navController() = findNavController()
}
