//
//  BlePeripheral.java
//
//  Copyright (c) 2016 OMRON HEALTHCARE Co.,Ltd. All rights reserved.
//

package jp.co.omron.healthcare.samplelibs.ble.blenativewrapper;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.Looper;
import android.os.Message;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.EventListener;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import jp.co.omron.healthcare.samplelibs.ble.blenativewrapper.sm.State;
import jp.co.omron.healthcare.samplelibs.ble.blenativewrapper.sm.StateMachine;

public class BlePeripheral extends StateMachine {

    private static final int EVT_BASE = BlePrivateConstants.BLE_PERIPHERAL_EVT_BASE;
    private static final int LOCAL_EVT_BASE = BlePrivateConstants.LOCAL_EVT_BASE;

    private static final int EVT_CONNECT = EVT_BASE + 0x0001;
    private static final int EVT_DISCONNECT = EVT_BASE + 0x0002;
    private static final int EVT_COMMUNICATION_REQ = EVT_BASE + 0x0003;
    private static final int EVT_GET_SERVICES = EVT_BASE + 0x0004;
    private static final int EVT_DESTROY = EVT_BASE + 0xffff;

    private static final int EVT_GATT_CONNECTED = EVT_BASE + 0x1001;
    private static final int EVT_GATT_DISCONNECTED = EVT_BASE + 0x1002;
    private static final int EVT_DISCOVER_SERVICE_SUCCESS = EVT_BASE + 0x1004;
    private static final int EVT_DISCOVER_SERVICE_FAILURE = EVT_BASE + 0x1005;
    private static final int EVT_ON_CHARACTERISTIC_CHANGED = EVT_BASE + 0x1006;
    private static final int EVT_COMMUNICATION_RES = EVT_BASE + 0x1007;
    private static final int EVT_ON_MTU_CHANGED = EVT_BASE + 0x1008;
    @NonNull
    private final Context mContext;
    @NonNull
    private final String mAddress;
    @NonNull
    private final String mLocalName;
    @NonNull
    private final BluetoothDeviceWrapper mBluetoothDeviceWrapper;
    @NonNull
    private final BleReceiver mBleReceiver;
    @NonNull
    private final StateInfo mStateInfo;
    @NonNull
    private final BlePeripheralSettings mSettings;
    @NonNull
    private final BluetoothGattCallbackWrapper mGattCallbackWrapper = new BluetoothGattCallbackWrapper() {
        @Override
        public void onConnectionStateChange(final int status, final int newState) {
            if (BluetoothProfile.STATE_CONNECTED == newState) {
                sendMessage(EVT_GATT_CONNECTED, status);
            } else if (BluetoothProfile.STATE_DISCONNECTED == newState) {
                sendMessage(EVT_GATT_DISCONNECTED, status);
            }
        }

        @Override
        public void onServicesDiscovered(final int status) {
            if (GattStatusCode.GATT_SUCCESS == status) {
                sendMessage(EVT_DISCOVER_SERVICE_SUCCESS);
            } else {
                sendMessage(EVT_DISCOVER_SERVICE_FAILURE, status);
            }
        }

        @Override
        public void onCharacteristicChanged(final BluetoothGattCharacteristic characteristic) {
            final Object[] objects = {characteristic};
            sendMessage(EVT_ON_CHARACTERISTIC_CHANGED, objects);
        }

        @Override
        public void onCharacteristicWrite(final BluetoothGattCharacteristic characteristic, final int status) {
            final Object[] objects = {CommunicationResType.OnCharacteristicWrite, characteristic, status};
            sendMessage(EVT_COMMUNICATION_RES, objects);
        }

        @Override
        public void onCharacteristicRead(final BluetoothGattCharacteristic characteristic, final int status) {
            final Object[] objects = {CommunicationResType.OnCharacteristicRead, characteristic, status};
            sendMessage(EVT_COMMUNICATION_RES, objects);
        }

        @Override
        public void onDescriptorWrite(final BluetoothGattDescriptor descriptor, final int status) {
            final Object[] objects = {CommunicationResType.OnDescriptorWrite, descriptor, status};
            sendMessage(EVT_COMMUNICATION_RES, objects);
        }

        @Override
        public void onDescriptorRead(BluetoothGattDescriptor descriptor, final int status) {
            final Object[] objects = {CommunicationResType.OnDescriptorRead, descriptor, status};
            sendMessage(EVT_COMMUNICATION_RES, objects);
        }

        @Override
        public void onMtuChanged(int mtu, int status) {
            BleLog.dMethodIn();

            final Object[] objects = {mtu, status};
            sendMessage(EVT_ON_MTU_CHANGED, objects);
        }
    };
    private final State mDefaultState = new DefaultState();
    private final State mDeadObjectState = new DeadObjectState();
    private final State mBluetoothOnState = new BluetoothOnState();
    private final State mDisconnectedState = new DisconnectedState();
    private final State mConnectingState = new ConnectingState();
    private final State mConnectedState = new ConnectedState();
    private final State mDisconnectingState = new DisconnectingState();
    private final State mConnectStartingState = new ConnectStartingState();
    private final State mPairingState = new PairingState();
    private final State mGattConnectingState = new GattConnectingState();
    private final State mServiceDiscoveringState = new ServiceDiscoveringState();
    private final State mConnectCleanupState = new ConnectCleanupState();
    private final State mCommunicationReadyState = new CommunicationReadyState();
    private final State mCommunicatingState = new CommunicatingState();
    private final State mConnectionFailedState = new ConnectionFailedState();
    private ActionReceiver mActionReceiver;
    private ConnectionListener mConnectionListener;
    private DisconnectionListener mDisconnectionListener;
    private int mConnectRetryCount;
    private boolean mIsShowPairingDialog;


    public BlePeripheral(
            @NonNull final Context context,
            @NonNull final DiscoverPeripheral discoverPeripheral) {
        this(context, discoverPeripheral, null);
    }

    public BlePeripheral(
            @NonNull final Context context,
            @NonNull final DiscoverPeripheral discoverPeripheral,
            @Nullable final Looper looper) {
        super(BleLog.TAG, looper);

        mContext = context;
        mAddress = discoverPeripheral.getBluetoothDevice().getAddress();
        mLocalName = discoverPeripheral.getBluetoothDevice().getName();
        mBluetoothDeviceWrapper = new BluetoothDeviceWrapper(mContext, discoverPeripheral.getBluetoothDevice());
        mSettings = new BlePeripheralSettings(mAddress);

        mBleReceiver = new BleReceiver(mContext, getHandler());
        mBleReceiver.setAddressFilter(mAddress);

        addState(mDefaultState);
        addState(mBluetoothOnState, mDefaultState);
        addState(mDisconnectedState, mBluetoothOnState);
        addState(mConnectingState, mBluetoothOnState);
        addState(mConnectedState, mBluetoothOnState);
        addState(mDisconnectingState, mBluetoothOnState);
        addState(mConnectStartingState, mConnectingState);
        addState(mPairingState, mConnectingState);
        addState(mGattConnectingState, mConnectingState);
        addState(mServiceDiscoveringState, mConnectingState);
        addState(mConnectCleanupState, mConnectingState);
        addState(mCommunicationReadyState, mConnectedState);
        addState(mCommunicatingState, mConnectedState);
        addState(mConnectionFailedState, mDisconnectedState);
        addState(mDeadObjectState, mDefaultState);

        mStateInfo = new StateInfo();
        mStateInfo.setBondState(mBluetoothDeviceWrapper.getBondState(), false);

        if (BluetoothProfile.STATE_DISCONNECTED != mBluetoothDeviceWrapper.getGattState()) {
            BleLog.w("This peripheral is connected by the other module.");
            setInitialState(mDeadObjectState);
        } else if (isBluetoothOn(mContext)) {
            setInitialState(mDisconnectedState);
        } else {
            BleLog.w("Bluetooth off.");
            setInitialState(mDeadObjectState);
        }

        setDbg(BleLog.OUTPUT_LOG_MODE);
        start();
    }

    private static boolean isBluetoothOn(Context context) {
        BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        return bluetoothManager.getAdapter().isEnabled();
    }

    public void destroy() {
        BleLog.dMethodIn("Address:" + mAddress);
        sendMessage(EVT_DESTROY);
    }

    @NonNull
    public String getAddress() {
        return mAddress;
    }

    @NonNull
    public String getLocalName() {
        return mLocalName;
    }

    @NonNull
    public BlePeripheralSettings getSettings() {
        return mSettings;
    }

    @NonNull
    public StateInfo getStateInfo() {
        return mStateInfo;
    }

    public boolean connect(
            @NonNull ActionReceiver actionReceiver,
            @NonNull ConnectionListener connectionListener) {
        return connect(actionReceiver, connectionListener, null);
    }

    public boolean connect(
            @NonNull ActionReceiver actionReceiver,
            @NonNull ConnectionListener connectionListener,
            @Nullable StateInfo.StateMonitor stateMonitor) {
        BleLog.dMethodIn("Address:" + mAddress);

        final Object[] objects = {actionReceiver, connectionListener, stateMonitor};
        sendMessage(EVT_CONNECT, objects);

        return true;
    }

    public boolean disconnect(@NonNull DisconnectionListener disconnectionListener) {
        BleLog.dMethodIn("Address:" + mAddress);

        final Object[] objects = {disconnectionListener};
        sendMessage(EVT_DISCONNECT, objects);

        return true;
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public List<BluetoothGattService> getServices() {
        final SynchronizeCallback callback = new SynchronizeCallback();
        sendMessage(EVT_GET_SERVICES, callback);
        try {
            callback.lock();
        } catch (TimeoutException | InterruptedException e) {
            e.printStackTrace();
            return null;
        }
        return (List<BluetoothGattService>) callback.getResult();
    }

    @Nullable
    public BluetoothGattCharacteristic getCharacteristic(
            @NonNull final BluetoothGattService service,
            @NonNull final UUID characteristicUUID) {

        List<BluetoothGattCharacteristic> characteristics = service.getCharacteristics();
        if (null == characteristics) {
            BleLog.e("null == characteristics");
            return null;
        }
        if (0 == characteristics.size()) {
            BleLog.e("0 == characteristics.size()");
            return null;
        }

        BluetoothGattCharacteristic ret = null;
        for (BluetoothGattCharacteristic characteristic : characteristics) {
            if (characteristicUUID.equals(characteristic.getUuid())) {
                ret = characteristic;
                break;
            }
        }

        return ret;
    }

    @Nullable
    public BluetoothGattCharacteristic getCharacteristic(
            @NonNull final UUID characteristicUUID) {

        List<BluetoothGattService> services = getServices();
        if (null == services) {
            BleLog.e("null == services");
            return null;
        }
        if (0 == services.size()) {
            BleLog.e("0 == services.size()");
            return null;
        }

        BluetoothGattCharacteristic ret = null;
        for (BluetoothGattService service : services) {
            ret = getCharacteristic(service, characteristicUUID);
            if (null != ret) {
                break;
            }
        }

        return ret;
    }

    public boolean writeCharacteristic(
            @NonNull final BluetoothGattCharacteristic characteristic,
            @NonNull final WriteCharacteristicResultListener writeCharacteristicResultListener) {
        return writeCharacteristic(characteristic, false, writeCharacteristicResultListener);
    }

    public boolean writeCharacteristic(
            @NonNull final BluetoothGattCharacteristic characteristic, boolean noResponse,
            @NonNull final WriteCharacteristicResultListener writeCharacteristicResultListener) {
        BleLog.dMethodIn("Address:" + mAddress);

        if (noResponse) {
            characteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
        } else {
            characteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
        }

        final Object[] objects = {CommunicationReqType.WriteCharacteristic, characteristic, writeCharacteristicResultListener};
        sendMessage(EVT_COMMUNICATION_REQ, objects);

        return true;
    }

    public boolean readCharacteristic(
            @NonNull final BluetoothGattCharacteristic characteristic,
            @NonNull final ReadCharacteristicResultListener readCharacteristicResultListener) {
        BleLog.dMethodIn("Address:" + mAddress);

        final Object[] objects = {CommunicationReqType.ReadCharacteristic, characteristic, readCharacteristicResultListener};
        sendMessage(EVT_COMMUNICATION_REQ, objects);

        return true;
    }

    public boolean setNotificationEnabled(
            @NonNull final BluetoothGattCharacteristic characteristic, boolean enable,
            @NonNull final SetNotificationResultListener setNotificationResultListener) {
        BleLog.dMethodIn("Address:" + mAddress);

        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(
                GattUUID.Descriptor.ClientCharacteristicConfigurationDescriptor.getUuid());
        if (null == descriptor) {
            BleLog.e("null == descriptor");
            return false;
        }

        byte[] value;
        int properties = characteristic.getProperties();
        if (CharacteristicProperty.contains(properties, CharacteristicProperty.Indicate)) {
            if (enable) {
                BleLog.d("Enable indication.");
                value = BluetoothGattDescriptor.ENABLE_INDICATION_VALUE;
            } else {
                value = BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE;
            }
        } else if (CharacteristicProperty.contains(properties, CharacteristicProperty.Notify)) {
            if (enable) {
                BleLog.d("Enable notification.");
                value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE;
            } else {
                value = BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE;
            }
        } else {
            BleLog.e("Notification unsupported.");
            return false;
        }

        boolean result = descriptor.setValue(value);
        if (!result) {
            BleLog.e("Descriptor set value failed.");
            return false;
        }

        final Object[] objects = {CommunicationReqType.SetNotificationEnabled, characteristic, enable, setNotificationResultListener};
        sendMessage(EVT_COMMUNICATION_REQ, objects);

        return true;
    }

    private void communicationRequestError(Object[] requestObjects, ErrorCode errorCode) {
        final CommunicationReqType type = (CommunicationReqType) requestObjects[0];
        switch (type) {
            case SetNotificationEnabled: {
                final BluetoothGattCharacteristic characteristic = (BluetoothGattCharacteristic) requestObjects[1];
                final SetNotificationResultListener resultListener = (SetNotificationResultListener) requestObjects[3];
                resultListener.onComplete(mAddress, characteristic, GattStatusCode.GATT_UNKNOWN, errorCode);
                break;
            }
            case WriteCharacteristic: {
                final BluetoothGattCharacteristic characteristic = (BluetoothGattCharacteristic) requestObjects[1];
                final WriteCharacteristicResultListener resultListener = (WriteCharacteristicResultListener) requestObjects[2];
                resultListener.onComplete(mAddress, characteristic, GattStatusCode.GATT_UNKNOWN, errorCode);
                break;
            }
            case ReadCharacteristic: {
                final BluetoothGattCharacteristic characteristic = (BluetoothGattCharacteristic) requestObjects[1];
                final ReadCharacteristicResultListener resultListener = (ReadCharacteristicResultListener) requestObjects[2];
                resultListener.onComplete(mAddress, characteristic, GattStatusCode.GATT_UNKNOWN, errorCode);
                break;
            }
            default:
                break;
        }
    }

    private void assistPairingDialogIfNeeded() {
        if (mSettings.AssistPairingDialog) {
            // Show pairing dialog mandatorily.
            // The app calls start discovery and cancel interface so that app will show pairing dialog each time
            // based on specification that Android O/S shows the dialog when the app pairs with a device
            // within 60 seconds after cancel discovery.
            BluetoothManager bluetoothManager = (BluetoothManager) mContext.getSystemService(Context.BLUETOOTH_SERVICE);
            bluetoothManager.getAdapter().startDiscovery();
            bluetoothManager.getAdapter().cancelDiscovery();
        }
    }

    private void autoPairingIfNeeded(int variant) {
        if (!mSettings.AutoPairingEnabled) {
            return;
        }
        switch (variant) {
            case BlePrivateConstants.PAIRING_VARIANT_PIN:
            case BlePrivateConstants.PAIRING_VARIANT_PIN_16_DIGITS:
                if (null != mSettings.AutoPairingPinCode &&
                        !mSettings.AutoPairingPinCode.isEmpty()) {
                    mBluetoothDeviceWrapper.setPin(mSettings.AutoPairingPinCode);
                }
                break;
            case BlePrivateConstants.PAIRING_VARIANT_PASSKEY:
                if (null != mSettings.AutoPairingPinCode &&
                        !mSettings.AutoPairingPinCode.isEmpty()) {
                    mBluetoothDeviceWrapper.setPasskey(mSettings.AutoPairingPinCode);
                }
                break;
            case BlePrivateConstants.PAIRING_VARIANT_PASSKEY_CONFIRMATION:
                mBluetoothDeviceWrapper.setPairingConfirmation(true);
                break;
            case BlePrivateConstants.PAIRING_VARIANT_CONSENT:
                mBluetoothDeviceWrapper.setPairingConfirmation(true);
                break;
            case BlePrivateConstants.PAIRING_VARIANT_DISPLAY_PASSKEY:
                break;
            case BlePrivateConstants.PAIRING_VARIANT_DISPLAY_PIN:
                break;
            case BlePrivateConstants.PAIRING_VARIANT_OOB_CONSENT:
                break;
            default:
                break;
        }
    }

    public enum CommunicationReqType {
        WriteCharacteristic,
        ReadCharacteristic,
        WriteDescriptor,
        ReadDescriptor,
        SetNotificationEnabled
    }

    public enum CommunicationResType {
        OnCharacteristicWrite,
        OnCharacteristicRead,
        OnDescriptorWrite,
        OnDescriptorRead
    }

    public interface ConnectionListener extends EventListener {
        void onComplete(@NonNull String address, ErrorCode errorCode);
    }

    public interface DisconnectionListener extends EventListener {
        void onComplete(@NonNull String address, ErrorCode errorCode);
    }

    public interface WriteCharacteristicResultListener extends EventListener {
        void onComplete(@NonNull String address, BluetoothGattCharacteristic characteristic, int gattStatus, ErrorCode errorCode);
    }

    public interface ReadCharacteristicResultListener extends EventListener {
        void onComplete(@NonNull String address, BluetoothGattCharacteristic characteristic, int gattStatus, ErrorCode errorCode);
    }

    public interface SetNotificationResultListener extends EventListener {
        void onComplete(@NonNull String address, BluetoothGattCharacteristic characteristic, int gattStatus, ErrorCode errorCode);
    }

    public interface ActionReceiver {
        void didDisconnection(@NonNull String address);

        void onCharacteristicChanged(@NonNull String address, @NonNull BluetoothGattCharacteristic characteristic);
    }

    private static class DefaultState extends State {
        @Override
        public boolean processMessage(@NonNull Message msg) {
            return StateMachine.HANDLED;
        }
    }

    private static class SynchronizeCallback {
        private final static int DEFAULT_TIMEOUT = 10 * 1000;
        @NonNull
        private final CountDownLatch mLock;
        private Object mResult;

        SynchronizeCallback() {
            mLock = new CountDownLatch(1);
        }

        void lock() throws InterruptedException, TimeoutException {
            lock(DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS);
        }

        void lock(long timeout, TimeUnit unit) throws InterruptedException, TimeoutException {
            mLock.await(timeout, unit);
            if (0 < mLock.getCount()) {
                throw new TimeoutException("CountDownLatch.await() is timeout.");
            }
        }

        void unlock() {
            mLock.countDown();
        }

        Object getResult() {
            return mResult;
        }

        void setResult(Object result) {
            mResult = result;
        }
    }

    private class DeadObjectState extends State {
        @Override
        public void enter(Object[] transferObjects) {
            if (mBluetoothDeviceWrapper.hasGatt()) {
                mBluetoothDeviceWrapper.closeGatt();
            }
            if (null != transferObjects) {
                ErrorCode errorCode = (ErrorCode) transferObjects[0];
                if (null != mConnectionListener) {
                    mConnectionListener.onComplete(mAddress, errorCode);
                    mConnectionListener = null;
                }
                if (null != mDisconnectionListener) {
                    mDisconnectionListener.onComplete(mAddress, errorCode);
                    mDisconnectionListener = null;
                }
            }
            if (null != mActionReceiver) {
                mActionReceiver.didDisconnection(mAddress);
            }
            mActionReceiver = null;
            mStateInfo.setDetailedState(StateInfo.DetailedState.Dead, null, true);
            mStateInfo.setBondState(StateInfo.BondState.Unknown, false);
            mStateInfo.setAclConnectionState(StateInfo.AclConnectionState.Unknown, false);
            mStateInfo.setGattConnectionState(StateInfo.GattConnectionState.Unknown, false);
        }

        @Override
        public boolean processMessage(@NonNull Message msg) {
            switch (msg.what) {
                case EVT_CONNECT: {
                    final Object[] objects = (Object[]) msg.obj;
                    ConnectionListener connectionListener = (ConnectionListener) objects[1];
                    connectionListener.onComplete(mAddress, ErrorCode.DeadObject);
                    break;
                }
                case EVT_DISCONNECT: {
                    final Object[] objects = (Object[]) msg.obj;
                    DisconnectionListener disconnectionListener = (DisconnectionListener) objects[0];
                    disconnectionListener.onComplete(mAddress, ErrorCode.DeadObject);
                    break;
                }
                case EVT_COMMUNICATION_REQ: {
                    communicationRequestError((Object[]) msg.obj, ErrorCode.DeadObject);
                    break;
                }
                case EVT_GET_SERVICES: {
                    final SynchronizeCallback callback = (SynchronizeCallback) msg.obj;
                    callback.setResult(null);
                    callback.unlock();
                    break;
                }
                default:
                    return StateMachine.NOT_HANDLED;
            }
            return StateMachine.HANDLED;
        }
    }

    private class BluetoothOnState extends State {
        @Override
        public boolean processMessage(@NonNull Message msg) {
            switch (msg.what) {
                case EVT_DESTROY: {
                    mBleReceiver.unregisterReceiver();
                    if (mBluetoothDeviceWrapper.hasGatt()) {
                        mBluetoothDeviceWrapper.disconnectGatt();
                    }
                    Object[] transferObjects = {ErrorCode.Destroy};
                    transitionTo(mDeadObjectState, transferObjects);
                    break;
                }
                case BleReceiver.EVT_BLUETOOTH_STATE_CHANGED: {
                    final int bluetoothState = msg.arg1;
                    if (BluetoothAdapter.STATE_TURNING_OFF == bluetoothState
                            || BluetoothAdapter.STATE_OFF == bluetoothState) {
                        mBleReceiver.unregisterReceiver();
                        Object[] transferObjects = {ErrorCode.BluetoothOff};
                        transitionTo(mDeadObjectState, transferObjects);
                    }
                    break;
                }
                case EVT_CONNECT: {
                    final Object[] objects = (Object[]) msg.obj;
                    ConnectionListener connectionListener = (ConnectionListener) objects[1];
                    connectionListener.onComplete(mAddress, ErrorCode.BadState);
                    break;
                }
                case EVT_DISCONNECT: {
                    final Object[] objects = (Object[]) msg.obj;
                    DisconnectionListener disconnectionListener = (DisconnectionListener) objects[0];
                    disconnectionListener.onComplete(mAddress, ErrorCode.BadState);
                    break;
                }
                case EVT_COMMUNICATION_REQ: {
                    communicationRequestError((Object[]) msg.obj, ErrorCode.BadState);
                    break;
                }
                case EVT_GET_SERVICES: {
                    final SynchronizeCallback callback = (SynchronizeCallback) msg.obj;
                    callback.setResult(mBluetoothDeviceWrapper.getServices());
                    callback.unlock();
                    break;
                }
                case BleReceiver.EVT_BOND_NONE: {
                    mStateInfo.setBondState(StateInfo.BondState.NotBonded, true);
                    break;
                }
                case BleReceiver.EVT_BONDING: {
                    mStateInfo.setBondState(StateInfo.BondState.Bonding, true);
                    break;
                }
                case BleReceiver.EVT_BONDED: {
                    mStateInfo.setBondState(StateInfo.BondState.Bonded, true);
                    break;
                }
                default:
                    return StateMachine.NOT_HANDLED;
            }
            return StateMachine.HANDLED;
        }
    }

    private class ConnectionFailedState extends State {
        @Override
        public void enter(Object[] transferObjects) {
            ErrorCode errorCode = (ErrorCode) transferObjects[0];
            mConnectionListener.onComplete(mAddress, errorCode);
            mConnectionListener = null;
        }
    }

    private class DisconnectedState extends State {
        @Override
        public void enter(Object[] transferObjects) {
            mBleReceiver.unregisterReceiver();

            if (mBluetoothDeviceWrapper.hasGatt()) {
                if (mSettings.UseRefreshGatt) {
                    mBluetoothDeviceWrapper.refreshGatt();
                }
                mBluetoothDeviceWrapper.closeGatt();
            }

            if (null != mDisconnectionListener) {
                // Disconnection by disconnect request.
                mDisconnectionListener.onComplete(mAddress, null);
                mDisconnectionListener = null;
            } else if (null != mActionReceiver) {
                // Disconnection by peripheral or OS.
                mActionReceiver.didDisconnection(mAddress);
            }
            mActionReceiver = null;
            mStateInfo.setDetailedState(StateInfo.DetailedState.Disconnected, null, true);
        }

        @Override
        public boolean processMessage(@NonNull Message msg) {
            switch (msg.what) {
                case EVT_CONNECT: {
                    final Object[] objects = (Object[]) msg.obj;
                    mActionReceiver = (ActionReceiver) objects[0];
                    mConnectionListener = (ConnectionListener) objects[1];
                    final StateInfo.StateMonitor stateMonitor = (StateInfo.StateMonitor) objects[2];
                    mStateInfo.setStateMonitor(stateMonitor);
                    transitionTo(mConnectStartingState);
                    break;
                }
                default:
                    return StateMachine.NOT_HANDLED;
            }
            return StateMachine.HANDLED;
        }
    }

    private class ConnectingState extends State {

        @Override
        public void enter(Object[] transferObjects) {
            mConnectRetryCount = 0;
            mBleReceiver.registerReceiver();
        }

        @Override
        public boolean processMessage(@NonNull Message msg) {
            switch (msg.what) {
                case EVT_DISCONNECT: {
                    final Object[] objects = (Object[]) msg.obj;
                    mDisconnectionListener = (DisconnectionListener) objects[0];
                    Object[] transferObjects = {StateInfo.Reason.DisconnectRequest};
                    transitionTo(mDisconnectingState, transferObjects);
                    break;
                }
                case EVT_GATT_DISCONNECTED: {
                    mStateInfo.setGattConnectionState(StateInfo.GattConnectionState.Disconnected, true);
                    transitionToCleanupState();
                    break;
                }
                case BleReceiver.EVT_BOND_NONE: {
                    mStateInfo.setBondState(StateInfo.BondState.NotBonded, true);
                    transitionToCleanupState();
                    break;
                }
                case BleReceiver.EVT_ACL_DISCONNECTED: {
                    mStateInfo.setAclConnectionState(StateInfo.AclConnectionState.Disconnected, true);
                    transitionToCleanupState();
                    break;
                }
                default:
                    return StateMachine.NOT_HANDLED;
            }
            return StateMachine.HANDLED;
        }

        private void transitionToCleanupState() {
            if (mConnectCleanupState == getCurrentState()) {
                BleLog.d("Already transition to ConnectCleanupState.");
                return;
            }
            if (mIsShowPairingDialog) {
                // No retry when connection failed in showing pairing dialog.
                // ex) Select [Cancel] / Invalid PIN input
                BleLog.w("Pairing failed.");
                Object[] transferObjects = {ErrorCode.PairingFailed, ConnectCleanupState.NOT_RETRY};
                transitionTo(mConnectCleanupState, transferObjects);
            } else {
                // Retry when unexpected connection failed.
                BleLog.e("Connection failed.");
                Object[] transferObjects = {ErrorCode.GattConnectionFailure, ConnectCleanupState.RETRY};
                transitionTo(mConnectCleanupState, transferObjects);
            }
        }
    }

    private class DisconnectingState extends State {

        private static final int EVT_DISCONNECTING_TIMEOUT = LOCAL_EVT_BASE + 0x0001;
        private static final long DISCONNECTING_WAIT_TIME = 1000 * 10;

        @Override
        public void enter(Object[] transferObjects) {
            StateInfo.Reason disconnectingReason = (StateInfo.Reason) transferObjects[0];
            mStateInfo.setDetailedState(StateInfo.DetailedState.Disconnecting, disconnectingReason, true);
            teardownOrTransitionToDisconnectedState();
            sendMessageDelayed(EVT_DISCONNECTING_TIMEOUT, DISCONNECTING_WAIT_TIME);
        }

        @Override
        public boolean processMessage(@NonNull Message msg) {
            switch (msg.what) {
                case EVT_DISCONNECT: {
                    final Object[] objects = (Object[]) msg.obj;
                    final DisconnectionListener disconnectionListener = (DisconnectionListener) objects[0];
                    if (null != mDisconnectionListener) {
                        disconnectionListener.onComplete(mAddress, ErrorCode.Busy);
                        break;
                    }
                    mDisconnectionListener = disconnectionListener;
                    break;
                }
                case EVT_GATT_DISCONNECTED: {
                    mStateInfo.setGattConnectionState(StateInfo.GattConnectionState.Disconnected, true);
                    teardownOrTransitionToDisconnectedState();
                    break;
                }
                case BleReceiver.EVT_BOND_NONE: {
                    mStateInfo.setBondState(StateInfo.BondState.NotBonded, true);
                    teardownOrTransitionToDisconnectedState();
                    break;
                }
                case BleReceiver.EVT_ACL_DISCONNECTED: {
                    mStateInfo.setAclConnectionState(StateInfo.AclConnectionState.Disconnected, true);
                    teardownOrTransitionToDisconnectedState();
                    break;
                }
                case EVT_DISCONNECTING_TIMEOUT: {
                    // There are cases when timeout has occurred without notification of
                    // ACL Disconnected or Bond None and move to next state in theses cases.
                    transitionTo(mDisconnectedState);
                    break;
                }
                default:
                    return StateMachine.NOT_HANDLED;
            }
            return StateMachine.HANDLED;
        }

        @Override
        public void exit() {
            removeMessages(EVT_DISCONNECTING_TIMEOUT);
        }

        private boolean isTeardownCompleted() {
            if (StateInfo.GattConnectionState.Disconnected != mStateInfo.getGattConnectionState()) {
                BleLog.i("Gatt disconnecting.");
                return false;
            }
            if (mSettings.UseRemoveBond &&
                    StateInfo.BondState.NotBonded != mStateInfo.getBondState()) {
                BleLog.i("Bond removing.");
                return false;
            }
            if (StateInfo.BondState.Bonding == mStateInfo.getBondState()) {
                BleLog.i("Bond processing.");
                return false;
            }
            if (StateInfo.AclConnectionState.Disconnected != mStateInfo.getAclConnectionState()) {
                BleLog.i("Acl disconnecting.");
                return false;
            }
            BleLog.i("Teardown completed.");
            return true;
        }

        private void teardownOrTransitionToDisconnectedState() {
            if (isTeardownCompleted()) {
                transitionTo(mDisconnectedState);
            } else {
                if (StateInfo.GattConnectionState.Disconnected != mStateInfo.getGattConnectionState()) {
                    mBluetoothDeviceWrapper.disconnectGatt();
                } else if (mSettings.UseRemoveBond && mStateInfo.isBonded()) {
                    mBluetoothDeviceWrapper.removeBond();
                } else if (StateInfo.BondState.Bonding == mStateInfo.getBondState()) {
                    mBluetoothDeviceWrapper.cancelBondProcess();
                }
            }
        }
    }

    private class ConnectStartingState extends State {
        @Override
        public void enter(Object[] transferObjects) {
            mIsShowPairingDialog = false;
            if (mSettings.UseCreateBond && !mStateInfo.isBonded()) {
                transitionTo(mPairingState);
            } else {
                transitionTo(mGattConnectingState);
            }
        }
    }

    private class PairingState extends State {

        private static final int EVT_PAIRING_TIMEOUT = LOCAL_EVT_BASE + 0x0001;

        private static final long PAIRING_TIME = 1000 * 10;

        @Override
        public void enter(Object[] transferObjects) {
            mStateInfo.setDetailedState(StateInfo.DetailedState.Pairing, null, true);
            assistPairingDialogIfNeeded();
            if (!mBluetoothDeviceWrapper.createBond()) {
                Object[] objects = {ErrorCode.OSNativeError, ConnectCleanupState.RETRY};
                transitionTo(mConnectCleanupState, objects);
                return;
            }
            sendMessageDelayed(EVT_PAIRING_TIMEOUT, PAIRING_TIME);
        }

        @Override
        public boolean processMessage(@NonNull Message msg) {
            switch (msg.what) {
                case BleReceiver.EVT_PAIRING_REQUEST: {
                    removeMessages(EVT_PAIRING_TIMEOUT);
                    mIsShowPairingDialog = true;
                    autoPairingIfNeeded(msg.arg1);
                    break;
                }
                case BleReceiver.EVT_ACL_CONNECTED: {
                    mStateInfo.setAclConnectionState(StateInfo.AclConnectionState.Connected, true);
                    transitionToNextStateIfPaired();
                    break;
                }
                case BleReceiver.EVT_BONDED: {
                    mIsShowPairingDialog = false;
                    mStateInfo.setBondState(StateInfo.BondState.Bonded, true);
                    transitionToNextStateIfPaired();
                    break;
                }
                case EVT_PAIRING_TIMEOUT: {
                    Object[] transferObjects = {ErrorCode.PairingTimeout, ConnectCleanupState.NOT_RETRY};
                    transitionTo(mConnectCleanupState, transferObjects);
                    break;
                }
                default:
                    return StateMachine.NOT_HANDLED;
            }
            return StateMachine.HANDLED;
        }

        @Override
        public void exit() {
            removeMessages(EVT_PAIRING_TIMEOUT);
        }

        private void transitionToNextStateIfPaired() {
            if (StateInfo.AclConnectionState.Connected != mStateInfo.getAclConnectionState()) {
                BleLog.i("Acl connecting.");
                return;
            }
            if (!mStateInfo.isBonded()) {
                BleLog.i("Wait bonded.");
                return;
            }
            BleLog.i("Pairing completed.");
            transitionTo(mGattConnectingState);
        }
    }

    private class GattConnectingState extends State {

        private static final int EVT_GATT_CONNECTION_TIMEOUT = LOCAL_EVT_BASE + 0x0001;

        private static final int EVT_STABLE_CONNECTION = LOCAL_EVT_BASE + 0x0002;

        private static final long GATT_CONNECTION_TIME = 1000 * 10;

        private boolean mNotBeenPairing;

        @Override
        public void enter(Object[] transferObjects) {
            mStateInfo.setDetailedState(StateInfo.DetailedState.GattConnecting, null, true);
            mNotBeenPairing = false;
            if (!mStateInfo.isBonded()) {
                assistPairingDialogIfNeeded();
            }
            if (!mBluetoothDeviceWrapper.connectGatt(mContext, mGattCallbackWrapper)) {
                Object[] objects = {ErrorCode.OSNativeError, ConnectCleanupState.RETRY};
                transitionTo(mConnectCleanupState, objects);
                return;
            }
            sendMessageDelayed(EVT_GATT_CONNECTION_TIMEOUT, GATT_CONNECTION_TIME);
        }

        @Override
        public boolean processMessage(@NonNull Message msg) {
            switch (msg.what) {
                case BleReceiver.EVT_PAIRING_REQUEST: {
                    removeMessages(EVT_GATT_CONNECTION_TIMEOUT);
                    mIsShowPairingDialog = true;
                    autoPairingIfNeeded(msg.arg1);
                    break;
                }
                case BleReceiver.EVT_ACL_CONNECTED: {
                    mStateInfo.setAclConnectionState(StateInfo.AclConnectionState.Connected, true);
                    transitionToNextStateIfGattConnectionStabled();
                    break;
                }
                case EVT_GATT_CONNECTED: {
                    removeMessages(EVT_GATT_CONNECTION_TIMEOUT);
                    mStateInfo.setGattConnectionState(StateInfo.GattConnectionState.Connected, true);
                    long stableConnectionWaitTime = 0;
                    if (mSettings.StableConnectionEnabled) {
                        stableConnectionWaitTime = mSettings.StableConnectionWaitTime;
                    }
                    sendMessageDelayed(EVT_STABLE_CONNECTION, stableConnectionWaitTime);
                    break;
                }
                case BleReceiver.EVT_BONDING: {
                    if (mStateInfo.isBonded()) {
                        // Set foreground because of showing pairing dialog when state is from Bonded to Bonding.
                        assistPairingDialogIfNeeded();
                    }
                    mStateInfo.setBondState(StateInfo.BondState.Bonding, true);
                    break;
                }
                case BleReceiver.EVT_BONDED: {
                    mIsShowPairingDialog = false;
                    mStateInfo.setBondState(StateInfo.BondState.Bonded, true);
                    transitionToNextStateIfGattConnectionStabled();
                    break;
                }
                case EVT_STABLE_CONNECTION: {
                    if (StateInfo.BondState.NotBonded == mStateInfo.getBondState()) {
                        // Target device does not pair by connectGatt() if pairing function
                        // is not run after GATT connect within defined time.
                        mNotBeenPairing = true;
                        BleLog.i("Not been pairing in the connection process.");
                    }
                    transitionToNextStateIfGattConnectionStabled();
                    break;
                }
                case EVT_GATT_CONNECTION_TIMEOUT: {
                    Object[] transferObjects = {ErrorCode.GattConnectionTimeout, ConnectCleanupState.NOT_RETRY};
                    transitionTo(mConnectCleanupState, transferObjects);
                    break;
                }
                default:
                    return StateMachine.NOT_HANDLED;
            }
            return StateMachine.HANDLED;
        }

        @Override
        public void exit() {
            removeMessages(EVT_GATT_CONNECTION_TIMEOUT);
            removeMessages(EVT_STABLE_CONNECTION);
        }

        private void transitionToNextStateIfGattConnectionStabled() {
            if (StateInfo.AclConnectionState.Connected != mStateInfo.getAclConnectionState()) {
                BleLog.i("Acl connecting.");
                return;
            }
            if (StateInfo.GattConnectionState.Connected != mStateInfo.getGattConnectionState()) {
                BleLog.i("Gatt connecting.");
                return;
            }
            if (hasMessage(EVT_STABLE_CONNECTION)) {
                BleLog.i("Wait connection stabled.");
                return;
            }
            if (!mNotBeenPairing && !mStateInfo.isBonded()) {
                BleLog.i("Wait bonded.");
                return;
            }
            BleLog.i("Gatt connection completed.");
            transitionTo(mServiceDiscoveringState);
        }
    }

    private class ServiceDiscoveringState extends State {

        private static final int EVT_START_DISCOVER_SERVICE = LOCAL_EVT_BASE + 0x0001;
        private static final int EVT_DISCOVER_SERVICE_TIMEOUT = LOCAL_EVT_BASE + 0x0002;

        private static final long SERVICE_DISCOVERED_WAIT_TIME = 1000 * 30;

        private int mRetryCount;

        @Override
        public void enter(Object[] transferObjects) {
            mRetryCount = 0;
            mStateInfo.setDetailedState(StateInfo.DetailedState.ServiceDiscovering, null, true);
            List<BluetoothGattService> services = mBluetoothDeviceWrapper.getServices();
            if (null == services || 0 == services.size()) {
                sendMessage(EVT_START_DISCOVER_SERVICE);
            } else {
                sendMessage(EVT_DISCOVER_SERVICE_SUCCESS);
            }
        }

        @Override
        public boolean processMessage(@NonNull Message msg) {
            switch (msg.what) {
                case EVT_START_DISCOVER_SERVICE: {
                    boolean result = mBluetoothDeviceWrapper.discoverServices();
                    if (!result) {
                        sendMessage(EVT_DISCOVER_SERVICE_FAILURE, 0);
                        break;
                    }
                    sendMessageDelayed(EVT_DISCOVER_SERVICE_TIMEOUT, SERVICE_DISCOVERED_WAIT_TIME);
                    break;
                }
                case EVT_DISCOVER_SERVICE_SUCCESS: {
                    if (!verifyServices()) {
                        BleLog.e("Detected abnormality in services.");
                        sendMessage(EVT_DISCOVER_SERVICE_FAILURE, 0);
                        break;
                    }
                    BleLog.i("Discover service success.");
                    transitionTo(mCommunicationReadyState);
                    break;
                }
                case EVT_DISCOVER_SERVICE_TIMEOUT: {
                    BleLog.e("Discover service timeout.");
                    sendMessage(EVT_DISCOVER_SERVICE_FAILURE, 0);
                    break;
                }
                case EVT_DISCOVER_SERVICE_FAILURE: {
                    BleLog.e("Discover service failure.");
                    if (!mSettings.DiscoverServiceRetryEnabled) {
                        mBluetoothDeviceWrapper.disconnectGatt();
                        Object[] transferObjects = {ErrorCode.DiscoverServiceFailure, ConnectCleanupState.RETRY};
                        transitionTo(mConnectionFailedState, transferObjects);
                        break;
                    }
                    if (mSettings.DiscoverServiceRetryCount <= mRetryCount) {
                        BleLog.e("Discover serivce failed because retry count reaches the maximum value.");
                        mBluetoothDeviceWrapper.disconnectGatt();
                        Object[] transferObjects = {ErrorCode.DiscoverServiceFailure, ConnectCleanupState.RETRY};
                        transitionTo(mConnectionFailedState, transferObjects);
                        break;
                    }
                    mRetryCount++;
                    BleLog.w("Discover service retry. count:" + mRetryCount);
                    sendMessageDelayed(EVT_START_DISCOVER_SERVICE, mSettings.DiscoverServiceRetryDelayTime);
                    break;
                }
                default:
                    return StateMachine.NOT_HANDLED;
            }
            return StateMachine.HANDLED;
        }

        private boolean verifyServices() {
            List<BluetoothGattService> services = mBluetoothDeviceWrapper.getServices();
            if (null == services) {
                return false;
            }
            for (BluetoothGattService service : services) {
                if (null == service.getCharacteristics() || 0 >= service.getCharacteristics().size()) {
                    BleLog.e("Services verify failed.");
                    return false;
                }
            }
            return true;
        }

        @Override
        public void exit() {
            removeMessages(EVT_START_DISCOVER_SERVICE);
            removeMessages(EVT_DISCOVER_SERVICE_TIMEOUT);
        }
    }

    private class ConnectCleanupState extends State {

        public static final int NOT_RETRY = 0;
        public static final int RETRY = 1;

        // There are cases when GATT Disconnect is notified before Bluetooth Turning Off notification
        // within few ms under Bluetooth off in connect running.
        private static final int EVT_START_CLEANUP = LOCAL_EVT_BASE + 0x0001;

        private static final int EVT_CLEANUP_TIMEOUT = LOCAL_EVT_BASE + 0x0002;

        private static final int EVT_RETRY = LOCAL_EVT_BASE + 0x0003;

        private static final long CLEANUP_DELAY_TIME = 100;

        private static final long CLEANUP_TIME = 1000 * 10;

        private ErrorCode mCleanupReason;
        private boolean mIsRetryRequested;

        @Override
        public void enter(Object[] transferObjects) {
            mStateInfo.setDetailedState(StateInfo.DetailedState.Cleanup, null, true);
            mCleanupReason = (ErrorCode) transferObjects[0];
            int retry = (int) transferObjects[1];
            mIsRetryRequested = (RETRY == retry);
            sendMessageDelayed(EVT_START_CLEANUP, CLEANUP_DELAY_TIME);
        }

        @Override
        public boolean processMessage(@NonNull Message msg) {
            switch (msg.what) {
                case EVT_START_CLEANUP: {
                    sendMessageDelayed(EVT_CLEANUP_TIMEOUT, CLEANUP_TIME);
                    cleanupOrRetryConnect();
                    break;
                }
                case EVT_GATT_DISCONNECTED: {
                    mStateInfo.setGattConnectionState(StateInfo.GattConnectionState.Disconnected, true);
                    cleanupOrRetryConnect();
                    break;
                }
                case BleReceiver.EVT_BOND_NONE: {
                    mStateInfo.setBondState(StateInfo.BondState.NotBonded, true);
                    cleanupOrRetryConnect();
                    break;
                }
                case BleReceiver.EVT_ACL_DISCONNECTED: {
                    mStateInfo.setAclConnectionState(StateInfo.AclConnectionState.Disconnected, true);
                    cleanupOrRetryConnect();
                    break;
                }
                case EVT_RETRY: {
                    if (!mSettings.ConnectionRetryEnabled || !mIsRetryRequested) {
                        BleLog.w("Connection end because not request a retry.");
                        Object[] transferObjects = {mCleanupReason};
                        transitionTo(mConnectionFailedState, transferObjects);
                        break;
                    }
                    if (mSettings.ConnectionRetryCount <= mConnectRetryCount) {
                        BleLog.e("Connection failed because retry count reaches the maximum value.");
                        Object[] transferObjects = {mCleanupReason};
                        transitionTo(mConnectionFailedState, transferObjects);
                        break;
                    }
                    mConnectRetryCount++;
                    BleLog.w("Connection retry. count:" + mConnectRetryCount);
                    transitionTo(mConnectStartingState);
                    break;
                }
                case EVT_CLEANUP_TIMEOUT: {
                    // There are cases when timeout has occurred without notification of
                    // ACL Disconnected and move to next state in theses cases.
                    Object[] transferObjects = {mCleanupReason};
                    transitionTo(mConnectionFailedState, transferObjects);
                    break;
                }
                default:
                    return StateMachine.NOT_HANDLED;
            }
            return StateMachine.HANDLED;
        }

        @Override
        public void exit() {
            removeMessages(EVT_CLEANUP_TIMEOUT);
        }

        private boolean isCleanupCompleted() {
            if (hasMessage(EVT_START_CLEANUP)) {
                BleLog.i("Wait cleanup start.");
                return false;
            }
            if (StateInfo.GattConnectionState.Disconnected != mStateInfo.getGattConnectionState()) {
                BleLog.i("Gatt disconnecting.");
                return false;
            }
            if (StateInfo.BondState.Bonding == mStateInfo.getBondState()) {
                BleLog.i("Bond process canceling.");
                return false;
            }
            if (StateInfo.AclConnectionState.Disconnected != mStateInfo.getAclConnectionState()) {
                BleLog.i("Acl disconnecting.");
                return false;
            }
            BleLog.i("Cleanup completed.");
            return true;
        }

        private void cleanup() {
            if (StateInfo.GattConnectionState.Disconnected != mStateInfo.getGattConnectionState()) {
                mBluetoothDeviceWrapper.disconnectGatt();
            } else if (StateInfo.BondState.Bonding == mStateInfo.getBondState()) {
                mBluetoothDeviceWrapper.cancelBondProcess();
            }
        }

        private void cleanupOrRetryConnect() {
            if (isCleanupCompleted()) {
                sendMessageDelayed(EVT_RETRY, mSettings.ConnectionRetryDelayTime);
            } else {
                cleanup();
            }
        }
    }

    private class ConnectedState extends State {

        @Override
        public void enter(Object[] transferObjects) {
            mConnectionListener.onComplete(mAddress, null);
            mConnectionListener = null;
        }

        @Override
        public boolean processMessage(@NonNull Message msg) {
            switch (msg.what) {
                case EVT_ON_CHARACTERISTIC_CHANGED: {
                    final Object[] objects = (Object[]) msg.obj;
                    final BluetoothGattCharacteristic characteristic = (BluetoothGattCharacteristic) objects[0];
                    mActionReceiver.onCharacteristicChanged(mAddress, characteristic);
                    break;
                }
                case EVT_DISCONNECT: {
                    final Object[] objects = (Object[]) msg.obj;
                    mDisconnectionListener = (DisconnectionListener) objects[0];
                    Object[] transferObjects = {StateInfo.Reason.DisconnectRequest};
                    transitionTo(mDisconnectingState, transferObjects);
                    break;
                }
                case EVT_GATT_DISCONNECTED: {
                    mStateInfo.setGattConnectionState(StateInfo.GattConnectionState.Disconnected, true);
                    Object[] transferObjects = {StateInfo.Reason.DidDisconnection};
                    transitionTo(mDisconnectingState, transferObjects);
                    break;
                }
                case BleReceiver.EVT_ACL_DISCONNECTED: {
                    mStateInfo.setAclConnectionState(StateInfo.AclConnectionState.Disconnected, true);
                    break;
                }
                case BleReceiver.EVT_BOND_NONE: {
                    mStateInfo.setBondState(StateInfo.BondState.NotBonded, true);
                    Object[] transferObjects = {StateInfo.Reason.EncryptionFailed};
                    transitionTo(mDisconnectingState, transferObjects);
                    break;
                }
                default:
                    return StateMachine.NOT_HANDLED;
            }
            return StateMachine.HANDLED;
        }
    }

    private class CommunicationReadyState extends State {

        @Override
        public void enter(Object[] transferObjects) {
            mStateInfo.setDetailedState(StateInfo.DetailedState.CommunicationReady, null, true);
        }

        @Override
        public boolean processMessage(@NonNull Message msg) {
            switch (msg.what) {
                case EVT_COMMUNICATION_REQ: {
                    final Object[] objects = (Object[]) msg.obj;
                    transitionTo(mCommunicatingState, objects);
                    break;
                }
                default:
                    return StateMachine.NOT_HANDLED;
            }
            return StateMachine.HANDLED;
        }
    }

    private class CommunicatingState extends State {

        private static final int EVT_START_COMMUNICATION = LOCAL_EVT_BASE + 0x0001;
        private static final int EVT_COMMUNICATION_TIMEOUT = LOCAL_EVT_BASE + 0x0002;

        private static final int COMMUNICATION_TIMEOUT = 5000;
        private static final int RETRY_DELAY_TIME = 100;
        private static final int RETRY_COUNT_MAX = 2;

        private Object[] mRequestObjects;
        private int mRetryCount;

        @Override
        public void enter(Object[] transferObjects) {
            mStateInfo.setDetailedState(StateInfo.DetailedState.Communicating, null, true);
            mRequestObjects = transferObjects;
            mRetryCount = 0;
            sendMessage(EVT_START_COMMUNICATION);
        }

        @Override
        public boolean processMessage(@NonNull Message msg) {
            switch (msg.what) {
                case EVT_START_COMMUNICATION: {
                    ErrorCode errorCode = startCommunication(mRequestObjects);
                    if (null == errorCode) {
                        sendMessageDelayed(EVT_COMMUNICATION_TIMEOUT, COMMUNICATION_TIMEOUT);
                    } else {
                        retryOrErrorFinish(mRequestObjects, errorCode, RETRY_DELAY_TIME);
                    }
                    break;
                }
                case EVT_COMMUNICATION_RES: {
                    communicationFinished(mRequestObjects, (Object[]) msg.obj);
                    break;
                }
                case EVT_COMMUNICATION_REQ: {
                    communicationRequestError((Object[]) msg.obj, ErrorCode.Busy);
                    break;
                }
                case EVT_COMMUNICATION_TIMEOUT: {
                    retryOrErrorFinish(mRequestObjects, ErrorCode.CommunicationTimeout, 0);
                    break;
                }
                default:
                    return StateMachine.NOT_HANDLED;
            }
            return StateMachine.HANDLED;
        }

        @Override
        public void exit() {
            removeMessages(EVT_START_COMMUNICATION);
            removeMessages(EVT_COMMUNICATION_TIMEOUT);
        }

        private void retryOrErrorFinish(Object[] requestObjects, ErrorCode errorCode, int retryInterval) {
            if (RETRY_COUNT_MAX > mRetryCount++) {
                BleLog.w(mRetryCount + " retry.");
                sendMessageDelayed(EVT_START_COMMUNICATION, retryInterval);
            } else {
                BleLog.e("retry ... NG.");
                communicationRequestError(requestObjects, errorCode);
                transitionTo(mCommunicationReadyState);
            }
        }

        private ErrorCode startCommunication(Object[] requestObjects) {
            ErrorCode ret = ErrorCode.Unknown;
            final CommunicationReqType type = (CommunicationReqType) requestObjects[0];
            switch (type) {
                case SetNotificationEnabled: {
                    final BluetoothGattCharacteristic characteristic = (BluetoothGattCharacteristic) requestObjects[1];
                    final boolean enable = (boolean) requestObjects[2];
                    boolean result;
                    result = mBluetoothDeviceWrapper.setCharacteristicNotification(characteristic, enable);
                    if (!result) {
                        ret = ErrorCode.OSNativeError;
                        break;
                    }
                    result = mBluetoothDeviceWrapper.writeDescriptor(characteristic.getDescriptor(
                            GattUUID.Descriptor.ClientCharacteristicConfigurationDescriptor.getUuid()));
                    if (!result) {
                        ret = ErrorCode.OSNativeError;
                        break;
                    }
                    ret = null;
                    break;
                }
                case WriteCharacteristic: {
                    final BluetoothGattCharacteristic characteristic = (BluetoothGattCharacteristic) requestObjects[1];
                    boolean result = mBluetoothDeviceWrapper.writeCharacteristic(characteristic);
                    if (!result) {
                        ret = ErrorCode.OSNativeError;
                        break;
                    }
                    ret = null;
                    break;
                }
                case ReadCharacteristic: {
                    final BluetoothGattCharacteristic characteristic = (BluetoothGattCharacteristic) requestObjects[1];
                    boolean result = mBluetoothDeviceWrapper.readCharacteristic(characteristic);
                    if (!result) {
                        ret = ErrorCode.OSNativeError;
                        break;
                    }
                    ret = null;
                    break;
                }
                default:
                    BleLog.e("Fatal error.");
                    return ret;
            }
            return ret;
        }

        private void communicationFinished(Object[] requestObjects, Object[] responseObjects) {
            final CommunicationResType resType = (CommunicationResType) responseObjects[0];
            switch (resType) {
                case OnCharacteristicWrite: {
                    final CommunicationReqType reqType = (CommunicationReqType) requestObjects[0];
                    final BluetoothGattCharacteristic reqCharacteristic = (BluetoothGattCharacteristic) requestObjects[1];
                    final BluetoothGattCharacteristic resCharacteristic = (BluetoothGattCharacteristic) responseObjects[1];
                    final int gattStatus = (int) responseObjects[2];
                    if (!reqCharacteristic.getUuid().equals(resCharacteristic.getUuid())) {
                        retryOrErrorFinish(requestObjects, ErrorCode.InvalidResponseData, 0);
                        break;
                    }
                    if (CommunicationReqType.WriteCharacteristic != reqType) {
                        retryOrErrorFinish(requestObjects, ErrorCode.Unknown, 0);
                        break;
                    }
                    final WriteCharacteristicResultListener resultListener = (WriteCharacteristicResultListener) requestObjects[2];
                    resultListener.onComplete(mAddress, resCharacteristic, gattStatus, null);
                    transitionTo(mCommunicationReadyState);
                    break;
                }
                case OnCharacteristicRead: {
                    final CommunicationReqType reqType = (CommunicationReqType) requestObjects[0];
                    final BluetoothGattCharacteristic reqCharacteristic = (BluetoothGattCharacteristic) requestObjects[1];
                    final BluetoothGattCharacteristic resCharacteristic = (BluetoothGattCharacteristic) responseObjects[1];
                    final int gattStatus = (int) responseObjects[2];
                    if (!reqCharacteristic.getUuid().equals(resCharacteristic.getUuid())) {
                        retryOrErrorFinish(requestObjects, ErrorCode.InvalidResponseData, 0);
                        break;
                    }
                    if (CommunicationReqType.ReadCharacteristic != reqType) {
                        retryOrErrorFinish(requestObjects, ErrorCode.Unknown, 0);
                        break;
                    }
                    final ReadCharacteristicResultListener resultListener = (ReadCharacteristicResultListener) requestObjects[2];
                    resultListener.onComplete(mAddress, resCharacteristic, gattStatus, null);
                    transitionTo(mCommunicationReadyState);
                    break;
                }
                case OnDescriptorWrite: {
                    final CommunicationReqType reqType = (CommunicationReqType) requestObjects[0];
                    final BluetoothGattCharacteristic reqCharacteristic = (BluetoothGattCharacteristic) requestObjects[1];
                    final BluetoothGattDescriptor resDescriptor = (BluetoothGattDescriptor) responseObjects[1];
                    final int gattStatus = (int) responseObjects[2];
                    if (!reqCharacteristic.getUuid().equals(resDescriptor.getCharacteristic().getUuid())) {
                        retryOrErrorFinish(requestObjects, ErrorCode.InvalidResponseData, 0);
                        break;
                    }
                    if (CommunicationReqType.WriteDescriptor == reqType) {
                        // Nop
                    } else if (CommunicationReqType.SetNotificationEnabled == reqType) {
                        final SetNotificationResultListener resultListener = (SetNotificationResultListener) requestObjects[3];
                        resultListener.onComplete(mAddress, resDescriptor.getCharacteristic(), gattStatus, null);
                        transitionTo(mCommunicationReadyState);
                    } else {
                        retryOrErrorFinish(requestObjects, ErrorCode.Unknown, 0);
                    }
                    break;
                }
                case OnDescriptorRead: {
                    break;
                }
                default:
                    BleLog.e("Fatal error.");
                    break;
            }
        }
    }
}
