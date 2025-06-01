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
import android.widget.EditText;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TT_DauSachActivity extends Fragment {
    private DatabaseHelper dbHelper;
    private LinearLayout layoutDauSach;
    private List<DauSach> danhSachGoc = new ArrayList<>();
    private EditText txt_timkiem;
    private ImageButton ic_timkiem, ic_tailai, ic_boloc;
    private String taikhoan;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.tt_dausach, container, false);
        layoutDauSach = view.findViewById(R.id.layout_dausach);
        txt_timkiem = view.findViewById(R.id.txt_timkiem);
        ic_timkiem = view.findViewById(R.id.ic_timkiem);
        ic_tailai = view.findViewById(R.id.ic_tailai);
        ic_boloc = view.findViewById(R.id.ic_boloc);

        dbHelper = new DatabaseHelper(getContext());

        taikhoan = requireActivity().getIntent().getStringExtra("taikhoan");


        // Kiểm tra quyền trước khi load sách
        if (checkPermission()) {
            loadDauSach();
        }

        ic_timkiem.setOnClickListener(v -> {
            String keyword = txt_timkiem.getText().toString().trim().toLowerCase();
            List<DauSach> ketQua = new ArrayList<>();
            for (DauSach ds : danhSachGoc) {
                if (ds.tenSach.toLowerCase().contains(keyword)
                        || ds.tacGia.toLowerCase().contains(keyword)
                        || ds.theLoai.toLowerCase().contains(keyword)) {
                    ketQua.add(ds);
                }
            }
            renderDauSach(ketQua);
        });

        ic_tailai.setOnClickListener(v -> {
            txt_timkiem.setText("");
            loadDauSach(); // Tải lại từ database
        });

        ic_boloc.setOnClickListener(v -> {
            String[] options = {"Sắp xếp theo đánh giá (cao → thấp)", "Sắp xếp theo số lượng (cao → thấp)"};

            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle("Bộ lọc")
                    .setItems(options, (dialog, which) -> {
                        if (which == 0) {
                            Collections.sort(danhSachGoc, (a, b) -> Double.compare(
                                    b.danhGia == null ? 0 : b.danhGia,
                                    a.danhGia == null ? 0 : a.danhGia));
                        } else {
                            Collections.sort(danhSachGoc, (a, b) -> Integer.compare(b.soLuong, a.soLuong));
                        }
                        renderDauSach(danhSachGoc);
                    }).show();
        });


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

    @Override
    public void onResume() {
        super.onResume();

        if (checkPermission()) {
            loadDauSach();
        }
    }

    private void loadDauSach() {
        danhSachGoc.clear();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT ds.anhds, ds.tends, " +
                        "(SELECT AVG(dg.sosaodg) FROM danhgia dg WHERE dg.tends = ds.tends) AS sosaodg, " +
                        "ds.tentg, ds.tentl, " +
                        "(SELECT SUM(tts.soluongsach) FROM tinhtrangsach tts WHERE tts.tends = ds.tends) AS soluongsach " +
                        "FROM dausach ds",
                null
        );

        while (cursor.moveToNext()) {
            String anhds = cursor.getString(0);
            String tends = cursor.getString(1);
            Double sosaodg = cursor.isNull(2) ? null : cursor.getDouble(2);
            String tentg = cursor.getString(3);
            String tentl = cursor.getString(4);
            int soluong = cursor.getInt(5);
            danhSachGoc.add(new DauSach(anhds, tends, sosaodg, tentg, tentl, soluong));
        }

        cursor.close();
        db.close();

        renderDauSach(danhSachGoc); // Hiển thị lên giao diện
    }


    private void renderDauSach(List<DauSach> list) {
        layoutDauSach.removeAllViews();
        for (DauSach ds : list) {
            View itemView = getLayoutInflater().inflate(R.layout.item_dausach, layoutDauSach, false);

            ImageView imgBook = itemView.findViewById(R.id.img_book);
            TextView txtTenSach = itemView.findViewById(R.id.txt_tensach);
            TextView txtDanhGia = itemView.findViewById(R.id.txt_danhgia);
            TextView txtTacGia = itemView.findViewById(R.id.txt_tacgia);
            TextView txtTheLoai = itemView.findViewById(R.id.txt_theloai);
            TextView txtSoSach = itemView.findViewById(R.id.txt_sosach);
            ImageButton imgMore = itemView.findViewById(R.id.img_more);

            txtTenSach.setText(ds.tenSach);
            txtDanhGia.setText(ds.danhGia == null ? "Chưa có đánh giá" : String.format("%.1f / 5,0 sao", ds.danhGia));
            txtTacGia.setText(ds.tacGia);
            txtTheLoai.setText(ds.theLoai);
            txtSoSach.setText(ds.soLuong + " quyển");

            if (ds.anh != null && !ds.anh.isEmpty()) {
                try {
                    imgBook.setImageURI(Uri.parse(ds.anh));
                } catch (Exception e) {
                    imgBook.setImageResource(R.drawable.ic_book);
                }
            } else {
                imgBook.setImageResource(R.drawable.ic_book);
            }

            // Click xem chi tiết
            itemView.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), ChiTietSachActivity.class);
                intent.putExtra("tends", ds.tenSach);
                intent.putExtra("taikhoan", taikhoan);
                startActivity(intent);
            });

            // Menu thêm nếu có
            imgMore.setOnClickListener(v -> showPopupMenu(v, ds.tenSach));

            layoutDauSach.addView(itemView);
        }
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
                intent.putExtra("taikhoan", taikhoan);
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
