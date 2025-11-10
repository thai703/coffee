package com.example.myapplication.UI.profile;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.adapter.OrderAdapter;
import com.example.myapplication.model.Bill; // Thay Order bằng Bill
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class OrderHistoryActivity extends AppCompatActivity {

    private static final String TAG = "OrderHistoryActivity";

    private RecyclerView recyclerView;
    private OrderAdapter orderAdapter;
    private List<Bill> orderList; // Thay Order bằng Bill
    private ProgressBar progressBar;
    private TextView noOrdersTextView;

    private FirebaseAuth mAuth;
    private DatabaseReference dbRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_history);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Lịch sử Đơn hàng");
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        mAuth = FirebaseAuth.getInstance();
        dbRef = FirebaseDatabase.getInstance().getReference();

        recyclerView = findViewById(R.id.recycler_view_order_history);
        progressBar = findViewById(R.id.progress_bar);
        noOrdersTextView = findViewById(R.id.text_view_no_orders);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        orderList = new ArrayList<>();
        orderAdapter = new OrderAdapter(orderList, this); // Truyền context
        recyclerView.setAdapter(orderAdapter);

        loadOrderHistory();
    }

    private void loadOrderHistory() {
    FirebaseUser currentUser = mAuth.getCurrentUser();

    if (currentUser == null) {
        Toast.makeText(this, "Bạn cần đăng nhập để xem lịch sử đơn hàng.", Toast.LENGTH_LONG).show();
        progressBar.setVisibility(View.GONE);
        noOrdersTextView.setVisibility(View.VISIBLE);
        return;
    }

    String userId = currentUser.getUid();
    progressBar.setVisibility(View.VISIBLE);
    noOrdersTextView.setVisibility(View.GONE);

    // CHANGED: đọc lite history tại /users/{uid}/orders
    DatabaseReference ref = FirebaseDatabase.getInstance()
            .getReference("users").child(userId).child("orders"); // CHANGED

    ref.addValueEventListener(new ValueEventListener() { // KEEP: realtime listener
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            orderList.clear();

            for (DataSnapshot s : snapshot.getChildren()) {
                // NEW: lấy orderId từ key
                String orderId = s.getKey();

                // NEW: total có thể là Double hoặc Long => đọc linh hoạt
                Double totalD = s.child("total").getValue(Double.class);
                Long totalL = (totalD == null) ? s.child("total").getValue(Long.class) : null;
                double total = (totalD != null) ? totalD : (totalL != null ? totalL : 0);

                String status = s.child("status").getValue(String.class);
                Long createdAt = s.child("createdAt").getValue(Long.class);

                // NEW: build Bill "nhẹ" chỉ đủ cho list
                Bill b = new Bill();                 // KEEP: dùng model Bill
                b.setBillId(orderId);                // NEW: dùng làm id hiển thị & mở chi tiết
                b.setStatus(status);                 // NEW
                b.setTotal(total);                   // NEW
                if (createdAt != null) {
                    b.setTimestamp(new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm")
                            .format(new java.util.Date(createdAt))); // NEW
                }

                orderList.add(b);
            }

            progressBar.setVisibility(View.GONE);

            // KEEP/CHANGED: cập nhật adapter
            orderAdapter.setOrders(orderList); // nếu hàm bạn tên khác (update/setData) thì dùng đúng tên

            if (orderList.isEmpty()) {
                noOrdersTextView.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            } else {
                noOrdersTextView.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {
            progressBar.setVisibility(View.GONE);
            noOrdersTextView.setVisibility(View.VISIBLE);
            Toast.makeText(OrderHistoryActivity.this, "Không thể tải đơn hàng: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Firebase error: " + error.getMessage());
        }
    });
}

}