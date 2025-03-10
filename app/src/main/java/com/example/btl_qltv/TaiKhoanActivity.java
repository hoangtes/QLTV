package com.example.btl_qltv;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class TaiKhoanActivity extends Fragment {

    private TextView txtHoTen, txtChucVu, txtTaiKhoan;
    private String taikhoan;
    private DatabaseHelper dbHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.chitiet_taikhoan, container, false);

        // Ánh xạ View
        txtHoTen = view.findViewById(R.id.txt_hoten);
        txtChucVu = view.findViewById(R.id.txt_chucvu);
        txtTaiKhoan = view.findViewById(R.id.txt_taikhoan);

        // Lấy tài khoản từ Activity
        taikhoan = requireActivity().getIntent().getStringExtra("taikhoan");

        // Kiểm tra nếu tài khoản null
        if (taikhoan == null || taikhoan.isEmpty()) {
            Toast.makeText(requireContext(), "Lỗi: Không tìm thấy tài khoản!", Toast.LENGTH_SHORT).show();
            return view;
        }

        // Khởi tạo DatabaseHelper
        dbHelper = new DatabaseHelper(requireContext());

        // Load thông tin tài khoản
        loadThongTinTaiKhoan();

        return view;
    }

    private void loadThongTinTaiKhoan() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT hoten, chucvu FROM user WHERE taikhoan = ?", new String[]{taikhoan});

        if (cursor.moveToFirst()) {
            String hoTen = cursor.getString(0);
            String chucVu = cursor.getString(1);

            // Hiển thị dữ liệu lên TextView
            txtHoTen.setText("Họ tên: " + hoTen);
            txtChucVu.setText("Chức vụ: " + chucVu);
            txtTaiKhoan.setText("Tài khoản: " + taikhoan);
        } else {
            Toast.makeText(requireContext(), "Không tìm thấy tài khoản!", Toast.LENGTH_SHORT).show();
        }

        cursor.close();
        db.close();
    }
}
