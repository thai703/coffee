package com.example.myapplication.UI.Cart;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.adapter.CartAdapter;
import com.example.myapplication.manager.CartManager;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class CartActivity extends AppCompatActivity {

    private RecyclerView recyclerViewCart;
    private CartAdapter cartAdapter;
    private TextView tvTotalPrice, tvEmptyCartMessage;
    private Button btnConfirmPurchase;
    private CartManager cartManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ✅ Dùng layout GIỎ HÀNG (trước đây bạn set nhầm activity_qr)
        setContentView(R.layout.activity_cart);

        cartManager = CartManager.getInstance();

        // Ánh xạ view
        ImageButton btnBack = findViewById(R.id.btnBack);
        recyclerViewCart = findViewById(R.id.recyclerCartItems);
        tvTotalPrice = findViewById(R.id.tvTotalPrice);
        tvEmptyCartMessage = findViewById(R.id.tvEmptyCartMessage);
        btnConfirmPurchase = findViewById(R.id.btnConfirmPurchase);

        recyclerViewCart.setLayoutManager(new LinearLayoutManager(this));
        cartAdapter = new CartAdapter(this, cartManager.getCartItems());
        recyclerViewCart.setAdapter(cartAdapter);

        // Lắng nghe thay đổi giỏ hàng
        cartAdapter.setOnCartItemChangeListener(new CartAdapter.OnCartItemChangeListener() {
            @Override
            public void onCartItemQuantityChanged() {
                updateTotalPrice();
            }

            @Override
            public void onCartItemRemoved() {
                updateTotalPrice();
                checkEmptyCartState();
            }
        });

        btnBack.setOnClickListener(v -> finish());
        updateTotalPrice();
        checkEmptyCartState();

        // ✅ Giữ 2 phương thức: Tiền mặt & QR thanh toán
        btnConfirmPurchase.setOnClickListener(v -> {
            if (cartManager.getCartItems().isEmpty()) {
                Toast.makeText(CartActivity.this, "Giỏ hàng của bạn đang trống!", Toast.LENGTH_SHORT).show();
                return;
            }

            new AlertDialog.Builder(CartActivity.this)
                    .setTitle("Chọn phương thức thanh toán")
                    .setItems(new String[]{"Tiền mặt", "Mã QR"}, (dialog, which) -> {
                        String timestamp = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
                                .format(new Date());

                        // Chuẩn hoá paymentMethod để QRActivity phân nhánh
                        String paymentMethod = (which == 0) ? "cash" : "qr";

                        Intent intent = new Intent(CartActivity.this, QRActivity.class);
                        intent.putExtra(
                                "userName",
                                com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser() != null
                                        ? com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser().getEmail()
                                        : "Khách hàng ẩn danh"
                        );
                        intent.putExtra("timestamp", timestamp);
                        intent.putExtra("total", cartManager.getTotalPrice());
                        intent.putExtra("cartItems", new ArrayList<>(cartManager.getCartItems()));
                        intent.putExtra("paymentMethod", paymentMethod);
                        startActivity(intent);
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
        });
    }

    private void updateTotalPrice() {
        DecimalFormat formatter = new DecimalFormat("#,###");
        double total = cartManager.getTotalPrice();
        String result = "Tổng tiền: " + formatter.format(total) + " VNĐ";
        tvTotalPrice.setText(result);
    }

    private void checkEmptyCartState() {
        boolean isEmpty = cartManager.getCartItems().isEmpty();
        tvEmptyCartMessage.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        recyclerViewCart.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        btnConfirmPurchase.setEnabled(!isEmpty);
    }

    @Override
    protected void onResume() {
        super.onResume();
        cartAdapter.updateCartItems(cartManager.getCartItems());
        updateTotalPrice();
        checkEmptyCartState();
    }
}
    