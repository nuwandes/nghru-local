package org.southasia.ghru.ui.web


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
import androidx.databinding.DataBindingComponent
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.birbit.android.jobqueue.JobManager
import io.reactivex.disposables.CompositeDisposable
import org.southasia.ghru.R
import org.southasia.ghru.binding.FragmentDataBindingComponent
import org.southasia.ghru.databinding.WebFragmentBinding
import org.southasia.ghru.di.Injectable
import org.southasia.ghru.sync.SyncResponseEventType
import org.southasia.ghru.sync.SyncServeyRxBus
import org.southasia.ghru.util.autoCleared
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


//        disposables.add(
//                SyncServeyRxBus.getInstance().toObservable()
//                        .subscribe({ result ->
//                            if (isNetworkAvailable()) {
//                                activity?.runOnUiThread({
//                                    viewModel.setSurvey(result.json)
//                                })
//
//                            } else {
//                                jobManager.addJobInBackground(SyncSurveyJob(Survey(json = result.json)))
//                                activity!!.finish()
//                            }
//
//                        }, { error ->
//                            error.printStackTrace()
//                        }))
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
}
