package com.example.myapplication.UI.Cart;

import android.os.Bundle;
import android.view.Gravity;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.example.myapplication.Item.CartItem;

import java.text.DecimalFormat;
import java.util.List;

// NEW: lắng nghe trạng thái đơn theo thời gian thực (khuyến nghị)
import com.google.firebase.database.DataSnapshot;     // NEW
import com.google.firebase.database.DatabaseError;     // NEW
import com.google.firebase.database.DatabaseReference; // NEW
import com.google.firebase.database.FirebaseDatabase;  // NEW
import com.google.firebase.database.ValueEventListener;// NEW

public class BillActivity extends AppCompatActivity {

    // NEW: giữ orderId/pickupCode để hiển thị và lắng nghe
    private String orderId;                             // NEW
    private String pickupCode;                          // NEW

    // NEW: view hiển thị mã nhận & trạng thái
    private TextView tvPickupCode;                      // NEW
    private TextView tvOrderStatus;                     // NEW

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bill);

        // ===================== KEEP: ánh xạ các view từ layout =====================
        TextView txtStoreName = findViewById(R.id.txtStoreName);           // KEEP
        TextView txtStoreAddress = findViewById(R.id.txtStoreAddress);     // KEEP
        TextView txtTime = findViewById(R.id.txtTime);                     // KEEP
        TextView txtCustomer = findViewById(R.id.txtCustomer);             // KEEP
        TextView txtEmail = findViewById(R.id.txtEmail);                   // KEEP
        TableLayout layoutItems = findViewById(R.id.layoutItems);          // KEEP
        TextView txtTotal = findViewById(R.id.txtTotal);                   // KEEP
        TextView txtPaymentMethod = findViewById(R.id.txtPaymentMethod);   // KEEP
        TextView txtBillId = findViewById(R.id.txtBillId);                 // KEEP
        TextView txtCashier = findViewById(R.id.txtCashier);               // KEEP

        LinearLayout rootLayout = findViewById(R.id.billRootLayout);       // KEEP

        // ===================== CHANGED/NEW: lấy dữ liệu từ Intent =====================
        String userName = getIntent().getStringExtra("userName");          // KEEP
        String timestamp = getIntent().getStringExtra("timestamp");        // KEEP
        double total = getIntent().getDoubleExtra("total", 0.0);           // KEEP
        String billId = getIntent().getStringExtra("billId");              // KEEP (giữ để không vỡ UI cũ)
        List<CartItem> cartItems = (List<CartItem>) getIntent().getSerializableExtra("cartItems"); // KEEP
        String paymentMethod = getIntent().getStringExtra("paymentMethod");// KEEP
        String voucherCode = getIntent().getStringExtra("voucher");        // KEEP

        // NEW: nhận thêm orderId & pickupCode (được truyền từ QRActivity mới)
        orderId = getIntent().getStringExtra("orderId");                   // NEW
        pickupCode = getIntent().getStringExtra("pickupCode");             // NEW

        // ===================== KEEP: đặt dữ liệu vào các TextView =====================
        txtStoreName.setText("COFFEE");                                    // KEEP
        txtStoreAddress.setText("42/5 Đường Số 2, Hiệp Bình Phước, TP Thủ Đức, Hồ Chí Minh"); // KEEP
        txtTime.setText("Thời gian: " + timestamp);                        // KEEP
        txtCustomer.setText("Khách hàng: " + userName);                    // KEEP
        txtEmail.setText("Email: " + (userName != null && userName.equals("Khách hàng ẩn danh") ? "N/A" : userName)); // KEEP
        txtBillId.setText("Mã hóa đơn: " + (billId != null ? billId : (orderId != null ? orderId : "N/A"))); // CHANGED: ưu tiên hiển thị billId/orderId
        txtCashier.setText("Thu ngân: Đỗ Xuân Chiến");                     // KEEP
        txtPaymentMethod.setText("Phương thức thanh toán: " + (paymentMethod != null ? paymentMethod : "Không rõ")); // KEEP

        // ===================== NEW: gắn view cho pickupCode & status =====================
        tvPickupCode = findViewById(R.id.text_pickup_code);                // NEW
        tvOrderStatus = findViewById(R.id.text_order_status);              // NEW

        if (tvPickupCode != null) {
            tvPickupCode.setText(pickupCode != null ? pickupCode : "------"); // NEW
        }

        //  HIỂN THỊ MÃ VOUCHER (KEEP)
        if (voucherCode != null && !voucherCode.trim().isEmpty() && rootLayout != null) {
            TextView txtVoucher = new TextView(this);
            txtVoucher.setText("Mã khuyến mãi: " + voucherCode);
            txtVoucher.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
            txtVoucher.setTextSize(16);
            txtVoucher.setPadding(0, 10, 0, 10);
            txtVoucher.setGravity(Gravity.START);
            rootLayout.addView(txtVoucher, 7); // KEEP: chèn sau txtPaymentMethod
        }

        // ===================== KEEP: hiển thị danh sách sản phẩm =====================
        if (cartItems != null) {
            DecimalFormat formatter = new DecimalFormat("#,###");
            for (CartItem item : cartItems) {
                TableRow row = new TableRow(this);

                TextView tvName = new TextView(this);
                tvName.setText(item.getProduct().getName());
                tvName.setGravity(Gravity.CENTER);
                tvName.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f));

                TextView tvQuantity = new TextView(this);
                tvQuantity.setText(String.valueOf(item.getQuantity()));
                tvQuantity.setGravity(Gravity.CENTER);
                tvQuantity.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f));

                TextView tvPrice = new TextView(this);
                tvPrice.setText(formatter.format(item.getProduct().getPrice()) + " VNĐ");
                tvPrice.setGravity(Gravity.CENTER);
                tvPrice.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f));

                TextView tvTotalPrice = new TextView(this);
                tvTotalPrice.setText(formatter.format(item.getTotalPrice()) + " VNĐ");
                tvTotalPrice.setGravity(Gravity.CENTER);
                tvTotalPrice.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f));

                row.addView(tvName);
                row.addView(tvQuantity);
                row.addView(tvPrice);
                row.addView(tvTotalPrice);

                layoutItems.addView(row);
            }
        }

        // ===================== KEEP: hiển thị tổng =====================
        DecimalFormat formatter = new DecimalFormat("#,###");
        txtTotal.setText("Tổng dịch vụ: " + formatter.format(total) + " VNĐ");

        // ===================== NEW (khuyến nghị): lắng nghe trạng thái đơn =====================
        // Sẽ hiển thị: pending/accepted/making/ready/completed/canceled
        if (orderId != null && tvOrderStatus != null) {
            DatabaseReference orderRef = FirebaseDatabase.getInstance()
                    .getReference("orders")
                    .child(orderId);

            orderRef.addValueEventListener(new ValueEventListener() {
                @Override public void onDataChange(DataSnapshot s) {
                    if (!s.exists()) return;
                    String status = s.child("status").getValue(String.class);
                    tvOrderStatus.setText(mapStatus(status));
                }
                @Override public void onCancelled(DatabaseError error) { /* ignore */ }
            });
        }
    }

    // NEW: map status -> tiếng Việt gọn
    private String mapStatus(String st) {
        if (st == null) return "Trạng thái: (chưa có)";
        switch (st) {
            case "pending":   return "Trạng thái: Đang chờ xác nhận";
            case "accepted":  return "Trạng thái: Đã nhận đơn";
            case "making":    return "Trạng thái: Đang pha chế";
            case "ready":     return "Trạng thái: Đã pha xong — mời đến nhận";
            case "completed": return "Trạng thái: Đã giao/hoàn tất";
            case "canceled":  return "Trạng thái: Đã huỷ";
            default:          return "Trạng thái: " + st;
        }
    }
}
