package org.southasia.ghru

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.crashlytics.android.Crashlytics
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import org.southasia.ghru.sync.*
import org.southasia.ghru.util.LocaleManager
import timber.log.Timber
import java.util.*
import javax.inject.Inject


class SampleProcessingActivity : AppCompatActivity(), HasSupportFragmentInjector {
    @Inject
    lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Fragment>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.sample_processing_activity)

    }

    override fun supportFragmentInjector() = dispatchingAndroidInjector

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(LocaleManager(base).setLocale())
    }

    override fun onSupportNavigateUp(): Boolean {
        val currentDestination = Navigation.findNavController(this, R.id.container).currentDestination
        val parent = currentDestination?.parent
        if (parent == null || currentDestination.id != parent.id)
            super.onBackPressed()
        else
            onSupportNavigateUp()
        return true
    }

    val AINA_REQUEST_CODE_GLUCOSE = 10
    val AINA_REQUEST_CODE_Hb1Ac = 20
    val AINA_REQUEST_CODE_HDL = 30
    val AINA_REQUEST_CODE_TotalCholesterol = 40
    val AINA_REQUEST_CODE_Triglycerol = 50
    val AINA_REQUEST_CODE_Hemoglobin = 60

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            AINA_REQUEST_CODE_GLUCOSE -> {
//                val altUnit = data?.getBooleanExtra("alt_unit", false)
//                val apiVersion = data?.getStringExtra("version")
//                val versionCode = data?.getIntExtra("aina_app_version", 0)
//                val codeEntered = data?.getStringExtra("code_entered") // lot
//                val deviceId = data?.getIntExtra("device_id", 0)
//                val uiStatesList = data?.getStringArrayListExtra("ui_states_list")
//                val uiStatesData = Arrays.toString(data?.getDoubleArrayExtra("ui_states_data_list"))
                val glucose = data?.getFloatExtra("reading", Float.MIN_VALUE)
                var reading = String.format(Locale.US, "%.0f", glucose)
                //  val status = resultCode
                Timber.d("reading " + reading)
                Crashlytics.log(reading)

                JanaCareGlucoseRxBus.getInstance().post(JanacareResponse(CholesterolcomEventType.HDL,  AinaResponce(result = reading, lotNumber =  data?.getStringExtra("code_entered").toString())))
            }
            AINA_REQUEST_CODE_Hb1Ac -> {
//                val altUnit = data?.getBooleanExtra("alt_unit", false)
//                val apiVersion = data?.getStringExtra("version")
//                val versionCode = data?.getIntExtra("aina_app_version", 0)
//                val codeEntered = data?.getStringExtra("code_entered")
//                val deviceId = data?.getIntExtra("device_id", 0)
//                val uiStatesList = data?.getStringArrayListExtra("ui_states_list")
//                val uiStatesData = Arrays.toString(data?.getDoubleArrayExtra("ui_states_data_list"))
                val reading = data?.getFloatExtra("reading", Float.MIN_VALUE)
                // val status = resultCode
                JanaCareHb1AcRxBus.getInstance().post(JanacareResponse(CholesterolcomEventType.HDL,  AinaResponce(result = reading.toString(), lotNumber =  data?.getStringExtra("code_entered").toString())))
                Timber.d("reading " + reading)
            }

            AINA_REQUEST_CODE_HDL -> {
                JanaCareCholesterolcomRxBus.getInstance()
                    .post(JanacareResponse(CholesterolcomEventType.HDL,  AinaResponce(result = data?.getIntExtra("reading", 0).toString(), lotNumber =  data?.getStringExtra("code_entered").toString())))
            }
            AINA_REQUEST_CODE_TotalCholesterol -> {
                JanaCareCholesterolcomRxBus.getInstance().post(
                    JanacareResponse(
                        CholesterolcomEventType.TOTAL_CHOLESTEROLHDL,
                        AinaResponce(result = data?.getIntExtra("reading", 0).toString(), lotNumber =  data?.getStringExtra("code_entered").toString())
                    )
                )

            }
            AINA_REQUEST_CODE_Hemoglobin -> {
                JanaCareCholesterolcomRxBus.getInstance().post(
                    JanacareResponse(
                        CholesterolcomEventType.HEMOGLOBIN,
                        AinaResponce(result = data?.getFloatExtra("reading", Float.MIN_VALUE).toString(), lotNumber =  data?.getStringExtra("code_entered").toString())
                    )
                )

            }
            AINA_REQUEST_CODE_Triglycerol -> {
                JanaCareCholesterolcomRxBus.getInstance().post(
                    JanacareResponse(
                        CholesterolcomEventType.TRIGLYCERIDESCODE,
                        AinaResponce(result = data?.getIntExtra("reading", 0).toString(), lotNumber =  data?.getStringExtra("code_entered").toString())
                    )
                )

            }
        }
    }
}
