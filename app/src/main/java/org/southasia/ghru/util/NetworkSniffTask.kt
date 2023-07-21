package org.southasia.ghru.util

import android.content.Context
import android.os.AsyncTask
import android.util.Log
import androidx.lifecycle.MutableLiveData
import java.io.IOException
import java.lang.ref.WeakReference
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket

class NetworkSniffTask(var context: Context, var ips: List<String>?) : AsyncTask<Void, Void, String>() {

    private val mContextRef: WeakReference<Context>
    val result = MutableLiveData<String>()


    init {
        mContextRef = WeakReference(context)
    }

    override fun doInBackground(vararg voids: Void): String? {
        Log.d(TAG, "Let's sniff the network")

        try {
            val context = mContextRef.get()

            if (context != null) {

//                val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
//                val activeNetwork = cm.activeNetworkInfo
//                val wm = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
//
//                val connectionInfo = wm.connectionInfo
//                val ipAddress = connectionInfo.ipAddress
//                val ipString = Formatter.formatIpAddress(ipAddress)
//
//
//                Log.d(TAG, "activeNetwork: " + activeNetwork.toString())
//                Log.d(TAG, "ipString: " + ipString.toString())
//
//                val prefix = ipString.substring(0, ipString.lastIndexOf(".") + 1)
//                Log.d(TAG, "prefix: $prefix")

                ips?.forEach {
                    println("NetworkSniffTask $it")
                    val address = InetAddress.getByName(it.toString())
                    val reachable = address.isReachable(1000)
                    val hostName = address.canonicalHostName

                    if (reachable) {
                        val sockaddr = InetSocketAddress(hostName, 8000)
                        val socket = Socket()
                        var online = true
                        try {
                            socket.connect(sockaddr, 10000)
                        } catch (e: IOException) {
                            e.printStackTrace()
                            online = false
                        } finally {
                            // As the close() operation can also throw an IOException
                            // it must caught here
                            try {
                                socket.close()
                            } catch (ex: IOException) {
                                // feel free to do something moderately useful here, eg log the event
                            }

                        }
                        if (!online) {
                            println("OFFLINE: Restarting Server..")
                            //Runtime.getRuntime().exec("cmd /c start start.bat");
                        }
                        if (online) {
                            return hostName;
                        }
                    }
                }

//                for (i in 0..254) {
//                    val testIp = prefix + i.toString()
//
//                    val address = InetAddress.getByName(testIp)
//                    val reachable = address.isReachable(1000)
//                    val hostName = address.canonicalHostName
//
//                    if (reachable)
//                        Log.i(TAG, "Host: " + hostName.toString() + "(" + testIp + ") is reachable!")
//                }
            }
        } catch (t: Throwable) {
            Log.e(TAG, "Well that's not good.", t)
        }

        return null
    }


    override fun onPostExecute(ip: String?) {
        super.onPostExecute(ip)
        println("selected ip $ip")
        result.value = ip

    }

    companion object {

        private val TAG = NetworkSniffTask::class.java.simpleName + "nstask"
    }
}