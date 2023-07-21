package org.southasia.ghru.ui.login

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.Intent.getIntent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingComponent
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import br.com.ilhasoft.support.validation.Validator
import com.crashlytics.android.Crashlytics
import org.southasia.ghru.BuildConfig
import org.southasia.ghru.MainActivity
import org.southasia.ghru.R
import org.southasia.ghru.binding.FragmentDataBindingComponent
import org.southasia.ghru.databinding.LoginFragmentBinding
import org.southasia.ghru.db.AccessTokenDao
import org.southasia.ghru.di.Injectable
import org.southasia.ghru.ui.common.RetryCallback
import org.southasia.ghru.util.TokenManager
import org.southasia.ghru.util.autoCleared
import org.southasia.ghru.util.hideKeyboard
import org.southasia.ghru.util.singleClick
import org.southasia.ghru.vo.Status
import pub.devrel.easypermissions.EasyPermissions
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.regex.Matcher
import java.util.regex.Pattern
import java.util.*
import javax.inject.Inject

class LoginFragment : Fragment(), Injectable, EasyPermissions.PermissionCallbacks {
    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
    }

    private val RC_SMS_PERM = 122

    private val LOCATION_AND_CAMERA: Array<String> =
        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.CAMERA)
    private val REQUEST_EXTERNAL_STORAGE = 1
    private val PERMISSIONS_STORAGE =
        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory


    var binding by autoCleared<LoginFragmentBinding>()
    var dataBindingComponent: DataBindingComponent = FragmentDataBindingComponent(this)

    @Inject
    lateinit var loginViewModel: LoginViewModel

    private val registry = LifecycleRegistry(this)

    @Inject
    lateinit var tokenManager: TokenManager

    private lateinit var validator: Validator

    @Inject
    lateinit var accessTokenDao: AccessTokenDao

    var prefs : SharedPreferences? = null

    var dateFormat : String = "yyyy-MM-dd hh:mm"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val dataBinding = DataBindingUtil.inflate<LoginFragmentBinding>(
            inflater,
            R.layout.login_fragment,
            container,
            false
        )

        binding = dataBinding
        validator = Validator(binding)
        dataBinding.retryCallback = object : RetryCallback {
            override fun retry() {
                binding.userResource = null
                loginViewModel.onError()
            }
        }
        binding.textViewVesion.text = getApplicationVersionName()

        return dataBinding.root
    }

    private fun getApplicationVersionName(): String {

        try {
            val packageInfo = activity?.getPackageManager()?.getPackageInfo(activity?.getPackageName(), 0)
            return packageInfo?.versionName!!
        } catch (ignored: Exception) {
        }

        return ""
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = activity?.getSystemService(Context.CONNECTIVITY_SERVICE)
        return if (connectivityManager is ConnectivityManager) {
            val networkInfo: NetworkInfo? = connectivityManager.activeNetworkInfo
            networkInfo?.isConnected ?: false
        } else false
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        prefs = PreferenceManager.getDefaultSharedPreferences(context)
        binding.linearLayout.visibility = View.INVISIBLE
        binding.linearLayout2.visibility = View.INVISIBLE
        binding.buttonLogin.visibility = View.INVISIBLE

        loginViewModel.accessTokenOffline?.observe(this, Observer { accessToken ->


            if (accessToken?.status == Status.SUCCESS) {
                binding.progressBar.visibility = View.GONE
                if (accessToken.data != null) {
                    if (accessToken.data.status) {
                        if (!isLoginClick) {
                            loadMainActivity()
                        }

                    } else {
                        binding.linearLayout.visibility = View.VISIBLE
                        binding.linearLayout2.visibility = View.VISIBLE
                        binding.buttonLogin.visibility = View.VISIBLE
                    }

                }
            }
            else if (accessToken?.status == Status.ERROR){
                binding.linearLayout.visibility = View.VISIBLE
                binding.linearLayout2.visibility = View.VISIBLE
                binding.buttonLogin.visibility = View.VISIBLE
                binding.progressBar.visibility = View.GONE
            }
        })
        if (tokenManager.getEmail() != null) {
            loginViewModel.setEmail(tokenManager.getEmail()!!)
        } else {
            binding.linearLayout.visibility = View.VISIBLE
            binding.linearLayout2.visibility = View.VISIBLE
            binding.buttonLogin.visibility = View.VISIBLE
        }



        if (BuildConfig.DEBUG) {

//            binding.textInputEditTextEmail.setText("roshan@well.tech")
//            binding.textInputEditTextPassword.setText("Qwerty123#")

//            binding.textInputEditTextEmail.setText("stagingbnqa@nghru.org")
//            binding.textInputEditTextPassword.setText("Asdfgh123#")

        }

        loginViewModel.accessToken?.observe(this, Observer { accessToken ->

            binding.progressBar.visibility = View.GONE
            binding.userResource = accessToken
            if (accessToken?.status == Status.SUCCESS ) {
                //println(user)
                if(accessToken.data!=null) {
                    val token = accessToken.data!!
                    //tokenManager.saveToken(token)
                    tokenManager.saveEmail(binding.textInputEditTextEmail.text.toString())
                    binding.textViewError.visibility = View.INVISIBLE
                    token.status = true
                    //accessTokenDao.login(token)

                        loginViewModel.setStationDevice("GET")

                }else
                {
                    Timber.d(getString(R.string.user_not_found))

                    binding.textViewError.visibility = View.VISIBLE
                    binding.textViewError.setText(getString(R.string.user_not_found))
                    binding.linearLayout.visibility = View.VISIBLE
                    binding.linearLayout2.visibility = View.VISIBLE
                    binding.buttonLogin.visibility = View.VISIBLE
                    binding.userResource = null
                    loginViewModel.onError()
                }

            } else if (accessToken?.status == Status.ERROR) {
                Crashlytics.logException(Exception(accessToken.message?.message))
                Timber.d(accessToken.message?.message)

                binding.textViewError.visibility = View.VISIBLE
                binding.textViewError.setText(accessToken.message?.message)
                binding.linearLayout.visibility = View.VISIBLE
                binding.linearLayout2.visibility = View.VISIBLE
                binding.buttonLogin.visibility = View.VISIBLE
                binding.userResource = null
                loginViewModel.onError()
            }
        })

        loginViewModel.stationDevices?.observe(this, Observer {
            binding.progressBar.visibility = View.GONE

            if (it?.status == Status.SUCCESS) {
                loginViewModel.setStationDeviceList(it.data?.data!!)
            }
            else if(it?.status == Status.ERROR){
                // binding.textViewError.visibility = View.VISIBLE
                // binding.textViewError.setText(it.message?.message)
                loadMainActivity()
            }

        })
//        loginViewModel.hemoDevices?.observe(this, Observer {
//            binding.progressBar.visibility = View.GONE
//
//            if (it?.status == Status.SUCCESS) {
//                loginViewModel.setStationDeviceList(it.data?.data!!)
//            }
//            else if(it?.status == Status.ERROR){
//                // binding.textViewError.visibility = View.VISIBLE
//                // binding.textViewError.setText(it.message?.message)
//                loadMainActivity()
//            }
//
//        })
        loginViewModel.stationDeviceList?.observe(this, Observer {
            binding.progressBar.visibility = View.GONE
            if (it?.status == Status.SUCCESS || it?.status == Status.ERROR){

               loadMainActivity()
            }
        })
        binding.buttonLogin.singleClick {
            if (validator.validate()) {
                    binding.progressBar.visibility = View.VISIBLE
                   val mPattern: Pattern = Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9])(?=.*[!@#\$%\\^&\\*])(?=.{8,})")

        val matche: Matcher = mPattern.matcher(binding.textInputEditTextPassword.text.toString())
                binding.root.hideKeyboard()
        if(!matche.find())
        {
            binding.textInputLayoutPassword.error = getString(R.string.passowrd_reg_error)
            //weightEditText.setText(); // Don't know what to place
        }else{
            activity?.runOnUiThread(
                object : Runnable {
                    override fun run() {
                        binding.textViewError.text = ""
                        binding.linearLayout.visibility = View.INVISIBLE
                        binding.linearLayout2.visibility = View.INVISIBLE
                        binding.buttonLogin.visibility = View.INVISIBLE
                        isLoginClick = true

                            loginViewModel.setLogin(
                                binding.textInputEditTextEmail.text.toString(),
                                binding.textInputEditTextPassword.text.toString(),
                                isNetworkAvailable()
                            )

                    }
                }
            )

        }



            }
        }

        if (ContextCompat.checkSelfPermission(
                activity!!,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                activity!!,
                Manifest.permission.CAMERA
            )
            != PackageManager.PERMISSION_GRANTED && (ContextCompat.checkSelfPermission(
                activity!!,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED)
        ) {

            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    activity!!,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
                && ActivityCompat.shouldShowRequestPermissionRationale(
                    activity!!,
                    Manifest.permission.CAMERA
                ) && ActivityCompat.shouldShowRequestPermissionRationale(
                    activity!!,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) && ActivityCompat.shouldShowRequestPermissionRationale(
                    activity!!,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
            ) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(
                    activity!!,
                    LOCATION_AND_CAMERA,
                    RC_SMS_PERM
                )
                ActivityCompat.requestPermissions(
                    activity!!,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
                )


                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }
        // loginViewModel.setDevices("devices")
    }

    var isLoginClick: Boolean = false

    /**
     * Created to be able to override in tests
     */
    fun navController() = findNavController()

    fun getLocalTimeString(): String {
        val s = SimpleDateFormat(dateFormat, Locale.US)
        return s.format(Date())
    }
    fun  loadMainActivity()
    {
        prefs?.edit()?.putBoolean("isTimeOut", false)?.apply()
        prefs?.edit()?.putString("loginDateTime", getLocalTimeString())?.apply()
        prefs?.edit()?.putString("dateTime", getLocalTimeString())?.apply()

        val intent = Intent(activity, MainActivity::class.java)
        startActivity(intent)
        activity!!.finish()
    }
}
