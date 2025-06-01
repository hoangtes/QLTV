package com.example.btl_qltv;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AlertDialog;


public class Ls_MuonActivity extends Fragment {
    private DatabaseHelper dbHelper;
    private LinearLayout layoutLsMuonTra;
    private EditText txt_timkiem;
    private ImageButton ic_timkiem, ic_tailai, ic_boloc;
    private String taikhoan;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.ls_muontra, container, false);

        layoutLsMuonTra = view.findViewById(R.id.layout_ls_muontra);
        txt_timkiem = view.findViewById(R.id.txt_timkiem);
        ic_timkiem = view.findViewById(R.id.ic_timkiem);
        ic_tailai = view.findViewById(R.id.ic_tailai);
        ic_boloc = view.findViewById(R.id.ic_boloc);

        dbHelper = new DatabaseHelper(getContext());
        taikhoan = requireActivity().getIntent().getStringExtra("taikhoan");

        loadDanhSachMuon();

        ic_timkiem.setOnClickListener(v -> {
            String keyword = txt_timkiem.getText().toString().trim().toLowerCase();
            searchMuon(keyword);
        });

        ic_tailai.setOnClickListener(v -> {
            txt_timkiem.setText("");
            loadDanhSachMuon();
        });

        ic_boloc.setOnClickListener(v -> {
            final String[] trangThaiOptions = {"Đã trả", "Đang mượn", "Đang phê duyệt trả sách"};

            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            builder.setTitle("Lọc theo trạng thái");

            builder.setItems(trangThaiOptions, (dialog, which) -> {
                String trangThaiDaChon = trangThaiOptions[which];
                loadTheoTrangThai(trangThaiDaChon);
            });

            builder.show();
        });

        return view;
    }

    private void loadDanhSachMuon() {
        layoutLsMuonTra.removeAllViews();

        Cursor cursor = dbHelper.getReadableDatabase().rawQuery(
                "SELECT * FROM muontra WHERE taikhoan = ?", new String[]{taikhoan});

        if (cursor.moveToFirst()) {
            do {
                renderItem(cursor);
            } while (cursor.moveToNext());
        }

        cursor.close();
    }

    private void searchMuon(String keyword) {
        layoutLsMuonTra.removeAllViews();

        // Làm sạch từ khóa: loại bỏ khoảng trắng đầu/cuối
        keyword = keyword.trim().toLowerCase();

        Cursor cursor = dbHelper.getReadableDatabase().rawQuery(
                "SELECT * FROM muontra WHERE taikhoan = ? AND (" +
                        "CAST(idmt AS TEXT) LIKE ? OR " +
                        "CAST(idtt AS TEXT) LIKE ? OR " +
                        "LOWER(tensach) LIKE ?)",
                new String[]{
                        taikhoan,
                        "%" + keyword + "%",
                        "%" + keyword + "%",
                        "%" + keyword + "%"
                }
        );

        if (cursor.moveToFirst()) {
            do {
                renderItem(cursor);
            } while (cursor.moveToNext());
        }

        cursor.close();
    }

    private void renderItem(Cursor cursor) {
        int idmt = cursor.getInt(cursor.getColumnIndexOrThrow("idmt"));
        int idtt = cursor.getInt(cursor.getColumnIndexOrThrow("idtt"));
        String tensach = cursor.getString(cursor.getColumnIndexOrThrow("tensach"));
        String ngaymuon = cursor.getString(cursor.getColumnIndexOrThrow("ngaymuon"));
        String ngaytra = cursor.getString(cursor.getColumnIndexOrThrow("ngaytra"));
        String trangthai = cursor.getString(cursor.getColumnIndexOrThrow("trangthai"));

        View itemView = LayoutInflater.from(getContext()).inflate(R.layout.item_ls_muontra, null);

        ((TextView) itemView.findViewById(R.id.txt_id_ls_muon)).setText("ID mượn: " + idmt);
        ((TextView) itemView.findViewById(R.id.txt_tensach)).setText("Tên sách: " + tensach);
        ((TextView) itemView.findViewById(R.id.txt_idtt)).setText("ID sách: " + idtt);
        ((TextView) itemView.findViewById(R.id.txt_ngaymuon)).setText("Ngày mượn: " + ngaymuon);

        TextView txtNgayTra = itemView.findViewById(R.id.txt_ngaytra);
        txtNgayTra.setText("Ngày trả: " + (ngaytra != null ? ngaytra : ""));

        ((TextView) itemView.findViewById(R.id.txt_trangthai)).setText("Trạng thái: " + trangthai);

        layoutLsMuonTra.addView(itemView);
    }

    private void loadTheoTrangThai(String trangThai) {
        layoutLsMuonTra.removeAllViews();  // hoặc layout bạn đang dùng

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT * FROM muontra WHERE taikhoan = ? AND trangthai = ?",
                new String[]{taikhoan, trangThai}
        );

        if (cursor.moveToFirst()) {
            do {
                renderItem(cursor);
            } while (cursor.moveToNext());
        } else {
            Toast.makeText(getContext(), "Không có kết quả phù hợp!", Toast.LENGTH_SHORT).show();
        }

        cursor.close();
        db.close();
    }


    @Override
    public void onResume() {
        super.onResume();
        loadDanhSachMuon(); // Load lại danh sách khi quay lại màn hình
    }
}
