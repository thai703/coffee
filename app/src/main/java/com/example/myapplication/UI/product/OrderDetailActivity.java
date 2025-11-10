package com.example.myapplication.UI.product; // KEEP (đúng package của bạn)

import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.Item.CartItem;                   // KEEP
import com.example.myapplication.R;                              // KEEP
import com.example.myapplication.model.Bill;                     // KEEP
import com.google.firebase.auth.FirebaseAuth;                    // KEEP
import com.google.firebase.auth.FirebaseUser;                    // KEEP
import com.google.firebase.database.DataSnapshot;                // NEW
import com.google.firebase.database.DatabaseError;               // NEW
import com.google.firebase.database.DatabaseReference;           // CHANGED
import com.google.firebase.database.FirebaseDatabase;            // CHANGED
import com.google.firebase.database.ValueEventListener;          // NEW

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class OrderDetailActivity extends AppCompatActivity {

    private static final String TAG = "OrderDetailActivity";

    // KEEP: view cũ
    private TextView detailUserName, detailTimestamp, detailTotal, detailPaymentMethod;
    private ListView listViewItems;

    // NEW: view mới hiển thị trạng thái & mã nhận
    private TextView tvOrderStatus;   // NEW
    private TextView tvPickupCode;    // NEW
    private TextView tvBillId;        // NEW (đang có trong XML: detail_bill_id)

    // CHANGED: trỏ thẳng tới /orders/{orderId}
    private DatabaseReference orderRef; // CHANGED

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);

        // KEEP: ánh xạ view cũ
        detailUserName = findViewById(R.id.detail_user_name);
        detailTimestamp = findViewById(R.id.detail_timestamp);
        detailTotal = findViewById(R.id.detail_total);
        detailPaymentMethod = findViewById(R.id.detail_payment_method);
        listViewItems = findViewById(R.id.list_view_items);

        // NEW: ánh xạ view mới
        tvOrderStatus = findViewById(R.id.text_order_status); // NEW (bạn sẽ thêm trong XML)
        tvPickupCode  = findViewById(R.id.text_pickup_code);  // NEW (bạn sẽ thêm trong XML)
        tvBillId      = findViewById(R.id.detail_bill_id);    // NEW (đã có trong XML)

        // CHANGED: nhận id đơn — ưu tiên "orderId", fallback "billId"
        String orderId = getIntent().getStringExtra("orderId");     // CHANGED
        if (orderId == null) orderId = getIntent().getStringExtra("billId"); // CHANGED

        final FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        final String userId = (currentUser != null) ? currentUser.getUid() : null; // KEEP (dùng cho email/hiển thị)

        if (orderId == null) {
            Toast.makeText(this, "Không tìm thấy mã đơn hàng.", Toast.LENGTH_LONG).show();
            Log.e(TAG, "orderId is null");
            finish();
            return;
        }

        if (tvBillId != null) tvBillId.setText("Mã hóa đơn: " + orderId); // NEW: hiển thị id

        // CHANGED: lắng nghe realtime ở /orders/{orderId} thay vì /OrderHistory/{uid}/{billId}
        orderRef = FirebaseDatabase.getInstance().getReference("orders").child(orderId); // CHANGED
        orderRef.addValueEventListener(new ValueEventListener() { // CHANGED: realtime listener
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Bill bill = snapshot.getValue(Bill.class);
                Log.d(TAG, "Snapshot exists: " + snapshot.exists());
                if (bill == null) {
                    Toast.makeText(OrderDetailActivity.this, "Không tải được chi tiết đơn hàng.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // KEEP/CHANGED: set thông tin chung
                detailUserName.setText("Khách hàng: " + (bill.getUserName() != null
                        ? bill.getUserName()
                        : currentUser != null ? currentUser.getEmail() : "N/A"));

                if (bill.getTimestamp() != null) {
                    detailTimestamp.setText("Thời gian: " + bill.getTimestamp());
                } else if (bill.getCreatedAt() > 0) { // NEW: fallback createdAt
                    detailTimestamp.setText("Thời gian: " +
                            new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm")
                                    .format(new java.util.Date(bill.getCreatedAt())));
                } else {
                    detailTimestamp.setText("Thời gian: -");
                }

                DecimalFormat df = new DecimalFormat("#,###");
                detailTotal.setText("Tổng tiền: " + df.format((long) bill.getTotal()) + " VNĐ");

                // KEEP (đọc từ Intent như trước)
                String paymentMethod = getIntent().getStringExtra("paymentMethod");
                detailPaymentMethod.setText("Phương thức: " + (paymentMethod != null ? paymentMethod : "Không rõ"));

                // NEW: hiển thị trạng thái + pickupCode
                if (tvOrderStatus != null) tvOrderStatus.setText(mapStatus(bill.getStatus()));
                if (tvPickupCode != null)  tvPickupCode.setText(bill.getPickupCode() != null ? bill.getPickupCode() : "------");

                // KEEP: hiển thị danh sách món
                List<String> itemStrings = new ArrayList<>();
                if (bill.getItems() != null) {
                    for (CartItem item : bill.getItems()) {
                        if (item != null && item.getProduct() != null) {
                            itemStrings.add(item.getProduct().getName()
                                    + " x" + item.getQuantity()
                                    + " - " + String.format("%,.0f", item.getProduct().getPrice()) + " VNĐ");
                        }
                    }
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<>(OrderDetailActivity.this,
                        android.R.layout.simple_list_item_1, itemStrings);
                listViewItems.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(OrderDetailActivity.this, "Lỗi: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Firebase error: " + error.getMessage());
            }
        });
    }

    // NEW: map trạng thái → text VN
    private String mapStatus(String s) {
        if (s == null) return "Trạng thái: -";
        switch (s) {
            case "pending":   return "Trạng thái: Đang chờ";
            case "accepted":  return "Trạng thái: Đã nhận";
            case "making":    return "Trạng thái: Đang pha";
            case "ready":     return "Trạng thái: Đã pha xong";
            case "completed": return "Trạng thái: Đã giao";
            case "canceled":  return "Trạng thái: Đã hủy";
            default:          return "Trạng thái: " + s;
        }
    }
}
