package com.atakmap.android.pulsetool.plugin.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.format.DateFormat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.UiThread;
import androidx.recyclerview.widget.RecyclerView;

import com.atakmap.android.pulsetool.plugin.R;
import com.atakmap.android.pulsetool.plugin.ui.charts.HeartRateLoggingChart;
import com.garmin.health.ConnectionState;
import com.garmin.health.Device;
import com.garmin.health.DeviceManager;
import com.garmin.health.DeviceModel;
import com.garmin.health.database.dtos.HeartRateLog;
import com.garmin.health.sync.RestingHeartRate;
import com.garmin.health.sync.Timestamp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Copyright (c) 2017 Garmin International. All Rights Reserved.
 * <p></p>
 * This software is the confidential and proprietary information of
 * Garmin International.
 * You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement
 * you entered into with Garmin International.
 * <p></p>
 * Garmin International MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF THE
 * SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE, OR NON-INFRINGEMENT. Garmin International SHALL NOT BE LIABLE FOR ANY DAMAGES
 * SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING
 * THIS SOFTWARE OR ITS DERIVATIVES.
 * <p></p>
 * Created by jacksoncol on 6/22/17.
 */

public class PairedDeviceListAdapter extends BaseAdapter {
    private static final float IMAGE_SCREEN_PERCENT = .2f;
    private static final DisplayMetrics METRICS = Resources.getSystem().getDisplayMetrics();
    private static final int IMAGE_DIM = (int) (Math.min(METRICS.widthPixels, METRICS.heightPixels) * IMAGE_SCREEN_PERCENT);
    private Map<DeviceModel, Bitmap> mBitmapCache = new HashMap<>();

    private Context mContext;
    RestingHeartRate restingHR;
    private List<ConnectedDevice> mDevices;

    private OnItemClickListener mOnItemClickListener;

    public PairedDeviceListAdapter(Context context, List<Device> devices, OnItemClickListener onItemClickListener) {
        mDevices = new ArrayList<>();

        for (Device device : devices) {
            mDevices.add(new ConnectedDevice(device));
        }

        this.mContext = context;
        this.mOnItemClickListener = onItemClickListener;
    }

    @Override
    public boolean isEnabled(int position) {
        return true;
    }

    @Override
    public int getCount() {
        return mDevices.size();
    }

    @Override
    public ConnectedDevice getItem(int i) {
        return mDevices.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View getView(final int i, View view, ViewGroup viewGroup) {
        ViewHolder viewHolder;

        if (view == null) {
            LayoutInflater layoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = layoutInflater.inflate(R.layout.view_paired_device_row, viewGroup, false);
            viewHolder = new ViewHolder(view);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        final ConnectedDevice connectedDevice = mDevices.get(i);

        if (connectedDevice != null) {
            final Device device = connectedDevice.device;

            viewHolder.deviceIcon.setImageBitmap(getDeviceImage(device));
            viewHolder.address.setText(device.friendlyName() + "\n" + device.address());
            viewHolder.mForgetDevice.setOnClickListener(view16 -> mOnItemClickListener.onForgetDeviceClick(device));
            viewHolder.showSyncNow(connectedDevice);
            viewHolder.mSyncNow.setOnClickListener(view15 -> mOnItemClickListener.onSyncNowRequested(device));
            viewHolder.mFirmwareUpdate.setOnClickListener(v -> mOnItemClickListener.onRequestFirmwareUpdate(device));
            viewHolder.setConnectivityStatus(connectedDevice.isConnected());
            viewHolder.itemView.setOnClickListener(view13 -> mOnItemClickListener.onItemClick(device));
            viewHolder.mSettings.setOnClickListener(view12 -> mOnItemClickListener.onConfigureSettingsClick(device));
            viewHolder.toggleAnimator(connectedDevice.isSyncing);

            if (connectedDevice.shouldDisplaySyncProgress != null) {
                viewHolder.toggleSyncProgressBar(connectedDevice.shouldDisplaySyncProgress);
            }

            viewHolder.mSyncProgressBar.setProgress(connectedDevice.syncProgress);
            viewHolder.setBattery(connectedDevice.battery);
            viewHolder.updateBattery();
            try {
                viewHolder.displayRHRTextData(restingHR);
            }catch (Exception e ){
                e.printStackTrace();
            }
        }

        return view;
    }

    private Bitmap getDeviceImage(Device device) {
        if (!mBitmapCache.containsKey(device.model())) {
            Resources resources = mContext.getResources();

            Bitmap bitmap = BitmapFactory
                    .decodeResource(resources, device.image());

            mBitmapCache.put(
                    device.model(),
                    Bitmap.createScaledBitmap(bitmap, IMAGE_DIM, IMAGE_DIM, false));
        }

        return mBitmapCache.get(device.model());
    }


    private static class ViewHolder extends RecyclerView.ViewHolder {
        private ViewGroup mRootView;

        private final TextView mDeviceBattery;
        private final TextView mDeviceHeartRate;
        private final ImageView mDeviceBatteryIcon;
        private TextView address;

        private ImageView deviceIcon;
        private ImageView mForgetDevice;
        private ImageView mSyncNow;
        private ImageView mFirmwareUpdate;
        private ImageView mSettings;
        private ProgressBar mSyncProgressBar;

        private AlphaAnimation syncAnimator;

        int mBattery = -1;
        int mHeartRate;

        ViewHolder(View rowView) {
            super(rowView);

            mRootView = rowView.findViewById(R.id.device_root_view);
            address = rowView.findViewById(R.id.device_uuid);
            deviceIcon = rowView.findViewById(R.id.device_icon);
            mForgetDevice = rowView.findViewById(R.id.device_forget_device);
            mSyncNow = rowView.findViewById(R.id.device_sync_now);
            mFirmwareUpdate = rowView.findViewById(R.id.device_firmware_update);
            mSettings = rowView.findViewById(R.id.settings_configuration);
            mDeviceBattery = rowView.findViewById(R.id.device_battery);
            mDeviceBatteryIcon = rowView.findViewById(R.id.device_battery_icon);
            mSyncProgressBar = rowView.findViewById(R.id.sync_progress_bar);
            mDeviceHeartRate = rowView.findViewById(R.id.tv_heart_rate);

            syncAnimator = new AlphaAnimation(0.1f, 1.0f);
            syncAnimator.setDuration(1000);
            syncAnimator.setFillAfter(true);
            syncAnimator.setRepeatCount(Animation.INFINITE);
            syncAnimator.setRepeatMode(Animation.REVERSE);
        }

        void setConnectivityStatus(boolean isDeviceConnected) {
            deviceIcon.setAlpha(isDeviceConnected ? 1.0f : 0.5f);
        }

        void toggleAnimator(boolean toggle) {
            if (!toggle) {
                deviceIcon.clearAnimation();
            } else {
                deviceIcon.clearAnimation();
                deviceIcon.startAnimation(syncAnimator);
            }
        }

        private void toggleSyncProgressBar(boolean toggle) {
            if (toggle) {
                expand(mSyncProgressBar);
            } else {
                collapse(mSyncProgressBar);
            }
        }

        void showSyncNow(ConnectedDevice connectedDevice) {
            int visibility = connectedDevice.isConnected() && connectedDevice.device.supportsManualSync() ? View.VISIBLE : View.INVISIBLE;

            mSyncNow.setVisibility(visibility);
        }

        private void displayRHRTextData(RestingHeartRate restingHeartRate) {

            String result = "";

            result += "Date: " + DateFormat.format("MM/dd/yyyy HH:mm", restingHeartRate.getTimestamp() * 1000) + "\n";
            result += "RHR: " + restingHeartRate.getRestingHeartRate() + "\n";
            result += "Current Day RHR: " + restingHeartRate.getCurrentDayRestingHeartRate();
            mDeviceHeartRate.setText(result);

        }

        void updateBattery() {
            if (mBattery == -1) {
                mDeviceBattery.setText("--%");
                mDeviceBatteryIcon.setImageResource(R.drawable.ic_battery_unknown_black_24dp);
            } else {
                mDeviceBattery.setText(Integer.toString(mBattery) + "%");

                if (mBattery > 95) {
                    mDeviceBatteryIcon.setImageResource(R.drawable.ic_battery_full_black_24dp);
                } else if (mBattery > 40) {
                    mDeviceBatteryIcon.setImageResource(R.drawable.ic_battery_60_black_24dp);
                } else {
                    mDeviceBatteryIcon.setImageResource(R.drawable.ic_battery_20_black_24dp);
                }
            }
        }

        boolean setBattery(Integer battery) {
            if (mBattery == battery) {
                return false;
            } else {
                mBattery = battery;
                return true;
            }
        }
    }

    private ConnectedDevice getDevice(String address) {
        for (ConnectedDevice device : mDevices) {
            if (device.device.address().equals(address)) {
                return device;
            }
        }

        return null;
    }

    public void removeDevice(String macAddress) {
        ConnectedDevice connectedDevice = getDevice(macAddress);

        if (connectedDevice != null) {
            mDevices.remove(connectedDevice);
            notifyDataSetChanged();
        }
    }

    public interface OnItemClickListener {
        void onForgetDeviceClick(Device device);

        void onItemClick(Device device);

        void onSyncNowRequested(Device device);

        void onRequestFirmwareUpdate(Device device);

        void onConfigureSettingsClick(Device device);
    }

    private class ConnectedDevice {
        public int battery;
        private Device device;
        private boolean isSyncing;
        private HeartRateLog heartRate;

        private Boolean shouldDisplaySyncProgress;
        private int syncProgress;

        ConnectedDevice(Device device) {
            this.device = device;
            this.isSyncing = false;
            this.battery = -1;
            this.shouldDisplaySyncProgress = null;
        }

        boolean isConnected() {
            return DeviceManager.getDeviceManager().getDevice(device.address()).connectionState() == ConnectionState.CONNECTED;
        }
    }

    @UiThread
    private void updateSyncState(String deviceAddress, boolean syncState, int progress) {
        ConnectedDevice connectedDevice = getDevice(deviceAddress);

        if (connectedDevice != null) {
            if (connectedDevice.isSyncing != syncState) {
                connectedDevice.shouldDisplaySyncProgress = syncState;
            } else {
                connectedDevice.shouldDisplaySyncProgress = null;
            }

            connectedDevice.syncProgress = progress;
            connectedDevice.isSyncing = syncState;
        }

        notifyDataSetChanged();
    }

    @UiThread
    private void updateBattery(String deviceAddress, int battery) {
        ConnectedDevice connectedDevice = getDevice(deviceAddress);

        Log.d("UPDATE_BTT_LIST", "updateBattery: " + battery);

        if (connectedDevice != null) {
            connectedDevice.battery = battery;
        }

        notifyDataSetChanged();
    }
    @UiThread
    public void showSyncForDevice(Device device) {
        updateSyncState(device.address(), true, 0);
    }

    @UiThread
    public void hideSyncForDevice(Device device) {
        updateSyncState(device.address(), false, 100);
    }

    public void progressSyncforDevice(Device device, int progress) {
        updateSyncState(device.address(), true, progress);
    }

    @UiThread
    public void updateBattery(Device device, int battery) {
        updateBattery(device.address(), battery);
    }

    public void updateHeartRate(Device device) {
        updateHeartRate(device);
    }

    public static void expand(final View v) {
        int matchParentMeasureSpec = View.MeasureSpec.makeMeasureSpec(((View) v.getParent()).getWidth(), View.MeasureSpec.EXACTLY);
        int wrapContentMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        v.measure(matchParentMeasureSpec, wrapContentMeasureSpec);
        final int targetHeight = v.getMeasuredHeight();

        // Older versions of android (pre API 21) cancel animations for views with a height of 0.
        v.getLayoutParams().height = 1;
        v.setVisibility(View.VISIBLE);
        Animation a = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                v.getLayoutParams().height = interpolatedTime == 1
                        ? ViewGroup.LayoutParams.WRAP_CONTENT
                        : (int) (targetHeight * interpolatedTime);
                v.requestLayout();
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        // Expansion speed of 1dp/ms
        a.setDuration(500);
        v.startAnimation(a);
    }

    public static void collapse(final View v) {
        final int initialHeight = v.getMeasuredHeight();

        Animation a = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                if (interpolatedTime == 1) {
                    v.setVisibility(View.GONE);
                } else {
                    v.getLayoutParams().height = initialHeight - (int) (initialHeight * interpolatedTime);
                    v.requestLayout();
                }
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        // Collapse speed of 1dp/ms
        a.setDuration(500);
        v.startAnimation(a);
    }
}
