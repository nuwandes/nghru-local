package org.southasia.ghru

import android.app.Activity
import android.app.Application
import android.content.ComponentCallbacks2
import android.content.res.Configuration
import android.os.Bundle
import android.content.Context.KEYGUARD_SERVICE
import androidx.core.content.ContextCompat.getSystemService
import android.app.KeyguardManager

class AppLifecycleHandler(private val lifeCycleDelegate: LifecycleDelegate)
    : Application.ActivityLifecycleCallbacks, ComponentCallbacks2 {

    private var appInForeground = false

    override fun onActivityPaused(p0: Activity?) {}

    override fun onActivityResumed(p0: Activity?) {
        if (!appInForeground) {
            appInForeground = true
            lifeCycleDelegate.onAppForegrounded()
        }
    }

    override fun onActivityStarted(p0: Activity?) {
        lifeCycleDelegate.onScreenLocked()
    }

    override fun onActivityDestroyed(p0: Activity?) {
    }

    override fun onActivitySaveInstanceState(p0: Activity?, p1: Bundle?) {
        lifeCycleDelegate.onScreenLocked()
    }

    override fun onActivityStopped(p0: Activity?) {
    }

    override fun onActivityCreated(p0: Activity?, p1: Bundle?) {
    }

    override fun onLowMemory() {}

    override fun onConfigurationChanged(p0: Configuration?) {}

    override fun onTrimMemory(level: Int) {
        if (level == ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN) {
            appInForeground = false
            lifeCycleDelegate.onAppBackgrounded()
        }
    }

}