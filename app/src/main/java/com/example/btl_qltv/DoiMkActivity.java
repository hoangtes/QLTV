package com.example.btl_qltv;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class DoiMkActivity extends Fragment {

    private EditText txtMkCu, txtMkMoi;
    private Button btnLuu;
    private String taikhoan;
    private DatabaseHelper dbHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.doimk, container, false);

        // Ánh xạ View
        txtMkCu = view.findViewById(R.id.txt_mkcu);
        txtMkMoi = view.findViewById(R.id.txt_mkmoi);
        btnLuu = view.findViewById(R.id.btn_luu);

        // Lấy tài khoản từ Activity
        taikhoan = requireActivity().getIntent().getStringExtra("taikhoan");

        // Khởi tạo DatabaseHelper
        dbHelper = new DatabaseHelper(requireContext());

        // Xử lý sự kiện nhấn nút Lưu
        btnLuu.setOnClickListener(v -> doiMatKhau());

        return view;
    }

    private void doiMatKhau() {
        String mkCu = txtMkCu.getText().toString().trim();
        String mkMoi = txtMkMoi.getText().toString().trim();

        // Kiểm tra dữ liệu nhập vào
        if (mkCu.isEmpty() || mkMoi.isEmpty()) {
            Toast.makeText(getContext(), "Vui lòng nhập đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!(mkMoi.length() >= 5 && mkMoi.length() <= 22 && mkMoi.matches("[a-zA-Z0-9_]+"))) {
            Toast.makeText(getContext(), "Mật khẩu không hợp lệ! (5 - 22 ký tự, chỉ chứa chữ cái, số, _)", Toast.LENGTH_SHORT).show();
            return;
        }

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT matkhau FROM user WHERE taikhoan = ?", new String[]{taikhoan});

        if (cursor.moveToFirst()) {
            String matKhauHienTai = cursor.getString(0);
            cursor.close();

            if (!matKhauHienTai.equals(mkCu)) {
                Toast.makeText(getContext(), "Mật khẩu cũ không đúng!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Cập nhật mật khẩu mới
            SQLiteDatabase dbWrite = dbHelper.getWritableDatabase();
            dbWrite.execSQL("UPDATE user SET matkhau = ? WHERE taikhoan = ?", new String[]{mkMoi, taikhoan});
            dbWrite.close();

            Toast.makeText(getContext(), "Đổi mật khẩu thành công!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getContext(), "Tài khoản không tồn tại!", Toast.LENGTH_SHORT).show();
        }
        db.close();
    }
}
