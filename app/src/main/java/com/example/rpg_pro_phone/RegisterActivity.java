package com.example.rpg_pro_phone;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class RegisterActivity extends AppCompatActivity {

    private FirebaseAuth mFirebaseAuth;     // 파이어베이스 인증
    private DatabaseReference mDatabaseRef; // 실시간 데이터 베이스
    private EditText mEtEmail, mEtPwd, mEtPwdConfirm, mEtBirth, mEtNickname;
    private RadioGroup mRadioGroupGender;
    private Button mBtnRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mFirebaseAuth = FirebaseAuth.getInstance();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference();

        mEtEmail = findViewById(R.id.et_email);
        mEtPwd = findViewById(R.id.et_pwd);
        mEtPwdConfirm = findViewById(R.id.et_pwd_confirm);
        mEtNickname = findViewById(R.id.et_nickname);
        mEtBirth = findViewById(R.id.et_birth);
        mRadioGroupGender = findViewById(R.id.radioGroup_gender);
        mBtnRegister = findViewById(R.id.btn_register);

        mBtnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String strEmail = mEtEmail.getText().toString();
                String strPwd = mEtPwd.getText().toString();
                String strPwdConfirm = mEtPwdConfirm.getText().toString();
                String strNickname = mEtNickname.getText().toString();
                String strBirth = mEtBirth.getText().toString();
                int selectedGenderId = mRadioGroupGender.getCheckedRadioButtonId();
                RadioButton selectedGenderRadioButton = findViewById(selectedGenderId);
                String strGender = selectedGenderRadioButton != null ? selectedGenderRadioButton.getText().toString() : "";

                if (strEmail.isEmpty() || strPwd.isEmpty() || strPwdConfirm.isEmpty() || strNickname.isEmpty() || strBirth.isEmpty() || strGender.isEmpty()) {
                    Toast.makeText(RegisterActivity.this, "모든 필드를 입력해 주세요", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!Patterns.EMAIL_ADDRESS.matcher(strEmail).matches()) {
                    Toast.makeText(RegisterActivity.this, "올바른 이메일 형식을 입력해 주세요", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (strPwd.length() < 8) {
                    Toast.makeText(RegisterActivity.this, "비밀번호는 8자리 이상이어야 합니다", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!strPwd.equals(strPwdConfirm)) {
                    Toast.makeText(RegisterActivity.this, "비밀번호가 일치하지 않습니다", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!isValidBirthDate(strBirth)) {
                    Toast.makeText(RegisterActivity.this, "올바른 생년월일을 입력해 주세요 (8자리)", Toast.LENGTH_SHORT).show();
                    return;
                }

                registerUser(strEmail, strPwd, strNickname, strBirth, strGender);
            }
        });
    }

    private boolean isValidBirthDate(String birthDate) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        sdf.setLenient(false);

        try {
            Date date = sdf.parse(birthDate);
            Calendar birth = Calendar.getInstance();
            birth.setTime(date);
            Calendar now = Calendar.getInstance();
            Calendar minDate = Calendar.getInstance();
            minDate.set(1900, Calendar.JANUARY, 1);

            if (birth.before(minDate) || birth.after(now)) {
                return false;
            }

            int year = birth.get(Calendar.YEAR);
            int month = birth.get(Calendar.MONTH) + 1; // Calendar.MONTH is zero-based
            int day = birth.get(Calendar.DAY_OF_MONTH);

            if (month < 1 || month > 12 || day < 1 || day > getMaxDaysInMonth(year, month)) {
                return false;
            }

            return true;
        } catch (ParseException e) {
            return false;
        }
    }

    private int getMaxDaysInMonth(int year, int month) {
        switch (month) {
            case 2:
                if ((year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)) {
                    return 29; // leap year
                } else {
                    return 28;
                }
            case 4: case 6: case 9: case 11:
                return 30;
            default:
                return 31;
        }
    }

    private void registerUser(String email, String password, String nickname, String birth, String gender) {
        mFirebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    FirebaseUser firebaseUser = mFirebaseAuth.getCurrentUser();
                    UserAccount account = new UserAccount();
                    account.setIdToken(firebaseUser.getUid());
                    account.setEmailId(firebaseUser.getEmail());
                    account.setPassword(password);
                    account.setNickname(nickname);
                    account.setBirth(birth);
                    account.setGender(gender);

                    mDatabaseRef.child("UserAccount").child(firebaseUser.getUid()).setValue(account);
                    Toast.makeText(RegisterActivity.this, "회원가입에 성공했습니다", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                        Toast.makeText(RegisterActivity.this, "이미 존재하는 이메일입니다", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(RegisterActivity.this, "회원가입에 실패했습니다", Toast.LENGTH_SHORT).show();
                        Log.e("RegisterActivity", "Registration failed: " + task.getException().getMessage());
                    }
                }
            }
        });
    }
}
