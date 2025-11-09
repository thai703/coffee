package com.example.myapplication.UI.product;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.UI.Cart.CartActivity;
import com.example.myapplication.adapter.ProductAdapter;
import com.example.myapplication.model.Product;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ProductListActivity extends AppCompatActivity {

    private RecyclerView recyclerProducts;
    private ProductAdapter adapter;
    private final List<Product> productList = new ArrayList<>();

    private Button btnViewCart;
    private ImageButton btnBack;
    private ProgressBar progressBar;
    private TextView tvEmpty;

    private String selectedCategory; // nhận từ Intent (Coffee/Tea/...)
    private DatabaseReference productRef;

    // === URL RTDB của bạn: đổi nếu khác ===
    private static final String RTDB_URL =
            "https://coffee-8a6ce-default-rtdb.asia-southeast1.firebasedatabase.app";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_list);

        // View
        recyclerProducts = findViewById(R.id.recyclerProducts);
        btnViewCart = findViewById(R.id.btnViewCart);
        btnBack = findViewById(R.id.btnBack);
        progressBar = findViewById(R.id.progressBar);
        tvEmpty = findViewById(R.id.tvEmpty);

        // Category từ HomeActivity (có thể là English hoặc Vietnamese)
        selectedCategory = getIntent().getStringExtra("category");
        if (selectedCategory == null) selectedCategory = "";
        selectedCategory = selectedCategory.trim();

        // RecyclerView
        recyclerProducts.setLayoutManager(new GridLayoutManager(this, 2));
        adapter = new ProductAdapter(this, productList);
        recyclerProducts.setAdapter(adapter);

        // Firebase reference: ép đúng project + node HangHoa
        productRef = FirebaseDatabase.getInstance(RTDB_URL).getReference("HangHoa");

        // Load
        loadProducts();

        // Go cart
        btnViewCart.setOnClickListener(v -> {
            startActivity(new Intent(ProductListActivity.this, CartActivity.class));
        });
        btnBack.setOnClickListener(v -> finish());
    }

    private void loadProducts() {
        showLoading(true);

        // Chuẩn hoá category người dùng chọn (English/Vietnamese) -> mã CODE (COFFEE/TEA/JUICE/CAKE/…)
        final String wantCode = toCategoryCode(selectedCategory);

        productRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                productList.clear();
                Set<String> catsInDb = new HashSet<>();

                for (DataSnapshot doc : snapshot.getChildren()) {
                    Product p = doc.getValue(Product.class);
                    if (p == null) continue;
                    p.setId(doc.getKey());

                    // lưu lại các category đang có trong DB để debug
                    if (p.getCategory() != null) {
                        catsInDb.add(p.getCategory());
                    }

                    // Nếu không chọn category -> lấy hết
                    if (wantCode.isEmpty()) {
                        productList.add(p);
                        continue;
                    }

                    // Chuẩn hoá category của item trong DB -> CODE và so sánh
                    String itemCode = toCategoryCode(p.getCategory());
                    if (itemCode.equals(wantCode)) {
                        productList.add(p);
                    }
                }

                showLoading(false);

                if (productList.isEmpty()) {
                    tvEmpty.setText("Không có sản phẩm. Các category có trong DB: " + catsInDb);
                    tvEmpty.setVisibility(TextView.VISIBLE);
                    recyclerProducts.setVisibility(RecyclerView.GONE);
                } else {
                    tvEmpty.setVisibility(TextView.GONE);
                    recyclerProducts.setVisibility(RecyclerView.VISIBLE);
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                showLoading(false);
                Toast.makeText(ProductListActivity.this,
                        "Lỗi tải sản phẩm: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();
                Log.e("ProductListActivity", "loadProducts cancelled: " + error.getMessage());
            }
        });
    }

    private void showLoading(boolean loading) {
        progressBar.setVisibility(loading ? ProgressBar.VISIBLE : ProgressBar.GONE);
        recyclerProducts.setVisibility(loading ? RecyclerView.GONE : RecyclerView.VISIBLE);
        if (loading) tvEmpty.setVisibility(TextView.GONE);
    }

    // ====== Chuẩn hoá category EN/VN và biến thể -> CODE thống nhất ======
    private String toCategoryCode(String raw) {
        if (raw == null) return "";
        String s = raw.trim();

        // Coffee
        if (equalsAnyIgnoreCase(s, "Coffee", "Cà phê", "Ca phe")) return "COFFEE";
        // Tea
        if (equalsAnyIgnoreCase(s, "Tea", "Trà", "Tra")) return "TEA";
        // Juice (tuỳ bạn đặt trong DB là "Nước ép", "Sinh tố"…)
        if (equalsAnyIgnoreCase(s, "Juice", "Nước ép", "Nuoc ep", "Sinh tố", "Sinh to")) return "JUICE";
        // Cake
        if (equalsAnyIgnoreCase(s, "Cake", "Bánh", "Banh")) return "CAKE";

        // Các loại khác nếu có
        if (equalsAnyIgnoreCase(s, "BestSeller")) return "BESTSELLER";
        if (equalsAnyIgnoreCase(s, "Other", "Khác", "Khac")) return "OTHER";

        return ""; // không biết loại gì
    }

    private boolean equalsAnyIgnoreCase(String value, String... candidates) {
        if (value == null) return false;
        for (String c : candidates) {
            if (value.equalsIgnoreCase(c)) return true;
        }
        return false;
    }
}
