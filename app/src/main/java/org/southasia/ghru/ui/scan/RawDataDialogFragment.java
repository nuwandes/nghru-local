//
//  RawDataDialogFragment.java
//
//  Copyright (c) 2016 OMRON HEALTHCARE Co.,Ltd. All rights reserved.
//

package org.southasia.ghru.ui.scan;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import jp.co.omron.healthcare.samplelibs.ble.blenativewrapper.BleAdvertiseData;
import jp.co.omron.healthcare.samplelibs.ble.blenativewrapper.DiscoverPeripheral;
import org.southasia.ghru.R;

import java.util.List;
import java.util.Locale;

public class RawDataDialogFragment extends DialogFragment {

    private static final String EXTRA_DISCOVER_PERIPHERAL = "extra_discover_peripheral";

    public static RawDataDialogFragment newInstance(DiscoverPeripheral discoverPeripheral) {
        Bundle args = new Bundle();
        args.putParcelable(EXTRA_DISCOVER_PERIPHERAL, discoverPeripheral);
        RawDataDialogFragment fragment = new RawDataDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public static String byteDataToHexString(byte[] data) {
        if (null == data) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("0x");
        for (byte b : data) {
            sb.append(String.format(Locale.US, "%02x", b));
        }
        return sb.toString();
    }

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        DiscoverPeripheral discoverPeripheral;
        if (savedInstanceState != null) {
            discoverPeripheral = savedInstanceState.getParcelable(EXTRA_DISCOVER_PERIPHERAL);
        } else {
            Bundle bundle = getArguments();
            discoverPeripheral = bundle.getParcelable(EXTRA_DISCOVER_PERIPHERAL);
        }

        View view = getActivity().getLayoutInflater().inflate(R.layout.raw_data_dialog, null);

        TextView rawData = (TextView) view.findViewById(R.id.raw_data);
        TableLayout detailsTable = (TableLayout) view.findViewById(R.id.table_details);

        rawData.setText(byteDataToHexString(discoverPeripheral.getScanRecord()));

        final List<BleAdvertiseData> advertiseDataList = discoverPeripheral.getAdvertiseDataList();
        for (int i = 0; i < advertiseDataList.size(); i++) {
            BleAdvertiseData advertiseData = advertiseDataList.get(i);
            getActivity().getLayoutInflater().inflate(
                    R.layout.raw_data_details_item, detailsTable);
            View rowView = detailsTable.getChildAt(i + 1);

            TextView len = (TextView) rowView.findViewById(R.id.len);
            len.setText(String.format(Locale.US, "%d", advertiseData.getLength()));

            TextView type = (TextView) rowView.findViewById(R.id.type);
            type.setText(String.format(Locale.US, "0x%02x", advertiseData.getType()));

            TextView value = (TextView) rowView.findViewById(R.id.value);
            value.setText(byteDataToHexString(advertiseData.getData()));
        }

        return new AlertDialog.Builder(getActivity())
                .setView(view)
                .setPositiveButton(android.R.string.ok, null)
                .create();
    }
}
