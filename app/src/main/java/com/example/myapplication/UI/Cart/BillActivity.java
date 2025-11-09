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

public class BillActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bill);

        // Ánh xạ các view từ layout
        TextView txtStoreName = findViewById(R.id.txtStoreName);
        TextView txtStoreAddress = findViewById(R.id.txtStoreAddress);
        TextView txtTime = findViewById(R.id.txtTime);
        TextView txtCustomer = findViewById(R.id.txtCustomer);
        TextView txtEmail = findViewById(R.id.txtEmail);
        TableLayout layoutItems = findViewById(R.id.layoutItems);
        TextView txtTotal = findViewById(R.id.txtTotal);
        TextView txtPaymentMethod = findViewById(R.id.txtPaymentMethod);
        TextView txtBillId = findViewById(R.id.txtBillId);
        TextView txtCashier = findViewById(R.id.txtCashier);

        // Nếu bạn có layout gốc là LinearLayout, có thể dùng để thêm voucher vào nếu cần
        LinearLayout rootLayout = findViewById(R.id.billRootLayout);

        // Lấy dữ liệu từ Intent
        String userName = getIntent().getStringExtra("userName");
        String timestamp = getIntent().getStringExtra("timestamp");
        double total = getIntent().getDoubleExtra("total", 0.0);
        String billId = getIntent().getStringExtra("billId");
        List<CartItem> cartItems = (List<CartItem>) getIntent().getSerializableExtra("cartItems");
        String paymentMethod = getIntent().getStringExtra("paymentMethod");
        String voucherCode = getIntent().getStringExtra("voucher"); // LẤY MÃ VOUCHER

        // Đặt dữ liệu vào các TextView
        txtStoreName.setText("COFFEE");
        txtStoreAddress.setText("42/5 Đường Số 2, Hiệp Bình Phước, TP Thủ Đức, Hồ Chí Minh");
        txtTime.setText("Thời gian: " + timestamp);
        txtCustomer.setText("Khách hàng: " + userName);
        txtEmail.setText("Email: " + (userName.equals("Khách hàng ẩn danh") ? "N/A" : userName));
        txtBillId.setText("Mã hóa đơn: " + billId);
        txtCashier.setText("Thu ngân: Đỗ Xuân Chiến");
        txtPaymentMethod.setText("Phương thức thanh toán: " + (paymentMethod != null ? paymentMethod : "Không rõ"));

        //  HIỂN THỊ MÃ VOUCHER
        if (voucherCode != null && !voucherCode.trim().isEmpty() && rootLayout != null) {
            TextView txtVoucher = new TextView(this);
            txtVoucher.setText("Mã khuyến mãi: " + voucherCode);
            txtVoucher.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
            txtVoucher.setTextSize(16);
            txtVoucher.setPadding(0, 10, 0, 10);
            txtVoucher.setGravity(Gravity.START);
            rootLayout.addView(txtVoucher, 7); // Chèn  sau txtPaymentMethod
        }

        // Hiển thị danh sách sản phẩm
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

        // Hiển thị tổng tiền
        DecimalFormat formatter = new DecimalFormat("#,###");
        txtTotal.setText("Tổng dịch vụ: " + formatter.format(total) + " VNĐ");
    }
}
