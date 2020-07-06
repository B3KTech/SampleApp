package com.atakmap.android.pulsetool.plugin.devices;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.atakmap.android.pulsetool.plugin.R;
import com.atakmap.android.pulsetool.plugin.adapters.PairedDeviceListAdapter;
import com.atakmap.android.pulsetool.plugin.pairing.ScanningDialogFragment;
import com.atakmap.android.pulsetool.plugin.ui.BaseFragment;
import com.atakmap.android.pulsetool.plugin.ui.UpdateManager;
import com.atakmap.android.pulsetool.plugin.ui.settings.ConfigureSettingsFragment;
import com.atakmap.android.pulsetool.plugin.ui.sync.DataDisplayFragment;
import com.atakmap.android.pulsetool.plugin.util.ConfirmationDialog;
import com.garmin.health.Device;
import com.garmin.health.DeviceConnectionStateListener;
import com.garmin.health.DeviceManager;
import com.garmin.health.bluetooth.FailureCode;
import com.garmin.health.customlog.LoggingSyncListener;
import com.garmin.health.settings.SupportStatus;
import com.garmin.health.sync.AbstractSyncListener;
import com.garmin.health.sync.BatteryLevel;
import com.garmin.health.sync.SyncListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Shows a list of paired Garmin devices. It also provides a way to perform bluetooth scanning and to add a new device.
 *
 * @author morajkar
 */
public class PairedDevicesDialogFragment extends BaseFragment implements DeviceConnectionStateListener {
    protected final String TAG = getClass().getSimpleName();

    public static final String DEVICE_ARG = "DEVICE_ARG";

    private PairedDeviceListAdapter mListAdapter;
    private DeviceManager deviceManager;
    private SyncProgressListener mDeviceSyncListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View rootView = super.onCreateView(inflater, container, savedInstanceState);

        setRetainInstance(true);

        deviceManager = DeviceManager.getDeviceManager();
        List<Device> devices = new ArrayList<>(deviceManager.getPairedDevices());
        mDeviceSyncListener = new SyncProgressListener();
        deviceManager.addConnectionStateListener(this);
        deviceManager.addSyncListener(mDeviceSyncListener);
        deviceManager.addLoggingSyncListener(mDeviceSyncListener);

        ListView mPairedDevicesList = rootView.findViewById(R.id.paired_devices_listview);
        mListAdapter = new PairedDeviceListAdapter(getContext(), devices, new DeviceItemClickListener());

        queryBattery(devices, mListAdapter, getActivity());

        mPairedDevicesList.setAdapter(mListAdapter);
        FloatingActionButton mAddDeviceButton = rootView.findViewById(R.id.add_device_button);
        mAddDeviceButton.setOnClickListener(mAddButtonListener);

        // if there are no paired devices, show an alert to begin the device scan
        if (devices.isEmpty()) {
            ConfirmationDialog scanningConfirm = new ConfirmationDialog(getContext(), getString(R.string.scan_devices_title), getString(R.string.scan_devices_msg), getString(R.string.alert_dialog_ok),
                    getString(R.string.alert_dialog_cancel), new ScanningBeginClickListener());
            scanningConfirm.show();
        }
        Toast.makeText(getContext(), "YAY Success", Toast.LENGTH_LONG).show();

        return rootView;
    }

    @Override
    public void onDestroyView() {
        Log.d(this.getClass().getSimpleName(), "UI TEST:   onDestroyView()");

        super.onDestroyView();

        deviceManager.removeConnectionStateListener(this);
        deviceManager.removeSyncListener(mDeviceSyncListener);
        deviceManager.removeLoggingSyncListener(mDeviceSyncListener);
    }

    @Override
    public void onResume() {
        Log.d(this.getClass().getSimpleName(), "UI TEST:   onResume()");

        super.onResume();

        setTitleInActionBar(R.string.connected_devices_title);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_paired_devices;
    }

    private View.OnClickListener mAddButtonListener = view ->
    {
        //Transitions to a new fragment to begin the Bluetooth scanning for the Garmin devices nearby
        ScanningDialogFragment scanningDialogFragment = new ScanningDialogFragment();

        getFragmentManager().beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .replace(R.id.main_root, scanningDialogFragment)
                .addToBackStack(getTag())
                .commit();
    };

    static void queryBattery(List<Device> devices, PairedDeviceListAdapter adapter, Activity activity) {
        for (Device device : devices) {
            queryBattery(device, adapter, activity);
        }
    }


    static void queryBattery(Device device, PairedDeviceListAdapter adapter, Activity activity) {
        ListenableFuture<BatteryLevel> batteryStatus = device.batteryLevel();


        Log.d("BATT_STAT", "queryBattery: " + batteryStatus);

        Futures.addCallback(device.batteryPercentage(), new FutureCallback<Integer>() {
            @Override
            public void onSuccess(@Nullable Integer result) {
                try {

                    activity.runOnUiThread(() ->
                            adapter.updateBattery(device, result == null ? -1 : result));


                } catch (Exception ignored) {
                }
            }

            @Override
            public void onFailure(@NonNull Throwable ignored) {
                Executors.newSingleThreadScheduledExecutor().schedule(() ->
                        queryBattery(device, adapter, activity), 5, TimeUnit.SECONDS);
            }
        }, Executors.newSingleThreadExecutor());


        Executors.newSingleThreadScheduledExecutor().schedule(() ->
                queryBattery(device, adapter, activity), 5, TimeUnit.SECONDS);

    }

    @Override
    public void onDeviceConnected(Device device) {
        mListAdapter.notifyDataSetChanged();
        Log.d(TAG, "onDeviceConnected(" + device.address() + ")");
    }

    @Override
    public void onDeviceDisconnected(Device device) {
        mListAdapter.notifyDataSetChanged();
    }

    @Override
    public void onDeviceConnectionFailed(Device device, FailureCode failure) {
    }

    /**
     * Listens to scanning begin click
     */
    private class ScanningBeginClickListener implements DialogInterface.OnClickListener {
        @Override
        public void onClick(DialogInterface dialogInterface, int i) {
            if (i == DialogInterface.BUTTON_POSITIVE) {
                ScanningDialogFragment scanningDialogFragment = new ScanningDialogFragment();

                getFragmentManager().beginTransaction()
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .replace(R.id.main_root, scanningDialogFragment)
                        .addToBackStack(getTag())
                        .commit();
            }
        }
    }

    /**
     * Listener to handle the various click actions on device list item.
     */
    private class DeviceItemClickListener implements PairedDeviceListAdapter.OnItemClickListener {
        @Override
        public void onForgetDeviceClick(final Device device) {
            Log.d(getTag(), "onForgetDeviceClick(device = " + device.friendlyName() + ")");
            ConfirmationDialog dialog = new ConfirmationDialog(getContext(), null, getString(R.string.connected_forget_device_message), getString(R.string.button_yes), getString(R.string.button_no),
                    (dialog1, which) ->
                    {
                        if (which == DialogInterface.BUTTON_POSITIVE) {
                            DeviceManager.getDeviceManager().forget(device.address());
                            mListAdapter.removeDevice(device.address());
                        }
                    });
            dialog.show();
        }

        @Override
        public void onItemClick(Device device) {
            Log.d(TAG, "onItemClick(device = " + device.address() + ")");

            Fragment fragment = DataDisplayFragment.getInstance(device.address());

            getFragmentManager().beginTransaction()
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .replace(R.id.main_root, fragment)
                    .addToBackStack(getTag())
                    .commit();
        }

        private void showConfigScreen(String deviceAddress) {
            Log.d(TAG, "showConfigScreen()");

            Fragment fragment = ConfigureSettingsFragment.getInstance(deviceAddress);

            getFragmentManager().beginTransaction()
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .replace(R.id.main_root, fragment)
                    .addToBackStack(getTag())
                    .commit();
        }

        @Override
        public void onSyncNowRequested(Device device) {
            Log.d(TAG, "onSyncNowRequested(" + device.address() + ")");
            device.requestSync();
        }

        @Override
        public void onRequestFirmwareUpdate(Device device) {
            Log.d(TAG, "onRequestFirmwareUpdate(" + device.address() + ")");

            Toast.makeText(PairedDevicesDialogFragment.this.getActivity(), "Checking Garmin for firmware updates.",
                    Toast.LENGTH_SHORT).show();

            UpdateManager updateManager = new UpdateManager();
            updateManager.checkForUpdates(device, getContext());
        }

        @Override
        public void onConfigureSettingsClick(Device device) {
            Log.d(TAG, "onConfigureSettingsClick(device = " + device.address() + ")");

            showConfigScreen(device.address());
        }
    }

    /**
     * SyncListener only active when this page is to update spinner and navigate to results page.
     */
    private class SyncProgressListener implements SyncListener, LoggingSyncListener {
        @Override
        public void onSyncStarted(final Device device) {
            Log.d(TAG, "onSyncStarted(device = " + device.address() + ")");
            mListAdapter.showSyncForDevice(device);
            Toast.makeText(getContext(), String.format("Sync Started for Device: [%s]", device.address()), Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onSyncComplete(Device device) {
            Log.d(TAG, "onSyncComplete(device = " + device.address() + ")");
            mListAdapter.hideSyncForDevice(device);

            Toast.makeText(getContext(), String.format("Sync Successful for Device: [%s]", device.address()), Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onSyncFailed(final Device device, Exception e) {
            mListAdapter.hideSyncForDevice(device);
            Toast.makeText(getContext(), String.format("Sync Failed for Device: [%s]", device.address()), Toast.LENGTH_LONG).show();
        }

        @Override
        public void onSyncProgress(Device device, int progress) {
            Log.d(TAG, "onSyncProgress(device = " + device.address() + ", " + "progress = " + progress + ")");
            mListAdapter.progressSyncforDevice(device, progress);
        }
    }
}
