package com.example.btl_qltv;

import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Calendar;

public class ThemDauSachActivity extends AppCompatActivity {
    private static final int PICK_IMAGE = 1;

    private ImageView imgBook, imgDate;
    private ImageButton imgBack, imgThem;
    private EditText txtTenSach, txtNgayXuat, txtSoTrang, txtNoiDung;
    private Spinner spnTacGia, spnNXB, spnTheLoai, spnViTri;

    private DatabaseHelper dbHelper;
    private Uri selectedImageUri;

    private String selectedTacGia, selectedNXB, selectedTheLoai, selectedViTri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.them_ds);

        dbHelper = new DatabaseHelper(this);

        // Ánh xạ các view
        imgBook = findViewById(R.id.img_book);
        imgDate = findViewById(R.id.img_date);
        imgBack = findViewById(R.id.img_back);
        imgThem = findViewById(R.id.img_them);
        txtTenSach = findViewById(R.id.txt_tensach);
        txtNgayXuat = findViewById(R.id.txt_ngayxuat);
        txtSoTrang = findViewById(R.id.txt_sotrang);
        txtNoiDung = findViewById(R.id.txt_noidung);
        spnTacGia = findViewById(R.id.spn_tacgia);
        spnNXB = findViewById(R.id.spn_nxb);
        spnTheLoai = findViewById(R.id.spn_theloai);
        spnViTri = findViewById(R.id.spn_vitri);


        // Xử lý sự kiện
        imgBook.setOnClickListener(v -> chooseImage());
        imgDate.setOnClickListener(v -> showDatePicker());
        imgBack.setOnClickListener(v -> {
            finish();
        });

        imgThem.setOnClickListener(v -> themDauSach());

        // Load dữ liệu vào Spinners
        loadSpinnerData(spnTacGia, "tacgia", "tentg");
        loadSpinnerData(spnNXB, "nhaxuatban", "tennxb");
        loadSpinnerData(spnTheLoai, "theloai", "tentl");
        loadSpinnerData(spnViTri, "vitri", "tenvt");

        // Bắt sự kiện chọn item Spinner
        spnTacGia.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedTacGia = parent.getItemAtPosition(position).toString();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        spnNXB.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedNXB = parent.getItemAtPosition(position).toString();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        spnTheLoai.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedTheLoai = parent.getItemAtPosition(position).toString();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        spnViTri.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedViTri = parent.getItemAtPosition(position).toString();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    // Mở thư viện chọn ảnh
    private void chooseImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE);
    }

    // Nhận kết quả ảnh đã chọn
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            imgBook.setImageURI(selectedImageUri);
        }
    }

    // Hiển thị DatePicker để chọn ngày xuất bản
    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year1, month1, dayOfMonth) -> txtNgayXuat.setText(dayOfMonth + "/" + (month1 + 1) + "/" + year1),
                year, month, day);
        datePickerDialog.show();
    }

    // Load dữ liệu vào Spinner
    private void loadSpinnerData(Spinner spinner, String tableName, String columnName) {
        ArrayList<String> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + columnName + " FROM " + tableName, null);

        if (cursor.moveToFirst()) {
            do {
                list.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, list);
        spinner.setAdapter(adapter);
    }

    // Thêm đầu sách vào cơ sở dữ liệu
    private void themDauSach() {
        String tenSach = txtTenSach.getText().toString().trim();
        String ngayXuatBan = txtNgayXuat.getText().toString().trim();
        String soTrangStr = txtSoTrang.getText().toString().trim();
        String noiDung = txtNoiDung.getText().toString().trim();
        String anhDaiDien = selectedImageUri != null ? selectedImageUri.toString() : "";

        if (tenSach.isEmpty() || ngayXuatBan.isEmpty() || soTrangStr.isEmpty() || noiDung.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
            return;
        }


        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // Kiểm tra xem tên sách đã tồn tại chưa
        Cursor cursor = db.rawQuery("SELECT * FROM dausach WHERE tends = ?", new String[]{tenSach});
        if (cursor.getCount() > 0) {
            Toast.makeText(this, "Tên sách đã tồn tại, vui lòng nhập tên khác!", Toast.LENGTH_SHORT).show();
            cursor.close();
            db.close();
            return;
        }
        cursor.close();
        db.close();

        int soTrang = Integer.parseInt(soTrangStr);
        db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("anhds", anhDaiDien);
        values.put("tends", tenSach);
        values.put("tentg", selectedTacGia);
        values.put("tennxb", selectedNXB);
        values.put("tentl", selectedTheLoai);
        values.put("tenvt", selectedViTri);
        values.put("ngayxb", ngayXuatBan);
        values.put("sotrang", soTrang);
        values.put("noidung", noiDung);

        long result = db.insert("dausach", null, values);
        db.close();

        if (result != -1) {
            Toast.makeText(this, "Thêm sách thành công!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Lỗi khi thêm sách!", Toast.LENGTH_SHORT).show();
        }
    }
}
