/*
 * Copyright (C) 2022 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.waydroidupdater;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import static android.os.Build.Partition.PARTITION_NAME_SYSTEM;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        UpdateNotification.createNotificationChannel(context);

        new Thread(() -> {
            Updates updates = UpdaterTools.fetchUpdates();
            UpdateInfo systemUpdate = updates.getLatestSystemUpdate();
            UpdateInfo vendorUpdate = updates.getLatestVendorUpdate();

            boolean notify = false;
            if (systemUpdate != null && systemUpdate.compareTo(
                    UpdaterTools.getBuildVersion(),
                    UpdaterTools.getPartitionTimestamp(PARTITION_NAME_SYSTEM)) > 0) {
                notify = true;
            }
            if (vendorUpdate != null && vendorUpdate.compareTo(
                    UpdaterTools.getBuildVersion(),
                    UpdaterTools.getPartitionTimestamp("vendor")) > 0) {
                notify = true;
            }

            if (notify) {
                UpdateNotification.show(context, systemUpdate, vendorUpdate);
            }
        }).start();
    }
}
