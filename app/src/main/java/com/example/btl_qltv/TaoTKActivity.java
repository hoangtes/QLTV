package com.example.btl_qltv;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class TaoTKActivity extends Fragment {

    private DatabaseHelper dbHelper;
    private LinearLayout layoutTaoTk;
    private String taikhoan;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.tt_tao_tk, container, false);

        dbHelper = new DatabaseHelper(getActivity());
        layoutTaoTk = view.findViewById(R.id.layout_tao_tk); // Ánh xạ layout chứa danh sách tài khoản

        taikhoan = requireActivity().getIntent().getStringExtra("taikhoan");

        // Gọi hàm load danh sách tài khoản
        loadTaiKhoan();

        // Ánh xạ BottomNavigationView
        BottomNavigationView bottomNav = view.findViewById(R.id.bottom_nav);

        // Bắt sự kiện khi chọn item trên Bottom Navigation
        bottomNav.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (item.getItemId() == R.id.ic_them) {
                    // Mở màn hình Thêm tài khoản
                    Intent intent = new Intent(getActivity(), ThemTKActivity.class);
                    startActivity(intent);
                    return true;
                }
                return false;
            }
        });

        return view;
    }

    // Hàm load danh sách tài khoản từ SQLite và hiển thị vào layout
    private void loadTaiKhoan() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT hoten, taikhoan, chucvu FROM user", null);

        layoutTaoTk.removeAllViews(); // Xóa dữ liệu cũ trước khi load lại

        while (cursor.moveToNext()) {
            String hoten = cursor.getString(0);
            String taikhoan = cursor.getString(1);
            String chucvu = cursor.getString(2);

            // Inflate item_taotk.xml vào layout_tao_tk
            View itemView = getLayoutInflater().inflate(R.layout.item_taotk, layoutTaoTk, false);

            // Ánh xạ các thành phần
            TextView txtTenNguoiDung = itemView.findViewById(R.id.txt_tennguoidung);
            TextView txtTaiKhoan = itemView.findViewById(R.id.txt_taikhoan);
            TextView txtChucVu = itemView.findViewById(R.id.txt_chucvu);
            ImageButton imgMore = itemView.findViewById(R.id.img_more);

            // Gán dữ liệu
            txtTenNguoiDung.setText(hoten);
            txtTaiKhoan.setText(taikhoan);
            txtChucVu.setText(chucvu);

            // Xử lý sự kiện khi nhấn vào img_more
            imgMore.setOnClickListener(v -> showPopupMenu(v, taikhoan));

            // Thêm item vào layout_tao_tk
            layoutTaoTk.addView(itemView);
        }

        cursor.close();
        db.close();
    }

    // Hàm hiển thị popup menu khi nhấn vào img_more
    private void showPopupMenu(View view, String taikhoan) {
        PopupMenu popupMenu = new PopupMenu(getContext(), view);
        popupMenu.getMenuInflater().inflate(R.menu.menu_more_options_taotk, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.menu_sua) {
                // Chuyển đến SuaDauSachActivity
                Intent intent = new Intent(getActivity(), SuaTKActivity.class);
                intent.putExtra("taikhoan", taikhoan);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.menu_xoa) {
                // Hiển thị hộp thoại xác nhận xóa
                showDeleteConfirmationDialog(taikhoan);
                return true;
            }
            return false;
        });

        popupMenu.show();
    }

    private void showDeleteConfirmationDialog(String taikhoan) {
        new AlertDialog.Builder(getContext())
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc chắn muốn xóa tài khoản \"" + taikhoan + "\" không?")
                .setPositiveButton("Có", (dialog, which) -> {
                    // Gọi hàm xóa tk
                    xoaTaiKhoan(taikhoan);
                })
                .setNegativeButton("Không", (dialog, which) -> dialog.dismiss())
                .show();
    }

    // Hàm xóa tài khoản
    private void xoaTaiKhoan(String taikhoanXoa) {
        // Kiểm tra nếu taikhoan hiện tại chưa được lấy
        if (taikhoan.isEmpty()) {
            Toast.makeText(getContext(), "Lỗi: Không xác định được tài khoản hiện tại!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Không thể xóa tài khoản "admin"
        if (taikhoanXoa.equals("admin")) {
            Toast.makeText(getContext(), "Không thể xóa tài khoản admin!", Toast.LENGTH_SHORT).show();
            return;
        }

        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // Lấy chức vụ của tài khoản cần xóa
        Cursor cursor = db.rawQuery("SELECT chucvu FROM user WHERE taikhoan = ?", new String[]{taikhoanXoa});
        if (!cursor.moveToFirst()) {
            cursor.close();
            db.close();
            Toast.makeText(getContext(), "Tài khoản không tồn tại!", Toast.LENGTH_SHORT).show();
            return;
        }
        String chucVuCanXoa = cursor.getString(0);
        cursor.close();
        db.close();

        //  Không thể xóa tài khoản của chính mình
        if (taikhoan.equals(taikhoanXoa)) {
            Toast.makeText(getContext(), "Bạn không thể xóa tài khoản của chính mình!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Nếu người dùng hiện tại là "Thủ thư", không thể xóa "Thủ thư" khác (trừ admin)
        if (chucVuCanXoa.equals("Thủ thư") && !taikhoan.equals("admin")) {
            Toast.makeText(getContext(), "Bạn không có quyền xóa tài khoản này!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Nếu hợp lệ, thực hiện xóa
        SQLiteDatabase dbWrite = dbHelper.getWritableDatabase();
        int deletedRows = dbWrite.delete("user", "taikhoan = ?", new String[]{taikhoanXoa});
        dbWrite.close();

        if (deletedRows > 0) {
            Toast.makeText(getContext(), "Xóa tài khoản thành công!", Toast.LENGTH_SHORT).show();
            loadTaiKhoan(); // Load lại danh sách sau khi xóa
        } else {
            Toast.makeText(getContext(), "Lỗi khi xóa tài khoản!", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        loadTaiKhoan(); // Load lại danh sách khi quay lại màn hình
    }
}
