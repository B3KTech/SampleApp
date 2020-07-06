/*
 * Copyright (c) 2013 Garmin International. All Rights Reserved.
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
 * Created by johnsongar on 8/16/2016.
 */
package com.atakmap.android.pulsetool.plugin.ui;

import android.content.Context;
import android.util.Log;
import com.garmin.health.Device;
import com.garmin.health.DeviceManager;
import com.garmin.health.firmware.FirmwareDownload;
import com.google.common.util.concurrent.Futures;

import java.io.File;
import java.util.List;
import java.util.Locale;

public class UpdateManager
{
    private static UpdateManager sInstance = new UpdateManager();

    public static UpdateManager getInstance() {
        return sInstance;
    }

    public void checkAllForUpdates(Context context)
    {
        DeviceManager deviceManager = DeviceManager.getDeviceManager();
        for(Device device : deviceManager.getPairedDevices())
        {
            checkForUpdates(device, context);
        }
    }

    /** Check for and download any new updates for device */
    public void checkForUpdates(final Device device, final Context context)
    {
        new Thread(() ->
        {
            try
            {
                final String dirPath = context.getFilesDir().toString() + "/" + device.address().hashCode();
                final File dir = new File(dirPath);
                dir.mkdirs();

                List<FirmwareDownload> downloads = Futures.getChecked(device.checkForFirmware(Locale.getDefault()), Exception.class);

                for(FirmwareDownload download : downloads)
                {
                    device.queueNewFirmware(download);
                }

                for(File file : dir.listFiles())
                {
                    Log.i("Update Files", file.getName());
                }
            }
            catch(Exception e)
            {
                Log.i(UpdateManager.class.getSimpleName(), String.format("Error occurred in firmware download. [ERROR=%s]", e));
            }
        }).start();
    }
}
