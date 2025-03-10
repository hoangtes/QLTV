package com.example.btl_qltv;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Arrays;
import java.util.List;

public class ThemTKActivity extends AppCompatActivity {

    private EditText txtHoTen, txtTaiKhoan, txtMatKhau;
    private Spinner spnChucVu;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.them_tk);

        // Ánh xạ view
        txtHoTen = findViewById(R.id.txt_hoten);
        txtTaiKhoan = findViewById(R.id.txt_taikhoan);
        txtMatKhau = findViewById(R.id.txt_matkhau);
        spnChucVu = findViewById(R.id.spn_chucvu);
        ImageButton imgThem = findViewById(R.id.img_them);
        ImageButton imgBack = findViewById(R.id.img_back);

        dbHelper = new DatabaseHelper(this);

        // Gán dữ liệu vào Spinner
        List<String> chucVuList = Arrays.asList("Thủ thư", "Độc giả");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, chucVuList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnChucVu.setAdapter(adapter);

        // Xử lý sự kiện nhấn nút thêm
        imgThem.setOnClickListener(v -> themTaiKhoan());

        // Xử lý sự kiện nhấn nút quay lại
        imgBack.setOnClickListener(v -> finish());
    }

    // Hàm thêm tài khoản vào SQLite
    private void themTaiKhoan() {
        String hoten = txtHoTen.getText().toString().trim();
        String taikhoan = txtTaiKhoan.getText().toString().trim();
        String matkhau = txtMatKhau.getText().toString().trim();
        String chucvu = spnChucVu.getSelectedItem().toString();

        // Kiểm tra dữ liệu nhập vào
        if (hoten.isEmpty() || taikhoan.isEmpty() || matkhau.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Kiểm tra họ tên: <= 22 ký tự, không chứa số & ký tự đặc biệt
        if (hoten.length() > 22 || !hoten.matches("[a-zA-ZÀ-ỹ0-9_ ]+")) {
            Toast.makeText(this, "Họ tên không hợp lệ! (Tối đa 22 ký tự, không chứa ký tự đặc biệt)", Toast.LENGTH_SHORT).show();
            return;
        }

        // Kiểm tra tài khoản: 5 - 22 ký tự, chỉ chứa chữ cái, số, dấu gạch dưới (_)
        if (!(taikhoan.length() >= 5 && taikhoan.length() <= 22 && taikhoan.matches("[a-zA-Z0-9_]+"))) {
            Toast.makeText(this, "Tài khoản không hợp lệ! (5 - 22 ký tự, chỉ chứa chữ cái, số, _)", Toast.LENGTH_SHORT).show();
            return;
        }

        // Kiểm tra mk: 5 - 22 ký tự, chỉ chứa chữ cái, số, dấu gạch dưới (_)
        if (!(matkhau.length() >= 5 && matkhau.length() <= 22 && matkhau.matches("[a-zA-Z0-9_]+"))) {
            Toast.makeText(this, "Mật khẩu không hợp lệ! (5 - 22 ký tự, chỉ chứa chữ cái, số, _)", Toast.LENGTH_SHORT).show();
            return;
        }

        // Thêm vào CSDL
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("hoten", hoten);
        values.put("taikhoan", taikhoan);
        values.put("matkhau", matkhau);
        values.put("chucvu", chucvu);

        long result = db.insert("user", null, values);
        db.close();

        if (result == -1) {
            Toast.makeText(this, "Tài khoản đã tồn tại!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Thêm tài khoản thành công!", Toast.LENGTH_SHORT).show();
        }
    }

}
