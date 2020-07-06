package com.atakmap.android.pulsetool.plugin.ui;

import android.content.Context;
import android.os.Build;

import com.atakmap.android.pulsetool.plugin.R;
import com.garmin.health.AbstractGarminHealth;
import com.garmin.health.GarminHealth;
import com.garmin.health.GarminHealthInitializationException;
import com.google.common.util.concurrent.ListenableFuture;

/**
 * Manages Health SDK initialization
 * Created by morajkar on 2/2/2018.
 */

public class HealthSDKManager {
    /**
     * Initializes the health SDK for syncing
     * License should be acquired by contacting Garmin. Each license has restriction on the type of data that can be accessed.
     * @param context
     * @throws GarminHealthInitializationException
     */
    public static ListenableFuture<Boolean> initializeHealthSDK(Context context) throws GarminHealthInitializationException
    {
        // Using system BLE bonding is very unreliable on phones version lower than LOLLIPOP,
        // so it is safer to turn it off entirely.
        boolean systemBonding = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;

        if(!GarminHealth.isInitialized())
        {
            GarminHealth.setLoggingLevel(AbstractGarminHealth.LoggingLevel.VERBOSE);
        }

        return GarminHealth.initialize(context, systemBonding, context.getString(R.string.standard_license), context.getString(R.string.client_id), context.getString(R.string.client_secret));
    }
}
