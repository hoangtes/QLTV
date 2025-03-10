package com.example.btl_qltv;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.InputType;
import android.view.MenuInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.Arrays;

public class TT_TinhTrangActivity extends AppCompatActivity {
    private LinearLayout layoutTinhTrang;
    private ImageButton imgBack;
    private DatabaseHelper dbHelper;
    private String tenSach;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tt_tinhtrang);

        layoutTinhTrang = findViewById(R.id.layout_tinhtrang);
        imgBack = findViewById(R.id.img_back);
        dbHelper = new DatabaseHelper(this);

        // Lấy tên sách từ Intent
        Intent intent = getIntent();
        if (intent.hasExtra("tends")) {
            tenSach = intent.getStringExtra("tends");
        }

        loadTinhTrangSach();

        imgBack.setOnClickListener(v -> {
            finish();
        });

        // Xử lý sự kiện BottomNavigationView
        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        bottomNav.setOnNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.ic_them) {
                showAddBookDialog();
                return true;
            }
            return false;
        });
    }

    // Hiển thị danh sách tình trạng sách
    private void loadTinhTrangSach() {
        layoutTinhTrang.removeAllViews(); // Xóa dữ liệu cũ trước khi load lại

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM tinhtrangsach WHERE tends = ?", new String[]{tenSach});

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    int indexTends = cursor.getColumnIndex("tends");
                    int indexId = cursor.getColumnIndex("idtt");
                    int indexTrangThai = cursor.getColumnIndex("trangthai");
                    int indexTinhTrang = cursor.getColumnIndex("tinhtrang");

                    View view = getLayoutInflater().inflate(R.layout.item_tinhtrang_tt, null);

                    TextView txtTenSach = view.findViewById(R.id.txt_tensach);
                    TextView txtId = view.findViewById(R.id.txt_idtt);
                    TextView txtTrangThai = view.findViewById(R.id.txt_trangthai);
                    TextView txtTinhTrang = view.findViewById(R.id.txt_tinhtrang);
                    ImageButton imgMore = view.findViewById(R.id.img_more);

                    int idSach = cursor.getInt(indexId);
                    String trangThai = cursor.getString(indexTrangThai);
                    String tinhTrang = cursor.getString(indexTinhTrang);

                    if (indexTends != -1) txtTenSach.setText(cursor.getString(indexTends));
                    if (indexId != -1) txtId.setText("ID: " + idSach);
                    if (indexTrangThai != -1) txtTrangThai.setText(trangThai);
                    if (indexTinhTrang != -1) txtTinhTrang.setText(tinhTrang);

                    // Xử lý sự kiện img_more
                    imgMore.setOnClickListener(v -> showPopupMenu(v, idSach, trangThai, tinhTrang));

                    layoutTinhTrang.addView(view);
                } while (cursor.moveToNext());
            } else {
                Toast.makeText(this, "Không có sách nào trong danh sách!", Toast.LENGTH_SHORT).show();
            }
            cursor.close();
        }
        db.close();
    }

    // Hiển thị PopupMenu khi nhấn vào img_more
    private void showPopupMenu(View view, int idSach, String trangThai, String tinhTrang) {
        PopupMenu popupMenu = new PopupMenu(view.getContext(), view);
        MenuInflater inflater = popupMenu.getMenuInflater();
        inflater.inflate(R.menu.menu_tinhtrang, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.menu_sua_trangthai) {
                showEditStatusDialog(idSach, trangThai, tinhTrang);
                return true;
            } else if (itemId == R.id.menu_xoa_sach) {
                confirmDeleteBook(idSach);
                return true;
            }
            return false;
        });

        popupMenu.show();
    }

    private void confirmDeleteBook(int idSach) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Xóa sách");
        builder.setMessage("Bạn có chắc chắn muốn xóa sách này?");

        builder.setPositiveButton("Xóa", (dialog, which) -> {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            int rowsDeleted = db.delete("tinhtrangsach", "idtt = ?", new String[]{String.valueOf(idSach)});
            db.close();

            if (rowsDeleted > 0) {
                Toast.makeText(this, "Xóa thành công!", Toast.LENGTH_SHORT).show();
                loadTinhTrangSach();
            } else {
                Toast.makeText(this, "Xóa thất bại!", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }


    // Hiển thị Dialog nhập số lượng sách muốn thêm
    private void showAddBookDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Nhập số lượng sách muốn thêm");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        builder.setView(input);

        builder.setPositiveButton("Thêm", (dialog, which) -> {
            String soLuongStr = input.getText().toString().trim();
            if (!soLuongStr.isEmpty()) {
                int soLuong = Integer.parseInt(soLuongStr);
                addBooksToDatabase(soLuong);
            } else {
                Toast.makeText(TT_TinhTrangActivity.this, "Vui lòng nhập số lượng hợp lệ!", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss());

        builder.show();
    }

    // Thêm sách vào database
    private void addBooksToDatabase(int soLuong) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        for (int i = 0; i < soLuong; i++) {
            ContentValues values = new ContentValues();
            values.put("tends", tenSach);
            values.put("trangthai", "Chưa cho mượn");
            values.put("tinhtrang", "Tốt");
            values.put("soluongsach", 1);

            db.insert("tinhtrangsach", null, values);
        }

        db.close();
        Toast.makeText(this, "Đã thêm " + soLuong + " sách!", Toast.LENGTH_SHORT).show();
        loadTinhTrangSach();
    }

    // Hiển thị Dialog sửa trạng thái
    private void showEditStatusDialog(int idSach, String trangThai, String tinhTrang) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Chỉnh sửa trạng thái");

        View view = getLayoutInflater().inflate(R.layout.dialog_sua_tinhtrang, null);
        Spinner spnTrangThai = view.findViewById(R.id.spn_trangthai);
        Spinner spnTinhTrang = view.findViewById(R.id.spn_tinhtrang);

        String[] trangThaiList = {"Chưa cho mượn", "Đã cho mượn", "Không được phép mượn"};
        String[] tinhTrangList = {"Tốt", "Bị hư", "Bị mất"};

        ArrayAdapter<String> adapterTrangThai = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, trangThaiList);
        ArrayAdapter<String> adapterTinhTrang = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, tinhTrangList);
        spnTrangThai.setAdapter(adapterTrangThai);
        spnTinhTrang.setAdapter(adapterTinhTrang);

        spnTrangThai.setSelection(Arrays.asList(trangThaiList).indexOf(trangThai));
        spnTinhTrang.setSelection(Arrays.asList(tinhTrangList).indexOf(tinhTrang));

        builder.setView(view);

        builder.setPositiveButton("Lưu", (dialog, which) -> {
            String newTrangThai = spnTrangThai.getSelectedItem().toString();
            String newTinhTrang = spnTinhTrang.getSelectedItem().toString();
            updateBookStatus(idSach, newTrangThai, newTinhTrang);
        });

        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss());

        builder.create().show();
    }

    private void updateBookStatus(int idSach, String trangThai, String tinhTrang) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("trangthai", trangThai);
        values.put("tinhtrang", tinhTrang);

        db.update("tinhtrangsach", values, "idtt = ?", new String[]{String.valueOf(idSach)});
        db.close();

        loadTinhTrangSach();
    }
}
