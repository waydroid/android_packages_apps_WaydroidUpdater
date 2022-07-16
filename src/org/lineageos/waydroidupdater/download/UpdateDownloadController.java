/*
 * Copyright (C) 2022 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.waydroidupdater.download;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;

import android.os.SystemProperties;

import org.lineageos.waydroidupdater.UpdateInfo;
import org.lineageos.waydroidupdater.UpdaterTools;

public class UpdateDownloadController {
    private static final String DOWNLOAD_DIR = "/data/lineageos_updates";
    private static final String HOST_DOWNLOAD_DIR = SystemProperties.get("waydroid.host_data_path", "/var/lib/waydroid/data") +
            "/lineageos_updates";

    private UpdateInfo update;
    private DownloadClient downloadClient;
    private DownloadClient.DownloadCallback downloadCallback;
    private DownloadClient.ProgressListener progressListener;
    private File destination;
    private boolean completed;

    public UpdateDownloadController(UpdateInfo update,
            DownloadClient.DownloadCallback downloadCallback,
            DownloadClient.ProgressListener progressListener) {
        this.update = update;
        this.downloadCallback = downloadCallback;
        this.progressListener = progressListener;
        this.destination = new File(DOWNLOAD_DIR, update.getFilename());
    }

    public void download() {
        if (downloadClient != null) {
            downloadClient.cancel();
        }

        try {
            downloadClient = new DownloadClient.Builder()
                .setUrl(update.getUrl())
                .setDestination(destination)
                .setDownloadCallback(downloadCallback)
                .setProgressListener(progressListener)
                .setUseDuplicateLinks(false)
                .setExpectedSHA256(update.getId())
                .build();
        } catch (IOException e) {
            downloadCallback.onFailure(false);
            return;
        }

        progressListener.update(0, 1, 0, -1);
        downloadClient.start();
    }

    public void abort() {
        if (downloadClient != null) {
            downloadClient.cancel();
        }
    }

    public String getHostPath() {
        return HOST_DOWNLOAD_DIR + "/" + update.getFilename();
    }

    public boolean isCompleted() {
        return downloadClient != null && downloadClient.isCompleted();
    }
}
