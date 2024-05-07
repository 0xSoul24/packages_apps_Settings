/*
 * Copyright (C) 2024 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings.development;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.settings.R;

public class PageAgnosticNotificationService extends Service {

    private static final String NOTIFICATION_CHANNEL_ID =
            "com.android.settings.development.PageAgnosticNotificationService";
    private static final int NOTIFICATION_ID = 1;

    private NotificationManager mNotificationManager;

    @Nullable
    @Override
    public IBinder onBind(@NonNull Intent intent) {
        return null;
    }

    // create a notification channel to post persistent notification
    private void createNotificationChannel() {
        NotificationChannel channel =
                new NotificationChannel(
                        NOTIFICATION_CHANNEL_ID,
                        getString(R.string.page_agnostic_notification_channel_name),
                        NotificationManager.IMPORTANCE_HIGH);
        mNotificationManager = getSystemService(NotificationManager.class);
        if (mNotificationManager != null) {
            mNotificationManager.createNotificationChannel(channel);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }

    private Notification buildNotification() {
        // Get the title and text according to page size
        boolean isIn16kbMode = Enable16kUtils.isUsing16kbPages();
        String title =
                isIn16kbMode
                        ? getString(R.string.page_agnostic_16k_pages_title)
                        : getString(R.string.page_agnostic_4k_pages_title);
        String text =
                isIn16kbMode
                        ? getString(R.string.page_agnostic_16k_pages_text_short)
                        : getString(R.string.page_agnostic_4k_pages_text_short);

        Intent notifyIntent = new Intent(this, PageAgnosticWarningActivity.class);
        // Set the Activity to start in a new, empty task.
        notifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        // Create the PendingIntent.
        PendingIntent notifyPendingIntent =
                PendingIntent.getActivity(
                        this,
                        0,
                        notifyIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Notification.Action action =
                new Notification.Action.Builder(
                                R.drawable.empty_icon,
                                getString(R.string.page_agnostic_notification_action),
                                notifyPendingIntent)
                        .build();

        Notification.Builder builder =
                new Notification.Builder(this, NOTIFICATION_CHANNEL_ID)
                        .setContentTitle(title)
                        .setContentText(text)
                        .setOngoing(true)
                        .setSmallIcon(R.drawable.ic_settings_24dp)
                        .setStyle(new Notification.BigTextStyle().bigText(text))
                        .setContentIntent(notifyPendingIntent)
                        .addAction(action);

        return builder.build();
    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        Notification notification = buildNotification();
        if (mNotificationManager != null) {
            mNotificationManager.notify(NOTIFICATION_ID, notification);
        }

        // When clicked on notification, show dialog with full text
        return Service.START_NOT_STICKY;
    }
}
