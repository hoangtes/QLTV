package com.example.btl_qltv;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.navigation.NavigationView;


public class TrangChuDGActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final int FRAGMENT_DS = 0;
    private static final int FRAGMENT_TK = 1;
    private static final int FRAGMENT_DMK = 2;

    private int m_CurrentFragment = FRAGMENT_DS;

    private DrawerLayout m_DrawerLayout;
    private DatabaseHelper dbHelper;
    private String taikhoan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.trangchu_dg);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.drawer_layout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Khởi tạo dbHelper
        dbHelper = new DatabaseHelper(this);

        taikhoan = getIntent().getStringExtra("taikhoan");

        // Kiểm tra nếu tài khoản không null thì mới load thông tin
        if (taikhoan != null) {
            loadThongTinNguoiDung(taikhoan);
        }

        // Gọi replaceFragment nếu cần
        replaceFragment(new DG_DauSachActivity(), taikhoan);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        m_DrawerLayout = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, m_DrawerLayout,
                toolbar, R.string.nav_drawer_open, R.string.nav_drawer_close);
        m_DrawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Xử lý sự kiện nút back
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (m_DrawerLayout.isDrawerOpen(GravityCompat.START)) {
                    m_DrawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    finish(); // Thoát Activity
                }
            }
        });


        navigationView.getMenu().findItem(R.id.nav_ds).setChecked(true);

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_ds) {
            if (m_CurrentFragment != FRAGMENT_DS) {
                replaceFragment(new DG_DauSachActivity(), taikhoan);
                m_CurrentFragment = FRAGMENT_DS;
            }
        } else if (id == R.id.nav_tt) {
            if (m_CurrentFragment != FRAGMENT_TK) {
                replaceFragment(new TaiKhoanActivity(), taikhoan);
                m_CurrentFragment = FRAGMENT_TK;
            }
        } else if (id == R.id.nav_doimk) {
            if (m_CurrentFragment != FRAGMENT_DMK) {
                replaceFragment(new DoiMkActivity(), taikhoan);
                m_CurrentFragment = FRAGMENT_DMK;
            }
        } else if (id == R.id.nav_dx) {
            // Xử lý đăng xuất
            Intent intent = new Intent(this, DangNhapActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }

        m_DrawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void loadThongTinNguoiDung(String taikhoan) {
        if (dbHelper == null) { // Kiểm tra lại dbHelper trước khi dùng
            Log.e("TrangChuTTActivity", "DatabaseHelper is null");
            return;
        }

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT hoten, chucvu FROM user WHERE taikhoan = ?", new String[]{taikhoan});

        if (cursor.moveToFirst()) {
            String hoTen = cursor.getString(0);
            String chucVu = cursor.getString(1);

            // Lấy header từ NavigationView
            NavigationView navigationView = findViewById(R.id.navigation_view);
            View headerView = navigationView.getHeaderView(0);

            // Ánh xạ TextView trong header
            TextView txtHoTen = headerView.findViewById(R.id.txt_hoten);
            TextView txtChucVu = headerView.findViewById(R.id.txt_chucvu);

            // Cập nhật dữ liệu
            txtHoTen.setText("Họ tên: " + hoTen);
            txtChucVu.setText("Chức vụ: " + chucVu);
        }

        cursor.close();
        db.close();
    }

    private void replaceFragment(Fragment fragment, String taikhoan) {
        Bundle bundle = new Bundle();
        bundle.putString("taikhoan", taikhoan);
        fragment.setArguments(bundle);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.content_frame, fragment);
        transaction.commit();
    }
}