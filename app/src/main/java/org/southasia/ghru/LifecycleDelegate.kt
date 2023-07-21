package org.southasia.ghru

interface LifecycleDelegate {

    fun onAppBackgrounded()
    fun onAppForegrounded()
    fun onScreenLocked()

}