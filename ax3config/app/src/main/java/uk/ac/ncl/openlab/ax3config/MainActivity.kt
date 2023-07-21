/*
* Copyright (c) 2018, Newcastle University, UK.
* All rights reserved.
*
* Redistribution and use in source and binary forms, with or without
* modification, are permitted provided that the following conditions are met:
* 1. Redistributions of source code must retain the above copyright notice,
*    this list of conditions and the following disclaimer.
* 2. Redistributions in binary form must reproduce the above copyright notice,
*    this list of conditions and the following disclaimer in the documentation
*    and/or other materials provided with the distribution.
*
* THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
* AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
* IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
* ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
* LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
* CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
* SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
* INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
* CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
* ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
* POSSIBILITY OF SUCH DAMAGE.
*/

// Open Movement AX3 Configuration Tool for Android
// Dan Jackson, 2018

package uk.ac.ncl.openlab.ax3config

import android.app.Activity
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager.FEATURE_USB_HOST
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import kotlinx.android.synthetic.main.activity_main.*
import java.io.IOException
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*


private const val ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION"
private const val ACTION_USB_PERMISSION_READ = "com.android.example.USB_PERMISSION"


class MainActivity : AppCompatActivity() {

    private fun log(message: String) {
        //editTextLog.append(message + "\n")
        Log.d("message", message)
        textViewError.setText(message)
    }

    private lateinit var permissionIntent: PendingIntent

    private lateinit var usbManager: UsbManager

    private val usbReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (ACTION_USB_PERMISSION == intent.action) {
                synchronized(this) {
                    val device: UsbDevice? = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE)
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        device?.apply {
                            log("Permission allowed.")
                            connect(device)
                        }
                    } else {
                        log("Permission denied for device.")
                    }
                }
            }
            if (UsbManager.ACTION_USB_DEVICE_DETACHED == intent.action) {
                val device: UsbDevice? = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE)
                device?.apply {
                    log("DEVICE: Detached")
                }
            }
            if (UsbManager.ACTION_USB_DEVICE_ATTACHED == intent.action) {
                val device: UsbDevice? = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE)
                log("DEVICE: Attached")
                device?.apply {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        log("DEVICE: Attached ${device.serialNumber}")
                    } else {
                        // Serial number not known until device is opened
                        log("DEVICE: Attached")
                    }
                }
            }
        }
    }


    private lateinit var permissionIntentRead: PendingIntent

    private lateinit var usbManagerRead: UsbManager

    private val usbReceiverRead = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (ACTION_USB_PERMISSION_READ == intent.action) {
                synchronized(this) {
                    val device: UsbDevice? = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE)
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        device?.apply {
                            log("Permission allowed.")
                            connectRead(device)
                        }
                    } else {
                        log("Permission denied for device.")
                    }
                }
            }
            if (UsbManager.ACTION_USB_DEVICE_DETACHED == intent.action) {
                val device: UsbDevice? = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE)
                device?.apply {
                    log("DEVICE: Detached")
                }
            }
            if (UsbManager.ACTION_USB_DEVICE_ATTACHED == intent.action) {
                val device: UsbDevice? = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE)
                log("DEVICE: Attached")
                device?.apply {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        log("DEVICE: Attached ${device.serialNumber}")
                    } else {
                        // Serial number not known until device is opened
                        log("DEVICE: Attached")
                    }
                }
            }
        }
    }


    fun connectRead(device: UsbDevice) {
        log("CONNECT")
        val port = UsbSerialPort(device)
        try {
            // log("PORT: $port")
            port.open(usbManager)
            log("Connected to: Device ID " + port.serialNumber)

            val config = AxConfig(port)

            log("CHECKING CONFIGURATION...")
//1111555353535555543
            val existingSid = config.sessionId
            log("EXISTING: sid=$existingSid")
            val existingStart = config.startTime
            log("EXISTING: start=$existingStart")
            val existingEnd = config.endTime
            log("EXISTING: end=$existingEnd")

            if (config.hasConfiguration()) {
                log("WARNING: Device has an existing configuration -- overwriting anyway (would not normally do this!)...")
            }

            log("CHECKING BATTERY....")
            val minimumBattery = 80
            var battery = config.battery
            log("INFO: Device battery ($battery%) below minimum required ($minimumBattery%)")

            if (battery < minimumBattery) {
                log("ERROR: Device battery ($battery%) below minimum required ($minimumBattery%)")
            }


        } catch (e: IOException) {
            // Deal with error.
            log("IO EXCEPTION: ${e.message}")
            log("OPEN DEBUG LOG: ${port.debugInfo}");
        } catch (e: Exception) {
            // Deal with error.
            log("EXCEPTION: ${e.message}")
        } finally {
            log("Closing...")
            port.close()
            log("Closed")
        }
    }

    val now = Calendar.getInstance()
    val start = now.clone() as Calendar
    val end = start.clone() as Calendar


    fun connect(device: UsbDevice) {
        log("CONNECT")
        val port = UsbSerialPort(device)
        try {
            // log("PORT: $port")
            port.open(usbManager)
            log("Connected to: Device ID " + port.serialNumber)

            if (userID.isEmpty()) {       // normal configuration (not a custom command)
                val idStr = textInputEditTextParticipantIDNumber.text.toString()
                val id = idStr.toIntOrNull()
                if (id == null) {
                    log("ERROR: Invalid id")
                } else {
                    val config = AxConfig(port)

//                    log("CHECKING CONFIGURATION...")
//
//                     val existingSid = config.sessionId
//                     log("EXISTING: sid=$existingSid")
//                     val existingStart = config.startTime
//                     log("EXISTING: start=$existingStart")
//                     val existingEnd = config.endTime
//                     log("EXISTING: end=$existingEnd")

                    if (config.hasConfiguration()) {
                        log("WARNING: Device has an existing configuration -- overwriting anyway (would not normally do this!)...")
                    }

                    log("CHECKING BATTERY....")
                    val minimumBattery = 80
                    var battery = config.battery
                    log("INFO: Device battery ($battery%) below minimum required ($minimumBattery%)")

                    if (battery < minimumBattery) {
                        log("ERROR: Device battery ($battery%) below minimum required ($minimumBattery%)")
                    } else {
                        log("CONFIGURING: for id $id")



                        config.setSessionId(id)
                        config.setStartTime(start.time)
                        config.setEndTime(end.time)
                        config.setRate(100, 8)
                        config.setTime(now.time)
                        config.commit(true)
                        config.setLed(5)    // magenta
                        log("DONE");

                        val intent = Intent()
                        val formatter = DecimalFormat("000000000")
                        // val toInt = convertIDToDigits(userID)?.toInt()
                        val format = formatter.format(id)
                        val inputFormat = SimpleDateFormat("yyyy/MM/dd' 'HH:mm:ss", Locale.US)
                        intent.putExtra("sesstion_id", format)
                        intent.putExtra("start_time", inputFormat.format(start.time)) // hard-code value for testing
                        intent.putExtra("end_time", inputFormat.format(end.time)) // hard-code value for testing
                        intent.putExtra("serial_number", port.serialNumber)
                        setResult(Activity.RESULT_OK, intent)
                        super.finish()
                    }
                }
            } else {
                // Custom command to device
//                text += "\r\n"
//
//                log("SEND: $text")
//
//                val written = port.writeString(text, 2000)
//                log("WRITTEN: $written")
//
//                val lines = port.readLines(2000, 250, null)
//                log("READ: ${lines.size}")
//                for (line in lines) {
//                    log("<<< $line")
//                }
            }
        } catch (e: IOException) {
            // Deal with error.
            log("IO EXCEPTION: ${e.message}")
            log("OPEN DEBUG LOG: ${port.debugInfo}");
        } catch (e: Exception) {
            // Deal with error.
            log("EXCEPTION: ${e.message}")
        } finally {
            log("Closing...")
            port.close()
            log("Closed")
        }
    }

    val userID: String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        log("Started.")
        if (intent?.hasExtra("data")!!) {
            val data: Bundle = intent?.extras?.getBundle("data")!!
            //println("data ${data.getString("user")}")
            val userID = data.getString("user")!!
            textInputEditTextParticipantID.setText(userID)
            val formatter = DecimalFormat("000000000")

            val toInt = convertIDToDigits(userID)?.toInt()
            val format = formatter.format(toInt)
            textInputEditTextParticipantIDNumber.setText(format.replace("0", "1"))
            end.add(Calendar.HOUR, 7 * 24);
            val inputFormat = SimpleDateFormat("yyyy/MM/dd' 'HH:mm:ss", Locale.US)

            textInputEditTextStarTime.setText(inputFormat.format(start.time))
            textInputEditTextEndTime.setText(inputFormat.format(end.time))
        }

        if (!this.applicationContext.packageManager.hasSystemFeature(FEATURE_USB_HOST))
            log("WARNING: This system is not a USB host")

        permissionIntent = PendingIntent.getBroadcast(this, 0, Intent(ACTION_USB_PERMISSION), 0)
        permissionIntentRead = PendingIntent.getBroadcast(this, 0, Intent(ACTION_USB_PERMISSION), 0)
        val filter = IntentFilter(ACTION_USB_PERMISSION)
        val filterRead = IntentFilter(ACTION_USB_PERMISSION_READ)

        registerReceiver(usbReceiver, filter)
       // registerReceiver(usbReceiverRead, filterRead)

        usbManager = getSystemService(Context.USB_SERVICE) as UsbManager
        usbManagerRead = getSystemService(Context.USB_SERVICE) as UsbManager


//        editTextInput.setOnEditorActionListener { _, actionId, _ ->
//            return@setOnEditorActionListener when (actionId) {
//                EditorInfo.IME_ACTION_SEND -> {
//                    buttonSend.performClick()
//                    true
//                }
//                else -> false
//            }
//        }

        button.setOnClickListener {
            var usbDevices = UsbSerialPort.getDevices(usbManager);
            // Find device from attached devices
            // log("(${usbDevices.size} devices)")
            if (usbDevices.isNotEmpty()) {
                var usbDevice = usbDevices[0];
                // log("DEVICE: ${usbDevice.manufacturerName} // ${usbDevice.productName} // ${usbDevice.serialNumber}")
                if (!usbManager.hasPermission(usbDevice)) {
                    log("DEVICE: Requesting permission...")
                }
                // request anyway so the code path is the same
                usbManager.requestPermission(usbDevice, permissionIntent)
            } else {
                log("DEVICE: No devices.")
            }
            log("---")
        }


        buttonRead.setOnClickListener {
            var usbDevices = UsbSerialPort.getDevices(usbManagerRead);
            // Find device from attached devices
            // log("(${usbDevices.size} devices)")
            if (usbDevices.isNotEmpty()) {
                var usbDevice = usbDevices[0];
                // log("DEVICE: ${usbDevice.manufacturerName} // ${usbDevice.productName} // ${usbDevice.serialNumber}")
                if (!usbManagerRead.hasPermission(usbDevice)) {
                    log("DEVICE: Requesting permission...")
                }
                // request anyway so the code path is the same
                usbManagerRead.requestPermission(usbDevice, permissionIntentRead)
            } else {
                log("DEVICE: No devices.")
            }
            log("---")
        }
    }


    override fun onPause() {
        super.onPause()
       // unregisterReceiver(usbReceiver);
        //unregisterReceiver(usbReceiverRead);
    }


    fun convertIDToDigits(string: String): String? {

        var digits: String? = string.substringAfterLast("-")
        var lastFourDigits : String? = string.substring(4,8)
        val chars: String? = string.substringBefore("-")

        val idList : MutableList<Int> = ArrayList<Int>()
        idList.add(lastFourDigits?.toInt()!!)
        idList.add(digits?.toInt()!!)

        val charsList = chars?.reversed()
        charsList?.forEach {
            val num = it.toUpperCase().toInt()
            idList?.add(num)

           // digits = num.toString() + digits
        }
        idList?.shuffle()
        var sessionID : String? = ""
        idList?.forEach{
            sessionID = sessionID + it.toString()
        }
        sessionID = sessionID?.substring(0,7)
        print("string $string digits $sessionID")
        return  sessionID// digits
    }


    override fun finish() {
        val intent = Intent()

        setResult(Activity.RESULT_OK, intent)
        super.finish()
    }
}

