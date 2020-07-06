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

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import com.atakmap.android.pulsetool.plugin.R;
import com.garmin.health.settings.Gender;
import com.garmin.health.settings.UserSettings;
import com.google.android.material.textfield.TextInputEditText;

public class RequiredUserSettingsView extends LinearLayout
{
    private RadioGroup mGenderRadioGroup;
    private TextInputEditText mHeightEditText;
    private TextInputEditText mWeightEditText;
    private TextInputEditText mAgeEditText;
    private TextInputEditText mSleepStartEditText;
    private TextInputEditText mSleepEndEditText;

    public RequiredUserSettingsView(Context context)
    {
        this(context, null);
    }

    public RequiredUserSettingsView(Context context, AttributeSet attrs)
    {
        this(context, attrs, 0);
    }

    public RequiredUserSettingsView(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init()
    {
        inflate(getContext(), R.layout.view_required_user_settings, this);

        mGenderRadioGroup = findViewById(R.id.config_gender);
        mHeightEditText = findViewById(R.id.config_height);
        mWeightEditText = findViewById(R.id.config_weight);
        mAgeEditText = findViewById(R.id.config_age);
        mSleepStartEditText = findViewById(R.id.config_sleep_start);
        mSleepEndEditText = findViewById(R.id.config_sleep_end);
    }

    public void setExistingSettings(UserSettings userSettings)
    {
        mAgeEditText.setText(String.valueOf(userSettings.age()));
        mHeightEditText.setText(String.valueOf(userSettings.height()));
        mWeightEditText.setText(String.valueOf(userSettings.weight()));
        int gender = userSettings.gender().equals(Gender.FEMALE) ? R.id.config_gender_female : R.id.config_gender_male;

        mGenderRadioGroup.check(gender);
        mSleepStartEditText.setText(reverseMilitaryTime(userSettings.sleepWindowStart()));
        mSleepEndEditText.setText(reverseMilitaryTime(userSettings.sleepWindowEnd()));
    }

    public void populateSettingsBuilder(UserSettings.Builder builder)
    {
        builder.setGender(getGender());
        builder.setAge(getAge());
        builder.setHeight(getHeightValue());
        builder.setWeight(getWeightValue());
        builder.setSleepStart(getSleepStart());
        builder.setSleepEnd(getSleepEnd());
    }

    public boolean isValid()
    {
        boolean isMissingValues = TextUtils.isEmpty(mAgeEditText.getText()) || getAge() <= 0 ||
                TextUtils.isEmpty(mHeightEditText.getText()) || getHeightValue() <= 0 ||
                TextUtils.isEmpty(mWeightEditText.getText()) || getWeightValue() <= 0 ||
                TextUtils.isEmpty(mSleepStartEditText.getText()) || getSleepStart() <= 0 ||
                TextUtils.isEmpty(mSleepEndEditText.getText()) || getSleepEnd() <= 0;
        return !isMissingValues;
    }

    private String getGender()
    {
        int checkedRadioButtonId = mGenderRadioGroup.getCheckedRadioButtonId();
        return (checkedRadioButtonId == R.id.config_gender_male) ? Gender.MALE : Gender.FEMALE;
    }

    private int getAge()
    {
        return Integer.valueOf(mAgeEditText.getText().toString());
    }

    private float getHeightValue()
    {
        return Float.valueOf(mHeightEditText.getText().toString());
    }

    private float getWeightValue()
    {
        return Float.valueOf(mWeightEditText.getText().toString());
    }

    private int getSleepEnd()
    {
        return parseMilitaryTime(Integer.valueOf(mSleepEndEditText.getText().toString()));
    }

    private int getSleepStart()
    {
        return parseMilitaryTime(Integer.valueOf(mSleepStartEditText.getText().toString()));
    }

    private int parseMilitaryTime(int i)
    {
        return ((i / 100) * 3600) + ((i % 100) * 60);
    }

    @SuppressLint("DefaultLocale")
    private String reverseMilitaryTime(int i)
    {
        return String.format("%04d", ((i / 3600) * 100) + (((i % 3600) / 60)));
    }
}
