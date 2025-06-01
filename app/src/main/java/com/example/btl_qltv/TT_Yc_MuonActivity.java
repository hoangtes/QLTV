package com.example.btl_qltv;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AlertDialog;


public class TT_Yc_MuonActivity extends Fragment {
    private DatabaseHelper dbHelper;
    private LinearLayout layoutYeuCauMuon;
    private EditText txt_timkiem;
    private ImageButton ic_timkiem, ic_tailai, ic_boloc;
    private String taikhoan;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.tt_yc_muon, container, false);

        layoutYeuCauMuon = view.findViewById(R.id.layout_yc_muon);
        txt_timkiem = view.findViewById(R.id.txt_timkiem);
        ic_timkiem = view.findViewById(R.id.ic_timkiem);
        ic_tailai = view.findViewById(R.id.ic_tailai);
        ic_boloc = view.findViewById(R.id.ic_boloc);

        dbHelper = new DatabaseHelper(getContext());
        taikhoan = requireActivity().getIntent().getStringExtra("taikhoan");

        loadYeuCauMuon(); // Tải yêu cầu mượn từ CSDL khi vào trang

        ic_timkiem.setOnClickListener(v -> {
            String keyword = txt_timkiem.getText().toString().trim().toLowerCase();
            loadYeuCauMuonWithSearch(keyword); // Tìm kiếm yêu cầu mượn
        });

        ic_tailai.setOnClickListener(v -> {
            txt_timkiem.setText("");
            loadYeuCauMuon();
        });

        ic_boloc.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle("Chọn cách sắp xếp");

            String[] options = {"Ngày yêu cầu (Tăng dần)", "Ngày yêu cầu (Giảm dần)"};
            builder.setItems(options, (dialog, which) -> {
                if (which == 0) {
                    loadYeuCauMuonSapXep("ASC");
                } else if (which == 1) {
                    loadYeuCauMuonSapXep("DESC");
                }
            });

            builder.show();
        });


        return view;
    }

    private void loadYeuCauMuon() {
        layoutYeuCauMuon.removeAllViews();
        Cursor cursor = dbHelper.getReadableDatabase().rawQuery(
                "SELECT * FROM yeucau_muon WHERE trangthai = ?", new String[]{"Chờ xử lý"});

        if (cursor.moveToFirst()) {
            do {
                int idyc = cursor.getInt(cursor.getColumnIndexOrThrow("idyc"));
                int idtt = cursor.getInt(cursor.getColumnIndexOrThrow("idtt"));
                String tensach = cursor.getString(cursor.getColumnIndexOrThrow("tensach"));
                String hoten = cursor.getString(cursor.getColumnIndexOrThrow("hoten"));
                String ngayyeucau = cursor.getString(cursor.getColumnIndexOrThrow("ngayyeucau"));
                String trangthai = cursor.getString(cursor.getColumnIndexOrThrow("trangthai"));
                String taikhoanNguoiMuon = cursor.getString(cursor.getColumnIndexOrThrow("taikhoan"));

                renderYeuCauMuon(idyc, idtt, tensach, hoten, ngayyeucau, trangthai, taikhoanNguoiMuon);
            } while (cursor.moveToNext());
        }
        cursor.close();
    }



    private void loadYeuCauMuonWithSearch(String keyword) {
        layoutYeuCauMuon.removeAllViews();

        String sql = "SELECT * FROM yeucau_muon WHERE trangthai = ? AND (" +
                "CAST(idyc AS TEXT) LIKE ? OR " +
                "CAST(idtt AS TEXT) LIKE ? OR " +
                "LOWER(REPLACE(tensach, ' ', '')) LIKE ? OR " +
                "LOWER(REPLACE(hoten, ' ', '')) LIKE ?)";

        String likeKeyword = "%" + keyword.toLowerCase().replaceAll("\\s+", "") + "%";

        Cursor cursor = dbHelper.getReadableDatabase().rawQuery(
                sql,
                new String[]{"Chờ xử lý", likeKeyword, likeKeyword, likeKeyword, likeKeyword}
        );

        if (cursor.moveToFirst()) {
            do {
                int idyc = cursor.getInt(cursor.getColumnIndexOrThrow("idyc"));
                int idtt = cursor.getInt(cursor.getColumnIndexOrThrow("idtt"));
                String tensach = cursor.getString(cursor.getColumnIndexOrThrow("tensach"));
                String hoten = cursor.getString(cursor.getColumnIndexOrThrow("hoten"));
                String ngayyeucau = cursor.getString(cursor.getColumnIndexOrThrow("ngayyeucau"));
                String trangthai = cursor.getString(cursor.getColumnIndexOrThrow("trangthai"));
                String taikhoanNguoiMuon = cursor.getString(cursor.getColumnIndexOrThrow("taikhoan"));

                renderYeuCauMuon(idyc, idtt, tensach, hoten, ngayyeucau, trangthai, taikhoanNguoiMuon);
            } while (cursor.moveToNext());
        }
        cursor.close();
    }


    private void renderYeuCauMuon(int idyc, int idtt, String tensach, String hoten, String ngayyeucau, String trangthai, String taikhoanNguoiMuon) {
        View itemView = LayoutInflater.from(getContext()).inflate(R.layout.item_yc_muon_tt, null);
        TextView txtIdYc = itemView.findViewById(R.id.txt_idyc);
        TextView txtTensach = itemView.findViewById(R.id.txt_tensach);
        TextView txtIdtt = itemView.findViewById(R.id.txt_idtt);
        TextView txtHoten = itemView.findViewById(R.id.txt_hoten);
        TextView txtNgayYc = itemView.findViewById(R.id.txt_ngay_yc);
        TextView txtTrangThai = itemView.findViewById(R.id.txt_trangthai);
        Button btnXacNhan = itemView.findViewById(R.id.btn_xacnhan);
        Button btnTuChoi = itemView.findViewById(R.id.btn_tuchoi);

        txtIdYc.setText("ID yêu cầu mượn: " + idyc);
        txtTensach.setText("Tên sách: " + tensach);
        txtIdtt.setText("ID sách: " + idtt);
        txtHoten.setText("Người mượn: " + hoten);
        txtNgayYc.setText("Ngày yêu cầu mượn: " + ngayyeucau);
        txtTrangThai.setText(trangthai != null ? "Trạng thái: " + trangthai : "Chờ xử lý");

        if ("Đã duyệt".equalsIgnoreCase(trangthai)) {
            btnTuChoi.setVisibility(View.GONE);
            btnXacNhan.setText("Đã duyệt");
            btnXacNhan.setBackgroundColor(getResources().getColor(R.color.gray));
            btnXacNhan.setEnabled(false);
        }

        btnXacNhan.setOnClickListener(v -> {
            updateTrangThai(idyc, "Đã duyệt");
            updateTinhTrangSach(idtt, "Đã cho mượn");

            themVaoMuonTra(taikhoanNguoiMuon, idtt, tensach); // truyền đúng người gửi yêu cầu

            xoaYeuCauKhac(idyc, idtt);
            loadYeuCauMuon();
        });

        btnTuChoi.setOnClickListener(v -> {
            int rows = dbHelper.getWritableDatabase().delete(
                    "yeucau_muon",
                    "idyc = ?",
                    new String[]{String.valueOf(idyc)}
            );

            if (rows > 0) {
                Toast.makeText(getContext(), "Đã từ chối thành công!", Toast.LENGTH_SHORT).show();
                loadYeuCauMuon();
            } else {
                Toast.makeText(getContext(), "Từ chối thất bại!", Toast.LENGTH_SHORT).show();
            }
        });

        layoutYeuCauMuon.addView(itemView);
    }


    private void themVaoMuonTra(String taikhoan, int idtt, String tensach) {
        ContentValues values = new ContentValues();
        values.put("taikhoan", taikhoan);
        values.put("idtt", idtt);
        values.put("tensach", tensach);

        String ngaymuon = new java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault()).format(new java.util.Date());
        values.put("ngaymuon", ngaymuon);
        values.put("trangthai", "Đang mượn");

        long result = dbHelper.getWritableDatabase().insert("muontra", null, values);
        if (result != -1) {
            Log.d("DEBUG", "Thêm vào bảng muontra thành công");
        } else {
            Log.e("DEBUG", "Thêm vào bảng muontra thất bại");
        }
    }


    private void updateTrangThai(int idyc, String newTrangThai) {
        ContentValues values = new ContentValues();
        values.put("trangthai", newTrangThai);

        int rows = dbHelper.getWritableDatabase().update("yeucau_muon", values, "idyc = ?", new String[]{String.valueOf(idyc)});
        if (rows > 0) {
            Toast.makeText(getContext(), newTrangThai + " thành công", Toast.LENGTH_SHORT).show();
            loadYeuCauMuon(); // Tải lại yêu cầu mượn sau khi cập nhật trạng thái
        } else {
            Toast.makeText(getContext(), "Cập nhật thất bại", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateTinhTrangSach(int idtt, String newTrangThai) {
        ContentValues values = new ContentValues();
        values.put("trangthai", newTrangThai);

        int rows = dbHelper.getWritableDatabase().update("tinhtrangsach", values, "idtt = ?", new String[]{String.valueOf(idtt)});
        if (rows > 0) {
            Log.d("DEBUG", "Cập nhật trạng thái sách thành công: " + newTrangThai);
        } else {
            Log.e("DEBUG", "Cập nhật trạng thái sách thất bại");
        }
    }

    private void xoaYeuCauKhac(int idycDuocDuyet, int idtt) {
        int rowsDeleted = dbHelper.getWritableDatabase().delete(
                "yeucau_muon",
                "idtt = ? AND idyc != ?",
                new String[]{String.valueOf(idtt), String.valueOf(idycDuocDuyet)}
        );

        Log.d("DEBUG", "Đã xoá " + rowsDeleted + " yêu cầu khác với idtt = " + idtt);
    }

    private void loadYeuCauMuonSapXep(String order) {
        layoutYeuCauMuon.removeAllViews();

        String sql = "SELECT * FROM yeucau_muon " +
                "WHERE trangthai = ? " +
                "ORDER BY ngayyeucau " + order;

        Cursor cursor = dbHelper.getReadableDatabase().rawQuery(
                sql, new String[]{"Chờ xử lý"}
        );

        if (cursor.moveToFirst()) {
            do {
                int idyc = cursor.getInt(cursor.getColumnIndexOrThrow("idyc"));
                int idtt = cursor.getInt(cursor.getColumnIndexOrThrow("idtt"));
                String tensach = cursor.getString(cursor.getColumnIndexOrThrow("tensach"));
                String hoten = cursor.getString(cursor.getColumnIndexOrThrow("hoten"));
                String ngayyeucau = cursor.getString(cursor.getColumnIndexOrThrow("ngayyeucau"));
                String trangthai = cursor.getString(cursor.getColumnIndexOrThrow("trangthai"));
                String taikhoanNguoiMuon = cursor.getString(cursor.getColumnIndexOrThrow("taikhoan"));

                renderYeuCauMuon(idyc, idtt, tensach, hoten, ngayyeucau, trangthai, taikhoanNguoiMuon);
            } while (cursor.moveToNext());
        }

        cursor.close();
    }



    @Override
    public void onResume() {
        super.onResume();
        loadYeuCauMuon(); // Load lại danh sách khi quay lại màn hình
    }
}
