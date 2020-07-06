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
import android.widget.LinearLayout;
import com.atakmap.android.pulsetool.plugin.R;
import com.atakmap.android.pulsetool.plugin.ui.settings.widget.SettingsSpinner;
import com.garmin.health.settings.Settings;
import com.garmin.health.settings.UnitSettings;


public class UnitSettingsView extends LinearLayout
{
    private SettingsSpinner<String> mDateFormatView;
    private SettingsSpinner<String> mTimeFormatView;
    private SettingsSpinner<String> mUnitSystemView;

    public UnitSettingsView(Context context)
    {
        this(context, null);
    }

    public UnitSettingsView(Context context, AttributeSet attrs)
    {
        this(context, attrs, 0);
    }

    public UnitSettingsView(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.view_unit_settings, this);

        mTimeFormatView = findViewById(R.id.config_time_format);
        mTimeFormatView.initialize(R.string.time_format, "time_format_");

        mDateFormatView = findViewById(R.id.config_date_format);
        mDateFormatView.initialize(R.string.date_format, "date_format_");

        mUnitSystemView = findViewById(R.id.config_unit_system);
        mUnitSystemView.initialize(R.string.unit_system, "unit_system_");
    }

    public void setExistingSettings(Settings settings)
    {
        UnitSettings unitSettings = settings.unitSettings();

        mTimeFormatView.setOptions(unitSettings.timeFormatOptions(), unitSettings.timeFormat());

        mDateFormatView.setOptions(unitSettings.dateFormatOptions(), unitSettings.dateFormat());

        mUnitSystemView.setOptions(unitSettings.unitSystemsOptions(), unitSettings.unitSystem());
    }

    public void populateSettingsBuilder(UnitSettings.Builder builder)
    {
        builder.setDateFormat(mDateFormatView.getSelectedValue());
        builder.setTimeFormat(mTimeFormatView.getSelectedValue());
        //Applies to the device, user settings with units must always be saved in metric
        builder.setUnitSystem(mUnitSystemView.getSelectedValue());
    }
}
