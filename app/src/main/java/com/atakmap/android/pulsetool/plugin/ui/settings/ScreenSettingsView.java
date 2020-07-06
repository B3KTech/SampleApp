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

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Toast;
import com.atakmap.android.pulsetool.plugin.R;
import com.atakmap.android.pulsetool.plugin.ui.settings.util.SettingsUtil;
import com.atakmap.android.pulsetool.plugin.ui.settings.widget.SettingsSpinner;
import com.atakmap.android.pulsetool.plugin.ui.settings.widget.SwitchOptionsView;
import com.garmin.health.Device;
import com.garmin.health.settings.ConnectIqItem;
import com.garmin.health.settings.DeviceSettings;
import com.garmin.health.settings.Settings;
import com.google.common.util.concurrent.Futures;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ScreenSettingsView extends BaseSettingsView
{
    private SettingsSpinner<String> mOrientationView;
    private SettingsSpinner<String> mWatchFaceView;
    private SettingsSpinner<String> mInitialScreenView;
    private SettingsSpinner<String> mScreenModeView;

    private SwitchOptionsView<String> mConfigDisplayScreensView;
    private SwitchOptionsView<ConnectIqItem> mWidgetScreensView;
    private SwitchOptionsView<ConnectIqItem> mAppsView;

    private Device mDevice;

    public ScreenSettingsView(Context context)
    {
        this(context, null);
    }

    public ScreenSettingsView(Context context, AttributeSet attrs)
    {
        this(context, attrs, 0);
    }

    public ScreenSettingsView(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init()
    {
        inflate(getContext(), R.layout.view_screen_settings, this);

        mWatchFaceView = findViewById(R.id.watchface_spinner);
        mWatchFaceView.initialize(R.string.watch_faces);
        mOrientationView = findViewById(R.id.config_orientation);
        mOrientationView.initialize(R.string.display_orientation, "display_orientation_");
        mConfigDisplayScreensView = findViewById(R.id.config_screen_settings);
        mConfigDisplayScreensView.initialize(R.string.display_screens);
        mInitialScreenView = findViewById(R.id.config_initial_screen);
        mInitialScreenView.initialize(R.string.initial_screen, "display_screen_");
        mScreenModeView = findViewById(R.id.screen_mode_spinner);
        mScreenModeView.initialize(R.string.screen_mode);

        mWidgetScreensView = findViewById(R.id.config_widget_settings);
        mWidgetScreensView.initialize(R.string.widgets);

        mAppsView = findViewById(R.id.config_apps_settings);
        mAppsView.initialize(R.string.custom_apps);

        mAppsView.setOnSwitchLongPressListener(connectIqItem ->
        {
            if(mDevice != null)
            {
                try
                {
                    if(Futures.getChecked(mDevice.launchConnectIqApp(connectIqItem), Exception.class))
                    {
                        Toast.makeText(getContext(), "App Launch Succeeded", Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        Toast.makeText(getContext(), "App Launch Failed", Toast.LENGTH_SHORT).show();
                    }
                }
                catch(Exception e)
                {
                    Toast.makeText(getContext(), "App Launch Failed With Exception", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void setupWatchface(Settings settings)
    {
        if(settings.deviceSettings().schema().supportsScreenModes())
        {
            mScreenModeView.setOptions(new ArrayList<>(settings.deviceSettings().schema().supportedScreenModes()), settings.deviceSettings().screenMode());
        }
        else
        {
            mScreenModeView.setVisibility(View.GONE);
        }

        final Set<String> supportedWatchFaces = settings.deviceSettings().schema().supportedWatchFaces();

        if(supportedWatchFaces != null && !supportedWatchFaces.isEmpty())
        {
            mWatchFaceView.setOptions(new ArrayList<>(supportedWatchFaces), settings.deviceSettings().watchface());
        }
        else
        {
            mWatchFaceView.setVisibility(View.GONE);
        }
    }

    private void setupDisplayOrientation(Settings settings)
    {
        if (settings.deviceSettings().schema().supportsDisplayOrientation())
        {
            mOrientationView.setOptions(settings.deviceSettings().displayOrientationOptions(), settings.deviceSettings().displayOrientation());
        }
        else
        {
            mOrientationView.setVisibility(View.GONE);
        }
    }

    private void setupDisplayScreens(Settings settings, Device device)
    {
        if(!settings.deviceSettings().schema().supportedScreens().isEmpty())
        {
            mConfigDisplayScreensView.setEnabledOptions(settings.deviceSettings().displayScreens(), settings.deviceSettings().schema().supportedScreens());
        }
        else
        {
            mConfigDisplayScreensView.setVisibility(View.GONE);
        }

        try
        {
            List<ConnectIqItem> supported = device.getSupportedCustomScreens().get();
            List<ConnectIqItem> enabled = device.getEnabledCustomScreens().get();

            if(!supported.isEmpty())
            {
                mWidgetScreensView.setVisibility(View.VISIBLE);
            }

            mWidgetScreensView.setEnabledOptions(enabled, supported);
        }
        catch(Exception e)
        {
            mWidgetScreensView.setVisibility(View.GONE);
        }

        try
        {
            List<ConnectIqItem> supported = device.getSupportedApps().get();
            List<ConnectIqItem> enabled = device.getEnabledApps().get();

            if(!supported.isEmpty())
            {
                mAppsView.setVisibility(View.VISIBLE);
            }

            mAppsView.setEnabledOptions(enabled, supported);
        }
        catch(Exception e)
        {
            mAppsView.setVisibility(View.GONE);
        }
    }

    private void setupDefaultScreen(Settings settings)
    {
        if (settings.deviceSettings().schema().supportsInitialDisplayScreen())
        {
            List<String> screens = new ArrayList<>(settings.deviceSettings().schema().supportedScreens());
            mInitialScreenView.setOptions(screens, settings.deviceSettings().initialDisplayScreen());
        }
        else
        {
            mInitialScreenView.setVisibility(View.GONE);
        }
    }

    public void setExistingSettings(Settings settings, Device device)
    {
        mDevice = device;

        setupWatchface(settings);
        setupDisplayOrientation(settings);
        setupDisplayScreens(settings, device);
        setupDefaultScreen(settings);
    }

    @Override
    public void populateSettingsBuilder(DeviceSettings.Builder builder)
    {
        if (SettingsUtil.isVisible(mOrientationView))
        {
            builder.setDisplayOrientation(mOrientationView.getSelectedValue());
        }

        if (SettingsUtil.isVisible(mConfigDisplayScreensView))
        {
            builder.setDisplayScreens(mConfigDisplayScreensView.checkedOptions());
        }

        if (SettingsUtil.isVisible(mWatchFaceView))
        {
            builder.setWatchFace(mWatchFaceView.getSelectedValue());
        }

        if (SettingsUtil.isVisible(mInitialScreenView))
        {
            builder.setInitialDisplayScreen(mInitialScreenView.getSelectedValue());
        }

        if (SettingsUtil.isVisible(mScreenModeView))
        {
            builder.setScreenMode(mScreenModeView.getSelectedValue());
        }

        if (SettingsUtil.isVisible(mWidgetScreensView) && SettingsUtil.isVisible(mAppsView))
        {
            //Custom widget screens will respect order of the list
            List<ConnectIqItem> widgets = new ArrayList<>(mWidgetScreensView.checkedOptions());

            //Custom apps will respect order of the list
            List<ConnectIqItem> apps = new ArrayList<>(mAppsView.checkedOptions());

            mDevice.updateConnectIqItems(apps, widgets);
        }
    }
}