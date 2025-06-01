package com.example.btl_qltv;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class LoaiSachActivity extends Fragment {
    EditText txttentl;
    TableLayout tbltl;
    Button btnThem, btnSua, btnXoa, btnTaiLai, btnTimKiem;
    DatabaseHelper dbHelper;
    SQLiteDatabase database;
    int selectedidtl = -1; // Lưu ID loại sách được chọn

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.theloai, container, false);

        // Ánh xạ UI
        txttentl = view.findViewById(R.id.txt_tenloai);
        btnThem = view.findViewById(R.id.btn_them);
        btnSua = view.findViewById(R.id.btn_sua);
        btnXoa = view.findViewById(R.id.btn_xoa);
        btnTaiLai = view.findViewById(R.id.btn_tailai);
        btnTimKiem = view.findViewById(R.id.btn_timkiem);
        tbltl = view.findViewById(R.id.tblLoaiSach);
        dbHelper = new DatabaseHelper(getContext());
        database = dbHelper.getWritableDatabase();

        // Hiển thị danh sách loại sách khi mở màn hình
        hienThiDanhSachtl("");

        // Sự kiện các nút
        btnThem.setOnClickListener(v -> themtl());
        btnSua.setOnClickListener(v -> suatl());
        btnXoa.setOnClickListener(v -> xoatl());

        btnTaiLai.setOnClickListener(v -> {
            txttentl.setText("");
            hienThiDanhSachtl("");
        });

        btnTimKiem.setOnClickListener(v -> timKiemtl());

        return view;
    }

    // **Thêm loại sách**
    private void themtl() {
        String tentl = txttentl.getText().toString().trim();

        if (!kiemTraHopLe(tentl)) return;

        // Kiểm tra trùng lặp
        if (istentlExists(tentl)) {
            Toast.makeText(getContext(), "Tên loại sách đã tồn tại!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Chèn vào CSDL
        ContentValues values = new ContentValues();
        values.put("tentl", tentl);

        long result = database.insert("theloai", null, values);
        if (result != -1) {
            Toast.makeText(getContext(), "Thêm loại sách thành công!", Toast.LENGTH_SHORT).show();
            txttentl.setText("");
        } else {
            Toast.makeText(getContext(), "Lỗi khi thêm loại sách!", Toast.LENGTH_SHORT).show();
        }

        hienThiDanhSachtl("");
    }

    // **Sửa loại sách**
    private void suatl() {
        if (selectedidtl == -1) {
            Toast.makeText(getContext(), "Vui lòng chọn loại sách để sửa!", Toast.LENGTH_SHORT).show();
            return;
        }

        String tenMoi = txttentl.getText().toString().trim();
        if (!kiemTraHopLe(tenMoi)) return;

        ContentValues values = new ContentValues();
        values.put("tentl", tenMoi);

        int rowsUpdated = database.update("theloai", values, "idtl = ?", new String[]{String.valueOf(selectedidtl)});

        if (rowsUpdated > 0) {
            Toast.makeText(getContext(), "Sửa thành công!", Toast.LENGTH_SHORT).show();
            selectedidtl = -1; // Reset ID
            txttentl.setText("");
        } else {
            Toast.makeText(getContext(), "Lỗi khi sửa loại sách!", Toast.LENGTH_SHORT).show();
        }

        hienThiDanhSachtl("");
    }

    // **Xóa loại sách**
    private void xoatl() {
        if (selectedidtl == -1) {
            Toast.makeText(getContext(), "Vui lòng chọn loại sách để xóa!", Toast.LENGTH_SHORT).show();
            return;
        }

        int rowsDeleted = database.delete("theloai", "idtl = ?", new String[]{String.valueOf(selectedidtl)});

        if (rowsDeleted > 0) {
            Toast.makeText(getContext(), "Xóa thành công!", Toast.LENGTH_SHORT).show();
            selectedidtl = -1;
            txttentl.setText("");
        } else {
            Toast.makeText(getContext(), "Lỗi khi xóa loại sách!", Toast.LENGTH_SHORT).show();
        }

        hienThiDanhSachtl("");
    }

    // **Tìm kiếm loại sách**
    private void timKiemtl() {
        String keyword = txttentl.getText().toString().trim();
        hienThiDanhSachtl(keyword);
    }

    // **Hiển thị danh sách loại sách**
    private void hienThiDanhSachtl(String keyword) {
        tbltl.removeAllViews(); // Xóa dữ liệu cũ trước khi hiển thị mới

        String query = "SELECT * FROM theloai";
        String[] args = null;

        // Kiểm tra nếu keyword không rỗng
        if (!keyword.isEmpty()) {
            // Kiểm tra nếu keyword là số => Tìm theo ID
            if (keyword.matches("\\d+")) {
                query += " WHERE idtl = ?";
                args = new String[]{keyword};
            } else {
                // Ngược lại tìm theo tên (dùng LIKE để tìm gần đúng)
                query += " WHERE tentl LIKE ?";
                args = new String[]{"%" + keyword + "%"};
            }
        }

        Cursor cursor = database.rawQuery(query, args);

        while (cursor.moveToNext()) {
            int idtl = cursor.getInt(0);
            String tentl = cursor.getString(1);

            TableRow row = new TableRow(getContext());
            row.setOnClickListener(v -> {
                selectedidtl = idtl;
                txttentl.setText(tentl);
            });

            TextView txtId = new TextView(getContext());
            txtId.setText(String.valueOf(idtl));
            txtId.setPadding(8, 8, 8, 8);
            txtId.setBackgroundResource(R.drawable.border_vienvuong);
            txtId.setLayoutParams(new TableRow.LayoutParams(244, TableRow.LayoutParams.WRAP_CONTENT));

            TextView txtTen = new TextView(getContext());
            txtTen.setText(tentl);
            txtTen.setPadding(8, 8, 8, 8);
            txtTen.setBackgroundResource(R.drawable.border_vienvuong);
            txtTen.setLayoutParams(new TableRow.LayoutParams(476, TableRow.LayoutParams.WRAP_CONTENT));

            row.addView(txtId);
            row.addView(txtTen);
            tbltl.addView(row);
        }
        cursor.close();
    }

    // **Kiểm tra hợp lệ**
    private boolean kiemTraHopLe(String ten) {
        if (TextUtils.isEmpty(ten)) {
            Toast.makeText(getContext(), "Tên loại sách không được để trống!", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (ten.length() > 24) {
            Toast.makeText(getContext(), "Tên loại sách không được quá 24 ký tự!", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!ten.matches("^[\\p{L}0-9\\s]+$")) {
            Toast.makeText(getContext(), "Tên tác giả không được chứa ký tự đặc biệt!", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private boolean istentlExists(String tentl) {
        Cursor cursor = database.rawQuery("SELECT * FROM theloai WHERE tentl = ?", new String[]{tentl});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (database != null) {
            database.close();
        }
    }
}
