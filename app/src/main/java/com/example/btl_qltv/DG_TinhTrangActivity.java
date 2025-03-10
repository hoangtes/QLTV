package com.example.btl_qltv;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;


public class DG_TinhTrangActivity extends AppCompatActivity {
    private LinearLayout layoutTinhTrang;
    private ImageButton imgBack;
    private DatabaseHelper dbHelper;
    private String tends;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dg_tinhtrang);

        layoutTinhTrang = findViewById(R.id.layout_tinhtrang);
        imgBack = findViewById(R.id.img_back);
        dbHelper = new DatabaseHelper(this);

        // Lấy tên sách từ Intent
        tends = getIntent().getStringExtra("tends");


        loadTinhTrangSach();

        // Gán sự kiện cho nút imgBack
        imgBack.setOnClickListener(v -> {
            finish();
        });

    }

    // Hiển thị danh sách tình trạng sách
    private void loadTinhTrangSach() {
        layoutTinhTrang.removeAllViews(); // Xóa dữ liệu cũ trước khi load lại

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM tinhtrangsach WHERE tends = ?", new String[]{tends});

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    int indexTends = cursor.getColumnIndex("tends");
                    int indexId = cursor.getColumnIndex("idtt");
                    int indexTrangThai = cursor.getColumnIndex("trangthai");
                    int indexTinhTrang = cursor.getColumnIndex("tinhtrang");

                    View view = getLayoutInflater().inflate(R.layout.item_tinhtrang_dg, null);

                    TextView txtTenSach = view.findViewById(R.id.txt_tensach);
                    TextView txtId = view.findViewById(R.id.txt_idtt);
                    TextView txtTrangThai = view.findViewById(R.id.txt_trangthai);
                    TextView txtTinhTrang = view.findViewById(R.id.txt_tinhtrang);

                    int idSach = cursor.getInt(indexId);
                    String trangThai = cursor.getString(indexTrangThai);
                    String tinhTrang = cursor.getString(indexTinhTrang);

                    if (indexTends != -1) txtTenSach.setText(cursor.getString(indexTends));
                    if (indexId != -1) txtId.setText("ID: " + idSach);
                    if (indexTrangThai != -1) txtTrangThai.setText(trangThai);
                    if (indexTinhTrang != -1) txtTinhTrang.setText(tinhTrang);

                    layoutTinhTrang.addView(view);
                } while (cursor.moveToNext());
            } else {
                Toast.makeText(this, "Không có sách nào trong danh sách!", Toast.LENGTH_SHORT).show();
            }
            cursor.close();
        }
        db.close();
    }

}
