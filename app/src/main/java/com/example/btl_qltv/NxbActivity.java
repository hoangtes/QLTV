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

public class NxbActivity extends Fragment {
    EditText txtTenNxb;
    TableLayout tblNxb;
    Button btnThem, btnSua, btnXoa, btnTaiLai, btnTimKiem;
    DatabaseHelper dbHelper;
    SQLiteDatabase database;
    int selectedIdNxb = -1; // Lưu ID nhà xuất bản được chọn

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.nhaxuatban, container, false);

        // Ánh xạ UI
        txtTenNxb = view.findViewById(R.id.txt_tennxb);
        btnThem = view.findViewById(R.id.btn_them);
        btnSua = view.findViewById(R.id.btn_sua);
        btnXoa = view.findViewById(R.id.btn_xoa);
        btnTaiLai = view.findViewById(R.id.btn_tailai);
        btnTimKiem = view.findViewById(R.id.btn_timkiem);
        tblNxb = view.findViewById(R.id.tblNxb);
        dbHelper = new DatabaseHelper(getContext());
        database = dbHelper.getWritableDatabase();

        // Hiển thị danh sách nhà xuất bản khi mở màn hình
        hienThiDanhSachNxb("");

        // Sự kiện các nút
        btnThem.setOnClickListener(v -> themNxb());
        btnSua.setOnClickListener(v -> suaNxb());
        btnXoa.setOnClickListener(v -> xoaNxb());

        btnTaiLai.setOnClickListener(v -> {
            txtTenNxb.setText("");
            hienThiDanhSachNxb("");
        });

        btnTimKiem.setOnClickListener(v -> timKiemNxb());

        return view;
    }

    // **Thêm nhà xuất bản**
    private void themNxb() {
        String tenNxb = txtTenNxb.getText().toString().trim();

        if (!kiemTraHopLe(tenNxb)) return;

        // Kiểm tra trùng lặp
        if (isTenNxbExists(tenNxb)) {
            Toast.makeText(getContext(), "Tên nhà xuất bản đã tồn tại!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Chèn vào CSDL
        ContentValues values = new ContentValues();
        values.put("tennxb", tenNxb);

        long result = database.insert("nhaxuatban", null, values);
        if (result != -1) {
            Toast.makeText(getContext(), "Thêm nhà xuất bản thành công!", Toast.LENGTH_SHORT).show();
            txtTenNxb.setText("");
        } else {
            Toast.makeText(getContext(), "Lỗi khi thêm nhà xuất bản!", Toast.LENGTH_SHORT).show();
        }

        hienThiDanhSachNxb("");
    }

    // **Sửa nhà xuất bản**
    private void suaNxb() {
        if (selectedIdNxb == -1) {
            Toast.makeText(getContext(), "Vui lòng chọn nhà xuất bản để sửa!", Toast.LENGTH_SHORT).show();
            return;
        }

        String tenMoi = txtTenNxb.getText().toString().trim();
        if (!kiemTraHopLe(tenMoi)) return;

        ContentValues values = new ContentValues();
        values.put("tennxb", tenMoi);

        int rowsUpdated = database.update("nhaxuatban", values, "idnxb = ?", new String[]{String.valueOf(selectedIdNxb)});

        if (rowsUpdated > 0) {
            Toast.makeText(getContext(), "Sửa thành công!", Toast.LENGTH_SHORT).show();
            selectedIdNxb = -1; // Reset ID
            txtTenNxb.setText("");
        } else {
            Toast.makeText(getContext(), "Lỗi khi sửa nhà xuất bản!", Toast.LENGTH_SHORT).show();
        }

        hienThiDanhSachNxb("");
    }

    // **Xóa nhà xuất bản**
    private void xoaNxb() {
        if (selectedIdNxb == -1) {
            Toast.makeText(getContext(), "Vui lòng chọn nhà xuất bản để xóa!", Toast.LENGTH_SHORT).show();
            return;
        }

        int rowsDeleted = database.delete("nhaxuatban", "idnxb = ?", new String[]{String.valueOf(selectedIdNxb)});

        if (rowsDeleted > 0) {
            Toast.makeText(getContext(), "Xóa thành công!", Toast.LENGTH_SHORT).show();
            selectedIdNxb = -1;
            txtTenNxb.setText("");
        } else {
            Toast.makeText(getContext(), "Lỗi khi xóa nhà xuất bản!", Toast.LENGTH_SHORT).show();
        }

        hienThiDanhSachNxb("");
    }

    // **Tìm kiếm nhà xuất bản**
    private void timKiemNxb() {
        String keyword = txtTenNxb.getText().toString().trim();
        hienThiDanhSachNxb(keyword);
    }

    // **Hiển thị danh sách nhà xuất bản**
    private void hienThiDanhSachNxb(String keyword) {
        tblNxb.removeAllViews(); // Xóa dữ liệu cũ trước khi hiển thị mới

        String query = "SELECT * FROM nhaxuatban";
        String[] args = null;

        // Kiểm tra nếu keyword không rỗng
        if (!keyword.isEmpty()) {
            // Kiểm tra nếu keyword là số => Tìm theo ID
            if (keyword.matches("\\d+")) {
                query += " WHERE idnxb = ?";
                args = new String[]{keyword};
            } else {
                // Ngược lại tìm theo tên (dùng LIKE để tìm gần đúng)
                query += " WHERE tennxb LIKE ?";
                args = new String[]{"%" + keyword + "%"};
            }
        }

        Cursor cursor = database.rawQuery(query, args);

        while (cursor.moveToNext()) {
            int idNxb = cursor.getInt(0);
            String tenNxb = cursor.getString(1);

            TableRow row = new TableRow(getContext());
            row.setOnClickListener(v -> {
                selectedIdNxb = idNxb;
                txtTenNxb.setText(tenNxb);
            });

            TextView txtId = new TextView(getContext());
            txtId.setText(String.valueOf(idNxb));
            txtId.setPadding(8, 8, 8, 8);
            txtId.setBackgroundResource(R.drawable.border_vienvuong);
            txtId.setLayoutParams(new TableRow.LayoutParams(244, TableRow.LayoutParams.WRAP_CONTENT));

            TextView txtTen = new TextView(getContext());
            txtTen.setText(tenNxb);
            txtTen.setPadding(8, 8, 8, 8);
            txtTen.setBackgroundResource(R.drawable.border_vienvuong);
            txtTen.setLayoutParams(new TableRow.LayoutParams(476, TableRow.LayoutParams.WRAP_CONTENT));

            row.addView(txtId);
            row.addView(txtTen);
            tblNxb.addView(row);
        }
        cursor.close();
    }

    // **Kiểm tra hợp lệ**
    private boolean kiemTraHopLe(String ten) {
        if (TextUtils.isEmpty(ten)) {
            Toast.makeText(getContext(), "Tên nhà xuất bản không được để trống!", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (ten.length() > 24) {
            Toast.makeText(getContext(), "Tên nhà xuất bản không được quá 24 ký tự!", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!ten.matches("^[\\p{L}0-9\\s]+$")) {
            Toast.makeText(getContext(), "Tên tác giả không được chứa ký tự đặc biệt!", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private boolean isTenNxbExists(String tenNxb) {
        Cursor cursor = database.rawQuery("SELECT * FROM nhaxuatban WHERE tennxb = ?", new String[]{tenNxb});
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
