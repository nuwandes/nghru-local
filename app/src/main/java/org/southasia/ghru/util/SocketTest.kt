package org.southasia.ghru.util

import android.os.AsyncTask
import android.util.Log
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Socket

class SocketTest(var ips: String) : AsyncTask<String, String, String>() {

    override fun doInBackground(vararg ipaddress: String): String? {
        Log.d(TAG, "Let's sniff the network")

        val sockaddr = InetSocketAddress(ips, 8000)
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
            println("ONLINE")
        }

        return null
    }

    companion object {


        private val TAG = "SocketTestv" + "nstask"
    }

}