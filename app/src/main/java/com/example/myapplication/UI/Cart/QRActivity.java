package com.example.myapplication.UI.Cart;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.example.myapplication.Item.CartItem;
import com.example.myapplication.manager.CartManager;
import com.example.myapplication.model.Bill;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class QRActivity extends AppCompatActivity {

    private static final String TAG = "QRActivity";
    private DatabaseReference rootRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr); // layout màn QR (chứa qrImage, tvInstruction, btnConfirmQR)

        // RTDB root
        rootRef = FirebaseDatabase.getInstance().getReference();

        // Views
        ImageView qrImage = findViewById(R.id.qrImage);
        TextView tvInstruction = findViewById(R.id.tvInstruction);
        Button btnConfirmQR = findViewById(R.id.btnConfirmQR);

        // Nhận dữ liệu truyền sang
        String userName = getIntent().getStringExtra("userName");
        String timestamp = getIntent().getStringExtra("timestamp");
        double total = getIntent().getDoubleExtra("total", 0.0);
        @SuppressWarnings("unchecked")
        List<CartItem> cartItems = (List<CartItem>) getIntent().getSerializableExtra("cartItems");
        String paymentMethod = getIntent().getStringExtra("paymentMethod"); // "cash" | "qr"

        Log.d(TAG, "paymentMethod=" + paymentMethod);

        // ===== Nhánh TIỀN MẶT: không hiển thị QR, tạo đơn (hoặc bấm nút để tạo) =====
        if ("cash".equalsIgnoreCase(paymentMethod)) {
            if (qrImage != null) qrImage.setVisibility(ImageView.GONE);
            if (tvInstruction != null) tvInstruction.setText("Thanh toán tiền mặt tại quầy. Nhấn nút để tạo đơn.");
            if (btnConfirmQR != null) {
                btnConfirmQR.setText("Tạo đơn (tiền mặt)");
                btnConfirmQR.setOnClickListener(v ->
                        createOrderAndNavigate(userName, timestamp, total, cartItems, paymentMethod)
                );
            }
            return; // ❗ Dừng tại đây để KHÔNG chạy phần hiển thị QR bên dưới
        }

        // ===== Nhánh MÃ QR (QR thanh toán tĩnh): hiển thị ảnh + bấm nút để tạo đơn =====
        if (qrImage != null) qrImage.setImageResource(R.drawable.qr_code); // ảnh QR tĩnh của bạn
        if (tvInstruction != null) tvInstruction.setText("Quét mã QR để thanh toán qua ứng dụng ngân hàng");
        if (btnConfirmQR != null) {
            btnConfirmQR.setOnClickListener(v ->
                    createOrderAndNavigate(userName, timestamp, total, cartItems, paymentMethod)
            );
        }
    }

    /**
     * Tạo ORDER trong /orders và bản sao nhẹ ở /users/{uid}/orders; sau đó chuyển BillActivity
     */
    private void createOrderAndNavigate(String userName, String timestamp, double total,
                                        List<CartItem> cartItems, String paymentMethod) {
        String uid = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid() : null;

        if (uid == null) {
            Toast.makeText(this, "Bạn cần đăng nhập trước khi đặt.", Toast.LENGTH_LONG).show();
            return;
        }

        DatabaseReference ordersRef = rootRef.child("orders");
        String orderId = ordersRef.push().getKey();
        if (orderId == null) {
            Toast.makeText(this, "Không tạo được mã đơn hàng.", Toast.LENGTH_LONG).show();
            return;
        }

        long now = System.currentTimeMillis();
        String pickupCode = String.format("%06d", new Random().nextInt(1_000_000));

        // (Tuỳ chọn) đọc FCM token để đính kèm vào order
        rootRef.child("users").child(uid).child("fcmToken").get().addOnSuccessListener(tokenSnap -> {
            String deviceToken = tokenSnap.getValue(String.class);

            Bill order = new Bill(userName, timestamp, total, cartItems, paymentMethod, null);
            order.setBillId(orderId);
            order.setUserId(uid);
            order.setStatus("pending");
            Map<String, Long> hist = new HashMap<>();
            hist.put("pendingAt", now);
            order.setStatusHistory(hist);
            order.setPickupCode(pickupCode);
            order.setCreatedAt(now);
            order.setUpdatedAt(now);
            order.setPaymentStatus("unpaid"); // tuỳ flow của bạn
            order.setDeviceToken(deviceToken);

            writeOrderThenGo(orderId, order, uid);
        }).addOnFailureListener(e -> {
            Log.w(TAG, "Không đọc được fcmToken, vẫn tạo đơn.", e);

            Bill order = new Bill(userName, timestamp, total, cartItems, paymentMethod, null);
            order.setBillId(orderId);
            order.setUserId(uid);
            order.setStatus("pending");
            Map<String, Long> hist = new HashMap<>();
            hist.put("pendingAt", now);
            order.setStatusHistory(hist);
            order.setPickupCode(pickupCode);
            order.setCreatedAt(now);
            order.setUpdatedAt(now);
            order.setPaymentStatus("unpaid");

            writeOrderThenGo(orderId, order, uid);
        });
    }

    private void writeOrderThenGo(String orderId, Bill order, String uid) {
        DatabaseReference ordersRef = rootRef.child("orders");

        Log.d(TAG, "Writing order /orders/" + orderId);
        ordersRef.child(orderId).setValue(order).addOnSuccessListener(v -> {
            // Bản sao nhẹ cho lịch sử user
            Map<String, Object> lite = new HashMap<>();
            lite.put("total", order.getTotal());
            lite.put("status", order.getStatus());
            lite.put("createdAt", order.getCreatedAt());
            lite.put("updatedAt", order.getUpdatedAt());
            if (order.getStoreId() != null) lite.put("storeId", order.getStoreId());

            rootRef.child("users").child(uid).child("orders").child(orderId).updateChildren(lite);

            // Xoá giỏ & chuyển màn
            CartManager.getInstance().clearCart();

            Intent intent = new Intent(QRActivity.this, BillActivity.class);
            intent.putExtra("userName", order.getUserName());
            intent.putExtra("timestamp", order.getTimestamp());
            intent.putExtra("total", order.getTotal());
            intent.putExtra("cartItems", new java.util.ArrayList<>(order.getItems()));
            intent.putExtra("billId", orderId);       // giữ key cũ để BillActivity dùng lại
            intent.putExtra("paymentMethod", order.getPaymentMethod());
            intent.putExtra("pickupCode", order.getPickupCode());
            startActivity(intent);

            finish(); // đóng QRActivity
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Ghi /orders thất bại", e);
            Toast.makeText(QRActivity.this, "Không thể tạo đơn: " + e.getMessage(), Toast.LENGTH_LONG).show();
        });
    }
}
