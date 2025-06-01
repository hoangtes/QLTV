package com.example.btl_qltv;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

public class TaiKhoanActivity extends Fragment {

    private TextView txtHoTen, txtChucVu, txtTaiKhoan;
    private String taikhoan;
    private Button btnSuaThongTin;
    private DatabaseHelper dbHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.chitiet_taikhoan, container, false);

        // Ánh xạ View
        txtHoTen = view.findViewById(R.id.txt_hoten);
        txtChucVu = view.findViewById(R.id.txt_chucvu);
        txtTaiKhoan = view.findViewById(R.id.txt_taikhoan);
        btnSuaThongTin = view.findViewById(R.id.btn_suathongtin);

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

        btnSuaThongTin.setOnClickListener(v -> showDialogSuaHoTen());

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

    private void showDialogSuaHoTen() {
        String currentHoTen = txtHoTen.getText().toString().replace("Họ tên: ", "");

        // Tạo layout chứa TextView và EditText
        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 30, 50, 10);

        // TextView "Họ tên:"
        TextView label = new TextView(requireContext());
        label.setText("Họ tên:");
        label.setTextSize(18);
        layout.addView(label);

        // EditText để nhập họ tên mới
        final EditText edtHoTen = new EditText(requireContext());
        edtHoTen.setText(currentHoTen);
        edtHoTen.setSelection(currentHoTen.length());
        edtHoTen.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        layout.addView(edtHoTen);

        // Tạo dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Chỉnh sửa thông tin");
        builder.setView(layout);

        builder.setPositiveButton("Sửa", (dialog, which) -> {
            String hoTenMoi = edtHoTen.getText().toString().trim();
            if (!hoTenMoi.isEmpty()) {
                updateHoTen(hoTenMoi);
            } else {
                Toast.makeText(requireContext(), "Họ tên không được để trống!", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss());

        builder.create().show();
    }


    private void updateHoTen(String hoTenMoi) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.execSQL("UPDATE user SET hoten = ? WHERE taikhoan = ?", new Object[]{hoTenMoi, taikhoan});
        db.close();

        Toast.makeText(requireContext(), "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
        loadThongTinTaiKhoan(); // Tải lại dữ liệu mới
    }


}
