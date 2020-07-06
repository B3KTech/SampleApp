package com.atakmap.android.pulsetool.plugin.pairing;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import com.atakmap.android.pulsetool.plugin.R;
import com.atakmap.android.pulsetool.plugin.util.ConfirmationDialog;
import com.atakmap.android.pulsetool.plugin.util.EditTextWithSuffix;
import com.garmin.health.*;
import com.garmin.health.bluetooth.PairingFailedException;
import com.garmin.health.settings.UserSettings;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Locale;
import java.util.concurrent.Future;

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
public class PairingDialogFragment extends DialogFragment
{
    public static final String DEVICE_ARG = "DEVICE_ARG";

    private UnitMeasures mCurrentUnits = UnitMeasures.IMPERIAL;

    private ScannedDevice mDevice;

    private boolean mHasReset = false;

    private AutoCompleteTextView mGenderEditText;

    private EditText mAgeEditText;
    private TextInputLayout mAgeTextInputLayout;

    private EditTextWithSuffix mHeightEditText;
    private TextInputLayout mHeightInputTextLayout;

    private EditTextWithSuffix mWeightEditText;
    private TextInputLayout mWeightInputTextLayout;

    private EditTextWithSuffix mSleepStartEditText;
    private TextInputLayout mSleepStartInputTextLayout;

    private EditTextWithSuffix mSleepEndEditText;
    private TextInputLayout mSleepEndInputTextLayout;

    private Future mPairingFuture;

    private Button mForwardButton;
    private Button mAutoButton;

    private ProgressBar mPairingProgressBar;

    private View mRootView;
    private ConfirmationDialog mResetConfirmationDialog;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState)
    {
        this.setRetainInstance(true);

        if(mRootView != null)
        {
            return mRootView;
        }

        mRootView = inflater.inflate(R.layout.fragment_pairing, container, false);

        mDevice = getArguments().getParcelable(DEVICE_ARG);

        Spinner mUnitMeasureSpinner = mRootView.findViewById(R.id.unit_measure_spinner);

        mGenderEditText = mRootView.findViewById(R.id.gender_edittext);

        ArrayAdapter<String> gender_adapter = new ArrayAdapter<>(getActivity(), android.R.layout.select_dialog_item, getResources().getStringArray(R.array.gender_options));
        mGenderEditText.setThreshold(0);
        mGenderEditText.setAdapter(gender_adapter);

        mAgeEditText = mRootView.findViewById(R.id.age_edittext);
        mAgeTextInputLayout = mRootView.findViewById(R.id.age_til);

        mHeightEditText = mRootView.findViewById(R.id.height_edittext);
        mHeightInputTextLayout = mRootView.findViewById(R.id.height_til);
        mWeightEditText = mRootView.findViewById(R.id.weight_edittext);
        mWeightInputTextLayout = mRootView.findViewById(R.id.weight_til);
        mSleepStartEditText = mRootView.findViewById(R.id.sleep_start_edittext);
        mSleepStartInputTextLayout = mRootView.findViewById(R.id.sleep_start_til);
        mSleepEndEditText = mRootView.findViewById(R.id.sleep_end_edittext);
        mSleepEndInputTextLayout = mRootView.findViewById(R.id.sleep_end_til);

        mUnitMeasureSpinner.setOnItemSelectedListener(unit_measure_listener);

        mWeightEditText.addTextChangedListener(weight_watcher);
        mAgeEditText.addTextChangedListener(age_watcher);
        mHeightEditText.addTextChangedListener(height_watcher);
        mSleepStartEditText.addTextChangedListener(sleep_watcher);
        mSleepEndEditText.addTextChangedListener(sleep_watcher);

        int screenWidth = (int) (Resources.getSystem().getDisplayMetrics().widthPixels * .95);

        mWeightEditText.setSuffixPadding(screenWidth/2 * .7f);
        mHeightEditText.setSuffixPadding(screenWidth/2 * .7f);

        mHeightEditText.invalidate();
        mWeightEditText.invalidate();

        mRootView.findViewById(R.id.back_button).setOnClickListener(backButtonListener);

        mAutoButton = mRootView.findViewById(R.id.auto_button);
        mAutoButton.setOnClickListener(autoButtonListener);

        mForwardButton = mRootView.findViewById(R.id.forward_button);
        mForwardButton.setOnClickListener(forwardButtonListener);

        ImageView mLargeDeviceImageView = mRootView.findViewById(R.id.large_device_image);
        mLargeDeviceImageView.setImageResource(DeviceModel.getDeviceImage(mDevice.deviceModel()));

        mGenderEditText.setOnTouchListener(autoCompleteListener);

        mPairingProgressBar = mRootView.findViewById(R.id.pairing_progress_bar);
        Animator mPairingProgressBarAnimator = ObjectAnimator.ofInt(mPairingProgressBar, "progress", 0, 100);
        mPairingProgressBarAnimator.setDuration(2000);
        mPairingProgressBarAnimator.start();

        return mRootView;
    }

    @Override
    public void onResume()
    {
        super.onResume();

        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();

        if(actionBar != null)
        {
            actionBar.setTitle(R.string.pairing_title);
        }
    }

    @Override
    public void onDestroyView()
    {
        super.onDestroyView();

        if (mPairingFuture != null && !mPairingFuture.isDone())
        {
            mPairingFuture.cancel(true);
        }
    }

    private class PairingObserver implements PairingCallback
    {
        @Override
        public void pairingSucceeded(Device device)
        {
            getActivity().runOnUiThread(() -> {
                Toast.makeText(getContext(), String.format("Pairing %s was successful!",
                        mDevice.friendlyName()),
                        Toast.LENGTH_SHORT).show();

                setPairing(false);
            });

            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();

            fragmentManager.popBackStack();
            fragmentManager.popBackStack();
        }

        @Override
        public void pairingFailed(PairingFailedException e)
        {
            if (mPairingFuture.isCancelled())
            {
                return;
            }

            getActivity().runOnUiThread(() -> {
                Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();

                setPairing(false);
            });
        }

        @Override
        public void authRequested(final AuthCompletion authCompletion)
        {
            if (mPairingFuture.isCancelled()) {
                return;
            }

            getActivity().runOnUiThread(() -> {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle(R.string.enter_passkey);

                final EditText input = new EditText(getContext());

                input.setInputType(InputType.TYPE_CLASS_NUMBER);
                builder.setView(input);

                builder.setPositiveButton(R.string.button_ok, (dialog, which) -> {
                    String text = input.getText().toString();
                    int passkey = Integer.parseInt(text);
                    authCompletion.setPasskey(passkey);
                });

                builder.setNegativeButton(R.string.button_cancel, (dialog, which) -> dialog.cancel());

                builder.show();
            });
        }

        @Override
        public void resetRequested(ResetCompletion completion)
        {
            if (mPairingFuture.isCancelled())
            {
                return;
            }

            getActivity().runOnUiThread(() ->
            {
                if(mHasReset)
                {
                    completion.shouldReset(false);
                }
                else
                {
                    mResetConfirmationDialog =
                            new ConfirmationDialog(getContext(),
                                    getString(R.string.reset_device),
                                    getString(R.string.reset_device_msg),
                                    getString(R.string.button_yes),
                                    getString(R.string.button_no),
                                    new ResetClickListener(completion));

                    mResetConfirmationDialog.show();
                }
            });
        }

        @Override
        public void resetRequestCancelled()
        {
            if(mResetConfirmationDialog != null)
            {
                mResetConfirmationDialog.cancel();
            }
        }
    }

    private class ResetClickListener implements DialogInterface.OnClickListener
    {
        private final ResetCompletion mCompletion;

        public ResetClickListener(ResetCompletion completion)
        {
            this.mCompletion = completion;
        }

        @Override
        public void onClick(DialogInterface dialogInterface, int i)
        {
            if (i == DialogInterface.BUTTON_POSITIVE)
            {
                mCompletion.shouldReset(true);
                mHasReset = true;
            }
            else if (i == DialogInterface.BUTTON_NEGATIVE)
            {
                mCompletion.shouldReset(false);
            }
        }
    }

    private enum UnitMeasures
    {
        METRIC("m", "kgs"), IMPERIAL("in", "lbs");

        public static UnitMeasures match(String s)
        {
            for(UnitMeasures unitMeasure : values())
            {
                if(s.equalsIgnoreCase(unitMeasure.name()))
                {
                    return unitMeasure;
                }
            }

            return null;
        }

        private String height;
        private String weight;

        UnitMeasures(String height, String weight)
        {
            this.height = height;
            this.weight = weight;
        }

        public String getHeight()
        {
            return height;
        }

        public String getWeight()
        {
            return weight;
        }
    }

    private Spinner.OnItemSelectedListener unit_measure_listener =
            new Spinner.OnItemSelectedListener()
            {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l)
                {
                    UnitMeasures unitMeasure =
                            UnitMeasures.match(adapterView.getItemAtPosition(i).toString());

                    switch(unitMeasure)
                    {
                        case METRIC:
                            mCurrentUnits = UnitMeasures.METRIC;
                            setMetric();
                            break;
                        case IMPERIAL:
                            mCurrentUnits = UnitMeasures.IMPERIAL;
                            setImperial();
                            break;
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {}
            };

    private static final float LBS_PER_KG = 2.20462f;
    private static final float IN_PER_M = 39.3701f;

    private void setImperial()
    {
        try
        {
            float heightMeters = Float.parseFloat(mHeightEditText.getText().toString());

            float heightInches = (heightMeters * IN_PER_M);

            mHeightEditText.setText(String.format("%.2f", heightInches));
        }
        catch(NumberFormatException e)
        {
            // Ignore on bad parse;
        }

        mHeightEditText.setSuffix(UnitMeasures.IMPERIAL.getHeight());

        try
        {
            float weightKilograms = Float.parseFloat(mWeightEditText.getText().toString());

            float weightPounds = (weightKilograms * LBS_PER_KG);

            mWeightEditText.setText(String.format("%.2f", weightPounds));
        }
        catch(NumberFormatException e)
        {
            // Ignore on bad parse;
        }

        mWeightEditText.setSuffix(UnitMeasures.IMPERIAL.getWeight());

        mHeightEditText.invalidate();
        mWeightEditText.invalidate();
    }

    private void setMetric()
    {
        try
        {
            float heightInches = Float.parseFloat(mHeightEditText.getText().toString());

            float heightMeters = (heightInches / IN_PER_M);

            mHeightEditText.setText(String.format("%.2f", heightMeters));
        }
        catch(NumberFormatException e)
        {
            // Ignore on bad parse;
        }

        mHeightEditText.setSuffix(UnitMeasures.METRIC.getHeight());

        try
        {
            float weightPounds = Float.parseFloat(mWeightEditText.getText().toString());

            float weightKilograms = (weightPounds / LBS_PER_KG);

            mWeightEditText.setText(String.format("%.2f", weightKilograms));
        }
        catch(NumberFormatException e)
        {
            // Ignore on bad parse;
        }

        mWeightEditText.setSuffix(UnitMeasures.METRIC.getWeight());

        mHeightEditText.invalidate();
        mWeightEditText.invalidate();
    }

    private static final float MIN_HEIGHT_M = 0;
    private static final float MAX_HEIGHT_M = 3;

    private TextWatcher height_watcher = new TextWatcher()
    {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2)
        {
            try
            {
                float height = Float.parseFloat(charSequence.toString());

                float unitMultiplier = (mCurrentUnits == UnitMeasures.IMPERIAL ? IN_PER_M : 1);

                float heightMin = MIN_HEIGHT_M * unitMultiplier;
                float heightMax = MAX_HEIGHT_M * unitMultiplier;
                String heightUnits = mCurrentUnits.getHeight();

                if(height < heightMin || height > heightMax)
                {
                    String errorStringUnformatted =
                            getResources().getString(R.string.height_out_of_bounds);

                    String errorString =
                            String.format(errorStringUnformatted,
                                    heightMin,
                                    heightUnits,
                                    heightMax,
                                    heightUnits);

                    mHeightInputTextLayout.setErrorEnabled(true);
                    mHeightInputTextLayout.setError(errorString);

                    return;
                }
            }
            catch(NumberFormatException e)
            {
                if(charSequence.length() > 0)
                {
                    mHeightInputTextLayout.setErrorEnabled(true);
                    mHeightInputTextLayout.setError(getResources().getString(R.string.height_error));

                    return;
                }
            }

            mHeightInputTextLayout.setError(null);
            mHeightInputTextLayout.setErrorEnabled(false);
        }

        @Override
        public void afterTextChanged(Editable editable) {}
    };

    private static final float MIN_WEIGHT_KG = 0;
    private static final float MAX_WEIGHT_KG = 410;

    private TextWatcher weight_watcher = new TextWatcher()
    {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2)
        {
            try
            {
                float weight = Float.parseFloat(charSequence.toString());

                float unitMultiplier = (mCurrentUnits == UnitMeasures.IMPERIAL ? LBS_PER_KG : 1);

                float weightMin = MIN_WEIGHT_KG * unitMultiplier;
                float weightMax = MAX_WEIGHT_KG * unitMultiplier;
                String weightUnits = mCurrentUnits.getWeight();

                if(weight < weightMin || weight > weightMax)
                {
                    String errorStringUnformatted =
                            getResources().getString(R.string.weight_out_of_bounds);

                    String errorString =
                            String.format(errorStringUnformatted,
                                    weightMin,
                                    weightUnits,
                                    weightMax,
                                    weightUnits);

                    mWeightInputTextLayout.setErrorEnabled(true);
                    mWeightInputTextLayout.setError(errorString);

                    return;
                }
            }
            catch(NumberFormatException e)
            {
                if(charSequence.length() > 0)
                {
                    mWeightInputTextLayout.setErrorEnabled(true);
                    mWeightInputTextLayout.setError(getResources().getString(R.string.weight_error));

                    return;
                }
            }

            mWeightInputTextLayout.setError(null);
            mWeightInputTextLayout.setErrorEnabled(false);
        }

        @Override
        public void afterTextChanged(Editable editable) {}
    };

    private static final int MIN_AGE = 1;
    private static final int MAX_AGE = 150;

    private TextWatcher age_watcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2)
        {
            try
            {
                int age = Integer.parseInt(mAgeEditText.getText().toString());

                if(age < MIN_AGE || age > MAX_AGE)
                {
                    mAgeTextInputLayout.setErrorEnabled(true);
                    mAgeTextInputLayout.setError(String.format(getResources().getString(R.string.age_bound), MIN_AGE, MAX_AGE));
                }
                else
                {
                    mAgeTextInputLayout.setError(null);
                    mAgeTextInputLayout.setErrorEnabled(false);
                }
            }
            catch(NumberFormatException e)
            {
                if(!charSequence.toString().equals(""))
                {
                    mAgeTextInputLayout.setErrorEnabled(true);
                    mAgeTextInputLayout.setError(getResources().getString(R.string.age_error));
                }
            }
        }

        @Override
        public void afterTextChanged(Editable editable) {}
    };

    int MIN_SLEEP = 0;
    int MAX_SLEEP = 2359;

    private TextWatcher sleep_watcher = new TextWatcher()
    {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2)
        {
            try
            {
                int sleepStart = Integer.parseInt(mSleepStartEditText.getText().toString());
                int sleepEnd = Integer.parseInt(mSleepEndEditText.getText().toString());

                if(sleepEnd < MIN_SLEEP || sleepEnd > MAX_SLEEP || sleepEnd % 100 > 59)
                {
                    mSleepEndInputTextLayout.setErrorEnabled(true);
                    mSleepEndInputTextLayout.setError(String.format(getResources().getString(R.string.sleep_bound)));
                }
                else
                {
                    mSleepEndInputTextLayout.setError(null);
                    mSleepEndInputTextLayout.setErrorEnabled(false);
                }

                if(sleepStart < MIN_SLEEP || sleepStart > MAX_SLEEP || sleepStart % 100 > 59)
                {
                    mSleepStartInputTextLayout.setErrorEnabled(true);
                    mSleepStartInputTextLayout.setError(String.format(getResources().getString(R.string.sleep_bound)));
                }
                else
                {
                    mSleepStartInputTextLayout.setError(null);
                    mSleepStartInputTextLayout.setErrorEnabled(false);
                }
            }
            catch(NumberFormatException e)
            {
                if(!charSequence.toString().equals(""))
                {
                    mSleepStartInputTextLayout.setErrorEnabled(true);
                    mSleepStartInputTextLayout.setError(getResources().getString(R.string.sleep_error));

                    mSleepEndInputTextLayout.setErrorEnabled(true);
                    mSleepEndInputTextLayout.setError(getResources().getString(R.string.sleep_error));
                }
            }
        }

        @Override
        public void afterTextChanged(Editable editable) {}
    };

    private View.OnClickListener backButtonListener = new View.OnClickListener()
    {
        @Override
        public void onClick(View view)
        {
            if(mPairingFuture != null && !mPairingFuture.isCancelled())
            {
                mPairingFuture.cancel(true);
            }

            getFragmentManager().popBackStack();
        }
    };

    private static final int DEFAULT_AGE = 25;
    private static final float DEFAULT_HEIGHT_IN = 72f;
    private static final float DEFAULT_WEIGHT_LBS = 192f;
    private static final String DEFAULT_GENDER = "Male";
    private static final String DEFAULT_SLEEP_START = "2200";
    private static final String DEFAULT_SLEEP_END = "0600";

    private View.OnClickListener autoButtonListener = new View.OnClickListener()
    {
        @Override
        public void onClick(View view)
        {
            float heightMultiplier = (mCurrentUnits == UnitMeasures.IMPERIAL) ? 1 : IN_PER_M;
            float weightMultiplier = (mCurrentUnits == UnitMeasures.IMPERIAL) ? 1 : LBS_PER_KG;

            mGenderEditText.setText(DEFAULT_GENDER);
            mHeightEditText.setText(String.format(Locale.ENGLISH, "%.2f", DEFAULT_HEIGHT_IN / heightMultiplier));
            mWeightEditText.setText(String.format(Locale.ENGLISH, "%.2f", DEFAULT_WEIGHT_LBS / weightMultiplier));
            mAgeEditText.setText(String.format(Locale.ENGLISH, "%d", DEFAULT_AGE));
            mSleepStartEditText.setText(DEFAULT_SLEEP_START);
            mSleepEndEditText.setText(DEFAULT_SLEEP_END);

            pair();
        }
    };

    private View.OnClickListener forwardButtonListener = view -> pair();

    private View.OnTouchListener autoCompleteListener = new View.OnTouchListener()
    {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent)
        {
            mGenderEditText.showDropDown();

            return false;
        }
    };

    private void pair()
    {
        UserSettings.Builder userSettingsBuilder = new UserSettings.Builder();

        float heightFactor = (mCurrentUnits == UnitMeasures.IMPERIAL) ? IN_PER_M : 1;
        float weightFactor = (mCurrentUnits == UnitMeasures.IMPERIAL) ? LBS_PER_KG : 1;

        try
        {
            userSettingsBuilder.setGender(mGenderEditText.getText().toString().toUpperCase());
            userSettingsBuilder.setWeight(Float.parseFloat(mWeightEditText.getText().toString()) / weightFactor);
            userSettingsBuilder.setHeight(Float.parseFloat(mHeightEditText.getText().toString()) / heightFactor);
            userSettingsBuilder.setAge(Integer.parseInt(mAgeEditText.getText().toString()));
            userSettingsBuilder.setSleepStart(parseMilitaryTime(Integer.parseInt(mSleepStartEditText.getText().toString())));
            userSettingsBuilder.setSleepEnd(parseMilitaryTime(Integer.parseInt(mSleepEndEditText.getText().toString())));
        }
        catch(Exception e)
        {
            Toast.makeText(getContext(), "Please enter valid values in all fields, then try again.", Toast.LENGTH_SHORT).show();
            return;
        }

        mPairingFuture = DeviceManager.getDeviceManager().pair(mDevice, userSettingsBuilder.build(), new PairingObserver());

        setPairing(true);
    }

    private int parseMilitaryTime(int i)
    {
        return ((i / 100) * 3600) + ((i % 100) * 60);
    }

    private void setPairing(boolean isPairing)
    {
        mForwardButton.setEnabled(!isPairing);
        mAutoButton.setEnabled(!isPairing);
        mWeightEditText.setEnabled(!isPairing);
        mHeightEditText.setEnabled(!isPairing);
        mAgeEditText.setEnabled(!isPairing);
        mGenderEditText.setEnabled(!isPairing);
        mSleepStartEditText.setEnabled(!isPairing);
        mSleepEndEditText.setEnabled(!isPairing);
        mPairingProgressBar.setVisibility((isPairing) ? View.VISIBLE : View.INVISIBLE);
    }
}
