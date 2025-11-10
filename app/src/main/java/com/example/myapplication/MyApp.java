package com.example.myapplication;

import android.app.Application;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;

public class MyApp extends Application {
    @Override public void onCreate() {
        super.onCreate();
        // Lấy token FCM và lưu dưới /users/{uid}/fcmToken khi đã đăng nhập
        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(token -> {
            if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                FirebaseDatabase.getInstance().getReference("users")
                        .child(uid).child("fcmToken").setValue(token);
            }
        }).addOnFailureListener(e -> Log.e("MyApp", "FCM token error", e));
    }
}
