package com.example.btl_qltv;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.PopupMenu;


import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


import androidx.appcompat.app.AppCompatActivity;


public class ChiTietSachActivity extends AppCompatActivity {
    private ImageView imgBook;
    private TextView txtTenSach, txtDanhGia, txtTacGia, txtNoiDungSach, txtViTri, txtNXB, txtSoLuong, txtTheLoai, txtNgayXuat, txtSoTrang;
    private TextView txtClickDanhGia, txtMdDanhGia;
    private LinearLayout layoutDanhGia;
    private RatingBar ratingBar;
    private EditText txtNdDanhGia;
    private Button btnHuy, btnThemDanhGia;
    private LinearLayout layoutBinhLuan;
    private ImageButton imgBack;

    private DatabaseHelper dbHelper;
    private String taikhoan, tends;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chitiet_sach);

        // Ánh xạ view
        imgBook = findViewById(R.id.img_book);
        txtTenSach = findViewById(R.id.txt_tensach);
        txtDanhGia = findViewById(R.id.txt_danhgia);
        txtTacGia = findViewById(R.id.txt_tacgia);
        txtNoiDungSach = findViewById(R.id.txt_noidungsach);
        txtViTri = findViewById(R.id.txt_vitri);
        txtNXB = findViewById(R.id.txt_nxb);
        txtSoLuong = findViewById(R.id.txt_soluong);
        txtTheLoai = findViewById(R.id.txt_theloai);
        txtNgayXuat = findViewById(R.id.txt_ngayxuat);
        txtSoTrang = findViewById(R.id.txt_sotrang);
        layoutDanhGia = findViewById(R.id.layout_danhgia);
        ratingBar = findViewById(R.id.ratingbar_);
        txtMdDanhGia = findViewById(R.id.txt_md_danhgia);
        txtNdDanhGia = findViewById(R.id.txt_nd_danhgia);
        btnHuy = findViewById(R.id.btn_huy);
        btnThemDanhGia = findViewById(R.id.btn_themdanhgia);
        layoutBinhLuan = findViewById(R.id.layout_binhluan);
        imgBack = findViewById(R.id.img_back);
        txtClickDanhGia = findViewById(R.id.txtClickDanhGia);

        dbHelper = new DatabaseHelper(this);

        // Nhận dữ liệu từ Intent
        tends = getIntent().getStringExtra("tends");
        taikhoan = getIntent().getStringExtra("taikhoan");

        // Load chi tiết sách từ CSDL
        loadChiTietSach();
        loadDanhGia();

        // Sự kiện mở layout đánh giá
        txtClickDanhGia.setOnClickListener(v -> {
            layoutDanhGia.setVisibility(View.VISIBLE);
            txtClickDanhGia.setVisibility(View.GONE);
            kiemTraDanhGia();
        });


        // Gán sự kiện cho nút imgBack
        imgBack.setOnClickListener(v -> checkChucVu(taikhoan));


        // Sự kiện chọn số sao để hiển thị mức độ đánh giá
        ratingBar.setOnRatingBarChangeListener((ratingBar, rating, fromUser) -> {
            if (rating <= 2.5) {
                txtMdDanhGia.setText("Quá tệ!");
            } else if (rating <= 4.5) {
                txtMdDanhGia.setText("Hay!");
            } else {
                txtMdDanhGia.setText("Quá tuyệt vời!");
            }
        });



        btnHuy.setOnClickListener(v -> {
            layoutDanhGia.setVisibility(View.GONE);
            txtClickDanhGia.setVisibility(View.VISIBLE);
            kiemTraDanhGia();
        });


        // Sự kiện thêm đánh giá vào CSDL
        btnThemDanhGia.setOnClickListener(v -> {
            themDanhGia();
            kiemTraDanhGia();  // Cập nhật lại giao diện sau khi lưu
        });

    }

    // Load chi tiết sách từ SQLite
    private void loadChiTietSach() {
        if (tends == null || tends.isEmpty()) {
            Toast.makeText(this, "Lỗi: Không có thông tin sách!", Toast.LENGTH_SHORT).show();
            return;
        }

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT ds.anhds, ds.tends, " +
                        "(SELECT AVG(dg.sosaodg) FROM danhgia dg WHERE dg.tends = ds.tends) AS sosaodg, " +
                        "ds.tentg, ds.noidung, ds.tenvt, ds.tennxb, " +
                        "(SELECT SUM(tts.soluongsach) FROM tinhtrangsach tts WHERE tts.tends = ds.tends) AS soluong, " +
                        "ds.tentl, ds.ngayxb, ds.sotrang " +
                        "FROM dausach ds WHERE ds.tends = ?",
                new String[]{tends} // Đảm bảo tends không null
        );

        if (cursor.moveToFirst()) {
            String anhds = cursor.getString(0);
            String tensach = cursor.getString(1);
            double sosaodg = cursor.getDouble(2);
            String tentg = cursor.getString(3);
            String noidung = cursor.getString(4);
            String vitri = cursor.getString(5);
            String nxb = cursor.getString(6);
            int soluong = cursor.getInt(7);
            String tentl = cursor.getString(8);
            String ngayxuat = cursor.getString(9);
            int sotrang = cursor.getInt(10);

            // Gán dữ liệu lên giao diện
            if (anhds != null && !anhds.isEmpty()) {
                imgBook.setImageURI(Uri.parse(anhds));
            } else {
                imgBook.setImageResource(R.drawable.ic_book);
            }

            txtTenSach.setText(tensach);
            txtDanhGia.setText(sosaodg == 0 ? "Chưa có đánh giá" : String.format("%.1f sao", sosaodg));
            txtTacGia.setText(tentg);
            txtNoiDungSach.setText(noidung);
            txtViTri.setText("Vị trí: " + vitri);
            txtNXB.setText("Nhà xuất bản: " + nxb);
            txtSoLuong.setText("Số lượng: " + soluong);
            txtTheLoai.setText("Thể loại: " + tentl);
            txtNgayXuat.setText("Ngày xuất bản: " + ngayxuat);
            txtSoTrang.setText("Số trang: " + sotrang);
        } else {
            Toast.makeText(this, "Không tìm thấy thông tin sách!", Toast.LENGTH_SHORT).show();
        }

        cursor.close();
        db.close();
    }

    // Thêm đánh giá vào CSDL
    private void themDanhGia() {
        float sosaodg = ratingBar.getRating();
        String noidungDanhGia = txtNdDanhGia.getText().toString().trim();
        String ngayDanhGia = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());

        if (sosaodg == 0) {
            Toast.makeText(this, "Vui lòng chọn số sao!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (noidungDanhGia.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập nội dung đánh giá!", Toast.LENGTH_SHORT).show();
            return;
        }

        SQLiteDatabase db = dbHelper.getReadableDatabase();

        //  Lấy họ tên từ bảng user
        String hoTen = "";
        try (Cursor cursor = db.rawQuery("SELECT hoten FROM user WHERE taikhoan = ?", new String[]{taikhoan})) {
            if (cursor.moveToFirst()) {
                hoTen = cursor.getString(cursor.getColumnIndexOrThrow("hoten"));
            } else {
                Toast.makeText(this, "Không tìm thấy thông tin người dùng!", Toast.LENGTH_SHORT).show();
                db.close();
                return;
            }
        }

        //  Kiểm tra tài khoản đã đánh giá chưa
        boolean daDanhGia = false;
        try (Cursor cursor = db.rawQuery("SELECT * FROM danhgia WHERE taikhoan = ? AND tends = ?", new String[]{taikhoan, tends})) {
            if (cursor.moveToFirst()) {
                daDanhGia = true;
            }
        }

        db.close(); // Đóng kết nối đọc xong rồi mới mở lại để ghi

        db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("hoten", hoTen);
        values.put("sosaodg", sosaodg);
        values.put("noidung", noidungDanhGia);
        values.put("ngaydg", ngayDanhGia);

        long result;
        if (daDanhGia) {
            // Nếu đã có đánh giá → UPDATE
            result = db.update("danhgia", values, "taikhoan = ? AND tends = ?", new String[]{taikhoan, tends});
        } else {
            // Nếu chưa có → INSERT
            values.put("taikhoan", taikhoan);
            values.put("tends", tends);
            result = db.insert("danhgia", null, values);
        }

        db.close(); // Đóng kết nối ghi sau khi cập nhật xong

        if (result == -1) {
            Toast.makeText(this, "Lỗi khi cập nhật đánh giá!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Đánh giá đã được cập nhật!", Toast.LENGTH_SHORT).show();
            layoutDanhGia.setVisibility(View.GONE);
            txtClickDanhGia.setVisibility(View.VISIBLE);
            loadDanhGia(); // Load lại danh sách đánh giá
            loadChiTietSach();
        }
    }


    private void loadDanhGia() {
        layoutBinhLuan.removeAllViews(); // Xóa dữ liệu cũ trước khi load mới

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT iddg, taikhoan, hoten, sosaodg, ngaydg, noidung FROM danhgia WHERE tends = ?", new String[]{tends});

        while (cursor.moveToNext()) {
            try {
                // Lấy id đánh giá để xóa
                int idDanhGia = cursor.getInt(cursor.getColumnIndexOrThrow("iddg"));
                String taiKhoan = cursor.getString(cursor.getColumnIndexOrThrow("taikhoan"));
                String tenNguoiDung = cursor.getString(cursor.getColumnIndexOrThrow("hoten"));
                float soSao = cursor.getFloat(cursor.getColumnIndexOrThrow("sosaodg"));
                String ngayDanhGia = cursor.getString(cursor.getColumnIndexOrThrow("ngaydg"));
                String noiDung = cursor.getString(cursor.getColumnIndexOrThrow("noidung"));

                View danhGiaView = LayoutInflater.from(this).inflate(R.layout.item_danhgia, layoutBinhLuan, false);

                // Ánh xạ View trong item_danhgia.xml
                TextView txtTenNguoiDung = danhGiaView.findViewById(R.id.txt_tennguoidung);
                RatingBar ratingBarSoDanhGia = danhGiaView.findViewById(R.id.ratingbar_sodanhgia);
                TextView txtNgayDanhGia = danhGiaView.findViewById(R.id.txt_ngaydanhgia);
                TextView txtNoiDungDanhGia = danhGiaView.findViewById(R.id.txt_nd_danhgia);
                ImageButton imgMore = danhGiaView.findViewById(R.id.img_more);

                // Chỉ hiển thị tên người dùng
                txtTenNguoiDung.setText(tenNguoiDung);
                ratingBarSoDanhGia.setRating(soSao);
                txtNgayDanhGia.setText(ngayDanhGia);
                txtNoiDungDanhGia.setText(noiDung);

                // Ẩn "more" nếu không phải tài khoản của người dùng hiện tại
                if (!taiKhoan.equals(taikhoan)) {
                    imgMore.setVisibility(View.GONE);
                } else {
                    imgMore.setOnClickListener(v -> showPopupMenu(v, idDanhGia));
                }

                // Thêm item vào layout_binhluan
                layoutBinhLuan.addView(danhGiaView);
            } catch (Exception e) {
                Log.e("DatabaseError", "Lỗi khi lấy dữ liệu từ Cursor", e);
            }
        }

        cursor.close();
        db.close();
    }


    private void showPopupMenu(View view, int idDanhGia) {
        PopupMenu popupMenu = new PopupMenu(this, view);
        MenuInflater inflater = popupMenu.getMenuInflater();
        inflater.inflate(R.menu.menu_xoa_danhgia, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.menu_xoa_danhgia) {
                    showDeleteConfirmationDialog(idDanhGia);
                    return true;
                }
                return false;
            }
        });

        popupMenu.show();
    }

    private void showDeleteConfirmationDialog(int idDanhGia) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc chắn muốn xóa đánh giá này không?")
                .setPositiveButton("Có", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        xoaDanhGia(idDanhGia);
                    }
                })
                .setNegativeButton("Không", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        AlertDialog dialog = builder.create();
        dialog.show();
    }


    private void xoaDanhGia(int idDanhGia) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int deletedRows = db.delete("danhgia", "iddg = ?", new String[]{String.valueOf(idDanhGia)});
        db.close();

        if (deletedRows > 0) {
            Toast.makeText(this, "Đã xóa đánh giá!", Toast.LENGTH_SHORT).show();
            loadDanhGia(); // Load lại danh sách sau khi xóa
        } else {
            Toast.makeText(this, "Xóa không thành công!", Toast.LENGTH_SHORT).show();
        }
    }

    private void checkChucVu(String taikhoan) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT chucvu FROM user WHERE taikhoan = ?", new String[]{taikhoan});

        if (cursor.moveToFirst()) {
            String chucVu = cursor.getString(0);
            Intent intent_;

            if ("Thủ thư".equals(chucVu)) {
                intent_ = new Intent(ChiTietSachActivity.this, TrangChuTTActivity.class);
            } else {
                intent_ = new Intent(ChiTietSachActivity.this, TrangChuDGActivity.class);
            }

            intent_.putExtra("taikhoan", taikhoan);
            startActivity(intent_);
            finish(); // Đóng Activity hiện tại
        } else {
            Toast.makeText(this, "Không tìm thấy thông tin tài khoản!", Toast.LENGTH_SHORT).show();
        }

        cursor.close();
        db.close();
    }

    private void kiemTraDanhGia() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;

        try {
            cursor = db.rawQuery(
                    "SELECT sosaodg, noidung FROM danhgia WHERE taikhoan = ? AND tends = ?",
                    new String[]{taikhoan, tends}
            );

            if (cursor.moveToFirst()) {
                // Lấy vị trí của cột (sẽ báo lỗi nếu cột không tồn tại)
                int indexSoSao = cursor.getColumnIndexOrThrow("sosaodg");
                int indexNoiDung = cursor.getColumnIndexOrThrow("noidung");

                // Lấy dữ liệu cũ
                float sosaodg = cursor.getFloat(indexSoSao);
                String noidung = cursor.getString(indexNoiDung);

                // Hiển thị dữ liệu cũ
                ratingBar.setRating(sosaodg);
                txtNdDanhGia.setText(noidung);

                // Hiển thị mức độ đánh giá theo số sao
                if (sosaodg <= 2.5) {
                    txtMdDanhGia.setText("Quá tệ!");
                } else if (sosaodg <= 4.5) {
                    txtMdDanhGia.setText("Hay!");
                } else {
                    txtMdDanhGia.setText("Quá tuyệt vời!");
                }

            } else {
                // Nếu chưa có đánh giá, reset về mặc định
                ratingBar.setRating(0);
                txtNdDanhGia.setText("");
                txtMdDanhGia.setText("Chọn số sao để đánh giá");
            }
        } catch (Exception e) {
            Log.e("DB_ERROR", "Lỗi khi lấy đánh giá: " + e.getMessage());
        } finally {
            if (cursor != null) cursor.close();
            db.close();
        }
    }

}

