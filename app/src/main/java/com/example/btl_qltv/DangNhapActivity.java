package com.example.btl_qltv;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class DangNhapActivity extends AppCompatActivity {
    EditText txtTaiKhoan, txtMatKhau;
    Button btnDangNhap;
    TextView txtError;
    DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dangnhap);

        // Ánh xạ view
        txtTaiKhoan = findViewById(R.id.txt_tk);
        txtMatKhau = findViewById(R.id.txt_mk);
        btnDangNhap = findViewById(R.id.btn_dangnhap);
        txtError = findViewById(R.id.txt_error);

        // Khởi tạo database
        dbHelper = new DatabaseHelper(this);

        // Bắt sự kiện đăng nhập
        btnDangNhap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String taikhoan = txtTaiKhoan.getText().toString().trim();
                String matkhau = txtMatKhau.getText().toString().trim();

                if (taikhoan.isEmpty() || matkhau.isEmpty()) {
                    txtError.setText("Vui lòng nhập đầy đủ thông tin!");
                    txtError.setVisibility(View.VISIBLE);
                } else {
                    checkDangNhap(taikhoan, matkhau);
                }
            }
        });
    }

    private void checkDangNhap(String taikhoan, String matkhau) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT chucvu FROM user WHERE taikhoan=? AND matkhau=?",
                new String[]{taikhoan, matkhau});

        if (cursor.moveToFirst()) {
            String chucvu = cursor.getString(0); // Lấy giá trị cột "chucvu"
            cursor.close();
            db.close();

            // Ẩn thông báo lỗi nếu đăng nhập thành công
            txtError.setVisibility(View.GONE);

            // Tạo Intent để chuyển màn hình
            Intent intent;
            if (chucvu.equals("Thủ thư")) {
                intent = new Intent(DangNhapActivity.this, TrangChuTTActivity.class);
            } else { // Độc giả
                intent = new Intent(DangNhapActivity.this, TrangChuDGActivity.class);
            }

            // Truyền tài khoản vào Intent
            intent.putExtra("taikhoan", taikhoan);
            startActivity(intent);
            finish(); // Đóng màn hình đăng nhập
        } else {
            txtError.setText("Thông tin tài khoản hoặc mật khẩu không chính xác!");
            txtError.setVisibility(View.VISIBLE);
        }
    }
}
