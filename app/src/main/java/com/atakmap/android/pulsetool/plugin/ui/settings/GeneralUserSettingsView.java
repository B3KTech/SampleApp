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
import android.widget.LinearLayout;
import android.widget.Toast;
import com.atakmap.android.pulsetool.plugin.R;
import com.atakmap.android.pulsetool.plugin.ui.settings.util.SettingsUtil;
import com.atakmap.android.pulsetool.plugin.ui.settings.widget.CustomAutoValueListView;
import com.atakmap.android.pulsetool.plugin.ui.settings.widget.CustomAutoValueView;
import com.atakmap.android.pulsetool.plugin.ui.settings.widget.SettingsSpinner;
import com.garmin.health.Device;
import com.garmin.health.settings.DeviceSettingsSchema;
import com.garmin.health.settings.Language;
import com.garmin.health.settings.Settings;
import com.garmin.health.settings.UserSettings;
import com.google.common.util.concurrent.Futures;

import java.util.*;

public class GeneralUserSettingsView extends LinearLayout
{
    private DeviceSettingsSchema mDeviceSettingsSchema;
    private Device mDevice;

    private SettingsSpinner<String> mLanguageView;
    private CustomAutoValueListView<String> mGoalsView;

    private CustomAutoValueView mCustomRunningStride;

    private CustomAutoValueView mCustomWalkingStride;

    public GeneralUserSettingsView(Context context)
    {
        this(context, null);
    }

    public GeneralUserSettingsView(Context context, AttributeSet attrs)
    {
        this(context, attrs, 0);
    }

    public GeneralUserSettingsView(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init()
    {
        inflate(getContext(), R.layout.view_general_user_settings, this);

        mGoalsView = findViewById(R.id.custom_goals_list);
        mGoalsView.initialize(String.class, R.string.goals);
        mLanguageView = findViewById(R.id.config_language);
        mLanguageView.initialize(R.string.config_language, "config_");

        mCustomRunningStride = findViewById(R.id.custom_stride_running);
        mCustomRunningStride.setTitle(R.string.running);

        mCustomWalkingStride = findViewById(R.id.custom_stride_walking);
        mCustomRunningStride.setTitle(R.string.walking);
    }

    public void setExistingSettings(Settings settings, Device device)
    {
        //Some user settings are only applicable by devices
        mDeviceSettingsSchema = settings.deviceSettings().schema();
        mDevice = device;

        setupLanguage(settings);
        setupGoals(settings);
        setupStrideLengths(settings);
    }

    private void setupLanguage(Settings settings)
    {
        Set<String> supportedLanguages = null;

        try
        {
            supportedLanguages = Futures.getChecked(mDevice.getLanguagesSupportedByDevice(), Exception.class);
        }
        catch (Exception e)
        {
            Toast.makeText(getContext(), "Error Receiving Languages", Toast.LENGTH_SHORT).show();
        }

        if (supportedLanguages != null && !supportedLanguages.isEmpty())
        {
            String selectedLanguage = settings.userSettings().deviceLanguage();
            if(selectedLanguage == null)
            {
                selectedLanguage = Language.ENGLISH;
            }

            List<String> languages = new ArrayList<>(supportedLanguages);
            mLanguageView.setOptions(languages, selectedLanguage);
        }
        else
        {
            mLanguageView.setVisibility(View.GONE);
        }
    }

    private void setupGoals(Settings settings) {
        if (mDeviceSettingsSchema.supportsGoals()) {
            mGoalsView.setOptions(SettingsUtil.generateGoalsMap(settings));
        }
        else {
            mGoalsView.setVisibility(GONE);
        }
    }

    private void setupStrideLengths(Settings settings) {

        mCustomRunningStride.setValue(settings.userSettings().runningStepLength());
        mCustomWalkingStride.setValue(settings.userSettings().walkingStepLength());
    }

    public void populateSettingsBuilder(UserSettings.Builder builder) {
        if (SettingsUtil.isVisible(mCustomRunningStride)) {
            builder.setRunningStepLength(mCustomRunningStride.getValue());
        }

        if (SettingsUtil.isVisible(mCustomWalkingStride)) {
            builder.setWalkingStepLength(mCustomWalkingStride.getValue());
        }

        if (SettingsUtil.isVisible(mGoalsView)) {
            builder.setGoals(SettingsUtil.parseGoalsMap(mGoalsView.getOptions()));
        }

        if (SettingsUtil.isVisible(mLanguageView)) {
            builder.setDeviceLanguage(mLanguageView.getSelectedValue());
        }
    }

    public boolean isValid()
    {
        if(SettingsUtil.isVisible(mCustomRunningStride) && mCustomRunningStride.getValue() < 0)
        {
            return false;
        }

        if(SettingsUtil.isVisible(mCustomWalkingStride) && mCustomWalkingStride.getValue() < 0)
        {
            return false;
        }

        if(SettingsUtil.isVisible(mGoalsView))
        {
            Map<String, Float> options = mGoalsView.getOptions();

            for(String type : options.keySet())
            {
                if(options.get(type) < 0)
                {
                    return false;
                }
            }
        }

        return true;
    }
}
