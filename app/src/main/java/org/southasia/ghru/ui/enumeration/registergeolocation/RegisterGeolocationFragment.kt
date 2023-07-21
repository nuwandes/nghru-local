package org.southasia.ghru.ui.enumeration.registergeolocation

import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.AlertDialog
import android.app.PendingIntent
import android.app.ProgressDialog
import android.content.Context
import android.content.Context.LOCATION_SERVICE
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.*
import android.location.LocationListener
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingComponent
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import br.com.ilhasoft.support.validation.Validator
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.RuntimeExecutionException
import org.southasia.ghru.BuildConfig
import org.southasia.ghru.R
import org.southasia.ghru.binding.FragmentDataBindingComponent
import org.southasia.ghru.databinding.RegisterGeoLocationFragmentBinding
import org.southasia.ghru.di.Injectable
import org.southasia.ghru.util.*
import org.southasia.ghru.vo.Meta
import org.southasia.ghru.vo.User
import org.southasia.ghru.vo.request.Address
import org.southasia.ghru.vo.request.HouseholdRequest
import org.southasia.ghru.vo.request.Position
import java.security.Provider
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.math.roundToInt


class RegisterGeolocationFragment : Fragment(), LocationListener, Injectable {


    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    var binding by autoCleared<RegisterGeoLocationFragmentBinding>()

    var dataBindingComponent: DataBindingComponent = FragmentDataBindingComponent(this)

    private lateinit var validator: Validator

    var onPause: Boolean = false
    var isAtUserLocationSelected: Boolean = false

    private var mLocationRequest: LocationRequest? = null

    private val UPDATE_INTERVAL = (1 * 1000).toLong()  /* 10 secs */

    private val FASTEST_INTERVAL: Long = 1000 /* 2 sec */
    var isLocationFromGPS : Boolean = true
    var user: User? = null
    //  var progressDialog = ProgressDialog(context)

    @Inject
    lateinit var viewModel: RegisterGeolocationViewModel

    var meta: Meta? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val dataBinding = DataBindingUtil.inflate<RegisterGeoLocationFragmentBinding>(
            inflater,
            R.layout.register_geo_location_fragment,
            container,
            false
        )
        binding = dataBinding
        validator = Validator(binding)
        setHasOptionsMenu(true)
        val appCompatActivity = requireActivity() as AppCompatActivity
        appCompatActivity.setSupportActionBar(binding.detailToolbar)
        appCompatActivity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        //  progressDialog.setCancelable(false)

        return dataBinding.root
    }


    override fun onResume() {
        super.onResume()
        onPause = false
    }

    override fun onPause() {
        super.onPause()
        // stopLocationUpdates()
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (BuildConfig.DEBUG) {
//            binding.householdIdentifier.setText("IN-ND-11002201123")
//            binding.householdAddress.setText("South Zone, R.K. Puram, Sector 9, New Delhi, Delhi 110022, India")
        }

        val sTime: String = convertTimeTo24Hours()
        val sDate: String = getDate()
        val sDateTime:String = sDate + " " + sTime

        meta = Meta(collectedBy = "user", startTime = sDateTime)
        viewModel.setUser("user")
        viewModel.user?.observe(this, Observer { userData ->
            if (userData?.data != null) {
                // setupNavigationDrawer(userData.data)
                user = userData.data
                meta = Meta(collectedBy = user?.id, startTime = sDateTime)
            }
        })

        binding.nextButton.singleClick {
            if (validator.validate()) {
                binding.root.hideKeyboard()
//                meta?.endTime = sDateTime
                var identifier = ""
                if (binding.householdIdentifier.text.toString().isNullOrEmpty())
                    identifier = "N/A"
                else
                    identifier = binding.householdIdentifier.text.toString()

                if (location != null) {
                    //  val bundle = bundleOf("household" to Household(binding.householdIdentifier.text.toString(), "LK", binding.householdAddress.text.toString(), Position(location?.longitude!!, location?.latitude!!), syncPending = true))


                    val bundle = bundleOf(
                        "HouseholdRequest" to HouseholdRequest(
                            address = Address(
                                country = if (!user?.team?.country.isNullOrEmpty()) user?.team?.country else "LK",
                                postcode = if (binding.householdVilage.text.toString().isEmpty()) null else binding.householdVilage.text.toString(),
                                street = binding.householdAddress.text.toString(),
                                locality = binding.householdDistrict.text.toString()
                            ),
                            position = Position(
                                location?.longitude!!,
                                location?.latitude!!,
                                location?.accuracy!!.roundToInt(),
                                identifier
                            )
                        ), "meta" to meta, "countryCode" to user?.team?.country
                    )
                    Navigation.findNavController(binding.root)
                        .navigate(
                            R.id.action_registerGeolocationFragment_to_concentFragment,
                            bundle
                        )
                    binding.executePendingBindings()
                } else {
                    if (!isLocationAvailable) {
                        val bundle = bundleOf(
                            "HouseholdRequest" to HouseholdRequest(
                                address = Address(
                                    country = if (!user?.team?.country.isNullOrBlank()) user?.team?.country else "LK",
                                    postcode = if (binding.householdVilage.text.toString().isNullOrBlank()) null else binding.householdVilage.text.toString(),
                                    street = binding.householdAddress.text.toString(),
                                    locality = binding.householdDistrict.text.toString()
                                ),
                                position = Position(
                                    null,
                                    null,
                                    null,
                                    identifier
                                )
                            ), "meta" to meta, "countryCode" to user?.team?.country
                        )
                        Navigation.findNavController(binding.root)
                            .navigate(
                                R.id.action_registerGeolocationFragment_to_concentFragment,
                                bundle
                            )
                    }
//                    binding.registerGPS.background = resources.getDrawable(R.drawable.ic_error_button, null)
//                    binding.registerGPS.setTextColor(Color.parseColor("#FF5E45"))
//                    binding.registerGPS.setDrawbleLeftColor("#FF5E45")
                    binding.invalidateAll()
                }
            } else {
                if (!isAtUserLocationSelected)
                    binding.textViewGeoLocationText.setTextColor(Color.parseColor("#d50000"))

                binding.executePendingBindings()
            }
        }
//        binding.registerGPS.singleClick {
//            binding.root.hideKeyboard()
//            binding.registerGPS.isEnabled = false
//            startLocationUpdates()
//        }


        binding.radioGroupGeoLocation.setOnCheckedChangeListener({ radioGroup, i ->

            isAtUserLocationSelected = true
            binding.textViewGeoLocationText.setTextColor(Color.parseColor("#DE000000"))
            if (radioGroup.checkedRadioButtonId == R.id.yesLocation) {

                startLocationUpdates()
            } else {
                binding.registeredSuccessGeoView.collapse()
                binding.registeredGeoErrorView.collapse()
                location = null
                isLocationAvailable = false
            }
            binding.executePendingBindings()
        })

        binding.executePendingBindings()

        binding.registeredSuccessGeoView.singleClick {

            val gmmIntentUri =
                Uri.parse("geo:${this.location!!.latitude},${this.location!!.longitude}")
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)

            mapIntent.setPackage("com.google.android.apps.maps");
            startActivity(mapIntent)
        }
        binding.buttonRetryGeoCoordinates.singleClick {

            binding.registeredSuccessGeoView.collapse()
            binding.registeredGeoErrorView.collapse()
            binding.executePendingBindings()
            isLocationAvailable = true
            startLocationUpdates()
        }

    }


    private var locationManager: LocationManager? = null
    private var hasGPS = false
    private var hasNetworkProvider = false

    protected fun startLocationUpdates() {
        if (checkPermission())
            getLocation()
        else
            requestPermissions()
    }

    private var location: Location? = null

    @SuppressLint("MissingPermission")
    var gpsStatus : TextView? = null
    var button_continue : Button? = null
    var alertDialog : AlertDialog? = null
    var gpsSatInformations : TextView? = null
    @SuppressLint("MissingPermission")

    private fun getLocation() {
        isLocationFromGPS = true
        val alertBuilder = AlertDialog.Builder(context);
        val dialogView = layoutInflater.inflate(R.layout.progress_dialog, null)
        val message = dialogView.findViewById<TextView>(R.id.dialogTextView)
        gpsStatus = dialogView.findViewById<TextView>(R.id.gpsStatusTextView)
        gpsSatInformations = dialogView.findViewById<TextView>(R.id.gpsSatTextView)

        button_continue = dialogView.findViewById<Button>(R.id.button_continue_geo)
        val button_no_geo_continue = dialogView.findViewById<Button>(R.id.button_no_geo)
        val button_network_provider = dialogView.findViewById<Button>(R.id.button_get_network_provider_geo)

        gpsSatInformations!!.setText(getString(R.string.serching_satellites))
        message.setText(getString(R.string.geo_cordinate_message))
        alertBuilder.setView(dialogView)
        alertBuilder.setCancelable(false)
        alertDialog = alertBuilder.create()
        alertDialog!!.show()

        locationManager = context?.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        hasGPS = locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER)!!
        hasNetworkProvider = locationManager?.isProviderEnabled(LocationManager.NETWORK_PROVIDER)!!

        gpsStatus!!.setText(getString(R.string.gps_available))
        button_continue!!.visibility = View.GONE

        if(hasNetworkProvider)  button_network_provider!!.visibility = View.VISIBLE else button_network_provider!!.visibility = View.GONE

        button_continue!!.singleClick {

            if (location != null) {
                binding.registeredSuccessGeoView.expand()
                binding.registeredGeoErrorView.collapse()


                val image =
                    resources.getDrawable(R.drawable.ic_icon_reset, null)
                image.setBounds(
                    0,
                    0,
                    image.getIntrinsicWidth(),
                    image.getIntrinsicHeight()
                );

                binding.textViewCoordinates.text =
                    getString(R.string.geo_coordinates) + " " + location!!.latitude + "," + location!!.longitude
                binding.executePendingBindings()

                locationManager!!.removeUpdates(this)

                alertDialog!!.dismiss()

            }

        }
        button_no_geo_continue.singleClick {

            locationManager!!.removeUpdates(this)

            gpsStatus!!.setText(getString(R.string.gps_not_available))
            gpsStatus!!.setTextColor(resources.getColor(R.color.red))
            isLocationAvailable = false
            binding.registeredSuccessGeoView.collapse()
            binding.registeredGeoErrorView.expand()
            binding.executePendingBindings()
            Toast.makeText(
                context,
                getString(R.string.enumeration_location_not_available),
                Toast.LENGTH_SHORT
            )
                .show()
            alertDialog!!.dismiss()
        }

        button_network_provider.singleClick {
            locationManager!!.removeUpdates(this)
            isLocationFromGPS = false

            gpsStatus!!.setText(getString(R.string.getting_network_location))
            gpsSatInformations!!.setText(" ")

            locationManager!!.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER,
                5000,
                0.0F,
                this
            )


        }

        if (hasGPS) {
            gpsStatus!!.setText(getString(R.string.gps_available))
            locationManager!!.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                5000,
                0.0F,
                this
            )

            Log.d("GEO_FRAGMENT", "LOCATION: " + locationManager)

            try
            {
                locationManager!!.addGpsStatusListener(GpsStatus.Listener {
                    var gpsStatus: GpsStatus = locationManager!!.getGpsStatus(null)

                    if(isLocationFromGPS) {
                        if (gpsStatus != null) {

                            if (context != null)
                            {
                                try
                                {
                                    gpsSatInformations!!.setText(
                                        gpsStatus.satellites?.count().toString() + " " + getString(
                                            R.string.gps_sat_info
                                        ) + " " + gpsStatus.maxSatellites.toString()
                                    )
                                    Log.d(
                                        "SAT_COUNT - ",
                                        gpsStatus.satellites?.count().toString() + " TOTAL_SAT - " + gpsStatus.maxSatellites.toString()
                                    )
                                }
                                catch (ex:IllegalStateException)
                                {
                                    throw ex
                                }
                            }

                        } else {
                            gpsSatInformations!!.setText(getString(R.string.serching_satellites))
                        }
                    }
                    else
                    {
                        gpsSatInformations!!.setText("")
                    }
                })
            }
            catch (ex: Exception) {
                Log.d("GEO_EXCEPTION" , "EXCEPTION:" + ex.toString())
                Toast.makeText(activity, "Geo Location error" + ex.toString(), Toast.LENGTH_SHORT).show()
            }



//            val localLocation =
//                locationManager!!.getLastKnownLocation(LocationManager.GPS_PROVIDER);
//            if (localLocation != null) {
//
//                location = localLocation
//                isLocationAvailable = true
//                binding.registeredSuccessGeoView.expand()
//                binding.registeredGeoErrorView.collapse()
//
//                Log.d("LAST_KNOWN_PROVIDER","LAST_KNOWN_PROVIDER")
//                val image = resources.getDrawable(R.drawable.ic_icon_reset, null)
//                image.setBounds(
//                    0,
//                    0,
//                    image.getIntrinsicWidth(),
//                    image.getIntrinsicHeight()
//                );
//
//                binding.textViewCoordinates.text =
//                    getString(R.string.geo_coordinates) + " " + location!!.latitude + "," + location!!.longitude
//                binding.executePendingBindings()
//                alertDialog.dismiss()
//
//            }
        } else {
            gpsStatus!!.setText(getString(R.string.gps_not_available))
            gpsStatus!!.setTextColor(resources.getColor(R.color.red))
            isLocationAvailable = false
            binding.registeredSuccessGeoView.collapse()
            binding.registeredGeoErrorView.expand()
            binding.executePendingBindings()
            Toast.makeText(
                context,
                getString(R.string.enumeration_location_not_available),
                Toast.LENGTH_SHORT
            )
                .show()
            alertDialog!!.dismiss()

        }
    }

    override fun onLocationChanged(Location: Location?) {
        gpsStatus!!.setText(getString(R.string.found_coordinates))

        button_continue!!.visibility = View.VISIBLE
        Log.d("Location", Location!!.provider)
        isLocationAvailable = true
        location = Location

        if(isLocationFromGPS) {
            if (location!!.accuracy < 7) {
                binding.registeredSuccessGeoView.expand()
                binding.registeredGeoErrorView.collapse()


                val image =
                    resources.getDrawable(R.drawable.ic_icon_reset, null)
                image.setBounds(
                    0,
                    0,
                    image.getIntrinsicWidth(),
                    image.getIntrinsicHeight()
                )

                binding.textViewCoordinates.text =
                    getString(R.string.geo_coordinates) + " " + location!!.latitude + "," + location!!.longitude
                binding.executePendingBindings()
                locationManager!!.removeUpdates(this)
                alertDialog!!.dismiss()
            } else {

                gpsStatus!!.setText(getString(R.string.gps_accuracy) + " " + location!!.accuracy + " m")
            }
        }
        else
        {
            binding.registeredSuccessGeoView.expand()
            binding.registeredGeoErrorView.collapse()
            binding.textViewCoordinates.text =
                getString(R.string.geo_coordinates) + " " + location!!.latitude + "," + location!!.longitude
            binding.executePendingBindings()
            locationManager!!.removeUpdates(this)
            alertDialog!!.dismiss()
        }


    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
        Log.d("PROVIDER", provider + " STATUS " + status)

    }

    override fun onProviderEnabled(provider: String?) {
        Log.d("PROVIDER", provider)
    }

    override fun onProviderDisabled(provider: String?) {
        isLocationAvailable = false
        binding.registeredSuccessGeoView.collapse()
        binding.registeredGeoErrorView.expand()
        binding.executePendingBindings()
        Toast.makeText(
            context,
            getString(R.string.enumeration_location_not_available),
            Toast.LENGTH_SHORT
        )
            .show()
    }


    var isLocationAvailable = true

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            if (permissions[0] == Manifest.permission.ACCESS_FINE_LOCATION) {
                val alertBuilder = AlertDialog.Builder(context);
                val dialogView = layoutInflater.inflate(R.layout.progress_dialog, null)
                val message = dialogView.findViewById<TextView>(R.id.dialogTextView)
                message.setText(getString(R.string.geo_cordinate_message))
                alertBuilder.setView(dialogView)
                alertBuilder.setCancelable(false)
                val alertDialog = alertBuilder.create()

                startLocationUpdates()
            }
        }
    }

    private fun checkPermission(): Boolean {
        if (ContextCompat.checkSelfPermission(
                activity!!,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                activity!!,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            return true;
        } else {
            requestPermissions()
            return false
        }
    }

    /**
     * if application is not allowed for the ACCESS_FINE_LOCATION
     * then it will open pop-up to grant that permission
     */
    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            activity!!,
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            1
        )
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

    /**
     * Created to be able to override in tests
     */
    fun navController() = findNavController()
}
