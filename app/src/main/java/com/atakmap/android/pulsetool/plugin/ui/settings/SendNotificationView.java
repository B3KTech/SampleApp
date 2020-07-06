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
 * Created by johnsongar on 2/1/2017.
 */
package com.atakmap.android.pulsetool.plugin.ui.settings;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.atakmap.android.pulsetool.plugin.R;
import com.garmin.health.Device;
import com.garmin.health.NotificationManager;
import com.garmin.health.notification.NotificationResult;
import com.garmin.health.settings.SupportStatus;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;

import java.util.concurrent.Executors;

public class SendNotificationView extends LinearLayout
{
    private static final String TAG = SendNotificationView.class.getSimpleName();

    private EditText mNotificationMessage;
    private EditText mNotificationTitle;

    private long mLastMessageId = -1;

    private Device mDevice;

    public SendNotificationView(Context context)
    {
        this(context, null);
    }

    public SendNotificationView(Context context, AttributeSet attrs)
    {
        this(context, attrs, 0);
    }

    public SendNotificationView(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init()
    {
        inflate(getContext(), R.layout.view_send_notification, this);

        mNotificationMessage = findViewById(R.id.message);
        mNotificationTitle = findViewById(R.id.title);

        mNotificationTitle.setSingleLine();
        mNotificationMessage.setSingleLine();

        findViewById(R.id.send).setOnClickListener(v ->
        {
            NotificationManager manager = NotificationManager.getNotificationManager();

            Futures.addCallback(manager.createNotification(mDevice, mNotificationTitle.getText().toString(), mNotificationMessage.getText().toString(), getContext().getString(R.string.clear)), new FutureCallback<NotificationResult>()
            {
                @Override
                public void onSuccess(@Nullable NotificationResult result)
                {
                    if(result != null)
                    {
                        new Handler(Looper.getMainLooper()).post(() ->
                        {
                            mLastMessageId = result.id();
                            mNotificationMessage.getText().clear();
                            mNotificationTitle.getText().clear();
                        });
                    }
                }

                @Override
                public void onFailure(@NonNull Throwable t)
                {
                    Log.e(TAG, "failed sending notification", t);
                }
            }, Executors.newSingleThreadExecutor());
        });

        findViewById(R.id.clear).setOnClickListener(v ->
        {
            NotificationManager manager = NotificationManager.getNotificationManager();

            try
            {
                if(mLastMessageId == -1) return;

                manager.clearNotification(mLastMessageId);
                mLastMessageId = -1;
            }
            catch (Exception e)
            {
                Log.e(TAG, "failed to clear notification");
            }
        });
    }

    public void setDevice(Device device)
    {
        mDevice = device;

        if(mDevice.notificationSupportStatus() != SupportStatus.ENABLED)
        {
            this.setVisibility(View.GONE);
        }
    }
}
