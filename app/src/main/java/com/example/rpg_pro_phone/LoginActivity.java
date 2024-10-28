package com.example.rpg_pro_phone;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class LoginActivity extends AppCompatActivity {
    private FirebaseAuth mFirebaseAuth;
    private DatabaseReference mDatabaseRef;
    private EditText mEtEmail, mEtPwd;
    private Button mBtnRegister, mBtnLogin;
    private CheckBox mCbAutoLogin, mCbSaveEmail;
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mFirebaseAuth = FirebaseAuth.getInstance();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference();
        preferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);

        mEtEmail = findViewById(R.id.et_email);
        mEtPwd = findViewById(R.id.et_pwd);
        mBtnRegister = findViewById(R.id.btn_register);
        mBtnLogin = findViewById(R.id.btn_login);
        mCbAutoLogin = findViewById(R.id.cb_auto_login);
        mCbSaveEmail = findViewById(R.id.cb_save_email);

        mBtnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = mEtEmail.getText().toString().trim();
                String password = mEtPwd.getText().toString().trim();
                loginUser(email, password);
            }
        });

        mBtnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

        loadPreferences();
    }

    private void loadPreferences() {
        String savedEmail = preferences.getString("Email", "");
        mEtEmail.setText(savedEmail);
        mCbSaveEmail.setChecked(!savedEmail.isEmpty());

        FirebaseUser currentUser = mFirebaseAuth.getCurrentUser();
        if (currentUser != null && preferences.getBoolean("AutoLogin", false)) {
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            Toast.makeText(LoginActivity.this, "자동 로그인 되었습니다.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void loginUser(String email, String password) {
        if (email.isEmpty()) {
            Toast.makeText(LoginActivity.this, "이메일을 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (password.isEmpty()) {
            Toast.makeText(LoginActivity.this, "비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        mFirebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    if (mCbSaveEmail.isChecked()) {
                        preferences.edit().putString("Email", email).apply();
                    } else {
                        preferences.edit().remove("Email").apply();
                    }
                    if (mCbAutoLogin.isChecked()) {
                        preferences.edit().putBoolean("AutoLogin", true).apply();
                    } else {
                        preferences.edit().putBoolean("AutoLogin", false).apply();
                    }

                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(LoginActivity.this, "로그인 실패", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}