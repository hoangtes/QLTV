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
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class SuaDauSachActivity extends AppCompatActivity {
    private static final int PICK_IMAGE = 1;

    private DatabaseHelper dbHelper;
    private Uri selectedImageUri;

    private EditText txtTenSach, txtNgayXuat, txtSoTrang, txtNoiDung;
    private Spinner spnTacGia, spnNXB, spnTheLoai, spnViTri;
    private ImageView imgBook, imgDate;
    private String tenSachCu; // Lưu tên sách gốc để kiểm tra khi cập nhật
    private ImageButton imgSua, imgBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sua_ds);

        dbHelper = new DatabaseHelper(this);

        // Ánh xạ các view
        txtTenSach = findViewById(R.id.txt_tensach);
        txtNgayXuat = findViewById(R.id.txt_ngayxuat);
        txtSoTrang = findViewById(R.id.txt_sotrang);
        txtNoiDung = findViewById(R.id.txt_noidung);
        spnTacGia = findViewById(R.id.spn_tacgia);
        spnNXB = findViewById(R.id.spn_nxb);
        spnTheLoai = findViewById(R.id.spn_theloai);
        spnViTri = findViewById(R.id.spn_vitri);
        imgBook = findViewById(R.id.img_book);
        imgSua = findViewById(R.id.img_sua);
        imgBack = findViewById(R.id.img_back);
        imgDate = findViewById(R.id.img_date);

        // Nhận dữ liệu từ Intent
        tenSachCu = getIntent().getStringExtra("tends");

        // Hiển thị thông tin sách
        loadThongTinSach(tenSachCu);

        // Xử lý sự kiện
        imgBook.setOnClickListener(v -> chooseImage());
        imgDate.setOnClickListener(v -> showDatePicker());

        // Xử lý sự kiện khi nhấn nút sửa
        imgSua.setOnClickListener(v -> updateDauSach());

        // Xử lý nút quay lại
        imgBack.setOnClickListener(v -> {
            finish();
        });
    }

    private void loadThongTinSach(String tenSach) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM dausach WHERE tends = ?", new String[]{tenSach});

        if (cursor.moveToFirst()) {
            // Lấy index các cột
            int indexTensach = cursor.getColumnIndex("tends");
            int indexNgayXuat = cursor.getColumnIndex("ngayxb");
            int indexSoTrang = cursor.getColumnIndex("sotrang");
            int indexNoiDung = cursor.getColumnIndex("noidung");
            int indexAnh = cursor.getColumnIndex("anhds");
            int indexTacGia = cursor.getColumnIndex("tentg");
            int indexNXB = cursor.getColumnIndex("tennxb");
            int indexTheLoai = cursor.getColumnIndex("tentl");
            int indexViTri = cursor.getColumnIndex("tenvt");

            // Hiển thị thông tin sách
            if (indexTensach != -1) txtTenSach.setText(cursor.getString(indexTensach));
            if (indexNgayXuat != -1) txtNgayXuat.setText(cursor.getString(indexNgayXuat));
            if (indexSoTrang != -1) txtSoTrang.setText(String.valueOf(cursor.getInt(indexSoTrang)));
            if (indexNoiDung != -1) txtNoiDung.setText(cursor.getString(indexNoiDung));

            if (indexAnh != -1) {
                String imgPath = cursor.getString(indexAnh);
                if (imgPath != null && !imgPath.isEmpty()) {
                    imgBook.setImageURI(Uri.parse(imgPath));
                }
            }

            // Load dữ liệu vào Spinner
            loadDanhSachSpinner();

            // Chọn giá trị đúng cho Spinner
            if (indexTacGia != -1) setSpinnerValue(spnTacGia, cursor.getString(indexTacGia));
            if (indexNXB != -1) setSpinnerValue(spnNXB, cursor.getString(indexNXB));
            if (indexTheLoai != -1) setSpinnerValue(spnTheLoai, cursor.getString(indexTheLoai));
            if (indexViTri != -1) setSpinnerValue(spnViTri, cursor.getString(indexViTri));

        } else {
            Toast.makeText(this, "Không tìm thấy dữ liệu!", Toast.LENGTH_SHORT).show();
        }

        cursor.close();
    }

    private void loadDanhSachSpinner() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // Load danh sách tác giả
        List<String> tacGiaList = new ArrayList<>();
        Cursor cursor = db.rawQuery("SELECT tentg FROM tacgia", null);
        while (cursor.moveToNext()) {
            tacGiaList.add(cursor.getString(0));
        }
        cursor.close();
        setSpinnerAdapter(spnTacGia, tacGiaList);

        // Load danh sách NXB
        List<String> nxbList = new ArrayList<>();
        cursor = db.rawQuery("SELECT tennxb FROM nhaxuatban", null);
        while (cursor.moveToNext()) {
            nxbList.add(cursor.getString(0));
        }
        cursor.close();
        setSpinnerAdapter(spnNXB, nxbList);

        // Load danh sách thể loại
        List<String> theLoaiList = new ArrayList<>();
        cursor = db.rawQuery("SELECT tentl FROM theloai", null);
        while (cursor.moveToNext()) {
            theLoaiList.add(cursor.getString(0));
        }
        cursor.close();
        setSpinnerAdapter(spnTheLoai, theLoaiList);

        // Load danh sách vị trí
        List<String> viTriList = new ArrayList<>();
        cursor = db.rawQuery("SELECT tenvt FROM vitri", null);
        while (cursor.moveToNext()) {
            viTriList.add(cursor.getString(0));
        }
        cursor.close();
        setSpinnerAdapter(spnViTri, viTriList);
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

    private void setSpinnerAdapter(Spinner spinner, List<String> data) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, data);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    private void setSpinnerValue(Spinner spinner, String value) {
        ArrayAdapter<String> adapter = (ArrayAdapter<String>) spinner.getAdapter();
        if (adapter != null) {
            int position = adapter.getPosition(value);
            if (position != -1) {
                spinner.setSelection(position);
            }
        }
    }


    private void updateDauSach() {
        String tenSachMoi = txtTenSach.getText().toString().trim();
        String ngayXuatBan = txtNgayXuat.getText().toString().trim();
        String soTrangStr = txtSoTrang.getText().toString().trim();
        String noiDung = txtNoiDung.getText().toString().trim();
        String anhDaiDien = selectedImageUri != null ? selectedImageUri.toString() : "";

        // Lấy giá trị từ Spinner
        String tacGia = spnTacGia.getSelectedItem().toString();
        String nxb = spnNXB.getSelectedItem().toString();
        String theLoai = spnTheLoai.getSelectedItem().toString();
        String viTri = spnViTri.getSelectedItem().toString();

        // Kiểm tra nhập liệu
        if (tenSachMoi.isEmpty() || ngayXuatBan.isEmpty() || soTrangStr.isEmpty() || noiDung.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
            return;
        }

        int soTrang;
        try {
            soTrang = Integer.parseInt(soTrangStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Số trang không hợp lệ!", Toast.LENGTH_SHORT).show();
            return;
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Kiểm tra trùng tên sách (nếu đổi tên)
        Cursor cursor = db.rawQuery("SELECT * FROM dausach WHERE tends = ? AND tends != ?", new String[]{tenSachMoi, tenSachCu});
        if (cursor.getCount() > 0) {
            Toast.makeText(this, "Tên sách đã tồn tại!", Toast.LENGTH_SHORT).show();
            cursor.close();
            db.close();
            return;
        }
        cursor.close();

        // Cập nhật dữ liệu
        ContentValues values = new ContentValues();
        values.put("anhds", anhDaiDien);
        values.put("tends", tenSachMoi);
        values.put("ngayxb", ngayXuatBan);
        values.put("sotrang", soTrang);
        values.put("noidung", noiDung);
        values.put("tentg", tacGia);
        values.put("tennxb", nxb);
        values.put("tentl", theLoai);
        values.put("tenvt", viTri);

        int rowsAffected = db.update("dausach", values, "tends = ?", new String[]{tenSachCu});
        db.close();

        if (rowsAffected > 0) {
            Toast.makeText(this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Lỗi khi cập nhật!", Toast.LENGTH_SHORT).show();
        }
    }

}
