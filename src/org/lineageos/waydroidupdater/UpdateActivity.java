/*
 * Copyright (C) 2022 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.waydroidupdater;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.lineageos.waydroidupdater.download.UpdateDownloadController;
import org.lineageos.waydroidupdater.download.DownloadClient;
import lineageos.waydroid.Hardware;

import static android.os.Build.Partition.PARTITION_NAME_SYSTEM;

public class UpdateActivity extends Activity {

    private RotateAnimation mRefreshAnimation;
    private View mRefreshIconView;
    private Hardware mWaydroidHardware;
    private UpdateInfo mSystemUpdate;
    private UpdateInfo mVendorUpdate;
    private UpdateDownloadController mSystemDownloader;
    private UpdateDownloadController mVendorDownloader;
    private View mSystemCard;
    private View mVendorCard;
    private boolean mCanUpdateSystem;
    private boolean mCanUpdateVendor;
    private Button mDownloadBtn;
    private Button mUpdateBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mWaydroidHardware = Hardware.getInstance(this);

        mRefreshAnimation = new RotateAnimation(0, 360, Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        mRefreshAnimation.setInterpolator(new LinearInterpolator());
        mRefreshAnimation.setDuration(1000);

        mDownloadBtn = findViewById(R.id.download_btn);
        mDownloadBtn.setOnClickListener(v -> {
            if (mCanUpdateSystem)
                mSystemDownloader.download();
            if (mCanUpdateVendor)
                mVendorDownloader.download();
        });

        mUpdateBtn = findViewById(R.id.update_btn);
        mUpdateBtn.setOnClickListener(v -> {
            mWaydroidHardware.upgrade(
                    mCanUpdateSystem ? mSystemDownloader.getHostPath() : "",
                    mCanUpdateSystem ? (mSystemUpdate.getTimestamp()/1000) : 0,
                    mCanUpdateVendor ? mVendorDownloader.getHostPath() : "",
                    mCanUpdateVendor ? (mVendorUpdate.getTimestamp()/1000) : 0);
        });

        onUpdates(getIntent().getParcelableExtra(UpdaterTools.INTENT_EXTRA_SYSTEM_UPDATEINFO),
                getIntent().getParcelableExtra(UpdaterTools.INTENT_EXTRA_VENDOR_UPDATEINFO));

        if (mSystemUpdate == null && mVendorUpdate == null) {
            refreshUpdates();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.toolbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
      switch (item.getItemId()) {
      case R.id.menu_refresh:
        refreshUpdates();
        return true;
      }

      return super.onOptionsItemSelected(item);
    }

    private void onUpdates(UpdateInfo s, UpdateInfo v) {
        if (mSystemDownloader != null)
            mSystemDownloader.abort();
        if (mVendorDownloader != null)
            mVendorDownloader.abort();

        mSystemUpdate = s;
        mVendorUpdate = v;
        mCanUpdateSystem = mSystemUpdate != null && mSystemUpdate.compareTo(UpdaterTools.getBuildVersion(),
                UpdaterTools.getPartitionTimestamp(PARTITION_NAME_SYSTEM)) > 0;
        mCanUpdateVendor = mVendorUpdate != null && mVendorUpdate.compareTo(UpdaterTools.getBuildVersion(),
                UpdaterTools.getPartitionTimestamp("vendor")) > 0;

        drawStatus();

        if (mCanUpdateSystem) {
            ProgressBarDownloadCallbacks cb = new ProgressBarDownloadCallbacks(mSystemCard);
            mSystemDownloader = new UpdateDownloadController(mSystemUpdate, cb, cb);
        }
        if (mCanUpdateVendor) {
            ProgressBarDownloadCallbacks cb = new ProgressBarDownloadCallbacks(mVendorCard);
            mVendorDownloader = new UpdateDownloadController(mVendorUpdate, cb, cb);
        }

        mUpdateBtn.setVisibility(View.GONE);
        if (mCanUpdateSystem || mCanUpdateVendor) {
            mDownloadBtn.setVisibility(View.VISIBLE);
        }
    }

    private void onDownloadsFinished() {
        mDownloadBtn.setVisibility(View.GONE);
        mUpdateBtn.setVisibility(View.VISIBLE);
    }

    private class ProgressBarDownloadCallbacks implements DownloadClient.DownloadCallback, DownloadClient.ProgressListener {
        private ProgressBar pb;

        public ProgressBarDownloadCallbacks(View root) {
            this.pb = root.findViewById(R.id.progress_bar);
        }

        @Override
        public void onFailure(boolean cancelled) {
            runOnUiThread(() -> {
                pb.setProgress(0, true);
                pb.setVisibility(View.GONE);
                if (!cancelled) {
                    Toast.makeText(UpdateActivity.this, getString(R.string.download_failed_text), Toast.LENGTH_SHORT);
                }
            });
        }
        @Override
        public void onResponse(DownloadClient.Headers headers) {
            runOnUiThread(() -> {
                pb.setProgress(0, true);
            });
        }
        @Override
        public void onSuccess() {
            runOnUiThread(() -> {
                pb.setProgress(100, true);
                //pb.setVisibility(View.GONE);

                if ((!mCanUpdateSystem || mSystemDownloader.isCompleted()) &&
                    (!mCanUpdateVendor || mVendorDownloader.isCompleted())) {
                    onDownloadsFinished();
                }
            });
        }
        @Override
        public void update(long bytesRead, long contentLength, long speed, long eta) {
            runOnUiThread(() -> {
                pb.setVisibility(View.VISIBLE);
                pb.setProgress((int)(100 * bytesRead/contentLength), true);
            });
        }
    }

    private void refreshUpdates() {
        mDownloadBtn.setVisibility(View.GONE);
        mUpdateBtn.setVisibility(View.GONE);
        refreshAnimationStart();
        new Thread(() -> {
            Updates updates = UpdaterTools.fetchUpdates();
            UpdateInfo s = updates.getLatestSystemUpdate();
            UpdateInfo v = updates.getLatestVendorUpdate();
            runOnUiThread(() -> onUpdates(s, v));
        }).start();
    }

    private void drawStatus() {
        LinearLayout root = findViewById(R.id.cards_root);
        root.removeAllViews();
        mSystemCard = drawImageStatus("System", mSystemUpdate, UpdaterTools.getPartitionTimestamp(PARTITION_NAME_SYSTEM));
        mVendorCard = drawImageStatus("Vendor", mVendorUpdate, UpdaterTools.getPartitionTimestamp("vendor"));
        refreshAnimationStop();
    }

    private View drawImageStatus(String imageName, UpdateInfo update, long buildTimestamp) {
        LayoutInflater factory = LayoutInflater.from(this);
        LinearLayout root = findViewById(R.id.cards_root);

        View card = factory.inflate(R.layout.card, root, false);
        TextView imageText = card.findViewById(R.id.image_text);
        imageText.setText(getString(R.string.card_image_text, imageName));
        root.addView(card);

        LinearLayout cardLayout = card.findViewById(R.id.card_layout);
        if (update == null) {
            View upToDate = factory.inflate(R.layout.up_to_date, cardLayout, false);
            TextView tv = upToDate.findViewById(R.id.up_to_date_text);
            tv.setText(getString(R.string.card_image_status_unknown));
            cardLayout.addView(upToDate);
        } else {
            String yourVersion = UpdaterTools.getBuildVersion();

            if (update.compareTo(yourVersion, buildTimestamp) <= 0) {
                View upToDate = factory.inflate(R.layout.up_to_date, cardLayout, false);
                TextView tv = upToDate.findViewById(R.id.up_to_date_text);
                tv.setText(getString(R.string.card_image_status_up_to_date));
                cardLayout.addView(upToDate);
            } else {
                View outOfDate = factory.inflate(R.layout.out_of_date, cardLayout, false);
                TextView yours = outOfDate.findViewById(R.id.yours_text);
                TextView latest = outOfDate.findViewById(R.id.latest_text);
                LocalDateTime yourDate = LocalDateTime.ofInstant(Instant.ofEpochMilli(buildTimestamp), ZoneId.systemDefault());
                LocalDateTime latestDate = LocalDateTime.ofInstant(Instant.ofEpochMilli(update.getTimestamp()), ZoneId.systemDefault());
                DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd MMM uuuu");
                yours.setText(getString(R.string.card_image_status_yours, yourVersion, yourDate.format(dtf)));
                latest.setText(getString(R.string.card_image_status_latest, update.getVersion(), latestDate.format(dtf)));
                cardLayout.addView(outOfDate);
            }
        }

        return card;
    }

    private void refreshAnimationStart() {
        if (mRefreshIconView == null) {
            mRefreshIconView = findViewById(R.id.menu_refresh);
        }
        if (mRefreshIconView != null) {
            mRefreshAnimation.setRepeatCount(Animation.INFINITE);
            mRefreshIconView.startAnimation(mRefreshAnimation);
            mRefreshIconView.setEnabled(false);
        }
    }

    private void refreshAnimationStop() {
        if (mRefreshIconView != null) {
            mRefreshAnimation.setRepeatCount(0);
            mRefreshIconView.setEnabled(true);
        }
    }
}
