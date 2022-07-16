/*
 * Copyright (C) 2022 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.waydroidupdater;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

public class UpdateNotification {
    private static String CHANNEL_ID = "systemupdates";
    private static int NOTIFICATION_ID = 1;

    public static void createNotificationChannel(Context context) {
        CharSequence name = context.getString(R.string.notification_channel_name);
        String description = context.getString(R.string.notification_channel_description);
        int importance = NotificationManager.IMPORTANCE_HIGH;
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
        channel.setDescription(description);
        NotificationManager notificationManager = NotificationManager.from(context);
        notificationManager.createNotificationChannel(channel);
    }

    public static void show(Context context, UpdateInfo sys, UpdateInfo vnd) {
        NotificationManager notificationManager = NotificationManager.from(context);

        Intent intent = new Intent(context, UpdateActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        if (sys != null) intent.putExtra(UpdaterTools.INTENT_EXTRA_SYSTEM_UPDATEINFO, sys);
        if (vnd != null) intent.putExtra(UpdaterTools.INTENT_EXTRA_VENDOR_UPDATEINFO, vnd);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
                intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Notification.Builder builder = new Notification.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_system_update)
            .setContentTitle(context.getString(R.string.notification_title))
            .setContentText(context.getString(R.string.notification_text))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true);

        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }
}
