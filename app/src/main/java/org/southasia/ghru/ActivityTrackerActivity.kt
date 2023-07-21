package org.southasia.ghru

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import org.southasia.ghru.event.AxivityRxBus
import org.southasia.ghru.util.LocaleManager
import org.southasia.ghru.vo.Axivity
import timber.log.Timber
import javax.inject.Inject

class ActivityTrackerActivity : AppCompatActivity(), HasSupportFragmentInjector {

    @Inject
    lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Fragment>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tracker_activity)

    }

    override fun supportFragmentInjector() = dispatchingAndroidInjector
    val ACTIVITY_TRACKER_REQUEST_CODE = 1200
    override fun onSupportNavigateUp(): Boolean {
        val currentDestination = Navigation.findNavController(this, R.id.container).currentDestination
        val parent = currentDestination?.parent
        if (parent == null || currentDestination.id != parent.id)
            super.onBackPressed()
        else
            onSupportNavigateUp()
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            ACTIVITY_TRACKER_REQUEST_CODE -> {
                Timber.d("sesstion_id" + data?.getStringExtra("sesstion_id"))
                Timber.d("start_time" + data?.getStringExtra("start_time"))
                Timber.d("end_time" + data?.getStringExtra("end_time"))
                val axivity = Axivity(
                    sessionId = data?.getStringExtra("sesstion_id"),
                    endTime = data?.getStringExtra("end_time"),
                    startTime = data?.getStringExtra("start_time"),
                    serialNumber = data?.getIntExtra("serial_number", 0).toString()
                )
                AxivityRxBus.getInstance().post(axivity)
            }
        }
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(LocaleManager(base).setLocale())
    }


}