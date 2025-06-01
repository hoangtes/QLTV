package com.example.btl_qltv;

import android.content.ContentValues;
import android.database.Cursor;
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
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AlertDialog;

import java.util.ArrayList;
import java.util.List;

public class TraSachActivity extends Fragment {
    private LinearLayout layoutTraSach;
    private EditText txt_timkiem;
    private ImageButton ic_timkiem, ic_tailai, ic_boloc;
    private DatabaseHelper dbHelper;
    private String taikhoan;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.trasach, container, false);

        layoutTraSach = view.findViewById(R.id.layout_trasach);
        txt_timkiem = view.findViewById(R.id.txt_timkiem);
        ic_timkiem = view.findViewById(R.id.ic_timkiem);
        ic_tailai = view.findViewById(R.id.ic_tailai);
        ic_boloc = view.findViewById(R.id.ic_boloc);

        dbHelper = new DatabaseHelper(getContext());
        taikhoan = requireActivity().getIntent().getStringExtra("taikhoan");

        loadDanhSachMuon();

        ic_timkiem.setOnClickListener(v -> {
            String keyword = txt_timkiem.getText().toString().trim();
            loadDanhSachMuonWithSearch(keyword);
        });

        ic_tailai.setOnClickListener(v -> {
            txt_timkiem.setText("");
            loadDanhSachMuon();
        });

        ic_boloc.setOnClickListener(v -> {
            String[] options = {"Đang mượn", "Đang phê duyệt trả sách"};

            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle("Chọn trạng thái");

            builder.setItems(options, (dialog, which) -> {
                String selectedStatus = options[which];
                loadDanhSachMuonWithFilter(selectedStatus);
            });

            builder.show();
        });

        return view;
    }

    private void loadDanhSachMuon() {
        layoutTraSach.removeAllViews();
        Cursor cursor = dbHelper.getReadableDatabase().rawQuery(
                "SELECT * FROM muontra WHERE taikhoan = ?", new String[]{taikhoan});

        if (cursor.moveToFirst()) {
            do {
                int idmt = cursor.getInt(cursor.getColumnIndexOrThrow("idmt"));
                String tensach = cursor.getString(cursor.getColumnIndexOrThrow("tensach"));
                int idtt = cursor.getInt(cursor.getColumnIndexOrThrow("idtt"));
                String trangthai = cursor.getString(cursor.getColumnIndexOrThrow("trangthai"));

                if (trangthai != null && (trangthai.equals("Đang mượn") || trangthai.equals("Đang phê duyệt trả sách"))) {
                    renderItem(idmt, tensach, idtt, trangthai);
                }

            } while (cursor.moveToNext());
        }
        cursor.close();
    }

    private void loadDanhSachMuonWithSearch(String keyword) {
        layoutTraSach.removeAllViews();

        // Xử lý keyword: chuyển thành lowercase, xóa khoảng trắng thừa
        keyword = keyword.toLowerCase().trim().replaceAll("\\s+", " ");
        String likeKeyword = "%" + keyword + "%";

        String sql = "SELECT * FROM muontra WHERE taikhoan = ? AND (" +
                "LOWER(CAST(idmt AS TEXT)) LIKE ? OR " +
                "LOWER(CAST(idtt AS TEXT)) LIKE ? OR " +
                "LOWER(tensach) LIKE ?)";

        Cursor cursor = dbHelper.getReadableDatabase().rawQuery(
                sql,
                new String[]{taikhoan, likeKeyword, likeKeyword, likeKeyword}
        );

        if (cursor.moveToFirst()) {
            do {
                int idmt = cursor.getInt(cursor.getColumnIndexOrThrow("idmt"));
                String tensach = cursor.getString(cursor.getColumnIndexOrThrow("tensach"));
                int idtt = cursor.getInt(cursor.getColumnIndexOrThrow("idtt"));
                String trangthai = cursor.getString(cursor.getColumnIndexOrThrow("trangthai"));

                if (trangthai != null && (trangthai.equals("Đang mượn") || trangthai.equals("Đang phê duyệt trả sách"))) {
                    renderItem(idmt, tensach, idtt, trangthai);
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
    }

    private void renderItem(int idmt, String tensach, int idtt, String trangthai) {
        View itemView = LayoutInflater.from(getContext()).inflate(R.layout.item_trasach, null);
        TextView txtIdMt = itemView.findViewById(R.id.txt_id_ls_muon);
        TextView txtTensach = itemView.findViewById(R.id.txt_tensach);
        TextView txtIdtt = itemView.findViewById(R.id.txt_idtt);
        TextView txtTrangthai = itemView.findViewById(R.id.txt_trangthai);
        Button btnTraSach = itemView.findViewById(R.id.btn_trasach);

        txtIdMt.setText("ID mượn: " + idmt);
        txtTensach.setText("Tên sách: " + tensach);
        txtIdtt.setText("ID sách: " + idtt);
        txtTrangthai.setText("Trạng thái: " + trangthai);

        // Nếu đã gửi yêu cầu trả
        if (trangthai.equals("Đang phê duyệt trả sách")) {
            btnTraSach.setEnabled(false);
            btnTraSach.setBackgroundColor(Color.GRAY);
            btnTraSach.setText("Đã gửi yêu cầu");
        }

        btnTraSach.setOnClickListener(v -> {
            // Cập nhật trạng thái mượn
            ContentValues values = new ContentValues();
            values.put("trangthai", "Đang phê duyệt trả sách");
            int rows = dbHelper.getWritableDatabase().update("muontra", values, "idmt = ?", new String[]{String.valueOf(idmt)});

            if (rows > 0) {
                Toast.makeText(getContext(), "Đã gửi yêu cầu trả sách", Toast.LENGTH_SHORT).show();
                btnTraSach.setEnabled(false);
                btnTraSach.setBackgroundColor(Color.GRAY);
                btnTraSach.setText("Đã gửi yêu cầu");
                txtTrangthai.setText("Trạng thái: Đang phê duyệt trả sách");
            } else {
                Toast.makeText(getContext(), "Gửi yêu cầu thất bại", Toast.LENGTH_SHORT).show();
            }
        });

        layoutTraSach.addView(itemView);
    }

    private void loadDanhSachMuonWithFilter(String trangThaiLoc) {
        layoutTraSach.removeAllViews();

        String sql = "SELECT * FROM muontra WHERE taikhoan = ? AND trangthai = ?";
        String[] args = new String[]{taikhoan, trangThaiLoc};

        Cursor cursor = dbHelper.getReadableDatabase().rawQuery(sql, args);

        if (cursor.moveToFirst()) {
            do {
                int idmt = cursor.getInt(cursor.getColumnIndexOrThrow("idmt"));
                String tensach = cursor.getString(cursor.getColumnIndexOrThrow("tensach"));
                int idtt = cursor.getInt(cursor.getColumnIndexOrThrow("idtt"));
                String trangthai = cursor.getString(cursor.getColumnIndexOrThrow("trangthai"));

                renderItem(idmt, tensach, idtt, trangthai);
            } while (cursor.moveToNext());
        }
        cursor.close();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadDanhSachMuon(); // Reload khi quay lại
    }
}
