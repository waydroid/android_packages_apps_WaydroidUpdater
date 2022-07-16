/*
 * Copyright (C) 2022 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.waydroidupdater;

import android.os.Build;
import android.os.Build.Partition;
import android.os.SystemProperties;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class UpdaterTools {
    private static final String SYSTEM_OTA_PROP = "waydroid.system_ota";
    private static final String VENDOR_OTA_PROP = "waydroid.vendor_ota";
    private static final String WAYDROID_DISABLE_UPDATER_PROP = "waydroid.updater.disabled";
    private static final String BUILD_VERSION_PROP = "ro.lineage.build.version";

    public static final String INTENT_EXTRA_SYSTEM_UPDATEINFO = "SYSTEM_UPDATEINFO";
    public static final String INTENT_EXTRA_VENDOR_UPDATEINFO = "VENDOR_UPDATEINFO";

    public static Updates fetchUpdates() {
        Updates updates = new Updates();

        if (SystemProperties.getBoolean(WAYDROID_DISABLE_UPDATER_PROP, false))
            return updates;

        try {
            JSONArray ja = readJsonFromUrl(SystemProperties.get(SYSTEM_OTA_PROP))
                    .getJSONArray("response");
            for (int i = 0; i < ja.length(); i++) {
                updates.addSystemUpdate(new UpdateInfo(ja.getJSONObject(i)));
            }
        } catch (Exception ignored) {
            ignored.printStackTrace();
        };

        try {
            JSONArray ja = readJsonFromUrl(SystemProperties.get(VENDOR_OTA_PROP))
                    .getJSONArray("response");
            for (int i = 0; i < ja.length(); i++) {
                updates.addVendorUpdate(new UpdateInfo(ja.getJSONObject(i)));
            }
        } catch (Exception ignored) {
            ignored.printStackTrace();
        };

        return updates;
    }

    public static JSONObject readJsonFromUrl(String url) throws IOException, MalformedURLException, JSONException {
        InputStream is = new URL(url).openStream();
        try {
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
            StringBuilder text = new StringBuilder();
            for (String line; (line = rd.readLine()) != null; ) {
                text.append(line).append('\n');
            }
            return new JSONObject(text.toString());
        } catch (Throwable t) {
            throw t;
        } finally {
            is.close();
        }
    }

    public static long getPartitionTimestamp(String name) {
        List<Partition> partitions = Build.getFingerprintedPartitions();
        Partition part = partitions.stream().filter(p -> p.getName() == name).findFirst().orElse(null);
        if (part != null) {
            return part.getBuildTimeMillis();
        }
        return 0;
    }

    public static String getBuildVersion() {
        return SystemProperties.get(BUILD_VERSION_PROP, "0");
    }
}
