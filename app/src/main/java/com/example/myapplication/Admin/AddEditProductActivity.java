package com.example.myapplication.Admin;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.model.Product;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Arrays;
import java.util.List;

public class AddEditProductActivity extends AppCompatActivity {

    private EditText edtName, edtPrice, edtDesc, edtImageUrl;
    private Spinner spnCategory;
    private Button btnSave;
    private ImageView imgPreview;

    private DatabaseReference productRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_product);

        // Ánh xạ view
        edtName = findViewById(R.id.edtName);
        edtPrice = findViewById(R.id.edtPrice);
        edtDesc = findViewById(R.id.edtDesc);
        edtImageUrl = findViewById(R.id.edtImageUrl);
        spnCategory = findViewById(R.id.spnCategory);
        btnSave = findViewById(R.id.btnSave);
        imgPreview = findViewById(R.id.imgPreview);

        // Gán danh sách danh mục vào Spinner
        List<String> categoryList = Arrays.asList("Cà phê", "Trà", "Sinh tố", "Tea", "Khác");
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                categoryList
        );
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnCategory.setAdapter(categoryAdapter);

        // Khởi tạo Firebase Database
        productRef = FirebaseDatabase.getInstance().getReference("HangHoa");

        // Sự kiện click nút lưu
        btnSave.setOnClickListener(v -> saveProduct());
    }

    private void saveProduct() {
        String name = edtName.getText().toString().trim();
        String priceStr = edtPrice.getText().toString().trim();
        String desc = edtDesc.getText().toString().trim();
        String imageUrl = edtImageUrl.getText().toString().trim();
        String category = spnCategory.getSelectedItem().toString();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(priceStr) || TextUtils.isEmpty(imageUrl)) {
            Toast.makeText(this, "Vui lòng điền đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        int price = Integer.parseInt(priceStr);
        String id = productRef.push().getKey();
        Product product = new Product(id, name, price, imageUrl, category, desc);

        productRef.child(id).setValue(product)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Thêm sản phẩm thành công", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show());

        Glide.with(this).load(imageUrl).into(imgPreview);
    }
}
