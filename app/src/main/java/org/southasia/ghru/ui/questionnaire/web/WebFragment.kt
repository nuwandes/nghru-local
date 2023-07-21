package org.southasia.ghru.ui.questionnaire.web


import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.crashlytics.android.Crashlytics
import io.reactivex.disposables.CompositeDisposable
import org.southasia.ghru.R
import org.southasia.ghru.binding.FragmentDataBindingComponent
import org.southasia.ghru.databinding.WebFragmentBinding
import org.southasia.ghru.di.Injectable
import org.southasia.ghru.jobs.SyncSurveyJob
import org.southasia.ghru.sync.SyncResponseEventType
import org.southasia.ghru.sync.SyncServeyRxBus
import org.southasia.ghru.ui.questionnaire.cancel.CancelDialogFragment
import org.southasia.ghru.util.autoCleared
import org.southasia.ghru.util.getLocalTimeString
import org.southasia.ghru.util.showToast
import org.southasia.ghru.util.singleClick
import org.southasia.ghru.vo.*
//import org.southasia.ghru.vo.Date
import org.southasia.ghru.vo.request.ParticipantRequest
import timber.log.Timber
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import java.util.Date
import javax.inject.Inject


class WebFragment : Fragment(), Injectable {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    var binding by autoCleared<WebFragmentBinding>()
    var dataBindingComponent: DataBindingComponent = FragmentDataBindingComponent(this)
    @Inject
    lateinit var viewModel: WebViewModel
    private var myWebSettings: WebSettings? = null
    private var databasePath: String? = null


    @Inject
    lateinit var jobManager: JobManager

    private val disposables = CompositeDisposable()

    private var participant: ParticipantRequest? = null
    var user: User? = null
    var meta: Meta? = null
    val endTime: String = ""
    private var questionnaire: Questionnaire? = null
    var questionareJson : String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            participant = arguments?.getParcelable<ParticipantRequest>("ParticipantRequest")!!
            questionnaire = arguments?.getParcelable<Questionnaire>("Questionnaire")!!
            questionareJson = questionnaire?.json!!
        } catch (e: KotlinNullPointerException) {

        }

//        val endTime: String = convertTimeTo24Hours()
//        val endDate: String = getDate()
//        val endDateTime:String = endDate + " " + endTime

        disposables.add(
            SyncServeyRxBus.getInstance().toObservable()
                .subscribe({ result ->
                    if (isNetworkAvailable()) {
                        activity?.runOnUiThread {
                            participant?.meta?.endTime = binding.root.getLocalTimeString()

                            viewModel.setSurvey(
                                QuestionMeta(
                                    meta = participant?.meta,
                                    json = result.json,
                                    screeningId = participant?.screeningId!!,
                                    questionnaireId = questionnaire?.id,
                                    language = questionnaire?.language
                                )
                            )
                        }

                    } else {
                        participant?.meta?.endTime = binding.root.getLocalTimeString()
                        jobManager.addJobInBackground(
                            SyncSurveyJob(
                                QuestionMeta(
                                    meta = participant?.meta,
                                    json = result.json,
                                    screeningId = participant?.screeningId!!,
                                    questionnaireId = questionnaire?.id,
                                    language = questionnaire?.language
                                )
                            )
                        )
                        activity!!.finish()
                    }

                }, { error ->
                    error.printStackTrace()
                })
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val dataBinding = DataBindingUtil.inflate<WebFragmentBinding>(
            inflater,
            R.layout.web_fragment,
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
        binding.participant = participant
        viewModel.setUser("user")
        viewModel.user?.observe(this, Observer { userData ->
            if (userData?.data != null) {
                // setupNavigationDrawer(userData.data)
                user = userData.data

                val sTime: String = convertTimeTo24Hours()
                val sDate: String = getDate()
                val sDateTime:String = sDate + " " + sTime

                meta = Meta(collectedBy = user?.id, startTime = sDateTime)

                //meta?.registeredBy = user?.id
                //meta?.registeredBy = user?.id
            }

        })

        myWebSettings = binding.webView.getSettings()
        databasePath = activity!!.getDir("database", Context.MODE_PRIVATE).getPath()

        myWebSettings!!.setJavaScriptEnabled(true)
        myWebSettings!!.setDatabaseEnabled(true)
        myWebSettings!!.setDatabasePath(databasePath)

        binding.webView.addJavascriptInterface(JavascriptInterface(activity, viewModel, jobManager), "Android")
        viewModel.survey?.observe(this, Observer { commonResponce ->

            //println(commonResponce.toString())
            if (commonResponce?.status == Status.SUCCESS) {
                //println(user)
               // println("Status.SUCCESS")
                Toast.makeText(activity!!, getString(R.string.questionnaire_success), Toast.LENGTH_SHORT).show()
                activity!!.finish()
            } else if (commonResponce?.status == Status.ERROR) {
                //Crashlytics.logException(Exception(commonResponce.message?.message))
               // Crashlytics.setString("comment", binding.comment.text.toString())
                Crashlytics.setString("participant", participant.toString())
                Crashlytics.logException(Exception("questionareJson " + commonResponce.message?.data?.message))
                //Timber.d(commonResponce.message?.message)
              //  activity!!.showToast(commonResponce.message?.message!!)
            }
        })





//        viewModel.language?.observe(this, Observer { commonResponce ->
//
//           // println(commonResponce.toString())
//            if (commonResponce?.status == Status.SUCCESS) {
//                //println(user)
//                questionareJson = commonResponce.data?.json!!
//                binding.webView.loadUrl("file:///android_asset/q/index.html")
//
//            } else if (commonResponce?.status == Status.ERROR) {
//                //Crashlytics.logException(Exception(commonResponce.message?.message))
//            }
//        })
//
//        viewModel.getQuestionnaire(network =  isNetworkAvailable(), language = "en")

        binding.webView.setWebViewClient(object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                if(questionareJson != null) {
                    binding.webView.loadUrl("javascript:init(" + questionareJson + ")")
                }

            }

//            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
//                super.onPageStarted(view, url, favicon)
//                view?.loadUrl("javascript:init(\"bciwbcibe\")");
//            }

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

        binding.webView.loadUrl("file:///android_asset/q/index.html")

        binding.buttonCancel.singleClick {

            val cancelDialogFragment = CancelDialogFragment()
            cancelDialogFragment.arguments = bundleOf("participant" to participant)
            cancelDialogFragment.show(fragmentManager!!)
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
