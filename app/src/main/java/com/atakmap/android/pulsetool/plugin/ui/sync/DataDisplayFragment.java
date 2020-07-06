package com.atakmap.android.pulsetool.plugin.ui.sync;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Looper;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.garmin.function.Consumer;
import com.atakmap.android.pulsetool.plugin.R;
import com.atakmap.android.pulsetool.plugin.sleep.SleepGraphFragment;
import com.atakmap.android.pulsetool.plugin.ui.BaseFragment;
import com.garmin.health.Device;
import com.garmin.health.DeviceManager;
import com.garmin.health.GarminRequestManager;
import com.garmin.health.customlog.DataSource;
import com.garmin.health.customlog.LoggingStatus;
import com.garmin.health.sleep.RawSleepData;
import com.garmin.health.sleep.SleepResult;
import com.garmin.health.sleep.SleepResultListener;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

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
 * Created by jacksoncol on 7/3/18.
 */
public class DataDisplayFragment extends BaseFragment
{
    private static final String DEVICE_ADDRESS = "device.address";

    private Device mDevice = null;

    private Button mSyncDataButton = null;
    private Button mSleepButton = null;
    private Button mLogButton = null;
    private Button mErrorFileButton = null;
    private Button mProcessedFileButton = null;
    private Button mHrvDataFileButton = null;

    private TextView mStartText = null;
    private TextView mEndText = null;
    private ImageView mStartButton = null;
    private ImageView mEndButton = null;

    private long mEndTime = Calendar.getInstance().getTimeInMillis()/1000;
    private long mStartTime = mEndTime - 86400;

    @Override
    protected int getLayoutId()
    {
        return R.layout.fragment_data_display;
    }

    public static DataDisplayFragment getInstance(String deviceAddress)
    {
        Bundle bundle = new Bundle();
        bundle.putString(DEVICE_ADDRESS, deviceAddress);

        DataDisplayFragment fragment = new DataDisplayFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        if(getArguments() != null)
        {
            String deviceAddress = getArguments().getString(DEVICE_ADDRESS);
            mDevice = deviceAddress == null ? null : DeviceManager.getDeviceManager().getDevice(deviceAddress);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        mSyncDataButton = view.findViewById(R.id.data_query_button);
        mSleepButton = view.findViewById(R.id.sleep_button);
        mLogButton = view.findViewById(R.id.data_logging_button);
        mErrorFileButton = view.findViewById(R.id.error_file_button);
        mProcessedFileButton = view.findViewById(R.id.processed_file_button);
        mHrvDataFileButton = view.findViewById(R.id.hrv_data_file_button);
        mStartText = view.findViewById(R.id.start_text);
        mEndText = view.findViewById(R.id.end_text);
        mStartButton = view.findViewById(R.id.start_button);
        mEndButton = view.findViewById(R.id.end_button);

        mErrorFileButton.setVisibility(FileViewFragment.getDefaultInstance(getContext()).isImplemented() ? View.VISIBLE : View.GONE);
        mProcessedFileButton.setVisibility(FileViewFragment.getDefaultInstance(getContext()).isImplemented() ? View.VISIBLE : View.GONE);
        mHrvDataFileButton.setVisibility(FileViewFragment.getDefaultInstance(getContext()).isImplemented() ? View.VISIBLE : View.GONE);

        checkAvailableData();

        mStartText.setEnabled(false);
        mStartText.setText(formatDateTime(mStartTime));

        mEndText.setEnabled(false);
        mEndText.setText(formatDateTime(mEndTime));

        mStartButton.setOnClickListener(v -> new DateTimePicker(timestamp ->
        {
            if(timestamp < mEndTime)
            {
                mStartText.setText(formatDateTime(timestamp));
                mStartTime = timestamp;
            }
            else
            {
                Toast.makeText(getContext(), "Invalid time.", Toast.LENGTH_SHORT).show();
            }

        }, getActivity()));

        mEndButton.setOnClickListener(v -> new DateTimePicker(timestamp ->
        {
            if(timestamp > mStartTime)
            {
                mEndText.setText(formatDateTime(timestamp));
                mEndTime = timestamp;
            }
            else
            {
                Toast.makeText(getContext(), "Invalid time.", Toast.LENGTH_SHORT).show();
            }
        }, getActivity()));

        mSleepButton.setOnClickListener((v) ->
                GarminRequestManager.getRequestManager().requestSleepData(mDevice.address(), new Date(mEndTime * 1000), new SleepResultListener() {
                    @Override
                    public void onSuccess(SleepResult result)
                    {
                        getFragmentManager().beginTransaction()
                                .replace(R.id.main_root, SleepGraphFragment.getInstance(result, null))
                                .addToBackStack(null)
                                .commit();
                    }

                    @Override
                    public void onError(SleepErrorCode errorCode)
                    {
                        String message = String.format("Sleep Error: %s", errorCode == null ? "NULL" : errorCode.name().toLowerCase().replace('_', ' '));
                        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                    }
                }));

        mSleepButton.setOnLongClickListener(v ->
                {
                    DeviceManager.getDeviceManager().getSyncDataForDevice(mDevice.address(), mStartTime, mEndTime, (data) ->
                            GarminRequestManager.getRequestManager().requestSleepData(data, new SleepResultListener()
                            {
                                RawSleepData rawSleepData = RawSleepData.createRawSleepData(data);

                                @Override
                                public void onSuccess(SleepResult result)
                                {
                                    getFragmentManager().beginTransaction()
                                            .replace(R.id.main_root, SleepGraphFragment.getInstance(result, rawSleepData))
                                            .addToBackStack(null)
                                            .commit();
                                }

                                @Override
                                public void onError(SleepErrorCode errorCode)
                                {
                                    String message = String.format("Sleep Error: %s", errorCode == null ? "NULL" : errorCode.name().toLowerCase().replace('_', ' '));
                                    Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                                }
                            }));

                    return true;
                });

        mSyncDataButton.setOnClickListener((v) ->
                DeviceManager.getDeviceManager().hasSyncData(mDevice.address(), mStartTime, mEndTime, (hasData) ->
                {
                    if(hasData)
                    {
                        DeviceManager.getDeviceManager().getSyncDataForDevice(mDevice.address(), mStartTime, mEndTime, (syncData) ->
                                getFragmentManager().beginTransaction()
                                        .replace(R.id.main_root, TabFragment.getInstance(mDevice.address(), syncData))
                                        .addToBackStack(null)
                                        .commit());
                    }
                    else
                    {
                        if(Looper.myLooper() == null)
                        {
                            Looper.prepare();
                        }

                        Toast.makeText(getContext(), "No data available on this interval.", Toast.LENGTH_SHORT).show();
                    }
                }));

        mLogButton.setOnClickListener((v) ->
                DeviceManager.getDeviceManager().hasLoggedData(mDevice.address(), mStartTime, mEndTime, (hasData) ->
                {
                    if(hasData)
                    {
                        DeviceManager.getDeviceManager().getLoggedDataForDevice(mDevice.address(), mStartTime, mEndTime, (loggingResult) ->
                                getFragmentManager().beginTransaction()
                                        .replace(R.id.main_root, LoggingFragment.getInstance(mDevice.address(), loggingResult))
                                        .addToBackStack(null)
                                        .commit());
                    }
                    else
                    {
                        if(Looper.myLooper() == null)
                        {
                            Looper.prepare();
                        }

                        Toast.makeText(getContext(), "No data available on this interval.", Toast.LENGTH_SHORT).show();
                    }
                }));

        if(FileViewFragment.getDefaultInstance(getContext()).isImplemented())
        {
            mErrorFileButton.setOnClickListener((v) ->
                    getFragmentManager().beginTransaction()
                            .replace(R.id.main_root, FileViewFragment.getInstance(getContext(), mDevice, true, false))
                            .addToBackStack(null)
                            .commit());

            mProcessedFileButton.setOnClickListener((v) ->
                    getFragmentManager().beginTransaction()
                            .replace(R.id.main_root, FileViewFragment.getInstance(getContext(), mDevice, false, false))
                            .addToBackStack(null)
                            .commit());

            mHrvDataFileButton.setOnClickListener((v) ->
                    getFragmentManager().beginTransaction()
                            .replace(R.id.main_root, FileViewFragment.getInstance(getContext(), mDevice, false, true))
                            .addToBackStack(null)
                            .commit());
        }
    }

    private String formatDateTime(long startTime)
    {
        return DateFormat.format("MM/dd/yyyy h:mm:ss a", startTime * 1000).toString();
    }

    private void checkAvailableData()
    {
        DeviceManager manager = DeviceManager.getDeviceManager();

        manager.hasSyncData(mDevice.address(), hasData ->
                updateButtons(mSyncDataButton, hasData == null ? false : hasData));

        manager.hasSyncData(mDevice.address(), hasData ->
                updateButtons(mSleepButton, hasData == null ? false : hasData));

        manager.hasLoggedData(mDevice.address(), hasData ->
                updateButtons(mLogButton, hasData == null ? false : hasData));


        boolean hasData = FileViewFragment.getDefaultInstance(getContext()).shouldDisplay();
        updateButtons(mErrorFileButton, hasData);
    }

    private void updateButtons(Button button, boolean enabled)
    {
        if(getActivity() != null)
        {
            getActivity().runOnUiThread(() -> button.setEnabled(enabled));
        }
    }

    private static class DateTimePicker implements TimePickerDialog.OnTimeSetListener, DatePickerDialog.OnDateSetListener
    {
        private TimestampSetListener mTimestampSetListener;
        private Context mContext;

        private int year;
        private int month;
        private int day;
        private int hour;
        private int minute;
        private int second;

        public DateTimePicker(TimestampSetListener listener, Context context)
        {
            mTimestampSetListener = listener;
            mContext = context;

            Calendar now = Calendar.getInstance();
            DatePickerDialog dpd = new DatePickerDialog(
                    context,
                    this,
                    now.get(Calendar.YEAR),
                    now.get(Calendar.MONTH),
                    now.get(Calendar.DAY_OF_MONTH)
            );

            dpd.show();
        }

        @Override
        public void onDateSet(DatePicker datePicker, int year, int monthOfYear, int dayOfMonth)
        {
            this.year = year;
            this.month = monthOfYear + 1;
            this.day = dayOfMonth;

            Calendar now = Calendar.getInstance();
            TimePickerDialog tpd = new TimePickerDialog(
                    mContext,
                    this,
                    now.get(Calendar.HOUR),
                    now.get(Calendar.MINUTE),
                    true);

            tpd.show();
        }

        @Override
        public void onTimeSet(TimePicker timePicker, int hourOfDay, int minute)
        {
            this.hour = hourOfDay;
            this.minute = minute;

            DateTime dateTime = new DateTime(this.year, this.month, this.day, this.hour, this.minute, 0, DateTimeZone.forTimeZone(TimeZone.getDefault()));

            mTimestampSetListener.onTimestampSet(dateTime.getMillis() / 1000);
        }
    }

    private interface TimestampSetListener
    {
        void onTimestampSet(long timestamp);
    }
}
