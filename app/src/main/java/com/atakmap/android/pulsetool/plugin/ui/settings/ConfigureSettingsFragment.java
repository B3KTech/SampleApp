package com.atakmap.android.pulsetool.plugin.ui.settings;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.atakmap.android.pulsetool.plugin.R;
import com.atakmap.android.pulsetool.plugin.ui.BaseFragment;
import com.atakmap.android.pulsetool.plugin.ui.settings.widget.SettingsSpinner.SpinnerListener;
import com.garmin.health.Device;
import com.garmin.health.DeviceManager;
import com.garmin.health.settings.DeviceSettings;
import com.garmin.health.settings.Settings;
import com.garmin.health.settings.UnitSettings;
import com.garmin.health.settings.UserSettings;
import com.google.android.material.snackbar.Snackbar;
import com.google.common.util.concurrent.Futures;

import java.util.concurrent.ExecutionException;

/**
 * Fragment used to configure device Settings.
 *
 * @author ioana.morari on 6/21/16.
 */
public class ConfigureSettingsFragment extends BaseFragment
{
    private static final String TAG = ConfigureSettingsFragment.class.getSimpleName();

    private final static String DEVICE_ADDRESS = "device.address";

    private RequiredUserSettingsView mPrimaryUserSettingsView;
    private GeneralUserSettingsView mGeneralUserSettingsView;

    private UnitSettingsView mUnitSettingsView;

    private ScreenSettingsView mScreenSettingsView;
    private DeviceSettingsView mDeviceSettingsView;

    private SendNotificationView mNotificationsView;

    private String mDeviceMacAddress;

    private Device mDevice;

    public static ConfigureSettingsFragment getInstance(String deviceAddress)
    {
        Bundle bundle = new Bundle();
        bundle.putString(DEVICE_ADDRESS, deviceAddress);

        ConfigureSettingsFragment configureSettingsFragment = new ConfigureSettingsFragment();
        configureSettingsFragment.setArguments(bundle);

        return configureSettingsFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        if (getArguments() != null)
        {
            mDeviceMacAddress = getArguments().getString(DEVICE_ADDRESS);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {

        super.onViewCreated(view, savedInstanceState);

        setTitleInActionBar(R.string.action_bar_config);

        mPrimaryUserSettingsView = view.findViewById(R.id.primay_user_settings);
        mGeneralUserSettingsView = view.findViewById(R.id.general_user_settings);
        mUnitSettingsView = view.findViewById(R.id.unit_settings);
        mScreenSettingsView = view.findViewById(R.id.screen_settings);
        mDeviceSettingsView = view.findViewById(R.id.device_settings);
        mDeviceSettingsView.setFrequencyChangeListener(new FrequencySelectedListener());
        mNotificationsView = view.findViewById(R.id.notification_section);

        DeviceManager deviceManager = DeviceManager.getDeviceManager();

        if (mDeviceMacAddress != null)
        {
            mDevice = deviceManager.getDevice(mDeviceMacAddress);

            setTitleInActionBar(mDevice.friendlyName());

            Settings settings = null;

            try
            {
                settings = Futures.getChecked(mDevice.settings(), Exception.class);

                //Required user settings are needed by all devices
                mPrimaryUserSettingsView.setExistingSettings(settings.userSettings());

                //Other settings vary by what the device supports
                mGeneralUserSettingsView.setExistingSettings(settings, mDevice);
                mUnitSettingsView.setExistingSettings(settings);
                mScreenSettingsView.setExistingSettings(settings, mDevice);
                mDeviceSettingsView.setExistingSettings(settings, mDevice);
            }
            catch (Exception e)
            {
                Toast.makeText(getContext(), "Error occured receiving data from device.", Toast.LENGTH_SHORT).show();
            }

            mNotificationsView.setDevice(mDevice);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.menu_config, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.config_done:
                save();

                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPause()
    {
        // After validation, save the data
        if(isValid())
        {
            saveSettings();
        }

        super.onPause();
    }

    @Override
    protected int getLayoutId()
    {
        return R.layout.fragment_config_settings;
    }

    private void save()
    {
        if(isValid())
        {
            if (saveSettings())
            {
                getActivity().onBackPressed();
            }
        }
        else
        {
            Snackbar.make(getView(), R.string.config_empty_field_error, Snackbar.LENGTH_LONG).show();
        }
    }


    private boolean isValid()
    {
        //Currently only validation is on the required user settings
        //Additional validation will be needed on other settings
        return mPrimaryUserSettingsView.isValid() && mGeneralUserSettingsView.isValid();
    }

    private boolean saveSettings()
    {
        Log.i(TAG, "Saving settings");

        try
        {
            UserSettings userSettings = buildUserSettings();
            UnitSettings unitSettings = buildUnitSettings();
            DeviceSettings deviceSettings = buildDeviceSettings();

            Settings settings = mDevice.settings().get();

            //Combine all the entered settings values
            //Could be done separately
            settings = settings.updateUserSettings(userSettings);
            settings = settings.updateUnitSettings(unitSettings);
            settings = settings.updateDeviceSettings(deviceSettings);
            mDevice.updateSettings(settings);
            return true;
        }
        //Validation error wasn't prevented
        catch (Exception e)
        {
            Log.e(TAG, "Exception: " + e.getMessage());
            Snackbar.make(getView(), e.getMessage(), Snackbar.LENGTH_LONG).show();
            return false;
        }
    }

    private UserSettings buildUserSettings() throws ExecutionException, InterruptedException
    {
        UserSettings.Builder builder = mDevice.settings().get().userSettings().edit();
        mPrimaryUserSettingsView.populateSettingsBuilder(builder);
        mGeneralUserSettingsView.populateSettingsBuilder(builder);

        return builder.build();
    }

    private UnitSettings buildUnitSettings() throws ExecutionException, InterruptedException
    {
        UnitSettings.Builder builder = mDevice.settings().get().unitSettings().edit();
        mUnitSettingsView.populateSettingsBuilder(builder);

        return builder.build();
    }

    private DeviceSettings buildDeviceSettings() throws ExecutionException, InterruptedException
    {
        DeviceSettings.Builder builder = mDevice.settings().get().deviceSettings().edit();
        mDeviceSettingsView.populateSettingsBuilder(builder);
        mScreenSettingsView.populateSettingsBuilder(builder);

        return builder.build();
    }

    private class FrequencySelectedListener implements SpinnerListener<String>
    {
        @Override
        public void onItemSelected(String syncFrequency)
        {
            mDeviceSettingsView.updateSyncFrequency(syncFrequency);
        }
    }
}
