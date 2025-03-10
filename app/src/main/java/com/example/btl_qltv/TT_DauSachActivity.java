package com.example.btl_qltv;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class TT_DauSachActivity extends Fragment {
    private DatabaseHelper dbHelper;
    private LinearLayout layoutDauSach;
    private String taikhoan;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.tt_dausach, container, false);
        layoutDauSach = view.findViewById(R.id.layout_dausach);
        dbHelper = new DatabaseHelper(getContext());

        taikhoan = requireActivity().getIntent().getStringExtra("taikhoan");


        // Kiểm tra quyền trước khi load sách
        if (checkPermission()) {
            loadDauSach();
        }

        // Ánh xạ BottomNavigationView
        BottomNavigationView bottomNav = view.findViewById(R.id.bottom_nav);

        // Bắt sự kiện khi chọn item trên Bottom Navigation
        bottomNav.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();

                if (id == R.id.ic_them) {
                    // Mở màn hình Thêm đầu sách
                    Intent intent = new Intent(getActivity(), ThemDauSachActivity.class);
                    intent.putExtra("taikhoan", taikhoan);
                    startActivity(intent);
                    return true;
                }
                return false;
            }
        });

        return view;
    }

    private void loadDauSach() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT ds.anhds, ds.tends, " +
                        "(SELECT AVG(dg.sosaodg) FROM danhgia dg WHERE dg.tends = ds.tends) AS sosaodg, " +
                        "ds.tentg, ds.tentl, " +
                        "(SELECT SUM(tts.soluongsach) FROM tinhtrangsach tts WHERE tts.tends = ds.tends) AS soluongsach " +
                        "FROM dausach ds",
                null
        );

        layoutDauSach.removeAllViews(); // Xóa dữ liệu cũ

        while (cursor.moveToNext()) {
            String anhds = cursor.getString(0);
            String tends = cursor.getString(1);
            Double sosaodg = cursor.isNull(2) ? null : cursor.getDouble(2);
            String tentg = cursor.getString(3);
            String tentl = cursor.getString(4);
            int soluongsach = cursor.getInt(5);

            View itemView = getLayoutInflater().inflate(R.layout.item_dausach, layoutDauSach, false);

            ImageView imgBook = itemView.findViewById(R.id.img_book);
            TextView txtTenSach = itemView.findViewById(R.id.txt_tensach);
            TextView txtDanhGia = itemView.findViewById(R.id.txt_danhgia);
            TextView txtTacGia = itemView.findViewById(R.id.txt_tacgia);
            TextView txtTheLoai = itemView.findViewById(R.id.txt_theloai);
            TextView txtSoSach = itemView.findViewById(R.id.txt_sosach);
            ImageButton imgMore = itemView.findViewById(R.id.img_more);

            // Gán dữ liệu sách
            txtTenSach.setText(tends);
            txtDanhGia.setText(sosaodg == null ? "Chưa có đánh giá" : String.format("%.1f / 5,0 sao", sosaodg));
            txtTacGia.setText(tentg);
            txtTheLoai.setText(tentl);
            txtSoSach.setText(soluongsach + " quyển");

            // Kiểm tra ảnh sách
            if (anhds != null && !anhds.isEmpty()) {
                try {
                    imgBook.setImageURI(Uri.parse(anhds));
                } catch (Exception e) {
                    imgBook.setImageResource(R.drawable.ic_book);
                }
            } else {
                imgBook.setImageResource(R.drawable.ic_book);
            }

            // Xử lý sự kiện khi nhấn vào img_more
            imgMore.setOnClickListener(v -> showPopupMenu(v, tends));

            // Sự kiện khi nhấn vào sách
            itemView.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), ChiTietSachActivity.class);
                intent.putExtra("tends", tends);
                intent.putExtra("taikhoan", taikhoan);
                startActivity(intent);
            });

            layoutDauSach.addView(itemView);
        }

        cursor.close();
        db.close();
    }

    /**
     * Hiển thị menu khi nhấn vào img_more
     */
    private void showPopupMenu(View view, String tenSach) {
        PopupMenu popupMenu = new PopupMenu(getContext(), view);
        popupMenu.getMenuInflater().inflate(R.menu.menu_more_options_tt, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.menu_sua) {
                // Chuyển đến SuaDauSachActivity
                Intent intent = new Intent(getActivity(), SuaDauSachActivity.class);
                intent.putExtra("tends", tenSach);
                intent.putExtra("taikhoan", taikhoan);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.menu_tinhtrang) {
                // Chuyển đến TinhTrangActivity
                Intent intent = new Intent(getActivity(), TT_TinhTrangActivity.class);
                intent.putExtra("tends", tenSach);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.menu_xoa) {
                // Hiển thị hộp thoại xác nhận xóa
                showDeleteConfirmationDialog(tenSach);
                return true;
            }
            return false;
        });

        popupMenu.show();
    }

    private void showDeleteConfirmationDialog(String tenSach) {
        new AlertDialog.Builder(getContext())
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc chắn muốn xóa đầu sách \"" + tenSach + "\" không?")
                .setPositiveButton("Có", (dialog, which) -> {
                    // Gọi hàm xóa sách
                    deleteDauSach(tenSach);
                })
                .setNegativeButton("Không", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void deleteDauSach(String tenSach) {
        SQLiteDatabase db = new DatabaseHelper(getContext()).getWritableDatabase();
        int rowsDeleted = db.delete("dausach", "tends = ?", new String[]{tenSach});
        db.close();

        if (rowsDeleted > 0) {
            Toast.makeText(getContext(), "Xóa đầu sách thành công!", Toast.LENGTH_SHORT).show();
            // Cập nhật danh sách (tùy vào cách bạn hiển thị danh sách)
            loadDauSach();
        } else {
            Toast.makeText(getContext(), "Lỗi! Không thể xóa đầu sách.", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Kiểm tra quyền truy cập ảnh
     */
    private boolean checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_MEDIA_IMAGES}, 100);
                return false;
            }
        } else {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 100);
                return false;
            }
        }
        return true;
    }

    /**
     * Xử lý kết quả cấp quyền
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                loadDauSach();
            } else {
                Toast.makeText(getContext(), "Bạn cần cấp quyền để hiển thị ảnh!", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
