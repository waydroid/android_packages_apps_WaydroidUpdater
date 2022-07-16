/*
 * Copyright (C) 2022 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.waydroidupdater;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

public class Updates {
    private List<UpdateInfo> systemUpdates = new ArrayList<>();
    private List<UpdateInfo> vendorUpdates = new ArrayList<>();

    public void addSystemUpdate(UpdateInfo update) {
        systemUpdates.add(update);
    }

    public void addVendorUpdate(UpdateInfo update) {
        vendorUpdates.add(update);
    }

    public UpdateInfo getLatestSystemUpdate() {
        try {
            return Collections.max(systemUpdates);
        } catch (NoSuchElementException e) {
            return null;
        }
    }

    public UpdateInfo getLatestVendorUpdate() {
        try {
            return Collections.max(vendorUpdates);
        } catch (NoSuchElementException e) {
            return null;
        }
    }
}
