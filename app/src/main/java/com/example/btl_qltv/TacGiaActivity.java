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

public class TacGiaActivity extends Fragment {
    EditText txtTenTacGia;
    TableLayout tblTacGia;
    Button btnThem, btnSua, btnXoa, btnTaiLai, btnTimKiem;
    DatabaseHelper dbHelper;
    SQLiteDatabase database;
    int selectedIdTacGia = -1; // Lưu ID tác giả được chọn

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.tacgia, container, false);

        // Ánh xạ UI
        txtTenTacGia = view.findViewById(R.id.txt_tentacgia);
        btnThem = view.findViewById(R.id.btn_them);
        btnSua = view.findViewById(R.id.btn_sua);
        btnXoa = view.findViewById(R.id.btn_xoa);
        btnTaiLai = view.findViewById(R.id.btn_tailai);
        btnTimKiem = view.findViewById(R.id.btn_timkiem);
        tblTacGia = view.findViewById(R.id.tblTacGia);
        dbHelper = new DatabaseHelper(getContext());
        database = dbHelper.getWritableDatabase();

        // Hiển thị danh sách tác giả khi mở màn hình
        hienThiDanhSachTacGia("");

        // Sự kiện các nút
        btnThem.setOnClickListener(v -> themTacGia());
        btnSua.setOnClickListener(v -> suaTacGia());
        btnXoa.setOnClickListener(v -> xoaTacGia());
        btnTaiLai.setOnClickListener(v -> hienThiDanhSachTacGia(""));
        btnTimKiem.setOnClickListener(v -> timKiemTacGia());

        return view;
    }

    // **Thêm tác giả**
    private void themTacGia() {
        String tenTacGia = txtTenTacGia.getText().toString().trim();

        if (!kiemTraHopLe(tenTacGia)) return;

        // Kiểm tra trùng lặp
        if (isTenTacGiaExists(tenTacGia)) {
            Toast.makeText(getContext(), "Tên tác giả đã tồn tại!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Chèn vào CSDL
        ContentValues values = new ContentValues();
        values.put("tentg", tenTacGia);

        long result = database.insert("tacgia", null, values);
        if (result != -1) {
            Toast.makeText(getContext(), "Thêm tác giả thành công!", Toast.LENGTH_SHORT).show();
            txtTenTacGia.setText("");
        } else {
            Toast.makeText(getContext(), "Lỗi khi thêm tác giả!", Toast.LENGTH_SHORT).show();
        }

        hienThiDanhSachTacGia("");
    }

    // **Sửa tác giả**
    private void suaTacGia() {
        if (selectedIdTacGia == -1) {
            Toast.makeText(getContext(), "Vui lòng chọn tác giả để sửa!", Toast.LENGTH_SHORT).show();
            return;
        }

        String tenMoi = txtTenTacGia.getText().toString().trim();
        if (!kiemTraHopLe(tenMoi)) return;

        ContentValues values = new ContentValues();
        values.put("tentg", tenMoi);

        int rowsUpdated = database.update("tacgia", values, "idtg = ?", new String[]{String.valueOf(selectedIdTacGia)});

        if (rowsUpdated > 0) {
            Toast.makeText(getContext(), "Sửa thành công!", Toast.LENGTH_SHORT).show();
            selectedIdTacGia = -1; // Reset ID
            txtTenTacGia.setText("");
        } else {
            Toast.makeText(getContext(), "Lỗi khi sửa tác giả!", Toast.LENGTH_SHORT).show();
        }

        hienThiDanhSachTacGia("");
    }

    // **Xóa tác giả**
    private void xoaTacGia() {
        if (selectedIdTacGia == -1) {
            Toast.makeText(getContext(), "Vui lòng chọn tác giả để xóa!", Toast.LENGTH_SHORT).show();
            return;
        }

        int rowsDeleted = database.delete("tacgia", "idtg = ?", new String[]{String.valueOf(selectedIdTacGia)});

        if (rowsDeleted > 0) {
            Toast.makeText(getContext(), "Xóa thành công!", Toast.LENGTH_SHORT).show();
            selectedIdTacGia = -1;
            txtTenTacGia.setText("");
        } else {
            Toast.makeText(getContext(), "Lỗi khi xóa tác giả!", Toast.LENGTH_SHORT).show();
        }

        hienThiDanhSachTacGia("");
    }

    // **Tìm kiếm tác giả**
    private void timKiemTacGia() {
        String keyword = txtTenTacGia.getText().toString().trim();
        hienThiDanhSachTacGia(keyword);
    }

    // **Hiển thị danh sách tác giả**
    private void hienThiDanhSachTacGia(String keyword) {
        tblTacGia.removeAllViews(); // Xóa dữ liệu cũ trước khi hiển thị mới

        String query = "SELECT * FROM tacgia";
        String[] args = null;

        // Kiểm tra nếu keyword không rỗng
        if (!keyword.isEmpty()) {
            // Kiểm tra nếu keyword là số => Tìm theo ID
            if (keyword.matches("\\d+")) {
                query += " WHERE idtg = ?";
                args = new String[]{keyword};
            } else {
                // Ngược lại tìm theo tên (dùng LIKE để tìm gần đúng)
                query += " WHERE tentg LIKE ?";
                args = new String[]{"%" + keyword + "%"};
            }
        }

        Cursor cursor = database.rawQuery(query, args);

        while (cursor.moveToNext()) {
            int idTacGia = cursor.getInt(0);
            String tenTacGia = cursor.getString(1);

            TableRow row = new TableRow(getContext());
            row.setOnClickListener(v -> {
                selectedIdTacGia = idTacGia;
                txtTenTacGia.setText(tenTacGia);
            });

            TextView txtId = new TextView(getContext());
            txtId.setText(String.valueOf(idTacGia));
            txtId.setPadding(8, 8, 8, 8);
            txtId.setBackgroundResource(R.drawable.border_vienvuong);
            txtId.setLayoutParams(new TableRow.LayoutParams(244, TableRow.LayoutParams.WRAP_CONTENT));

            TextView txtTen = new TextView(getContext());
            txtTen.setText(tenTacGia);
            txtTen.setPadding(8, 8, 8, 8);
            txtTen.setBackgroundResource(R.drawable.border_vienvuong);
            txtTen.setLayoutParams(new TableRow.LayoutParams(476, TableRow.LayoutParams.WRAP_CONTENT));

            row.addView(txtId);
            row.addView(txtTen);
            tblTacGia.addView(row);
        }
        cursor.close();
    }

    // **Kiểm tra hợp lệ**
    private boolean kiemTraHopLe(String ten) {
        if (TextUtils.isEmpty(ten)) {
            Toast.makeText(getContext(), "Tên tác giả không được để trống!", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (ten.length() > 24) {
            Toast.makeText(getContext(), "Tên tác giả không được quá 24 ký tự!", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!ten.matches("^[a-zA-Z0-9\\s]+$")) {
            Toast.makeText(getContext(), "Tên tác giả không được chứa ký tự đặc biệt!", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private boolean isTenTacGiaExists(String tenTacGia) {
        Cursor cursor = database.rawQuery("SELECT * FROM tacgia WHERE tentg = ?", new String[]{tenTacGia});
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
