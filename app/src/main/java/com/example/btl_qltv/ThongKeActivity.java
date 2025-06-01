package com.example.btl_qltv;

import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class ThongKeActivity extends Fragment {
    private DatabaseHelper dbHelper;
    private LinearLayout layoutThongKe;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.tt_thongke, container, false);

        layoutThongKe = view.findViewById(R.id.layout_thongke);
        dbHelper = new DatabaseHelper(getContext());

        loadThongKe();

        return view;
    }

    private void loadThongKe() {
        layoutThongKe.removeAllViews();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // Thống kê từng loại
        addThongKeItem("Tài khoản", "Tổng tài khoản: " + getCount(db, "user"));
        addThongKeItem("Thể loại", "Tổng thể loại: " + getCount(db, "theloai"));
        addThongKeItem("Tác giả", "Tổng tác giả: " + getCount(db, "tacgia"));
        addThongKeItem("Nhà xuất bản", "Tổng nhà xuất bản: " + getCount(db, "nhaxuatban"));
        addThongKeItem("Vị trí", "Tổng vị trí: " + getCount(db, "vitri"));
        addThongKeItem("Đầu sách", "Tổng đầu sách: " + getCount(db, "dausach"));

        // Tổng số lượng sách từ bảng tinhtrangsach
        int tongSoLuongSach = 0;
        Cursor cursorSoLuong = db.rawQuery("SELECT SUM(soluongsach) FROM tinhtrangsach", null);
        if (cursorSoLuong.moveToFirst() && !cursorSoLuong.isNull(0)) {
            tongSoLuongSach = cursorSoLuong.getInt(0);
        }
        cursorSoLuong.close();
        addThongKeItem("Số lượng sách", "Tổng số lượng sách: " + tongSoLuongSach);

        // Sách đang mượn
        int dangMuon = getCountWithCondition(db, "muontra", "trangthai = 'Đang mượn'");
        addThongKeItem("Sách đang mượn", "Sách đang mượn: " + dangMuon);

        // Sách đã trả
        int daTra = getCountWithCondition(db, "muontra", "trangthai = 'Đã trả'");
        addThongKeItem("Sách đã trả", "Sách đã trả: " + daTra);

        db.close();
    }

    private void addThongKeItem(String title, String content) {
        View itemView = LayoutInflater.from(getContext()).inflate(R.layout.item_thongke, null);

        TextView txtTitle = itemView.findViewById(R.id.txt_title);
        TextView txtTong = itemView.findViewById(R.id.txt_tong);
        TextView txtChiTiet = itemView.findViewById(R.id.txt_chitiet);

        txtTitle.setText(title);
        txtTong.setText(content);
        txtChiTiet.setVisibility(View.GONE); // Ẩn nếu chưa dùng "Xem chi tiết"

        layoutThongKe.addView(itemView);
    }

    private int getCount(SQLiteDatabase db, String tableName) {
        return (int) DatabaseUtils.queryNumEntries(db, tableName);
    }

    private int getCountWithCondition(SQLiteDatabase db, String table, String condition) {
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + table + " WHERE " + condition, null);
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count;
    }
}
