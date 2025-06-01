package com.example.btl_qltv;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Date;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AlertDialog;

public class TT_Xn_TraSachActivity extends Fragment {

    private LinearLayout layoutXacNhanTra;
    private EditText txt_timkiem;
    private ImageButton ic_timkiem, ic_tailai, ic_boloc;
    private DatabaseHelper dbHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.tt_xacnhan_tra, container, false);

        layoutXacNhanTra = view.findViewById(R.id.layout_xacnhan_tra);
        txt_timkiem = view.findViewById(R.id.txt_timkiem);
        ic_timkiem = view.findViewById(R.id.ic_timkiem);
        ic_tailai = view.findViewById(R.id.ic_tailai);
        ic_boloc = view.findViewById(R.id.ic_boloc);

        dbHelper = new DatabaseHelper(getContext());

        loadDanhSachTra();

        ic_timkiem.setOnClickListener(v -> {
            String keyword = txt_timkiem.getText().toString().trim();
            loadDanhSachTraWithSearch(keyword);
        });

        ic_tailai.setOnClickListener(v -> {
            txt_timkiem.setText("");
            loadDanhSachTra();
        });

        ic_boloc.setOnClickListener(v -> {
            final String[] options = {
                    "ID mượn tăng dần",
                    "ID mượn giảm dần",
                    "ID sách tăng dần",
                    "ID sách giảm dần"
            };

            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            builder.setTitle("Sắp xếp theo");
            builder.setItems(options, (dialog, which) -> {
                String orderBy = "";

                switch (which) {
                    case 0: orderBy = "ORDER BY muontra.idmt ASC"; break;
                    case 1: orderBy = "ORDER BY muontra.idmt DESC"; break;
                    case 2: orderBy = "ORDER BY muontra.idtt ASC"; break;
                    case 3: orderBy = "ORDER BY muontra.idtt DESC"; break;
                }

                loadDanhSachTraWithOrder(orderBy);
            });
            builder.show();
        });

        return view;
    }

    private void loadDanhSachTra() {
        layoutXacNhanTra.removeAllViews();

        Cursor cursor = dbHelper.getReadableDatabase().rawQuery(
                "SELECT muontra.*, user.hoten FROM muontra JOIN user ON muontra.taikhoan = user.taikhoan WHERE muontra.trangthai = ?",
                new String[]{"Đang phê duyệt trả sách"}
        );

        if (cursor.moveToFirst()) {
            do {
                int idmt = cursor.getInt(cursor.getColumnIndexOrThrow("idmt"));
                String tensach = cursor.getString(cursor.getColumnIndexOrThrow("tensach"));
                int idtt = cursor.getInt(cursor.getColumnIndexOrThrow("idtt"));
                String hoten = cursor.getString(cursor.getColumnIndexOrThrow("hoten"));
                String trangthai = cursor.getString(cursor.getColumnIndexOrThrow("trangthai"));

                renderItem(idmt, tensach, idtt, hoten, trangthai);

            } while (cursor.moveToNext());
        }
        cursor.close();
    }

    private void loadDanhSachTraWithSearch(String keyword) {
        layoutXacNhanTra.removeAllViews();

        // Xử lý keyword: loại bỏ khoảng trắng thừa, chuyển chữ thường
        String processedKeyword = keyword.trim().replaceAll("\\s+", " ").toLowerCase();
        String likeKeyword = "%" + processedKeyword + "%";

        Cursor cursor = dbHelper.getReadableDatabase().rawQuery(
                "SELECT muontra.*, user.hoten FROM muontra JOIN user ON muontra.taikhoan = user.taikhoan " +
                        "WHERE muontra.trangthai = ? AND (" +
                        "LOWER(CAST(muontra.idmt AS TEXT)) LIKE ? OR " +
                        "LOWER(muontra.tensach) LIKE ? OR " +
                        "LOWER(CAST(muontra.idtt AS TEXT)) LIKE ? OR " +
                        "LOWER(user.hoten) LIKE ?" +
                        ")",
                new String[]{
                        "Đang phê duyệt trả sách",
                        likeKeyword,
                        likeKeyword,
                        likeKeyword,
                        likeKeyword
                }
        );

        if (cursor.moveToFirst()) {
            do {
                int idmt = cursor.getInt(cursor.getColumnIndexOrThrow("idmt"));
                String tensach = cursor.getString(cursor.getColumnIndexOrThrow("tensach"));
                int idtt = cursor.getInt(cursor.getColumnIndexOrThrow("idtt"));
                String hoten = cursor.getString(cursor.getColumnIndexOrThrow("hoten"));
                String trangthai = cursor.getString(cursor.getColumnIndexOrThrow("trangthai"));

                renderItem(idmt, tensach, idtt, hoten, trangthai);

            } while (cursor.moveToNext());
        }
        cursor.close();
    }


    private void renderItem(int idmt, String tensach, int idtt, String hoten, String trangthai) {
        View itemView = LayoutInflater.from(getContext()).inflate(R.layout.item_xacnhan_tra_tt, null);
        TextView txtIdmt = itemView.findViewById(R.id.txt_idmt);
        TextView txtTensach = itemView.findViewById(R.id.txt_tensach);
        TextView txtIdtt = itemView.findViewById(R.id.txt_idtt);
        TextView txtHoten = itemView.findViewById(R.id.txt_hoten);
        TextView txtTrangthai = itemView.findViewById(R.id.txt_trangthai);
        Button btnXacnhan = itemView.findViewById(R.id.btn_xacnhan);
        Button btnTuchoi = itemView.findViewById(R.id.btn_tuchoi);

        txtIdmt.setText("ID mượn: " + idmt);
        txtTensach.setText("Tên sách: " + tensach);
        txtIdtt.setText("ID sách: " + idtt);
        txtHoten.setText("Họ tên: " + hoten);
        txtTrangthai.setText("Trạng thái: " + trangthai);

        btnXacnhan.setOnClickListener(v -> {
            SQLiteDatabase db = dbHelper.getWritableDatabase();

            // Lấy ngày hiện tại theo định dạng dd/MM/yyyy
            String currentDate = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());

            // Cập nhật trạng thái và ngày trả
            ContentValues valuesMuonTra = new ContentValues();
            valuesMuonTra.put("trangthai", "Đã trả");
            valuesMuonTra.put("ngaytra", currentDate);

            int rowsMuonTra = db.update("muontra", valuesMuonTra, "idmt = ?", new String[]{String.valueOf(idmt)});

            if (rowsMuonTra > 0) {
                ContentValues valuesTinhTrang = new ContentValues();
                valuesTinhTrang.put("trangthai", "Chưa cho mượn");

                int rowsTinhTrang = db.update("tinhtrangsach", valuesTinhTrang, "idtt = ?", new String[]{String.valueOf(idtt)});

                if (rowsTinhTrang > 0) {
                    Toast.makeText(getContext(), "Đã xác nhận trả sách", Toast.LENGTH_SHORT).show();
                    btnXacnhan.setText("Đã phê duyệt");
                    btnXacnhan.setBackgroundColor(Color.GRAY);
                    btnXacnhan.setEnabled(false);
                    btnTuchoi.setVisibility(View.GONE);
                    loadDanhSachTra();
                } else {
                    Toast.makeText(getContext(), "Cập nhật trạng thái sách thất bại!", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getContext(), "Lỗi khi xác nhận", Toast.LENGTH_SHORT).show();
            }
        });

        btnTuchoi.setOnClickListener(v -> {
            ContentValues values = new ContentValues();
            values.put("trangthai", "Đang mượn");

            int rows = dbHelper.getWritableDatabase().update("muontra", values, "idmt = ?", new String[]{String.valueOf(idmt)});

            if (rows > 0) {
                Toast.makeText(getContext(), "Đã từ chối yêu cầu trả", Toast.LENGTH_SHORT).show();
                layoutXacNhanTra.removeView(itemView); // Ẩn item sau khi xử lý
            } else {
                Toast.makeText(getContext(), "Lỗi khi từ chối", Toast.LENGTH_SHORT).show();
            }
        });

        layoutXacNhanTra.addView(itemView);
    }

    private void loadDanhSachTraWithOrder(String orderBy) {
        layoutXacNhanTra.removeAllViews();

        String sql = "SELECT muontra.*, user.hoten FROM muontra JOIN user ON muontra.taikhoan = user.taikhoan " +
                "WHERE muontra.trangthai = ? " + orderBy;

        Cursor cursor = dbHelper.getReadableDatabase().rawQuery(sql, new String[]{"Đang phê duyệt trả sách"});

        if (cursor.moveToFirst()) {
            do {
                int idmt = cursor.getInt(cursor.getColumnIndexOrThrow("idmt"));
                String tensach = cursor.getString(cursor.getColumnIndexOrThrow("tensach"));
                int idtt = cursor.getInt(cursor.getColumnIndexOrThrow("idtt"));
                String hoten = cursor.getString(cursor.getColumnIndexOrThrow("hoten"));
                String trangthai = cursor.getString(cursor.getColumnIndexOrThrow("trangthai"));

                renderItem(idmt, tensach, idtt, hoten, trangthai);
            } while (cursor.moveToNext());
        }

        cursor.close();
    }



    @Override
    public void onResume() {
        super.onResume();
        loadDanhSachTra();
    }
}
