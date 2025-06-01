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

public class ViTriActivity extends Fragment {
    EditText txttenvt;
    TableLayout tblvt;
    Button btnThem, btnSua, btnXoa, btnTaiLai, btnTimKiem;
    DatabaseHelper dbHelper;
    SQLiteDatabase database;
    int selectedidvt = -1; // Lưu ID vị trí được chọn

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.vitri, container, false);

        // Ánh xạ UI
        txttenvt = view.findViewById(R.id.txt_tenvitri);
        btnThem = view.findViewById(R.id.btn_them);
        btnSua = view.findViewById(R.id.btn_sua);
        btnXoa = view.findViewById(R.id.btn_xoa);
        btnTaiLai = view.findViewById(R.id.btn_tailai);
        btnTimKiem = view.findViewById(R.id.btn_timkiem);
        tblvt = view.findViewById(R.id.tblViTri);
        dbHelper = new DatabaseHelper(getContext());
        database = dbHelper.getWritableDatabase();

        // Hiển thị danh sách vị trí khi mở màn hình
        hienThiDanhSachvt("");

        // Sự kiện các nút
        btnThem.setOnClickListener(v -> themvt());
        btnSua.setOnClickListener(v -> suavt());
        btnXoa.setOnClickListener(v -> xoavt());

        btnTaiLai.setOnClickListener(v -> {
            txttenvt.setText("");
            hienThiDanhSachvt("");
        });

        btnTimKiem.setOnClickListener(v -> timKiemvt());

        return view;
    }

    // **Thêm vị trí**
    private void themvt() {
        String tenvt = txttenvt.getText().toString().trim();

        if (!kiemTraHopLe(tenvt)) return;

        // Kiểm tra trùng lặp
        if (istenvtExists(tenvt)) {
            Toast.makeText(getContext(), "Tên vị trí đã tồn tại!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Chèn vào CSDL
        ContentValues values = new ContentValues();
        values.put("tenvt", tenvt);

        long result = database.insert("vitri", null, values);
        if (result != -1) {
            Toast.makeText(getContext(), "Thêm vị trí thành công!", Toast.LENGTH_SHORT).show();
            txttenvt.setText("");
        } else {
            Toast.makeText(getContext(), "Lỗi khi thêm vị trí!", Toast.LENGTH_SHORT).show();
        }

        hienThiDanhSachvt("");
    }

    // **Sửa vị trí**
    private void suavt() {
        if (selectedidvt == -1) {
            Toast.makeText(getContext(), "Vui lòng chọn vị trí để sửa!", Toast.LENGTH_SHORT).show();
            return;
        }

        String tenMoi = txttenvt.getText().toString().trim();
        if (!kiemTraHopLe(tenMoi)) return;

        ContentValues values = new ContentValues();
        values.put("tenvt", tenMoi);

        int rowsUpdated = database.update("vitri", values, "idvt = ?", new String[]{String.valueOf(selectedidvt)});

        if (rowsUpdated > 0) {
            Toast.makeText(getContext(), "Sửa thành công!", Toast.LENGTH_SHORT).show();
            selectedidvt = -1; // Reset ID
            txttenvt.setText("");
        } else {
            Toast.makeText(getContext(), "Lỗi khi sửa vị trí!", Toast.LENGTH_SHORT).show();
        }

        hienThiDanhSachvt("");
    }

    // **Xóa vị trí**
    private void xoavt() {
        if (selectedidvt == -1) {
            Toast.makeText(getContext(), "Vui lòng chọn vị trí để xóa!", Toast.LENGTH_SHORT).show();
            return;
        }

        int rowsDeleted = database.delete("vitri", "idvt = ?", new String[]{String.valueOf(selectedidvt)});

        if (rowsDeleted > 0) {
            Toast.makeText(getContext(), "Xóa thành công!", Toast.LENGTH_SHORT).show();
            selectedidvt = -1;
            txttenvt.setText("");
        } else {
            Toast.makeText(getContext(), "Lỗi khi xóa vị trí!", Toast.LENGTH_SHORT).show();
        }

        hienThiDanhSachvt("");
    }

    // **Tìm kiếm vị trí**
    private void timKiemvt() {
        String keyword = txttenvt.getText().toString().trim();
        hienThiDanhSachvt(keyword);
    }

    // **Hiển thị danh sách vị trí**
    private void hienThiDanhSachvt(String keyword) {
        tblvt.removeAllViews(); // Xóa dữ liệu cũ trước khi hiển thị mới

        String query = "SELECT * FROM vitri";
        String[] args = null;

        // Kiểm tra nếu keyword không rỗng
        if (!keyword.isEmpty()) {
            // Kiểm tra nếu keyword là số => Tìm theo ID
            if (keyword.matches("\\d+")) {
                query += " WHERE idvt = ?";
                args = new String[]{keyword};
            } else {
                // Ngược lại tìm theo tên (dùng LIKE để tìm gần đúng)
                query += " WHERE tenvt LIKE ?";
                args = new String[]{"%" + keyword + "%"};
            }
        }

        Cursor cursor = database.rawQuery(query, args);

        while (cursor.moveToNext()) {
            int idvt = cursor.getInt(0);
            String tenvt = cursor.getString(1);

            TableRow row = new TableRow(getContext());
            row.setOnClickListener(v -> {
                selectedidvt = idvt;
                txttenvt.setText(tenvt);
            });

            TextView txtId = new TextView(getContext());
            txtId.setText(String.valueOf(idvt));
            txtId.setPadding(8, 8, 8, 8);
            txtId.setBackgroundResource(R.drawable.border_vienvuong);
            txtId.setLayoutParams(new TableRow.LayoutParams(244, TableRow.LayoutParams.WRAP_CONTENT));

            TextView txtTen = new TextView(getContext());
            txtTen.setText(tenvt);
            txtTen.setPadding(8, 8, 8, 8);
            txtTen.setBackgroundResource(R.drawable.border_vienvuong);
            txtTen.setLayoutParams(new TableRow.LayoutParams(476, TableRow.LayoutParams.WRAP_CONTENT));

            row.addView(txtId);
            row.addView(txtTen);
            tblvt.addView(row);
        }
        cursor.close();
    }

    // **Kiểm tra hợp lệ**
    private boolean kiemTraHopLe(String ten) {
        if (TextUtils.isEmpty(ten)) {
            Toast.makeText(getContext(), "Tên vị trí không được để trống!", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (ten.length() > 24) {
            Toast.makeText(getContext(), "Tên vị trí không được quá 24 ký tự!", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!ten.matches("^[\\p{L}0-9\\s,\\-]+$")) {
            Toast.makeText(getContext(), "Tên tác giả không được chứa ký tự đặc biệt!", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private boolean istenvtExists(String tenvt) {
        Cursor cursor = database.rawQuery("SELECT * FROM vitri WHERE tenvt = ?", new String[]{tenvt});
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
