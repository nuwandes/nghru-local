package org.southasia.ghru.ui.bodymeasurements.bp


import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice.ACTION_PAIRING_REQUEST
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.os.ParcelUuid
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingComponent
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import jp.co.omron.healthcare.samplelibs.ble.blenativewrapper.*
import org.southasia.ghru.AppExecutors
import org.southasia.ghru.R
import org.southasia.ghru.binding.FragmentDataBindingComponent
import org.southasia.ghru.databinding.BpFragmentBinding
import org.southasia.ghru.di.Injectable
import org.southasia.ghru.ui.scan.BleScanActivity
import org.southasia.ghru.util.LocaleManager
import org.southasia.ghru.util.autoCleared
import org.southasia.ghru.util.singleClick
import timber.log.Timber
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.experimental.and

class BPFragment : Fragment(), Injectable {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    @Inject
    lateinit var appExecutors: AppExecutors

    @Inject
    lateinit var localeManager: LocaleManager

    var binding by autoCleared<BpFragmentBinding>()
    var dataBindingComponent: DataBindingComponent = FragmentDataBindingComponent(this)
    @Inject
    lateinit var verifyIDViewModel: BPViewModel

    private var mIsBluetoothOn: Boolean = false

    private var mTargetPeripheral: BlePeripheral? = null
    private var mBleCommunicationExecutor: BleCommunicationExecutor? = null
    private var mScanFilteringServiceUuids: Array<UUID>? = null
    private val REQUEST_CODE_SCAN = 1
    private var mIsCtsWritten: Boolean = false


    private val mPairingRequestReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val dataBinding = DataBindingUtil.inflate<BpFragmentBinding>(
            inflater,
            R.layout.bp_fragment,
            container,
            false
        )
        binding = dataBinding
        setHasOptionsMenu(true)
        val appCompatActivity = requireActivity() as AppCompatActivity
        appCompatActivity.setSupportActionBar(binding.detailToolbar)
        appCompatActivity.supportActionBar?.setDisplayHomeAsUpEnabled(true)

        mMessageHandler = object : Handler() {
            override fun handleMessage(msg: Message) {
                if (!activity?.isDestroyed()!!) {
                    onReceiveMessage(msg)
                }
            }
        }

        val uuids = arrayOf(GattUUID.Service.BloodPressureService.uuid)
        setScanFilteringServiceUuids(uuids)
        return dataBinding.root
    }

    protected fun setScanFilteringServiceUuids(serviceUuids: Array<UUID>) {
        mScanFilteringServiceUuids = serviceUuids
    }

    fun byteDataToHexString(data: ByteArray?): String? {
        if (null == data) {
            return null
        }
        val sb = StringBuilder()
        sb.append("0x")
        for (b in data) {
            sb.append(String.format(Locale.US, "%02x", b))
        }
        return sb.toString()
    }


    protected fun onReceiveMessage(msg: Message) {
        val messageType = MessageType.values()[msg.what]
        when (messageType) {
            MessageType.BluetoothOff -> {
                mIsBluetoothOn = false
            }
            MessageType.BluetoothOn -> {
                mIsBluetoothOn = true
            }
            MessageType.ConnectionCompleted -> {
                //TimberbleInfo("Connect to " + mTargetPeripheral.getLocalName() + "(" + mTargetPeripheral.getAddress() + ")")
                binding.mConnectBtn.setText("Disconnect")
                startCommunication()
            }
            MessageType.ConnectionFailed -> {
                binding.mConnectBtn.setText(R.string.connect)
            }
            MessageType.DisconnectionCompleted -> {
                binding.mConnectBtn.setText(R.string.connect)
                binding.mConnectBtn.setEnabled(true)
            }
            MessageType.DisconnectionFailed -> if (StateInfo.ConnectionState.Disconnected == mTargetPeripheral?.getStateInfo()!!.getConnectionState()) {
                binding.mConnectBtn.setEnabled(true)
            }
            MessageType.DidDisconnection -> {
                //Timberi("Disconnection by peripheral or OS.")
                binding.mConnectBtn.setText(R.string.connect)
                stopIndicationWaitTimer()
                mBleCommunicationExecutor?.clear()
            }
            MessageType.BondStateChanged -> {
                val bondState = msg.obj as StateInfo.BondState
                if (mTargetPeripheral?.getStateInfo()!!.isConnected() && StateInfo.BondState.Bonded == bondState) {
                    // The IndicationWaitTimer will start when both of indication of
                    // BPM or WM is registered and in Bonded state. The timer will startwhen the state is Bonded
                    // because indication of BPM or WM is running in no Nonded state.
                    startIndicationWaitTimer()
                }
            }
            MessageType.AclConnectionStateChanged -> {
                // val aclConnectionState = msg.obj as StateInfo.AclConnectionState
                // mAclStatusView.setText(aclConnectionState.name)
            }
            MessageType.GattConnectionStateChanged -> {
                // val gattConnectionState = msg.obj as StateInfo.GattConnectionState
                //mGattStatusView.setText(gattConnectionState.name)
            }
            MessageType.DetailedStateChanged -> if (mIsBluetoothOn) {
                // val detailedState = msg.obj as StateInfo.DetailedState
                //mDetailedStateView.setText(detailedState.name)
            }
            MessageType.BatteryDataRcv -> {
                // val batteryData = msg.obj as ByteArray
                //TimberbleInfo("Battery Level Raw Data:" + Utils.byteDataToHexString(batteryData))
                // val batteryLevel = batteryData[0].toInt()
                //TimberbleInfo("Battery Level Data:$batteryLevel")
            }
            MessageType.CTSDataRcv -> {
//                val ctsData = msg.obj as ByteArray
//                //TimberbleInfo("Current Time Raw Data:" + Utils.byteDataToHexString(ctsData))
//                val buf = ByteArray(2)
//                System.arraycopy(ctsData, 0, buf, 0, 2)
//                val ctsYearByteBuffer = ByteBuffer.wrap(buf)
//                ctsYearByteBuffer.order(ByteOrder.LITTLE_ENDIAN)
//                val ctsYear = ctsYearByteBuffer.short.toInt()
//                val ctsMonth = ctsData[2].toInt()
//                val ctsDay = ctsData[3].toInt()
//                val ctsHour = ctsData[4].toInt()
//                val ctsMinute = ctsData[5].toInt()
//                val ctsSecond = ctsData[6].toInt()
//                val AdjustReason = ctsData[9]
//                val ctsTime = String.format(Locale.US,
//                        "%1$04d-%2$02d-%3$02d %4$02d:%5$02d:%6$02d",
//                        ctsYear, ctsMonth, ctsDay, ctsHour, ctsMinute, ctsSecond)
                // mCtsView.setText(ctsTime)
                //TimberbleInfo("CTS Data:$ctsTime (AdjustReason:$AdjustReason)")

//                if (SettingsFragment.isWriteCTS(this) && !mIsCtsWritten) {
//                    //Timberd("Write CTS")
//                    val characteristic = mTargetPeripheral.getCharacteristic(GattUUID.Characteristic.CurrentTimeCharacteristic.uuid)
//                    if (null == characteristic) {
//                        //Timbere("null == characteristic")
//                        break
//                    }
//                    val currentTimeData = getCurrentTimeData()
//                    characteristic!!.setValue(currentTimeData)
//                    mBleCommunicationExecutor.add(BleEvent(BleEvent.Type.WriteCharacteristic, characteristic))
//                    if (!mBleCommunicationExecutor.isExecuting()) {
//                        mBleCommunicationExecutor.exec()
//                    }
//                }
            }
            MessageType.BPMDataRcv -> restartIndicationWaitTimer()
            MessageType.WMDataRcv -> restartIndicationWaitTimer()
            MessageType.IndicationWaitTimeout -> {
                //Timbere("Indication wait timeout.")
                disconnect(mTargetPeripheral!!, DisconnectReason.IndicationWaitTimeout)
            }
            else -> {
            }
        }


        val data: ByteArray
        val buf = ByteArray(2)
        var byteBuffer: ByteBuffer

        // Timber.d(messageType.name())
        when (messageType) {
            MessageType.BPMDataRcv -> {

                var idx = 0
                data = msg.obj as ByteArray
                Timber.d("Blood Pressure Measurement Raw Data:" + byteDataToHexString(data))

                val flags = data[idx++]

                // 0: mmHg	1: kPa
                val kPa = flags and 0x01 > 0
                // 0: No Timestamp info 1: With Timestamp info
                val timestampFlag = flags and 0x02 > 0
                // 0: No PlseRate info 1: With PulseRate info
                val pulseRateFlag = flags and 0x04 > 0
                // 0: No UserID info 1: With UserID info
                val userIdFlag = flags and 0x08 > 0
                // 0: No MeasurementStatus info 1: With MeasurementStatus info
                val measurementStatusFlag = flags and 0x10 > 0

                // Set BloodPressureMeasurement unit
                val unit: String
                if (kPa) {
                    unit = "kPa"
                } else {
                    unit = "mmHg"
                }

                // Parse Blood Pressure Measurement
                val systolicVal: Short
                val diastolicVal: Short
                val meanApVal: Short

                System.arraycopy(data, idx, buf, 0, 2)
                idx += 2
                byteBuffer = ByteBuffer.wrap(buf)
                byteBuffer.order(ByteOrder.LITTLE_ENDIAN)
                systolicVal = byteBuffer.short

                System.arraycopy(data, idx, buf, 0, 2)
                idx += 2
                byteBuffer = ByteBuffer.wrap(buf)
                byteBuffer.order(ByteOrder.LITTLE_ENDIAN)
                diastolicVal = byteBuffer.short

                System.arraycopy(data, idx, buf, 0, 2)
                idx += 2
                byteBuffer = ByteBuffer.wrap(buf)
                byteBuffer.order(ByteOrder.LITTLE_ENDIAN)
                meanApVal = byteBuffer.short

                Timber.d("systolicValue:$systolicVal $unit")
                Timber.d("diastolicValue:$diastolicVal $unit")
                Timber.d("meanApValue:$meanApVal $unit")

                binding.mSystolicView.setText(java.lang.Float.toString(systolicVal.toFloat()) + " " + unit)
                binding.mDiastolicView.setText(java.lang.Float.toString(diastolicVal.toFloat()) + " " + unit)
                binding.mMeanApView.setText(java.lang.Float.toString(meanApVal.toFloat()) + " " + unit)

                // Parse Timestamp
                var timestampStr = "----"
                var dateStr = "--"
                var timeStr = "--"
                if (timestampFlag) {
                    System.arraycopy(data, idx, buf, 0, 2)
                    idx += 2
                    byteBuffer = ByteBuffer.wrap(buf)
                    byteBuffer.order(ByteOrder.LITTLE_ENDIAN)

                    val year = byteBuffer.short.toInt()
                    val month = data[idx++].toInt()
                    val day = data[idx++].toInt()
                    val hour = data[idx++].toInt()
                    val min = data[idx++].toInt()
                    val sec = data[idx++].toInt()

                    dateStr = String.format(Locale.US, "%1$04d-%2$02d-%3$02d", year, month, day)
                    timeStr = String.format(Locale.US, "%1$02d:%2$02d:%3$02d", hour, min, sec)
                    timestampStr = "$dateStr $timeStr"
                    Timber.d("Timestamp Data:$timestampStr")
                }
                binding.mTimestampView.setText(timestampStr)

                // Parse PulseRate
                var pulseRateVal: Short
                var pulseRateStr = "----"
                if (pulseRateFlag) {
                    System.arraycopy(data, idx, buf, 0, 2)
                    idx += 2
                    byteBuffer = ByteBuffer.wrap(buf)
                    byteBuffer.order(ByteOrder.LITTLE_ENDIAN)
                    pulseRateVal = byteBuffer.short
                    pulseRateStr = java.lang.Short.toString(pulseRateVal)
                    Timber.d("PulseRate Data:$pulseRateStr")
                }
                binding.mPulseRateView.setText(pulseRateStr)

                // Parse UserID
                var userIDVal: Int
                var userIDStr = "----"
                if (userIdFlag) {
                    userIDVal = data[idx++].toInt()
                    userIDStr = userIDVal.toString()
                    Timber.d("UserID Data:$userIDStr")
                }
                binding.mUserIDView.setText(userIDStr)

                // Parse Measurement Status
                var measurementStatusVal: Int
                var measurementStatusStr: String
                if (measurementStatusFlag) {
                    System.arraycopy(data, idx, buf, 0, 2)
                    idx += 2
                    byteBuffer = ByteBuffer.wrap(buf)
                    byteBuffer.order(ByteOrder.LITTLE_ENDIAN)
                    measurementStatusVal = byteBuffer.short.toInt()
                    measurementStatusStr = String.format(Locale.US, "%1$04x", measurementStatusVal.toShort())
                    Timber.d("MeasurementStatus Data:$measurementStatusStr")

                    binding.mBodyMovementView.setText(if (measurementStatusVal and 0x0001 == 0) "No" else "Yes")
                    binding.mIrregularPulseView.setText(if (measurementStatusVal and 0x0004 == 0) "No" else "Yes")
                } else {
                    binding.mBodyMovementView.setText("----")
                    binding.mIrregularPulseView.setText("----")
                }

                // Output to History
                Timber.d("Add history")
//                val entry = (timestampStr
//                        + "," + systolicVal
//                        + "," + diastolicVal
//                        + "," + meanApVal
//                        + "," + pulseRateStr
//                        + "," + String.format(Locale.US, "%1$02x", flags)
//                        + "," + measurementStatusStr)

                // Output log for data aggregation
                // Timber format: ## For aggregation ## timestamp(date), timestamp(time), systolic, diastolic, meanAP, current date time
                val c = Calendar.getInstance()
                val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                var agg = "## For aggregation ## "
                agg += "$dateStr,$timeStr"
                agg += ",$systolicVal,$diastolicVal,$meanApVal"
                agg += "," + sdf.format(c.time)
                Timber.i(agg)
            }

            MessageType.BPFDataRcv -> {
                data = msg.obj as ByteArray
                Timber.d("Blood Pressure Feature Raw Data:" + byteDataToHexString(data))
                System.arraycopy(data, 0, buf, 0, 2)
                val bpfByteBuffer = ByteBuffer.wrap(buf)
                bpfByteBuffer.order(ByteOrder.LITTLE_ENDIAN)
                val bpfVal = bpfByteBuffer.short
                val bpfStr = String.format(Locale.US, "%1$04x", bpfVal)
                Timber.d("Blood Pressure Feature Data:$bpfStr")
            }

            else -> {
            }
        }
    }

    private fun stopIndicationWaitTimer() {
        mMessageHandler?.removeMessages(MessageType.IndicationWaitTimeout.ordinal)
    }

    private fun restartIndicationWaitTimer() {
        stopIndicationWaitTimer()
        startIndicationWaitTimer()
    }

    private fun startIndicationWaitTimer() {
        mMessageHandler?.sendMessageDelayed(
            Message.obtain(
                mMessageHandler,
                MessageType.IndicationWaitTimeout.ordinal
            ), INDICATION_WAIT_TIME.toLong()
        )
    }

    private val INDICATION_WAIT_TIME = 1000 * 10

    private fun getCurrentTimeData(): ByteArray {
        val data = ByteArray(10)
        val cal = Calendar.getInstance()
        val year = cal.get(Calendar.YEAR)
        data[0] = year.toByte()
        data[1] = (year shr 8 and 0xFF).toByte()
        data[2] = (cal.get(Calendar.MONTH) + 1).toByte()
        data[3] = cal.get(Calendar.DAY_OF_MONTH).toByte()
        data[4] = cal.get(Calendar.HOUR_OF_DAY).toByte()
        data[5] = cal.get(Calendar.MINUTE).toByte()
        data[6] = cal.get(Calendar.SECOND).toByte()
        data[7] = ((cal.get(Calendar.DAY_OF_WEEK) + 5) % 7 + 1).toByte() // Rotate
        data[8] = (cal.get(Calendar.MILLISECOND) * 256 / 1000).toByte() // Fractions256
        data[9] = 0x01 // Adjust Reason: Manual time update

        val date = year.toString() + "/" + data[2] + "/" + data[3] + " " +
                String.format(Locale.US, "%1$02d:%2$02d:%3$02d", data[4], data[5], data[6]) +
                " (WeekOfDay:" + data[7] + " Fractions256:" + data[8] + " AdjustReason:" + data[9] + ")"
        val sb = StringBuilder("")
        for (b in data) {
            sb.append(String.format(Locale.US, "%02x,", b))
        }
        //TimberbleInfo("CTS Tx Time:$date")
        //TimberbleInfo("CTS Tx Data:" + sb.toString())
        return data
    }


    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (REQUEST_CODE_SCAN != requestCode) {
            return
        }
//        if (BleScanActivity.RESPONSE_CODE_CONNECT !== resultCode) {
//            return
//        }

        val discoverPeripheral =
            data!!.getParcelableExtra<DiscoverPeripheral>(BleScanActivity.EXTRA_CONNECT_REQUEST_PERIPHERAL)
        if (null == discoverPeripheral) {
            return
        }


        onConnect(discoverPeripheral)
    }


    protected fun onConnect(discoverPeripheral: DiscoverPeripheral) {

        val blePeripheral = BlePeripheral(activity!!, discoverPeripheral)


        blePeripheral.connect(object : BlePeripheral.ActionReceiver {
            override fun didDisconnection(address: String) {
                mMessageHandler?.sendMessage(Message.obtain(mMessageHandler, MessageType.DidDisconnection.ordinal))
            }

            override fun onCharacteristicChanged(address: String, characteristic: BluetoothGattCharacteristic) {
                if (GattUUID.Characteristic.BloodPressureMeasurementCharacteristic.uuid == characteristic.uuid) {
                    mMessageHandler?.sendMessage(
                        Message.obtain(
                            mMessageHandler,
                            MessageType.BPMDataRcv.ordinal,
                            characteristic.value
                        )
                    )
                } else if (GattUUID.Characteristic.WeightMeasurementCharacteristic.uuid == characteristic.uuid) {
                    mMessageHandler?.sendMessage(
                        Message.obtain(
                            mMessageHandler,
                            MessageType.WMDataRcv.ordinal,
                            characteristic.value
                        )
                    )
                } else if (GattUUID.Characteristic.BatteryLevelCharacteristic.uuid == characteristic.uuid) {
                    mMessageHandler?.sendMessage(
                        Message.obtain(
                            mMessageHandler,
                            MessageType.BatteryDataRcv.ordinal,
                            characteristic.value
                        )
                    )
                } else if (GattUUID.Characteristic.CurrentTimeCharacteristic.uuid == characteristic.uuid) {
                    mMessageHandler?.sendMessage(
                        Message.obtain(
                            mMessageHandler,
                            MessageType.CTSDataRcv.ordinal,
                            characteristic.value
                        )
                    )
                }
            }
        }, { _, errorCode ->
            if (null == errorCode) {
                mMessageHandler?.sendMessage(Message.obtain(mMessageHandler, MessageType.ConnectionCompleted.ordinal))
            } else {
                mMessageHandler?.sendMessage(
                    Message.obtain(
                        mMessageHandler,
                        MessageType.ConnectionFailed.ordinal,
                        errorCode
                    )
                )
            }
        }, object : StateInfo.StateMonitor {
            override fun onBondStateChanged(bondState: StateInfo.BondState) {
                mMessageHandler?.sendMessage(
                    Message.obtain(
                        mMessageHandler,
                        MessageType.BondStateChanged.ordinal,
                        bondState
                    )
                )
            }

            override fun onAclConnectionStateChanged(aclConnectionState: StateInfo.AclConnectionState) {
                mMessageHandler?.sendMessage(
                    Message.obtain(
                        mMessageHandler,
                        MessageType.AclConnectionStateChanged.ordinal,
                        aclConnectionState
                    )
                )
            }

            override fun onGattConnectionStateChanged(gattConnectionState: StateInfo.GattConnectionState) {
                mMessageHandler?.sendMessage(
                    Message.obtain(
                        mMessageHandler,
                        MessageType.GattConnectionStateChanged.ordinal,
                        gattConnectionState
                    )
                )
            }

            override fun onConnectionStateChanged(connectionState: StateInfo.ConnectionState) {
            }

            override fun onDetailedStateChanged(detailedState: StateInfo.DetailedState) {
                mMessageHandler?.sendMessage(
                    Message.obtain(
                        mMessageHandler,
                        MessageType.DetailedStateChanged.ordinal,
                        detailedState
                    )
                )
            }
        })

        mTargetPeripheral = blePeripheral

        mBleCommunicationExecutor = BleCommunicationExecutor(mTargetPeripheral!!, object : Handler() {
            override fun handleMessage(msg: Message) {
                onBleCommunicationComplete(msg)
            }
        })

        mIsCtsWritten = false
//        mLocalNameView.setText(mTargetPeripheral.getLocalName())
//        mAddressView.setText(mTargetPeripheral.getAddress())
//        updateConnectionView(mTargetPeripheral)
        binding.mConnectBtn.setText(R.string.connecting)
    }


    private fun onBleCommunicationComplete(msg: Message) {
        val type = BleEvent.Type.values()[msg.what]
        val objects = msg.obj as Array<*>
        val characteristic = objects[0] as BluetoothGattCharacteristic
        val gattStatus = objects[1] as Int
//        val errorCode = objects[2] as ErrorCode
//        if (null != errorCode) {
//            disconnect(mTargetPeripheral!!, DisconnectReason.CommunicationError)
//            return
//        }
        when (type) {
            BleEvent.Type.SetNotification -> {
                if (GattStatusCode.GATT_SUCCESS != gattStatus) {
                    disconnect(mTargetPeripheral!!, DisconnectReason.GattStatusError)
                }
                mBleCommunicationExecutor?.exec()
            }
            BleEvent.Type.SetIndication -> {
                if (GattStatusCode.GATT_SUCCESS != gattStatus) {
                    disconnect(mTargetPeripheral!!, DisconnectReason.GattStatusError)
                }
                mBleCommunicationExecutor?.exec()
                if (mTargetPeripheral?.getStateInfo()!!.isBonded) {
                    // The IndicationWaitTimer will start when both of indication of
                    // BPM or WM is registered and in Bonded state.The timer will start when the state is Bonded
                    // because indication of BPM or WM is running in Bonded state.
                    startIndicationWaitTimer()
                }
            }
            BleEvent.Type.WriteCharacteristic -> {
                if (GattUUID.Characteristic.CurrentTimeCharacteristic.uuid == characteristic.uuid) {
                    mIsCtsWritten = true
                    if (GattStatusCode.GATT_SUCCESS == gattStatus) {
                        mBleCommunicationExecutor?.exec()
                    } else if (GattStatusCode.GATT_NO_RESOURCES == gattStatus) {   // 0x80: Write Request Rejected
                        // If the slave sends error response in CTS,
                        // you don't retry and should send next request.
                        mBleCommunicationExecutor?.exec()
                    } else if (GattStatusCode.GATT_ERROR == gattStatus) {   // 0x85: Write Request Rejected
                        Timber.w("Write Request Rejected. (0x85)")
                        // The status, 0x80 (Data filed ignored) will be notified same status to the application
                        // but there are cases when notified other status, 0x85 to the application in some smartphones.
                        // So the application need to regard as 0x80 only for Current Time Characteristic.
                        mBleCommunicationExecutor?.exec()
                    } else {
                        Timber.e("Invalid gatt status. status:$gattStatus")
                        disconnect(mTargetPeripheral!!, DisconnectReason.GattStatusError)
                    }
                } else {
                    if (GattStatusCode.GATT_SUCCESS == gattStatus) {
                        mBleCommunicationExecutor?.exec()
                    } else {
                        Timber.e("Invalid gatt status. status:$gattStatus")
                        disconnect(mTargetPeripheral!!, DisconnectReason.GattStatusError)
                    }
                }
            }
            BleEvent.Type.ReadCharacteristic -> {
                if (GattStatusCode.GATT_SUCCESS != gattStatus) {
                    Timber.e("Invalid gatt status. status:$gattStatus")
                    disconnect(mTargetPeripheral!!, DisconnectReason.GattStatusError)

                }
                if (GattUUID.Characteristic.BloodPressureFeatureCharacteristic.uuid == characteristic.uuid) {
                    mMessageHandler?.sendMessage(
                        Message.obtain(
                            mMessageHandler,
                            MessageType.BPFDataRcv.ordinal,
                            characteristic.value
                        )
                    )
                }
                if (GattUUID.Characteristic.WeightScaleFeatureCharacteristic.uuid == characteristic.uuid) {
                    mMessageHandler?.sendMessage(
                        Message.obtain(
                            mMessageHandler,
                            MessageType.WSFDataRcv.ordinal,
                            characteristic.value
                        )
                    )
                }
                mBleCommunicationExecutor?.exec()
            }
        }
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.nextButton.singleClick({
            navController().navigate(R.id.action_bPFragment_to_reviewFragment)
        })
        binding.previousButton.singleClick({
            navController().popBackStack()
        })

        binding.mConnectBtn.singleClick({
            if (binding.mConnectBtn.getText() == getString(R.string.connect)) {
                startScanListView()

            } else if (binding.mConnectBtn.getText() == getString(R.string.connecting)) {
                disconnect(mTargetPeripheral!!, DisconnectReason.UserRequest)
            } else if (binding.mConnectBtn.getText() == getString(R.string.disconnect)) {
                disconnect(mTargetPeripheral!!, DisconnectReason.UserRequest)
            }
        })
    }


    private fun startScanListView() {

        val intent = Intent(activity, BleScanActivity::class.java)
        val parcelUuidList = ArrayList<ParcelUuid>()
        for (uuid in mScanFilteringServiceUuids!!) {
            parcelUuidList.add(ParcelUuid(uuid))
        }
        intent.putParcelableArrayListExtra(BleScanActivity.EXTRA_SCAN_FILTERING_SERVICE_UUIDS, parcelUuidList)
        startActivityForResult(intent, REQUEST_CODE_SCAN)
    }

    private fun startCommunication() {
        // var characteristic: BluetoothGattCharacteristic?
        //  if (null != (characteristic = mTargetPeripheral.getCharacteristic(GattUUID.Characteristic.BatteryLevelCharacteristic.uuid))) {
        mBleCommunicationExecutor?.add(
            BleEvent(
                BleEvent.Type.SetNotification,
                mTargetPeripheral?.getCharacteristic(GattUUID.Characteristic.BatteryLevelCharacteristic.uuid)!!
            )
        )
        // }
        mBleCommunicationExecutor?.add(
            BleEvent(
                BleEvent.Type.SetNotification,
                mTargetPeripheral?.getCharacteristic(GattUUID.Characteristic.CurrentTimeCharacteristic.uuid)!!
            )
        )

        mBleCommunicationExecutor?.add(
            BleEvent(
                BleEvent.Type.SetIndication,
                mTargetPeripheral?.getCharacteristic(GattUUID.Characteristic.BloodPressureMeasurementCharacteristic.uuid)!!
            )
        )

        mBleCommunicationExecutor?.add(
            BleEvent(
                BleEvent.Type.ReadCharacteristic,
                mTargetPeripheral?.getCharacteristic(GattUUID.Characteristic.BloodPressureFeatureCharacteristic.uuid)!!
            )
        )

        mBleCommunicationExecutor?.exec()
    }


    private var mMessageHandler: Handler? = null


    private val mBluetoothStateChangedReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val state = intent.extras!!.getInt(BluetoothAdapter.EXTRA_STATE)
            if (state == BluetoothAdapter.STATE_ON) {
                mMessageHandler?.sendMessage(Message.obtain(mMessageHandler, MessageType.BluetoothOn.ordinal))
            } else if (state == BluetoothAdapter.STATE_OFF) {
                mMessageHandler?.sendMessage(Message.obtain(mMessageHandler, MessageType.BluetoothOff.ordinal))
            }
        }
    }

    protected enum class MessageType {
        BluetoothOff,
        BluetoothOn,
        ConnectionCompleted,
        ConnectionFailed,
        DisconnectionCompleted,
        DisconnectionFailed,
        DidDisconnection,
        Disconnected,
        BondStateChanged,
        AclConnectionStateChanged,
        GattConnectionStateChanged,
        DetailedStateChanged,
        BPFDataRcv,
        BPMDataRcv,
        WMDataRcv,
        WSFDataRcv,
        BatteryDataRcv,
        CTSDataRcv,
        // The waiting time-out message for receiving Indication.
        // After Indication Enable setting, this message will be
        // displayed when not receive the Indication in the prescribed time
        // This is a solution of the  problem in some models. (The  Indication is received in OS level
        // but the OS would return the Indication Confirmation without notification to the app).
        IndicationWaitTimeout
    }

    private class BleEvent(val type: Type, val characteristic: BluetoothGattCharacteristic) {

        enum class Type {
            SetNotification, SetIndication, WriteCharacteristic, ReadCharacteristic
        }
    }

    enum class DisconnectReason {
        UserRequest,
        CommunicationError,
        GattStatusError,
        IndicationWaitTimeout
    }

    override fun onResume() {
        super.onResume()
        activity?.registerReceiver(mBluetoothStateChangedReceiver, IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED))
        activity?.registerReceiver(mPairingRequestReceiver, IntentFilter(ACTION_PAIRING_REQUEST))

        mIsBluetoothOn = isBluetoothEnabled()

    }

    override fun onPause() {
        super.onPause()
        activity?.unregisterReceiver(mBluetoothStateChangedReceiver)
        activity?.unregisterReceiver(mPairingRequestReceiver)

        mIsBluetoothOn = isBluetoothEnabled()

    }

    private fun isBluetoothEnabled(): Boolean {
        val bluetoothManager = activity?.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager?
        return bluetoothManager!!.adapter.isEnabled
    }

    private class BleCommunicationExecutor internal constructor(
        private val mTargetPeripheral: BlePeripheral,
        private val mCompletionHandler: Handler
    ) {
        private val mBleEventList = LinkedList<BleEvent>()
        var isExecuting: Boolean = false
            private set

        init {
            isExecuting = false
        }

        fun add(bleEvent: BleEvent) {
            mBleEventList.add(bleEvent)
        }

        fun clear() {
            mBleEventList.clear()
        }

        fun exec(): Boolean {
            if (mBleEventList.isEmpty()) {
                // Timber.d("event empty.")
                return false
            }
            if (isExecuting) {
                // Timber.e("event executing.")
                return false
            }
            val bleEvent = mBleEventList.poll()
            val characteristic = bleEvent!!.characteristic
            when (bleEvent.type) {
                BleEvent.Type.SetNotification -> mTargetPeripheral.setNotificationEnabled(
                    characteristic,
                    true
                ) { _, characteristic, gattStatus, errorCode ->
                    isExecuting = false
                    val objects = arrayOf(characteristic, gattStatus, errorCode)
                    mCompletionHandler.sendMessage(Message.obtain(mCompletionHandler, bleEvent.type.ordinal, objects))
                }
                BleEvent.Type.SetIndication -> mTargetPeripheral.setNotificationEnabled(
                    characteristic,
                    true
                ) { _, characteristic, gattStatus, errorCode ->
                    isExecuting = false
                    val objects = arrayOf(characteristic, gattStatus, errorCode)
                    mCompletionHandler.sendMessage(Message.obtain(mCompletionHandler, bleEvent.type.ordinal, objects))
                }
                BleEvent.Type.WriteCharacteristic -> mTargetPeripheral.writeCharacteristic(characteristic) { _, characteristic, gattStatus, errorCode ->
                    isExecuting = false
                    val objects = arrayOf(characteristic, gattStatus, errorCode)
                    mCompletionHandler.sendMessage(Message.obtain(mCompletionHandler, bleEvent.type.ordinal, objects))
                }
                BleEvent.Type.ReadCharacteristic -> mTargetPeripheral.readCharacteristic(characteristic) { _, characteristic, gattStatus, errorCode ->
                    isExecuting = false
                    val objects = arrayOf(characteristic, gattStatus, errorCode)
                    mCompletionHandler.sendMessage(Message.obtain(mCompletionHandler, bleEvent.type.ordinal, objects))
                }
            }
            isExecuting = true
            return true
        }
    }

    protected fun disconnect(blePeripheral: BlePeripheral, reason: DisconnectReason) {

        blePeripheral.disconnect { address, errorCode ->
            if (null == errorCode) {
                mMessageHandler?.sendMessage(
                    Message.obtain(
                        mMessageHandler,
                        MessageType.DisconnectionCompleted.ordinal
                    )
                )
            } else {
                mMessageHandler?.sendMessage(
                    Message.obtain(
                        mMessageHandler,
                        MessageType.DisconnectionFailed.ordinal,
                        errorCode
                    )
                )
            }
        }

        stopIndicationWaitTimer()
        mBleCommunicationExecutor?.clear()
        binding.mConnectBtn.setEnabled(false)
        binding.mConnectBtn.setText("Disconnecting")
    }

    override fun onDestroy() {
        super.onDestroy()
        if (null != mTargetPeripheral) {
            mTargetPeripheral!!.destroy()
            mTargetPeripheral = null
        }
        if (null != mBleCommunicationExecutor) {
            mBleCommunicationExecutor!!.clear()
            mBleCommunicationExecutor = null
        }
    }


    /**
     * Created to be able to override in tests
     */
    fun navController() = findNavController()
}
