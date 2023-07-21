package org.southasia.ghru.ui.intake.readings


import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.http.SslError
import android.opengl.Visibility
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.webkit.*
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingComponent
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.birbit.android.jobqueue.JobManager
import com.crashlytics.android.Crashlytics
import io.reactivex.disposables.CompositeDisposable
import org.southasia.ghru.IntakeActivity
import org.southasia.ghru.R
import org.southasia.ghru.binding.FragmentDataBindingComponent
import org.southasia.ghru.databinding.IntakeWebFragmentBinding
import org.southasia.ghru.databinding.WebFragmentBinding
import org.southasia.ghru.di.Injectable
import org.southasia.ghru.ui.intake.readings.completed.CompletedDialogFragment
import org.southasia.ghru.ui.intake.cancel.CancelDialogFragment
import org.southasia.ghru.ui.web.WebFragment
import org.southasia.ghru.ui.web.WebViewModel
import org.southasia.ghru.util.autoCleared
import org.southasia.ghru.util.getLocalTimeString
import org.southasia.ghru.util.singleClick
import org.southasia.ghru.vo.Meta
import org.southasia.ghru.vo.Status
import org.southasia.ghru.vo.request.IntakeRequest
import org.southasia.ghru.vo.request.IntakeRequestNew
import org.southasia.ghru.vo.request.ParticipantRequest
import java.io.IOException
import java.io.InputStream
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class IntakeReadingsFragment  : Fragment(), Injectable {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    var binding by autoCleared<IntakeWebFragmentBinding>()
    var dataBindingComponent: DataBindingComponent = FragmentDataBindingComponent(this)

    @Inject
    lateinit var viewModel: IntakeReadingsViewModel
    private var myWebSettings: WebSettings? = null
    private var databasePath: String? = null

    @Inject
    lateinit var jobManager: JobManager
    private var participant: ParticipantRequest? = null

    var webUrl: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            participant = arguments?.getParcelable<ParticipantRequest>("ParticipantRequest")!!
        } catch (e: KotlinNullPointerException) {

        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val dataBinding = DataBindingUtil.inflate<IntakeWebFragmentBinding>(
            inflater,
            R.layout.intake_web_fragment,
            container,
            false
        )
        binding = dataBinding
        binding.participant = participant;
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

        myWebSettings = binding.webViewDiet.getSettings()
        databasePath = activity!!.getDir("database", Context.MODE_PRIVATE).getPath()

        myWebSettings!!.setJavaScriptEnabled(true)
        myWebSettings!!.setDatabaseEnabled(true)
        myWebSettings!!.setDatabasePath(databasePath)
        myWebSettings!!.setLoadWithOverviewMode(true)
        myWebSettings!!.setUseWideViewPort(true)
        myWebSettings!!.setAppCacheEnabled(true)
        myWebSettings!!.setCacheMode(WebSettings.LOAD_NO_CACHE)
        myWebSettings!!.setDatabaseEnabled(true)
        myWebSettings!!.setDomStorageEnabled(true)
        myWebSettings!!.setGeolocationEnabled(false)
        myWebSettings!!.setSaveFormData(false)
        myWebSettings!!.setJavaScriptCanOpenWindowsAutomatically(true)

        binding.webViewDiet.addJavascriptInterface(JavascriptInterface(activity, viewModel, jobManager), "Android")

        binding.webViewDiet.setWebChromeClient(object : WebChromeClient() {
            private val TAG = "WebView"

            override fun onConsoleMessage(cm: ConsoleMessage): Boolean {
                Log.d(TAG, cm.sourceId() + ": Line " + cm.lineNumber() + " : " + cm.message())
                return true
            }

            override fun onExceededDatabaseQuota(
                url: String, databaseIdentifier: String, currentQuota: Long, estimatedSize: Long,
                totalUsedQuota: Long, quotaUpdater: WebStorage.QuotaUpdater
            ) {
                quotaUpdater.updateQuota(estimatedSize * 2)
            }

        })



        binding.completeButton.singleClick {
                showCompleteCOnfirmationDialog()
        }

        binding.cancelButton.singleClick {
            val cancelDialogFragment = CancelDialogFragment()
            cancelDialogFragment.arguments = bundleOf("participant" to participant)
            cancelDialogFragment.show(fragmentManager!!)
        }
        val intakeRequest = IntakeRequestNew(meta = participant!!.meta)

//        participant!!.meta!!.startTime = binding.root.getLocalTimeString()

//        intakeRequest.meta = participant!!.meta
        viewModel.setParticipant(intakeRequest, participant?.screeningId)
        Log.d("INTAKE_READING_FRAGMENT","REQUEST_BODY: " + intakeRequest.meta)

        viewModel.setIntakeMeta(intakeRequest = intakeRequest, screen_id = participant!!.screeningId)

        viewModel.intakePostComplete?.observe(this, Observer { participantResource ->
            if (participantResource?.status == Status.SUCCESS) {
                println(participantResource.data?.data)
                if (participantResource.data != null) {

                    val intakeData = participantResource.data.data
                    binding.webViewDiet.loadUrl(intakeData?.intake_url)
                    webUrl = intakeData!!.intake_url

                    Log.d("INTAKE_FRAGMENT", "URL: " + webUrl)

                }
            }
        })

        binding.webViewDiet.setWebViewClient(object : WebViewClient() {

            @SuppressWarnings("deprecation")
            override fun onReceivedError(view: WebView , errorCode: Int , description: String , failingUrl: String ) {
                Toast.makeText(activity!!, description, Toast.LENGTH_SHORT).show()
                Log.d("INTAKE_FRAGMENT","ERROR_IS: " + description)
            }

            override fun onReceivedError(view: WebView , req: WebResourceRequest , rerr: WebResourceError ) {
                // Redirect to deprecated method, so you can use it in all SDK versions
                onReceivedError(view, rerr.getErrorCode(), rerr.getDescription().toString(), req.getUrl().toString());
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                if(url != null) {
                    //binding.webView.loadUrl(webUrl)
                }

            }
//
            override fun onReceivedHttpError(view: WebView?, request: WebResourceRequest?, errorResponse: WebResourceResponse?
            ) {
                super.onReceivedHttpError(view, request, errorResponse)
                Log.d("INTAKE_READING","HTTP_ERROR_REQUEST: " + request.toString() + " AND HTTP_ERROR_RESPONSE " + errorResponse.toString())
            }

            override fun onReceivedHttpAuthRequest(
                view: WebView?,
                handler: HttpAuthHandler?,
                host: String?,
                realm: String?
            ) {
                super.onReceivedHttpAuthRequest(view, handler, host, realm)
                Log.d("INTAKE_READING" , "HTTP_AUTH_REQUEST_ERROR: " + realm + ", " + host)
            }

            override fun shouldInterceptRequest (view: WebView? , url: String? ): WebResourceResponse? {
                if (url!!.contains(".mime")) {
                    //return getCssWebResourceResponseFromAsset()
                    Log.d("INTAKE_READING" , "INTERCEPT_REQUEST: " + url)
                } else {
                    return super.shouldInterceptRequest(view, url)
                }

                return null
            }
        })

//        val intakeRequest1 = IntakeRequestNew(meta = null)
//
////        participant!!.meta!!.startTime = binding.root.getLocalTimeString()
//
//        intakeRequest.meta!!.endTime = binding.root.getLocalTimeString()
//        viewModel.setParticipant(intakeRequest1, participant?.screeningId)
//        Log.d("INTAKE_READING_FRAGMENT","REQUEST_BODY: " + intakeRequest.meta)
//
//        viewModel.setIntakeMetaUpdate(intakeRequest = intakeRequest, screen_id = participant!!.screeningId)

        viewModel.intakeUpdateComplete?.observe(this, Observer { assertsResource ->
            if (assertsResource?.status == Status.SUCCESS) {
                println(assertsResource.data?.data)
                if (assertsResource.data != null) {
                    val completedDialogFragment = CompletedDialogFragment()
                    completedDialogFragment.arguments = bundleOf("is_cancel" to false)
                    completedDialogFragment.show(fragmentManager!!)

                } else {
                    binding.completeButton.visibility = View.VISIBLE
                    toast(assertsResource.message.toString())
                    Crashlytics.logException(Exception("IntakeComplete " + assertsResource.message.toString()))
                    binding.executePendingBindings()
                }
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

    fun navController() = findNavController()

    private fun showCompleteCOnfirmationDialog(){

        lateinit var dialog: AlertDialog
        val builder = AlertDialog.Builder(context!!)


       // builder.setTitle("Title")
        builder.setMessage(getString(R.string.Intake_confirmation_message))

        // On click listener for dialog buttons
        val dialogClickListener = DialogInterface.OnClickListener{ _, which ->
            when(which){
                DialogInterface.BUTTON_POSITIVE -> { updateIntakeStation() }

//                DialogInterface.BUTTON_NEGATIVE -> toast("Negative/No button clicked.")

            }
        }

        builder.setPositiveButton(getString(R.string.app_yes),dialogClickListener)
        builder.setNegativeButton(getString(R.string.app_no),dialogClickListener)

        dialog = builder.create()

        dialog.show()
    }
    private fun toast(message: String) {
        Toast.makeText(context!!, message, Toast.LENGTH_SHORT).show()
    }
    private fun updateIntakeStation()
    {
        binding.completeButton.visibility = View.GONE
        val intakeRequest = IntakeRequestNew(meta = participant!!.meta)

        val endTime: String = convertTimeTo24Hours()
        val endDate: String = getDate()
        val endDateTime:String = endDate + " " + endTime

        intakeRequest.meta!!.endTime = endDateTime
        intakeRequest.status = "100"
//        intakeRequest.status = "100"
        //intakeRequest.meta = participant!!.meta
//        intakeRequest.status = "100"
        viewModel.updateParticipant(intakeRequest, participant?.screeningId)


    }

    class JavascriptInterface(
        val mContext: FragmentActivity?,
        val viewModel: IntakeReadingsViewModel,
        val jobManager: JobManager
    ) {
        fun finish() {

        }

        fun showAndroidToast() {
            Toast.makeText(mContext, "ss", Toast.LENGTH_LONG).show()
        }

        @android.webkit.JavascriptInterface
        fun showToast(json: String) {
            //SyncServeyRxBus.getInstance().post(SyncResponseEventType.SUCCESS, json = json)

        }

        private fun isNetworkAvailable(): Boolean {
            val connectivityManager = mContext?.getSystemService(Context.CONNECTIVITY_SERVICE)
            return if (connectivityManager is ConnectivityManager) {
                val networkInfo: NetworkInfo? = connectivityManager.activeNetworkInfo
                networkInfo?.isConnected ?: false
            } else false
        }

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
}


