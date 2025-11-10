package com.example.myapplication.UI.Cart;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast; 

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.example.myapplication.manager.CartManager;
import com.example.myapplication.model.Bill;
import com.example.myapplication.Item.CartItem;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.auth.FirebaseAuth; 


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random; 

public class QRActivity extends AppCompatActivity {

    // CHANGED: từ "bills" (1 node) -> rootRef (ghi /orders và /users/{uid}/orders)
    private DatabaseReference rootRef;                               // CHANGED

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr);

        // CHANGED: thay vì getReference("bills"), dùng root
        rootRef = FirebaseDatabase.getInstance().getReference();     // CHANGED

        // KEEP: ánh xạ view
        ImageView qrImage = findViewById(R.id.qrImage);
        TextView tvInstruction = findViewById(R.id.tvInstruction);
        Button btnConfirmQR = findViewById(R.id.btnConfirmQR);

        // KEEP: hiển thị ảnh QR minh hoạ + hướng dẫn
        qrImage.setImageResource(R.drawable.qr_code);
        tvInstruction.setText("Quét mã QR để thanh toán qua ứng dụng ngân hàng của bạn");

        // KEEP: lấy dữ liệu từ Intent
        String userName = getIntent().getStringExtra("userName");
        String timestamp = getIntent().getStringExtra("timestamp");
        double total = getIntent().getDoubleExtra("total", 0.0);
        @SuppressWarnings("unchecked")
        List<CartItem> cartItems = (List<CartItem>) getIntent().getSerializableExtra("cartItems");
        String paymentMethod = getIntent().getStringExtra("paymentMethod");

        // NEW: thay vì ghi "bills" trực tiếp, ta gọi hàm tạo ORDER + chuyển màn
        btnConfirmQR.setOnClickListener(v ->
                createOrderAndNavigate(userName, timestamp, total, cartItems, paymentMethod) // NEW
        );
    }

    /**
     * NEW: Tạo đơn hàng dưới /orders/{orderId} + bản sao /users/{uid}/orders/{orderId}
     *  - status = "pending"
     *  - pickupCode = mã 6 số
     *  - createdAt/updatedAt + statusHistory.pendingAt
     *  - optional: deviceToken (để FCM)
     */
    private void createOrderAndNavigate(
            String userName,
            String timestamp,
            double total,
            List<CartItem> cartItems,
            String paymentMethod
    ) {
        // NEW: cần uid để lưu lịch sử theo user
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {       // NEW
            Toast.makeText(this, "Bạn cần đăng nhập trước khi đặt.", Toast.LENGTH_LONG).show();
            return;
        }
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid(); // NEW

        DatabaseReference ordersRef = rootRef.child("orders");           // NEW

        // NEW: tạo orderId + pickupCode
        String orderId = ordersRef.push().getKey();                      // NEW
        if (orderId == null) {
            Toast.makeText(this, "Không tạo được mã đơn. Vui lòng thử lại.", Toast.LENGTH_LONG).show();
            return;
        }
        String pickupCode = String.format("%06d", new Random().nextInt(1_000_000)); // NEW
        long now = System.currentTimeMillis();                            // NEW

        // CHANGED: vẫn dùng Bill cũ, nhưng set thêm field mới (đã thêm ở mục 2)
        Bill order = new Bill(userName, timestamp, total, cartItems, paymentMethod, orderId); // KEEP + CHANGED
        order.setBillId(orderId);                 // KEEP (để không vỡ các chỗ đang dùng billId)
        order.setUserId(uid);                     // NEW  (field mới trong Bill.java)
        order.setPaymentStatus("unpaid");         // NEW  ("paid" nếu đã trả)
        order.setStatus("pending");               // NEW

        Map<String, Long> hist = new HashMap<>(); // NEW
        hist.put("pendingAt", now);               // NEW
        order.setStatusHistory(hist);             // NEW

        order.setPickupCode(pickupCode);          // NEW
        order.setCreatedAt(now);                  // NEW
        order.setUpdatedAt(now);                  // NEW
        order.setPickupMethod("pickup");          // NEW ("dine-in" nếu phục vụ tại bàn)

        // NEW: lấy FCM token (nếu đã lưu) để nhúng vào order -> web có thể gửi push
        rootRef.child("users").child(uid).child("fcmToken").get().addOnSuccessListener(snap -> { // NEW
            String token = snap.getValue(String.class);                 // NEW
            order.setDeviceToken(token);                                // NEW

            // NEW: ghi order + lịch sử và chuyển màn
            writeOrderThenGo(orderId, order, uid, total, now, userName, timestamp, cartItems, paymentMethod, pickupCode);

        }).addOnFailureListener(e -> {
            // NEW: không lấy được token vẫn tạo order bình thường
            writeOrderThenGo(orderId, order, uid, total, now, userName, timestamp, cartItems, paymentMethod, pickupCode);
        });
    }

    // NEW: tách hàm ghi DB + điều hướng để tái sử dụng
    private void writeOrderThenGo(
            String orderId,
            Bill order,
            String uid,
            double total,
            long now,
            String userName,
            String timestamp,
            List<CartItem> cartItems,
            String paymentMethod,
            String pickupCode
    ) {
        DatabaseReference ordersRef = rootRef.child("orders");           // NEW

        // NEW: 1) ghi /orders/{orderId}
        ordersRef.child(orderId).setValue(order).addOnSuccessListener(v -> {

            // NEW: 2) bản sao nhẹ cho lịch sử /users/{uid}/orders/{orderId}
            Map<String, Object> lite = new HashMap<>();
            lite.put("total", total);
            lite.put("status", "pending");
            lite.put("createdAt", now);
            lite.put("updatedAt", now);
            rootRef.child("users").child(uid).child("orders").child(orderId).setValue(lite);

            // CHANGED: 3) chuyển sang BillActivity + truyền thêm pickupCode & orderId
            Intent intent = new Intent(QRActivity.this, BillActivity.class);  // KEEP
            intent.putExtra("userName", userName);                            // KEEP
            intent.putExtra("timestamp", timestamp);                          // KEEP
            intent.putExtra("total", total);                                  // KEEP
            intent.putExtra("cartItems", new ArrayList<>(cartItems));         // KEEP
            intent.putExtra("billId", orderId);       // KEEP key cũ nhưng giá trị là orderId  (CHANGED)
            intent.putExtra("orderId", orderId);      // NEW
            intent.putExtra("pickupCode", pickupCode);// NEW
            intent.putExtra("paymentMethod", paymentMethod);                  // KEEP
            startActivity(intent);                                             // KEEP

            // KEEP: xoá giỏ hàng
            CartManager.getInstance().clearCart();

        }).addOnFailureListener(e -> {
            Toast.makeText(QRActivity.this, "Lỗi tạo đơn: " + e.getMessage(), Toast.LENGTH_LONG).show();
        });
    }
}