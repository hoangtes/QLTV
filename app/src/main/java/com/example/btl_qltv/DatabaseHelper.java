package com.example.btl_qltv;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "LibraryDB.db";
    private static final int DATABASE_VERSION = 6; // Cập nhật version để chạy onUpgrade()

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Bảng user
        String createUserTable = "CREATE TABLE user (" +
                "hoten TEXT NOT NULL, " +
                "taikhoan TEXT PRIMARY KEY, " +
                "matkhau TEXT NOT NULL, " +
                "chucvu TEXT NOT NULL)";
        db.execSQL(createUserTable);

        // Thêm dữ liệu mặc định cho bảng user
        String insertAdmin = "INSERT INTO user (hoten, taikhoan, matkhau, chucvu) " +
                "VALUES ('admin', 'admin', 'admin', 'Thủ thư')";
        db.execSQL(insertAdmin);

        // Bảng theloai
        String createTheLoaiTable = "CREATE TABLE theloai (" +
                "idtl INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "tentl TEXT NOT NULL)";
        db.execSQL(createTheLoaiTable);

        // Bảng tacgia
        String createTacGiaTable = "CREATE TABLE tacgia (" +
                "idtg INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "tentg TEXT NOT NULL)";
        db.execSQL(createTacGiaTable);

        // Bảng nhaxuatban
        String createNhaXuatBanTable = "CREATE TABLE nhaxuatban (" +
                "idnxb INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "tennxb TEXT NOT NULL)";
        db.execSQL(createNhaXuatBanTable);

        // Bảng vitri
        String createViTriTable = "CREATE TABLE vitri (" +
                "idvt INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "tenvt TEXT NOT NULL)";
        db.execSQL(createViTriTable);

        // Bảng dausach
        String createDauSachTable = "CREATE TABLE dausach (" +
                "idds INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "anhds TEXT, " +
                "tends TEXT NOT NULL, " +
                "tentg TEXT NOT NULL, " +
                "tennxb TEXT NOT NULL, " +
                "tentl TEXT NOT NULL, " +
                "tenvt TEXT NOT NULL, " +
                "ngayxb TEXT, " +
                "sotrang INTEGER, " +
                "noidung TEXT, " +
                "sosaodg REAL, " +
                "soluongsach INTEGER)";
        db.execSQL(createDauSachTable);

        // Bảng danhgia
        // Bảng danhgia
        String createDanhGiaTable = "CREATE TABLE danhgia (" +
                "iddg INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "sosaodg REAL NOT NULL, " +
                "taikhoan TEXT NOT NULL, " +
                "hoten TEXT NOT NULL, " +
                "ngaydg TEXT, " +
                "noidung TEXT, " +
                "tends TEXT NOT NULL, " +
                "FOREIGN KEY(taikhoan) REFERENCES user(taikhoan), " +
                "FOREIGN KEY(tends) REFERENCES dausach(tends) ON DELETE CASCADE)";
        db.execSQL(createDanhGiaTable);

        // Bảng tinhtrangsach (Mới)
        String createTinhTrangSachTable = "CREATE TABLE tinhtrangsach (" +
                "idtt INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "tends TEXT NOT NULL, " +
                "trangthai TEXT, " +
                "tinhtrang TEXT, " +
                "soluongsach INTEGER NOT NULL, " +
                "FOREIGN KEY(tends) REFERENCES dausach(tends))";
        db.execSQL(createTinhTrangSachTable);

        String createMuonTraTable = "CREATE TABLE muontra (" +
                "idmt INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "taikhoan TEXT NOT NULL, " +
                "idtt INTEGER NOT NULL, " +
                "tensach TEXT NOT NULL, " +
                "ngaymuon TEXT NOT NULL, " +
                "ngaytra TEXT, " +
                "trangthai TEXT, " +
                "FOREIGN KEY (taikhoan) REFERENCES user(taikhoan), " +
                "FOREIGN KEY (idtt) REFERENCES tinhtrangsach(idtt))";
        db.execSQL(createMuonTraTable);

        String createYeuCauMuonTable = "CREATE TABLE yeucau_muon (" +
                "idyc INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "taikhoan TEXT NOT NULL, " +
                "hoten TEXT NOT NULL, " +
                "idtt INTEGER NOT NULL, " +
                "tensach TEXT NOT NULL, " +
                "ngayyeucau TEXT NOT NULL, " +
                "trangthai TEXT, " +
                "FOREIGN KEY (taikhoan) REFERENCES user(taikhoan), " +
                "FOREIGN KEY (idtt) REFERENCES tinhtrangsach(idtt))";
        db.execSQL(createYeuCauMuonTable);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS user");
        db.execSQL("DROP TABLE IF EXISTS dausach");
        db.execSQL("DROP TABLE IF EXISTS theloai");
        db.execSQL("DROP TABLE IF EXISTS tacgia");
        db.execSQL("DROP TABLE IF EXISTS nhaxuatban");
        db.execSQL("DROP TABLE IF EXISTS vitri");
        db.execSQL("DROP TABLE IF EXISTS danhgia");
        db.execSQL("DROP TABLE IF EXISTS tinhtrangsach");
        db.execSQL("DROP TABLE IF EXISTS muontra");
        db.execSQL("DROP TABLE IF EXISTS yeucau_muon");
        onCreate(db);
    }
}
