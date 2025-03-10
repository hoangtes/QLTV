package com.example.btl_qltv;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
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

public class SuaTKActivity extends AppCompatActivity {

    private EditText txtHoTen, txtTaiKhoan;
    private Spinner spnChucVu;
    private DatabaseHelper dbHelper;
    private String taikhoan; // Lưu tài khoản cũ để tìm trong database

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sua_tk);

        // Ánh xạ view
        txtHoTen = findViewById(R.id.txt_hoten);
        txtTaiKhoan = findViewById(R.id.txt_taikhoan);
        spnChucVu = findViewById(R.id.spn_chucvu);
        ImageButton imgSua = findViewById(R.id.img_sua);
        ImageButton imgBack = findViewById(R.id.img_back);

        dbHelper = new DatabaseHelper(this);

        // Nhận tài khoản từ Intent
        taikhoan = getIntent().getStringExtra("taikhoan");
        if (taikhoan != null) {
            loadThongTinTaiKhoan(taikhoan);
        }

        if (txtTaiKhoan.getText().toString().trim().equals("admin")) {
            spnChucVu.setEnabled(false); // Không cho chọn chức vụ nếu là admin
        } else {
            spnChucVu.setEnabled(true); // Các tài khoản khác có thể chọn
        }

        // Xử lý sự kiện nút sửa
        imgSua.setOnClickListener(v -> suaTaiKhoan());

        // Xử lý sự kiện nút quay lại
        imgBack.setOnClickListener(v -> {
            finish();
        });

    }

    // Hàm load thông tin tài khoản cũ
    private void loadThongTinTaiKhoan(String taikhoan) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT hoten, taikhoan, chucvu FROM user WHERE taikhoan = ?", new String[]{taikhoan});

        if (cursor.moveToFirst()) {
            // Lấy dữ liệu theo đúng tên cột
            String hoTen = cursor.getString(cursor.getColumnIndexOrThrow("hoten"));
            String taiKhoan = cursor.getString(cursor.getColumnIndexOrThrow("taikhoan"));
            String chucVuHienTai = cursor.getString(cursor.getColumnIndexOrThrow("chucvu"));

            // Set dữ liệu vào TextView
            txtHoTen.setText(hoTen);
            txtTaiKhoan.setText(taiKhoan);

            // Danh sách chức vụ
            List<String> chucVuList = Arrays.asList("Thủ thư", "Độc giả");
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, chucVuList);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spnChucVu.setAdapter(adapter);

            // Chọn đúng chức vụ cũ
            int viTri = chucVuList.indexOf(chucVuHienTai);
            if (viTri != -1) {
                spnChucVu.setSelection(viTri);
            }

            // 🔒 Nếu tài khoản là "admin" thì không cho đổi chức vụ
            if (taiKhoan.equals("admin")) {
                spnChucVu.setEnabled(false);
            } else {
                spnChucVu.setEnabled(true);
            }
        }

        cursor.close();
        db.close();
    }

    // Hàm sửa thông tin tài khoản
    private void suaTaiKhoan() {
        String hoten = txtHoTen.getText().toString().trim();
        String chucvu = spnChucVu.getSelectedItem().toString();

        // Kiểm tra dữ liệu
        if (hoten.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập họ tên!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Kiểm tra họ tên: <= 22 ký tự, không chứa số & ký tự đặc biệt
        if (hoten.length() > 22 || !hoten.matches("[a-zA-ZÀ-ỹ0-9_ ]+")) {
            Toast.makeText(this, "Họ tên không hợp lệ! (Tối đa 22 ký tự, không chứa ký tự đặc biệt)", Toast.LENGTH_SHORT).show();
            return;
        }


        SQLiteDatabase db = dbHelper.getWritableDatabase();


        // Cập nhật thông tin tài khoản trong bảng user
        ContentValues values = new ContentValues();
        values.put("hoten", hoten);
        values.put("chucvu", chucvu);
        int rows = db.update("user", values, "taikhoan = ?", new String[]{taikhoan});

        // Nếu cập nhật user thành công thì cập nhật luôn bảng danhgia
        if (rows > 0) {
            ContentValues danhGiaValues = new ContentValues();
            danhGiaValues.put("hoten", hoten); // Cập nhật họ tên

            db.update("danhgia", danhGiaValues, "taikhoan = ?", new String[]{taikhoan});
            Toast.makeText(this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Lỗi cập nhật tài khoản!", Toast.LENGTH_SHORT).show();
        }

        db.close();
    }

}
