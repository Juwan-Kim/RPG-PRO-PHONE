package com.example.rpg_pro_phone;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Map;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private static final String TAG = "MainActivity";
    private DrawerLayout drawerLayout;
    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    private DatabaseReference gameRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();

        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ImageView hamburgerIcon = findViewById(R.id.hamburger_icon);
        hamburgerIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    drawerLayout.openDrawer(GravityCompat.START);
                }
            }
        });

        // 두 개의 버튼을 설정합니다.
        Button btnMakeDrink = findViewById(R.id.btn_make_drink);
        Button btnJoinGame = findViewById(R.id.btn_join_game);

        database = FirebaseDatabase.getInstance();
        gameRef = database.getReference("Game").child("status");

        btnMakeDrink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 음료제조 버튼 클릭 시의 동작을 여기에 추가
                Intent intent = new Intent(MainActivity.this, MakeDrinksActivity.class);
                startActivity(intent);
            }
        });

        btnJoinGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 게임참가 버튼 클릭 시의 동작을 여기에 추가
                checkGameStatus(new MainActivity.CheckGameStatusListener() {
                    @Override
                    public void onCheckComplete(int canStartGame) {
                        if (canStartGame == 1) {
                            Intent intent = new Intent(MainActivity.this, GameActivity.class);
                            startActivity(intent);
                        } else if (canStartGame == 2) {
                            String message = "게임 중이라 참가가 불가능 합니다.";
                            Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
                        } else {
                            String message = "방이 없습니다.";
                            Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

        // Firebase에서 사용자 정보 가져오기
        fetchUserInfo();
    }

    private void checkGameStatus(MainActivity.CheckGameStatusListener listener) {
        DatabaseReference gameStatusRef = FirebaseDatabase.getInstance().getReference().child("Game").child("status");

        gameStatusRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DataSnapshot snapshot = task.getResult();
                if (snapshot.exists()) {
                    try {
                        Object value = snapshot.getValue();
                        if (value instanceof Long) {
                            Long gameStatus = (Long) value;
                            if (gameStatus == 1) {
                                listener.onCheckComplete(1);
                            } else if(gameStatus == 2) {
                                listener.onCheckComplete(2);
                            } else {
                                listener.onCheckComplete(0);
                            }
                        } else if (value instanceof Integer) {
                            Integer gameStatus = (Integer) value;
                            if (gameStatus == 1) {
                                listener.onCheckComplete(1);
                            } else if(gameStatus == 2) {
                                listener.onCheckComplete(2);
                            } else {
                                listener.onCheckComplete(0);
                            }
                        } else {
                            throw new Exception("Unexpected data type");
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing data", e);
                        Toast.makeText(MainActivity.this, "데이터를 처리하는 동안 오류가 발생했습니다", Toast.LENGTH_SHORT).show();
                        listener.onCheckComplete(0);
                    }
                } else {
                    listener.onCheckComplete(1);
                }
            } else {
                Log.e(TAG, "Failed to read data", task.getException());
                Toast.makeText(MainActivity.this, "데이터를 읽어오는 동안 오류가 발생했습니다", Toast.LENGTH_SHORT).show();
                listener.onCheckComplete(0);
            }
        });
    }

    // 인터페이스 정의
    interface CheckGameStatusListener {
        void onCheckComplete(int canStartGame);
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_profile) {
            // 프로필 화면으로 이동하는 코드
            Toast.makeText(this, "준비 중 입니다.", Toast.LENGTH_SHORT).show();
            // startActivity(new Intent(MainActivity.this, ProfileActivity.class));
        } else if (id == R.id.nav_logout) {
            mAuth.signOut();
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
            Toast.makeText(this, "로그아웃", Toast.LENGTH_SHORT).show();
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void fetchUserInfo() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("UserAccount").child(userId);

            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String nickname = snapshot.child("nickname").getValue(String.class);
                        String email = snapshot.child("emailId").getValue(String.class);
                        updateNavigationHeader(nickname, email);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e(TAG, "Error fetching user info", error.toException());
                }
            });
        }
    }

    private void updateNavigationHeader(String nickname, String email) {
        NavigationView navigationView = findViewById(R.id.nav_view);
        View headerView = navigationView.getHeaderView(0);
        TextView navUsername = headerView.findViewById(R.id.nav_header_username);
        TextView navEmail = headerView.findViewById(R.id.nav_header_email);

        navUsername.setText(nickname);
        navEmail.setText(email);
    }
}
