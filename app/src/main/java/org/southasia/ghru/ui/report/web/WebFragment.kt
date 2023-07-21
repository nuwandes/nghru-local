package org.southasia.ghru.ui.report.web


import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.webkit.*
import android.webkit.WebStorage.QuotaUpdater
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingComponent
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.birbit.android.jobqueue.JobManager
import com.pixplicity.easyprefs.library.Prefs
import io.reactivex.disposables.CompositeDisposable
import org.southasia.ghru.BuildConfig
import org.southasia.ghru.R
import org.southasia.ghru.binding.FragmentDataBindingComponent
import org.southasia.ghru.databinding.ReportFragmentBinding
import org.southasia.ghru.di.Injectable
import org.southasia.ghru.sync.SyncResponseEventType
import org.southasia.ghru.sync.SyncServeyRxBus
import org.southasia.ghru.ui.registerpatient.scanqrcode.errordialog.ErrorDialogFragment
import org.southasia.ghru.ui.report.web.completed.CompletedDialogFragment
import org.southasia.ghru.util.autoCleared
import org.southasia.ghru.util.getLocalTimeString
import org.southasia.ghru.util.singleClick
import org.southasia.ghru.vo.Meta
import org.southasia.ghru.vo.Status
import org.southasia.ghru.vo.request.ParticipantRequest
import org.southasia.ghru.vo.request.ReportRequestMeta
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject


class WebFragment : Fragment(), Injectable {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    var binding by autoCleared<ReportFragmentBinding>()
    var dataBindingComponent: DataBindingComponent = FragmentDataBindingComponent(this)
    @Inject
    lateinit var viewModel: WebViewModel
    private var myWebSettings: WebSettings? = null
    private var databasePath: String? = null


    @Inject
    lateinit var jobManager: JobManager

    private var participant: ParticipantRequest? = null

    private var meta: Meta? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            participant = arguments?.getParcelable<ParticipantRequest>("ParticipantRequest")!!
            meta = arguments?.getParcelable<Meta>("meta")!!
        } catch (e: KotlinNullPointerException) {

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val dataBinding = DataBindingUtil.inflate<ReportFragmentBinding>(
            inflater,
            R.layout.report_fragment,
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


    @SuppressLint("SetJavaScriptEnabled", "JavascriptInterface")
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.setLifecycleOwner(this)
        myWebSettings = binding.webView.getSettings()
        databasePath = activity!!.getDir("database", Context.MODE_PRIVATE).getPath()

        myWebSettings!!.setJavaScriptEnabled(true)
        myWebSettings!!.setDatabaseEnabled(true)
        myWebSettings!!.setDatabasePath(databasePath)

        binding.webView.addJavascriptInterface(JavascriptInterface(activity, viewModel, jobManager), "Android")

        binding.webView.setWebViewClient(object : WebViewClient() {
            private var `in`: Animation? = null
            private var out: Animation? = null

            override fun onPageFinished(view: WebView, url: String) {
//                if (binding.webView.getVisibility() === View.VISIBLE) {
//                    `in` = AnimationUtils.loadAnimation(activity, android.R.anim.fade_in)
//                    out = AnimationUtils.loadAnimation(activity, android.R.anim.fade_out)
//                    binding.webView.startAnimation(`in`)
//                    binding.webView.setVisibility(View.VISIBLE)
//                    binding.webView.startAnimation(out)
//                    binding.webView.setVisibility(View.GONE)
//                }
            }
        })

        viewModel.participant?.observe(this, Observer { participantResource ->
            if (participantResource?.status == Status.SUCCESS) {
                val completedDialogFragment = CompletedDialogFragment()
                completedDialogFragment.arguments = bundleOf("is_cancel" to false)
                completedDialogFragment.show(fragmentManager!!)
            } else if (participantResource?.status == Status.ERROR) {
                val errorDialogFragment = ErrorDialogFragment()
                errorDialogFragment.setErrorMessage("Unable to complete report")
                errorDialogFragment.show(fragmentManager!!)
                //Crashlytics.logException(Exception(participantResource.toString()))
            }
            binding.executePendingBindings()
        })

        binding.webView.setWebChromeClient(object : WebChromeClient() {
            private val TAG = "WebView"

            override fun onConsoleMessage(cm: ConsoleMessage): Boolean {
                Log.d(TAG, cm.sourceId() + ": Line " + cm.lineNumber() + " : " + cm.message())
                return true
            }

            override fun onExceededDatabaseQuota(
                url: String, databaseIdentifier: String, currentQuota: Long, estimatedSize: Long,
                totalUsedQuota: Long, quotaUpdater: QuotaUpdater
            ) {
                quotaUpdater.updateQuota(estimatedSize * 2)
            }
        })
        val url: String = Prefs.getString(
            "Ipaddress",
            BuildConfig.SERVER_URL
        ) + "report.html?id=${participant?.screeningId}&token=" + Prefs.getString("ACCESS_TOKEN_ONLY", null)
        binding.webView.loadUrl(url)

        binding.buttonContinue.singleClick {

//            val endTime: String = convertTimeTo24Hours()
//            val endDate: String = getDate()
//            val endDateTime:String = endDate + " " + endTime

            viewModel.setScreeningId(participant)
            participant?.meta?.endTime =  binding.root.getLocalTimeString()
            meta!!.endTime =  binding.root.getLocalTimeString()
            val x = ReportRequestMeta(meta = meta)

            viewModel.setRepoMeta(reportRequestMeta = x, participantId = participant!!)
        }
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

    class JavascriptInterface(
        val mContext: FragmentActivity?,
        val viewModel: WebViewModel,
        val jobManager: JobManager
    ) {
        fun finish() {

        }

        fun showAndroidToast() {
            Toast.makeText(mContext, "ss", Toast.LENGTH_LONG).show();
        }

        @android.webkit.JavascriptInterface
        fun showToast(json: String) {
            SyncServeyRxBus.getInstance().post(SyncResponseEventType.SUCCESS, json = json)

            // Toast.makeText(mContext, toast, Toast.LENGTH_SHORT).show()
        }

        private fun isNetworkAvailable(): Boolean {
            val connectivityManager = mContext?.getSystemService(Context.CONNECTIVITY_SERVICE)
            return if (connectivityManager is ConnectivityManager) {
                val networkInfo: NetworkInfo? = connectivityManager.activeNetworkInfo
                networkInfo?.isConnected ?: false
            } else false
        }

    }

//    private fun convertTimeTo24Hours(): String
//    {
//        //Format of the date defined in the input String
//        val now: Calendar = Calendar.getInstance()
//
//        val inputFormat: DateFormat = SimpleDateFormat("MMM DD, yyyy HH:mm:ss")
//        //Desired format: 24 hour format: Change the pattern as per the need
//        val outputformat: DateFormat = SimpleDateFormat("HH:mm")
//        val date: Date
//        val output: String
//        try{
//            //Converting the input String to Date
//            date= inputFormat.parse(now.time.toLocaleString())
//            //Changing the format of date and storing it in String
//            output = outputformat.format(date)
//            //Displaying the date
//
//            return output
//        }catch(p: ParseException){
//            //p.printStackTrace()
//            return ""
//        }
//    }
//
//    private fun getDate(): String
//    {
//        val inputFormat: DateFormat = SimpleDateFormat("yyyy-MM-dd hh:mm")
//        //Desired format: 24 hour format: Change the pattern as per the need
//        val outputformat: DateFormat = SimpleDateFormat("yyyy-MM-dd")
//        val date: Date
//        val output: String
//        try{
//            //Converting the input String to Date
//            date= inputFormat.parse(binding.root.getLocalTimeString())
//            //Changing the format of date and storing it in String
//            output = outputformat.format(date)
//            //Displaying the date
//
//            return output
//        }catch(p: ParseException){
//            //p.printStackTrace()
//            return ""
//        }
//    }
}
