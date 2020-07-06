/*
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
 * Created by johnsongar on 2/2/2017.
 */
package com.atakmap.android.pulsetool.plugin.ui.settings;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import com.atakmap.android.pulsetool.plugin.R;
import com.atakmap.android.pulsetool.plugin.ui.settings.util.SettingsUtil;
import com.atakmap.android.pulsetool.plugin.ui.settings.widget.CustomAutoValueListView;
import com.atakmap.android.pulsetool.plugin.ui.settings.widget.SettingsSpinner;
import com.atakmap.android.pulsetool.plugin.ui.settings.widget.SettingsSpinner.SpinnerListener;
import com.atakmap.android.pulsetool.plugin.ui.settings.widget.SwitchOptionsView;
import com.garmin.health.Device;
import com.garmin.health.DeviceManager;
import com.garmin.health.customlog.DataSource;
import com.garmin.health.customlog.LoggingStatus.State;
import com.garmin.health.settings.*;
import com.google.common.util.concurrent.Futures;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;


public class DeviceSettingsView extends BaseSettingsView
{
    private Switch mAutoActivitySwitch;
    private Switch mHeartRateSwitch;
    private Switch mAutoUploadSwitch;
    private Switch mSpo2SleepSwitch;

    private SwitchOptionsView<String> mAlertSwitchView;
    private SettingsSpinner<String> mSyncFrequencyView;

    private EditText mTimeSyncFrequencyView;
    private EditText mStepsSyncFrequencyView;

    private TextView mStepsTextView;
    private TextView mTimeTextView;

    private SettingsSpinner<String> mBacklightView;

    private Button mUpdateLoggingButton;

    public DeviceSettingsView(Context context)
    {
        this(context, null);
    }

    public DeviceSettingsView(Context context, AttributeSet attrs)
    {
        this(context, attrs, 0);
    }

    public DeviceSettingsView(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init()
    {
        inflate(getContext(), R.layout.view_device_settings, this);

        mAutoActivitySwitch = findViewById(R.id.auto_activity_switch);
        mSpo2SleepSwitch = findViewById(R.id.spo2_sleep_switch);

        mHeartRateSwitch = findViewById(R.id.heart_rate_switch);
        mAutoUploadSwitch = findViewById(R.id.auto_upload_switch);

        mTimeSyncFrequencyView = findViewById(R.id.auto_sync_time_edit_text);
        mStepsSyncFrequencyView = findViewById(R.id.auto_sync_steps_edit_text);

        mStepsTextView = findViewById(R.id.auto_sync_steps_text);
        mTimeTextView = findViewById(R.id.auto_sync_time_text);

        mAlertSwitchView = findViewById(R.id.alert_sov);
        mAlertSwitchView.initialize(R.string.alerts);

        mSyncFrequencyView = findViewById(R.id.sync_frequency_spinner);
        mSyncFrequencyView.initialize(R.string.sync_frequency);

        mBacklightView = findViewById(R.id.backlight_mode_spinner);
        mBacklightView.initialize(R.string.backlight_mode);

        mUpdateLoggingButton = findViewById(R.id.update_logging_button);
    }

    public void setExistingSettings(Settings settings, Device device)
    {
        if (settings.deviceSettings().schema().supportsAutoActivity())
        {
            mAutoActivitySwitch.setChecked(settings.deviceSettings().autoActivityEnabled());
        }
        else
            {
            mAutoActivitySwitch.setVisibility(View.GONE);
        }

        List<String> supportedAlerts = getSupportedAlerts(settings.deviceSettings().schema());

        if(!supportedAlerts.isEmpty())
        {
            mAlertSwitchView.setEnabledOptions(settings.deviceSettings().alerts(), supportedAlerts);
        }
        else
        {
            mAlertSwitchView.setVisibility(View.GONE);
        }

        if(settings.deviceSettings().schema().supportsHeartRate())
        {
            mHeartRateSwitch.setChecked(settings.deviceSettings().heartrateEnabled());
        }
        else
        {
            mHeartRateSwitch.setVisibility(View.GONE);
        }

        if(settings.deviceSettings().schema().supportsAutoUpload())
        {
            mAutoUploadSwitch.setChecked(settings.deviceSettings().autoUploadEnabled());
        }
        else
        {
            mAutoUploadSwitch.setVisibility(View.GONE);
        }

        if (device.enhancedSleepSupportStatus() == SupportStatus.ENABLED)
        {
            mSpo2SleepSwitch.setChecked(settings.deviceSettings().pulseOxSleepEnabled());
        }
        else
        {
            mSpo2SleepSwitch.setVisibility(View.GONE);
        }

        if (settings.deviceSettings().schema().supportsAutoSyncFrequency())
        {
            mSyncFrequencyView.setOptions(settings.deviceSettings().autoSyncFrequencyOptions(), settings.deviceSettings().autoSyncFrequency());
        }
        else
        {
            mSyncFrequencyView.setVisibility(View.GONE);
        }

        if(SyncFrequency.CUSTOM.equals(settings.deviceSettings().autoSyncFrequency()))
        {
            mStepsSyncFrequencyView.setText(String.valueOf(settings.deviceSettings().autoSyncSteps()));
        }
        else
        {
            mStepsSyncFrequencyView.setVisibility(View.GONE);
            mStepsTextView.setVisibility(GONE);
        }

        if(SyncFrequency.CUSTOM.equals(settings.deviceSettings().autoSyncFrequency()))
        {
            mTimeSyncFrequencyView.setText(String.valueOf(settings.deviceSettings().autoSyncTime()));
        }
        else
        {
            mTimeSyncFrequencyView.setVisibility(View.GONE);
            mTimeTextView.setVisibility(GONE);
        }

        if(settings.deviceSettings().schema().supportsBacklightSettings())
        {
            mBacklightView.setOptions(new ArrayList<>(settings.deviceSettings().schema().supportedBacklightSettings()), settings.deviceSettings().backlightSetting());
        }
        else
        {
            mBacklightView.setVisibility(View.GONE);
        }

        if(device.dataLoggingSupportStatus() != SupportStatus.ENABLED)
        {
            mUpdateLoggingButton.setVisibility(GONE);
        }
        else
        {
            mUpdateLoggingButton.setOnClickListener((v) ->
            {
                mUpdateLoggingButton.setEnabled(false);

                new LoggingGetSettingsTask().execute(device);
            });
        }
    }

    private List<String> getSupportedAlerts(DeviceSettingsSchema schema)
    {
        List<String> ret = new ArrayList<>();

        if(schema.supportsHydrationAlert())
        {
            ret.add(Alert.HYDRATION);
        }

        if(schema.supportsMoveAlert())
        {
            ret.add(Alert.MOVE);
        }

        if(schema.supportsHourlyChimeAlert())
        {
            ret.add(Alert.HOURLY_CHIME);
        }

        if(schema.supportsAbnormalHeartRateAlert())
        {
            ret.add(Alert.ABNORMAL_HEART_RATE);
        }

        if(schema.supportsAutoActivity())
        {
            ret.add(Alert.ACTIVITY_DETECTION);
        }

        if(schema.supportsGoalAlert())
        {
            ret.add(Alert.GOAL_NOTIFICATIONS);
        }

        if(schema.supportsPushNotificationAlert())
        {
            ret.add(Alert.PUSH_NOTIFICATIONS);
        }

        if(schema.supportsSpo2Alert())
        {
            ret.add(Alert.SPO2_DETECTION);
        }

        if(schema.supportsStressAlert())
        {
            ret.add(Alert.STRESS_LEVEL);
        }

        return ret;
    }

    public void updateSyncFrequency(String syncFrequency)
    {
        if(syncFrequency.equals(SyncFrequency.CUSTOM))
        {
            mStepsSyncFrequencyView.setVisibility(View.VISIBLE);
            mStepsTextView.setVisibility(VISIBLE);
        }
        else
        {
            mStepsSyncFrequencyView.setVisibility(View.GONE);
            mStepsTextView.setVisibility(GONE);
        }

        if(syncFrequency.equals(SyncFrequency.CUSTOM))
        {
            mTimeSyncFrequencyView.setVisibility(View.VISIBLE);
            mTimeTextView.setVisibility(VISIBLE);
        }
        else
        {
            mTimeSyncFrequencyView.setVisibility(View.GONE);
            mTimeTextView.setVisibility(GONE);
        }
    }

    public void populateSettingsBuilder(DeviceSettings.Builder builder)
    {
        if(SettingsUtil.isVisible(mAutoActivitySwitch))
        {
            builder.setAutoDetectActivity(mAutoActivitySwitch.isChecked());
        }

        if(SettingsUtil.isVisible(mAlertSwitchView))
        {
            builder.setAlerts(mAlertSwitchView.checkedOptions());
        }

        if(SettingsUtil.isVisible(mHeartRateSwitch))
        {
            builder.setHeartrateEnabled(mHeartRateSwitch.isChecked());
        }

        if(SettingsUtil.isVisible(mAutoUploadSwitch))
        {
            builder.setAutoUploadEnabled(mAutoUploadSwitch.isChecked());
        }

        if(SettingsUtil.isVisible(mSyncFrequencyView))
        {
            builder.setAutoSyncFrequency(mSyncFrequencyView.getSelectedValue());
        }

        if(SettingsUtil.isVisible(mStepsSyncFrequencyView))
        {
            builder.setAutoSyncCustomStep(Integer.valueOf(mStepsSyncFrequencyView.getText().toString()));
        }

        if(SettingsUtil.isVisible(mTimeSyncFrequencyView))
        {
            builder.setAutoSyncCustomTime(Integer.valueOf(mTimeSyncFrequencyView.getText().toString()));
        }

        if(SettingsUtil.isVisible(mBacklightView))
        {
            builder.setBacklightSetting(mBacklightView.getSelectedValue());
        }

        if(SettingsUtil.isVisible(mSpo2SleepSwitch))
        {
            builder.setPulseOxSleepEnabled(mSpo2SleepSwitch.isChecked());
        }
    }

    public void setFrequencyChangeListener(SpinnerListener<String> syncFrequencyEnumSpinnerListener)
    {
        mSyncFrequencyView.setEnumSpinnerListener(syncFrequencyEnumSpinnerListener);
    }

    private class LoggingGetSettingsTask extends AsyncTask<Device, Integer, Map<DataSource, Float>>
    {
        Device mDevice;
        AtomicBoolean error = new AtomicBoolean(false);

        protected Map<DataSource, Float> doInBackground(Device... devices)
        {
            Device device = devices[0];
            mDevice = device;

            Map<DataSource, Float> ret = new HashMap<>();

            Set<DataSource> sources = device.supportedLoggingTypes();

            if(sources == null)
            {
                return ret;
            }

            for(DataSource source : sources)
            {
                try
                {
                    Futures.getChecked(DeviceManager.getDeviceManager().getLoggingState(device.address(), source, loggingStatus ->
                    {
                        if(loggingStatus != null && loggingStatus.getState() == State.LOGGING_ON)
                        {
                            ret.put(source, Float.valueOf(loggingStatus.getInterval()));
                        }
                        else if(loggingStatus != null && loggingStatus.getState() == State.LOGGING_OFF)
                        {
                            ret.put(source, 0.0f);
                        }
                        else
                        {
                            ret.put(source, null);
                            error.set(true);
                        }
                    }), Exception.class);
                }
                catch(Exception ignored) {}
            }

            return ret;
        }

        protected void onProgressUpdate(Integer... progress) {}

        protected void onPostExecute(Map<DataSource, Float> options)
        {
            AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
            View root = LayoutInflater.from(getContext()).inflate(R.layout.logging_options_dialog, null);
            CustomAutoValueListView<DataSource> dataSources = root.findViewById(R.id.logging_view_list);

            dataSources.initialize(DataSource.class, EnumSet.of(DataSource.HEART_RATE, DataSource.ZERO_CROSSING, DataSource.PULSE_OX, DataSource.STRESS), R.string.logging_title);

            if(options.isEmpty())
            {
                Toast.makeText(getContext(), "No Custom Types Returned Settings", Toast.LENGTH_SHORT).show();
                mUpdateLoggingButton.setEnabled(true);
                return;
            }

            if(error.get())
            {
                Toast.makeText(getContext(), "Some Error Occurred Receiving Settings", Toast.LENGTH_SHORT).show();
            }

            dataSources.setOptions(options);

            dialog.setView(root);
            dialog.setNegativeButton("Cancel", (dialog1, which) -> { dialog1.dismiss(); mUpdateLoggingButton.setEnabled(true); });
            dialog.setPositiveButton("Send", (dialog12, which) ->
                    new LoggingSetSettingsTask().execute(dataSources.getCheckedOptions(), mDevice, dialog12));

            dialog.create().show();
        }
    }

    private class LoggingSetSettingsTask extends AsyncTask<Object, Integer, Boolean>
    {
        Dialog mDialog;

        protected Boolean doInBackground(Object... settings)
        {
            Map<DataSource, Float> options = (Map<DataSource, Float>) settings[0];
            Device device = (Device) settings[1];
            mDialog = (Dialog) settings[2];

            DeviceManager manager = DeviceManager.getDeviceManager();

            AtomicBoolean error = new AtomicBoolean(false);

            Set<DataSource> sources = device.supportedLoggingTypes();

            if(sources == null)
            {
                return false;
            }

            for(DataSource source : sources)
            {
                int interval = options.containsKey(source) ? options.get(source).intValue() : 0;

                if(DataSource.NO_INTERVAL_SOURCES.contains(source))
                {
                    interval = source.defaultInterval();
                }

                try
                {
                    Futures.getChecked(manager.setLoggingStateWithInterval(device.address(), source, options.containsKey(source), interval, loggingStatus ->
                    {
                        if(loggingStatus != null && loggingStatus.getState() != State.LOGGING_ON && loggingStatus.getState() != State.LOGGING_OFF)
                        {
                            error.set(true);
                        }
                    }), Exception.class);
                }
                catch (Exception e)
                {
                    error.set(true);
                }
            }

            return error.get();
        }

        protected void onProgressUpdate(Integer... progress) {}

        protected void onPostExecute(Boolean result)
        {
            if(result)
            {
                Toast.makeText(getContext(), "Some Error Occurred Sending Settings", Toast.LENGTH_SHORT).show();
            }
            else
            {
                Toast.makeText(getContext(), "Settings Sent Successfully", Toast.LENGTH_SHORT).show();
            }

            mUpdateLoggingButton.setEnabled(true);

            mDialog.dismiss();
        }
    }
}