package com.example.myapplication.push;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.example.myapplication.R;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String CH_ID = "orders_updates";

    @Override public void onMessageReceived(RemoteMessage msg) {
        String title = msg.getNotification() != null ? msg.getNotification().getTitle() : "Cập nhật đơn hàng";
        String body  = msg.getNotification() != null ? msg.getNotification().getBody()  : "Đơn hàng đã cập nhật";

        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel ch = new NotificationChannel(CH_ID, "Order Updates", NotificationManager.IMPORTANCE_DEFAULT);
            nm.createNotificationChannel(ch);
        }
        NotificationCompat.Builder b = new NotificationCompat.Builder(this, CH_ID)
                .setSmallIcon(R.drawable.ic_notification) // dùng icon sẵn có của bạn
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true);
        nm.notify((int) (System.currentTimeMillis() % 100000), b.build());
    }
}
