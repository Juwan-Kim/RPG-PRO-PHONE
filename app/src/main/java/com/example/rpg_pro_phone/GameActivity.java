package com.example.rpg_pro_phone;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import com.bumptech.glide.Glide;

public class GameActivity extends AppCompatActivity {

    private static final String TAG = "GameActivity";
    private FirebaseDatabase database;
    private DatabaseReference gameRef;
    private FirebaseAuth mAuth;
    private String uid;
    private int playerNumber = -1;  // playerNumber 초기값을 -1로 설정
    private boolean haveBomb = false;

    private TextView playerNickname, tvMessage;
    private Button btnThrow, btnBack;
    private ImageView playerImage;
    private ImageView bombAnimation;

    private boolean isGameRunning = false;

    private Dialog countdownDialog;
    private TextView countdownText;

    private ValueEventListener gameStatusListener;
    private ValueEventListener bombListener;
    private Handler countdownHandler = new Handler();

    //입장 할 때
    private Transaction.Handler handler;
    private Handler retryHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        playerNickname = findViewById(R.id.player_nickname);
        tvMessage = findViewById(R.id.status_message);
        playerImage = findViewById(R.id.player_image);
        btnThrow = findViewById(R.id.throw_button);
        btnBack = findViewById(R.id.back_button);
        bombAnimation = findViewById(R.id.bomb_animation);

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        gameRef = database.getReference("Game");

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            uid = currentUser.getUid();
            joinGame();
        } else {
            Log.d(TAG, "User not logged in");
            // 여기서 로그인 화면으로 전환하는 등의 처리를 할 수 있습니다.
        }

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnThrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnThrow.setVisibility(View.GONE);
                tvMessage.setText("폭탄 던지는 중...");
                throwBomb();
            }
        });

        updateNickname();
        monitorGameStatus();
        monitorBomb();
    }

    private void endGame() {

        if (isDestroyed() || isFinishing()) {
            tvMessage.setText("게임 시작 전입니다.");
            btnBack.setVisibility(View.VISIBLE);
            isGameRunning = false;
            return;
        }

        tvMessage.setText("축하합니다!");
        Toast.makeText(GameActivity.this, "축하합니다!", Toast.LENGTH_SHORT).show();

        // 폭탄 애니메이션 보여주기
        bombAnimation.setVisibility(View.VISIBLE);

        Glide.with(this)
                .asGif()
                .load(R.drawable.bomb_ani) // bomb_ani.gif 파일
                .into(bombAnimation);

        // 4초 후 폭탄 애니메이션 숨기기
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                bombAnimation.setVisibility(View.GONE);
                btnBack.setVisibility(View.VISIBLE);
                tvMessage.setText("게임 시작 전입니다.");
                isGameRunning = false;
            }
        }, 4000); // 4000ms = 4초
    }

    private void updateNickname() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("UserAccount").child(userId);

            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String nickname = snapshot.child("nickname").getValue(String.class);
                        playerNickname.setText(nickname);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e(TAG, "Error fetching user info", error.toException());
                }
            });
        }
    }

    private void joinGame() {
        gameRef.child("status").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Integer status = snapshot.getValue(Integer.class);
                if (status != null && status == 1) {
                    findEmptySlotAndJoin();
                } else {
                    Log.d(TAG, "Game is not active");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });
    }

    private void findEmptySlotAndJoin() {
        final int maxRetries = 10;
        final int retryDelay = 150; // 0.15초 지연
        final int[] retryCount = {0};

        handler = new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                for (int i = 0; i < 8; i++) {
                    MutableData playerData = mutableData.child("player" + i);
                    Integer playerStatus = playerData.child("status").getValue(Integer.class);
                    if (playerStatus != null && playerStatus == 0) {
                        playerData.child("uid").setValue(uid);
                        playerData.child("status").setValue(1);
                        playerNumber = i;
                        return Transaction.success(mutableData);  // 트랜잭션 성공
                    }
                }
                return Transaction.abort();  // 자리가 없을 때 트랜잭션 중단
            }

            @Override
            public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot currentData) {
                if (committed) {
                    Log.d(TAG, "Joined as player" + playerNumber);
                    // 게임 모니터링 시작 등 추가 작업
                } else {
                    retryCount[0]++;
                    if (retryCount[0] < maxRetries) {
                        Log.d(TAG, "Retrying transaction... attempt " + retryCount[0]);
                        // 지연 후 트랜잭션 재시도
                        retryHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                gameRef.child("players").runTransaction(handler);
                            }
                        }, retryDelay);
                    } else {
                        Toast.makeText(GameActivity.this, "다시 시도해주세요.", Toast.LENGTH_SHORT).show();
                        playerNumber = -1;
                        finish();
                    }
                }
            }
        };

        gameRef.child("players").runTransaction(handler);
    }

    private void leaveGame() {
        if (playerNumber != -1) {
            gameRef.child("players").child("player" + playerNumber).child("uid").setValue("");
            gameRef.child("players").child("player" + playerNumber).child("status").setValue(0);
            Log.d(TAG, "Left the game as player" + playerNumber);
        }
    }

    private void monitorGameStatus() {
        gameStatusListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Integer gameStatus = snapshot.getValue(Integer.class);
                    if (gameStatus != null) {
                        if (gameStatus == 2) {
                            tvMessage.setText("게임이 시작되었습니다!");
                            btnBack.setVisibility(View.GONE);
                            isGameRunning = true;

                            // 액티비티가 유효한지 확인
                            if (!isFinishing() && !isDestroyed()) {
                                showCountdownDialog();
                            } else {
                                Log.w(TAG, "Activity is not running, cannot show dialog");
                            }
                        } else if (gameStatus == 1){
                            btnThrow.setVisibility(View.GONE);
                            playerImage.setImageResource(R.drawable.player_image);
                            if(haveBomb){
                                haveBomb = false;
                                endGame();
                            } else {
                                tvMessage.setText("게임 시작 전입니다.");
                                btnBack.setVisibility(View.VISIBLE);
                                isGameRunning = false;
                            }
                        } else if (gameStatus == 0) {
                            Toast.makeText(GameActivity.this, "방이 없어졌습니다.", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }
                } else {
                    Log.w(TAG, "GameStart value does not exist");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w(TAG, "Failed to read GameStart value.", error.toException());
            }
        };
        gameRef.child("status").addValueEventListener(gameStatusListener);
    }

    private void showCountdownDialog() {
        // 액티비티가 유효한 상태인지 확인
        if (isDestroyed() || isFinishing()) {
            return;
        }
        countdownDialog = new Dialog(this);
        countdownDialog.setContentView(R.layout.dialog_countdown);
        countdownDialog.setCancelable(false);
        countdownDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        countdownText = countdownDialog.findViewById(R.id.countdown_text);
        countdownDialog.show();

        startCountdown();
    }

    private void startCountdown() {
        final int[] countdown = { 0 };  //카운트다운 횟수 설정 0이면 바로 게임시작! 만 뜸

        countdownHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (countdown[0] > 0) {
                    countdownText.setText(String.valueOf(countdown[0]));
                    countdown[0]--;
                    countdownHandler.postDelayed(this, 1000);
                } else {
                    countdownText.setText("START!");
                    countdownHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (countdownDialog.isShowing()) {
                                countdownDialog.dismiss();
                            }
                        }
                    }, 700);
                }
            }
        }, 1000);
    }


    private void monitorBomb() {
        bombListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.exists()) {
                    Integer bombNumber = snapshot.getValue(Integer.class);
                    if (bombNumber != null && playerNumber != -1 && bombNumber != -1) {
                        if (bombNumber == playerNumber) { // Bomb 번호와 내 번호가 같으면
                            tvMessage.setText("다른 사람한테 폭탄을 던지세요!");
                            playerImage.setImageResource(R.drawable.player_image_bomb);
                            btnThrow.setVisibility(View.VISIBLE);
                            haveBomb = true;

                        } else {
                            tvMessage.setText("가지고 있는 폭탄이 없습니다.");
                            playerImage.setImageResource(R.drawable.player_image);
                            if(haveBomb){
                                tvMessage.setText("폭탄 던지기 성공!");
                                //Toast.makeText(GameActivity.this, "폭탄 던지기 성공!", Toast.LENGTH_SHORT).show();
                            }
                            haveBomb = false;
                        }
                    }
                } else {
                    Log.w(TAG, "GameStart value does not exist");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w(TAG, "Failed to read Bomb value.", error.toException());
            }
        };
        gameRef.child("Bomb").addValueEventListener(bombListener);
    }

    private void throwBomb() {
        long timestamp = System.currentTimeMillis();
        gameRef.child("BombThrow").setValue(timestamp); // BombThrow에 타임스탬프 기록
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isFinishing()) {
            leaveGame();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        leaveGame();
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // 리스너 제거
        if (gameStatusListener != null) {
            gameRef.child("status").removeEventListener(gameStatusListener);
        }
        if (bombListener != null) {
            gameRef.child("Bomb").removeEventListener(bombListener);
        }

        // 핸들러 작업 제거
        countdownHandler.removeCallbacksAndMessages(null);

        retryHandler.removeCallbacksAndMessages(null);

        if (isFinishing()) {
            leaveGame();
        }
    }

    @Override
    public void onBackPressed() {
        if (!isGameRunning) {
            // 게임 중에는 뒤로 가기 버튼을 막음
            super.onBackPressed();
        }
    }
}
