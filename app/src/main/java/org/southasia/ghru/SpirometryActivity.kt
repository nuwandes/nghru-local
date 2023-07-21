package org.southasia.ghru

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.nuvoair.sdk.launcher.*
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import org.southasia.ghru.event.SpirometryDeviceRecordTestRxBus
import org.southasia.ghru.event.SpirometryListRecordTestRxBus
import org.southasia.ghru.util.LocaleManager
import org.southasia.ghru.vo.SpirometryRecord
import javax.inject.Inject

class SpirometryActivity : AppCompatActivity(), HasSupportFragmentInjector {

    @Inject
    lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Fragment>

    lateinit var callbackManager: CallbackManager

    private var recordList: ArrayList<SpirometryRecord> = ArrayList()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.spirometry_activity)
        callbackManager = CallbackManager.Factory.create()

        NuvoairLauncherManager.getInstance()
            .registerCallback(callbackManager, object : LauncherCallback<NuvoairLauncherMeasurement> {

                override fun onMeasurementComplete(nuvoairLauncherMeasurement: NuvoairLauncherMeasurement) {
                    SpirometryDeviceRecordTestRxBus.getInstance().post(nuvoairLauncherMeasurement)

                }

                override fun onCancel() {
                    //renderStatus("Canceled")
                    Log.d("NuvoairLauncherManager", "Canceled")
                }

                override fun onError(
                    code: LauncherClient.Result.Code,
                    exception: NuvoairLauncherException,
                    originalRequest: LauncherClient.Request
                ) {
                    // renderStatus(exception.message)
                    Log.d("NuvoairLauncherManager", "exception.message ${exception.message}")

                }
            })
    }

    override fun supportFragmentInjector() = dispatchingAndroidInjector

    override fun onSupportNavigateUp(): Boolean {
        val currentDestination = Navigation.findNavController(this, R.id.container).currentDestination
        val parent = currentDestination?.parent
        if (parent == null || currentDestination.id != parent.id)
            super.onBackPressed()
        else
            onSupportNavigateUp()
        return true
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(LocaleManager(base).setLocale())
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        callbackManager.onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
    }
}