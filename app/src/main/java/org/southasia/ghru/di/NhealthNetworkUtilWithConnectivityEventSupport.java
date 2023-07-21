package org.southasia.ghru.di;

import android.content.Context;
import com.birbit.android.jobqueue.network.NetworkEventProvider;
import com.birbit.android.jobqueue.network.NetworkUtil;
import timber.log.Timber;

public class NhealthNetworkUtilWithConnectivityEventSupport extends NHealthNetworkUtil
        implements NetworkUtil, NetworkEventProvider {
    private int networkStatus;
    private Listener listener;

    protected void setNetworkStatus(int networkStatus, boolean notifyListener) {
        this.networkStatus = networkStatus;
        Timber.d("networkStatus " + networkStatus);
        if (notifyListener && listener != null) {
            listener.onNetworkChange(networkStatus);
        }
    }

    @Override
    protected void setNetworkStatus(int networkStatus) {
        setNetworkStatus(networkStatus, true);
    }

    @Override
    public int getNetworkStatus(Context context) {
        return networkStatus;
    }

    public boolean isDisconnected() {
        return networkStatus == DISCONNECTED;
    }

    @Override
    public void setListener(Listener listener) {
        this.listener = listener;
    }
}
