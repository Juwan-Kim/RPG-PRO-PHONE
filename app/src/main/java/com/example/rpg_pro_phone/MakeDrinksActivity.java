package com.example.rpg_pro_phone;

import android.app.Dialog;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.rpg_pro_phone.constant.Const;
import com.example.rpg_pro_phone.retrofit.RetrofitModule;
import com.example.rpg_pro_phone.retrofit.req.REQBever;
import com.example.rpg_pro_phone.retrofit.res.RESBever;
import com.example.rpg_pro_phone.rpg_lagacy.BeverRecipe;
import com.example.rpg_pro_phone.rpg_lagacy.RecipeDialog;
import com.example.rpg_pro_phone.rpg_lagacy.RecipeDialogAdapter;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MakeDrinksActivity extends AppCompatActivity {

    private static final String TAG = "MakeDrinksActivity";
    private DrinkDataViewModel drinkDataViewModel;
    private ProgressBar progressBar;
    private TextView textViewDrinkA, textViewDrinkB, textViewDrinkC;
    private TextView DrinkA_num, DrinkB_num, DrinkC_num;
    private SeekBar seekBarDrinkA, seekBarDrinkB, seekBarDrinkC;
    private Button buttonSave, buttonBack, recipe, aiRecipe, buttonHelp;
    private FirebaseAuth mAuth;
    private RecipeDialog recipeDialog;
    private int weight = 2;
    private static final int MAX_DRINKS = 10; // 0.5잔 단위로 5잔까지

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_make_drinks);

        progressBar = findViewById(R.id.progressBar);
        drinkDataViewModel = new ViewModelProvider(this).get(DrinkDataViewModel.class);

        mAuth = FirebaseAuth.getInstance();

        textViewDrinkA = findViewById(R.id.textViewDrinkA);
        seekBarDrinkA = findViewById(R.id.seekBarDrinkA);
        textViewDrinkB = findViewById(R.id.textViewDrinkB);
        seekBarDrinkB = findViewById(R.id.seekBarDrinkB);
        textViewDrinkC = findViewById(R.id.textViewDrinkC);
        seekBarDrinkC = findViewById(R.id.seekBarDrinkC);
        buttonSave = findViewById(R.id.buttonSave);
        buttonBack = findViewById(R.id.back2);
        buttonHelp = findViewById(R.id.help_button);
        recipe = findViewById(R.id.recipe);
        aiRecipe = findViewById(R.id.ai_recipe);

        DrinkA_num = findViewById(R.id.st1);
        DrinkA_num.setText("0.0잔");
        DrinkB_num = findViewById(R.id.st2);
        DrinkB_num.setText("0.0잔");
        DrinkC_num = findViewById(R.id.st3);
        DrinkC_num.setText("0.0잔");

        setupSeekBar(seekBarDrinkA, DrinkA_num, seekBarDrinkB, seekBarDrinkC);
        setupSeekBar(seekBarDrinkB, DrinkB_num, seekBarDrinkA, seekBarDrinkC);
        setupSeekBar(seekBarDrinkC, DrinkC_num, seekBarDrinkA, seekBarDrinkB);

        aiRecipe.setOnClickListener(v -> showAiRecipeDialog());

        recipe.setOnClickListener(v -> showRecipeDialog());

        buttonSave.setOnClickListener(v -> {
            int totalProgress = seekBarDrinkA.getProgress() + seekBarDrinkB.getProgress() + seekBarDrinkC.getProgress();
            if (totalProgress == 0) {
                Toast.makeText(MakeDrinksActivity.this, "음료를 선택해주세요", Toast.LENGTH_SHORT).show();
            } else {
                FirebaseUser currentUser = mAuth.getCurrentUser();
                if (currentUser != null) {
                    checkAndSaveData(currentUser.getUid(), seekBarDrinkA.getProgress() * 5, seekBarDrinkB.getProgress() * 5, seekBarDrinkC.getProgress() * 5);
                } else {
                    Toast.makeText(MakeDrinksActivity.this, "로그인이 필요합니다", Toast.LENGTH_SHORT).show();
                }
            }
        });

        buttonBack.setOnClickListener(v -> finish());

        buttonHelp.setOnClickListener(v -> {
            final Dialog dialog = new Dialog(MakeDrinksActivity.this);
            dialog.setContentView(R.layout.help_dialog);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

            int buttonColor = Color.parseColor("#0061A8");

            Button btnClose = dialog.findViewById(R.id.btn_close);
            btnClose.setBackgroundTintList(ColorStateList.valueOf(buttonColor));

            btnClose.setOnClickListener(v1 -> dialog.dismiss());

            dialog.show();
        });

        fetchDrinkData();
        listenForTypeOfDrinksChanges();
    }

    private void listenForTypeOfDrinksChanges() {
        DatabaseReference typeOfDrinksRef = FirebaseDatabase.getInstance().getReference("TypeOfDrinks");

        typeOfDrinksRef.child("drinkA").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String drinkA = dataSnapshot.getValue(String.class);
                    if (drinkA != null) {
                        textViewDrinkA.setText(drinkA);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Failed to read drinkA data", databaseError.toException());
            }
        });

        typeOfDrinksRef.child("drinkB").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String drinkB = dataSnapshot.getValue(String.class);
                    if (drinkB != null) {
                        textViewDrinkB.setText(drinkB);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Failed to read drinkB data", databaseError.toException());
            }
        });

        typeOfDrinksRef.child("drinkC").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String drinkC = dataSnapshot.getValue(String.class);
                    if (drinkC != null) {
                        textViewDrinkC.setText(drinkC);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Failed to read drinkC data", databaseError.toException());
            }
        });
    }

    private void fetchDrinkData() {
        progressBar.setVisibility(View.VISIBLE);

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("TypeOfDrinks");
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Map<String, String> drinkData = new HashMap<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    drinkData.put(snapshot.getKey(), snapshot.getValue(String.class));
                }
                drinkDataViewModel.setDrinkData(drinkData);

                // 데이터가 로드된 후 기본 값 설정
                textViewDrinkA.setText(drinkData.getOrDefault("drinkA", "1번음료"));
                textViewDrinkB.setText(drinkData.getOrDefault("drinkB", "2번음료"));
                textViewDrinkC.setText(drinkData.getOrDefault("drinkC", "3번음료"));

                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // 오류 처리
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    private void showRecipeDialog() {
        ArrayList<BeverRecipe> filteredList = new ArrayList<>();
        String firstBever = drinkDataViewModel.getDrinkData().getValue().getOrDefault("drinkA", "소주");
        String secondBever = drinkDataViewModel.getDrinkData().getValue().getOrDefault("drinkB", "맥주");
        String thirdBever = drinkDataViewModel.getDrinkData().getValue().getOrDefault("drinkC", "사이다");

        for (BeverRecipe beverRecipe : Const.RECIPE_LIST) {
            boolean containsAll = true;
            for (String recipeItem : beverRecipe.getRecipe()) {
                if (!recipeItem.equals(firstBever) && !recipeItem.equals(secondBever) && !recipeItem.equals(thirdBever)) {
                    containsAll = false;
                    break;
                }
            }
            if (containsAll) {
                filteredList.add(beverRecipe);
            }
        }

        recipeDialog = new RecipeDialog(this, onClickCommDialogConfirmButton, "추천 레시피", filteredList);
        RecipeDialogAdapter recipeRecyclerAdapter = new RecipeDialogAdapter(this, filteredList);
        recipeRecyclerAdapter.setOnClickRecipeItem(onClickRecipeItem);
        recipeRecyclerAdapter.setFirstBever(firstBever);
        recipeRecyclerAdapter.setSecondBever(secondBever);
        recipeRecyclerAdapter.setThirdBever(thirdBever);
        recipeDialog.setRecipeRecyclerAdapter(recipeRecyclerAdapter);

        recipeDialog.setOnClickRadioButton(new RecipeDialog.OnClickRadioButton() {
            @Override
            public void onClickButton() {
                weight = 2;
            }

            @Override
            public void onClickButton2() {
                weight = 4;
            }

            @Override
            public void onClickButton3() {
                weight = 6;
            }
        });

        recipeDialog.show();
    }

    private void showAiRecipeDialog() {
        ArrayList<BeverRecipe> filteredList = new ArrayList<>();
        String firstBever = drinkDataViewModel.getDrinkData().getValue().getOrDefault("drinkA", "소주");
        String secondBever = drinkDataViewModel.getDrinkData().getValue().getOrDefault("drinkB", "맥주");
        String thirdBever = drinkDataViewModel.getDrinkData().getValue().getOrDefault("drinkC", "사이다");

        RetrofitModule retrofitModule = new RetrofitModule();
        REQBever req = new REQBever();
        req.setUserKey(mAuth.getCurrentUser().getUid());

        recipeDialog = new RecipeDialog(this, onClickCommDialogConfirmButton, "AI 추천 레시피", filteredList);
        RecipeDialogAdapter recipeRecyclerAdapter = new RecipeDialogAdapter(this, filteredList);
        recipeRecyclerAdapter.setOnClickRecipeItem(onClickRecipeItem);
        recipeRecyclerAdapter.setFirstBever(firstBever);
        recipeRecyclerAdapter.setSecondBever(secondBever);
        recipeRecyclerAdapter.setThirdBever(thirdBever);
        recipeDialog.setRecipeRecyclerAdapter(recipeRecyclerAdapter);

        recipeDialog.setOnClickRadioButton(new RecipeDialog.OnClickRadioButton() {
            @Override
            public void onClickButton() {
                weight = 2;
            }

            @Override
            public void onClickButton2() {
                weight = 4;
            }

            @Override
            public void onClickButton3() {
                weight = 6;
            }
        });

        progressBar.setVisibility(View.VISIBLE);

        final Handler timeoutHandler = new Handler(Looper.getMainLooper());
        final Runnable timeoutRunnable = new Runnable() {
            @Override
            public void run() {
                if (progressBar.getVisibility() == View.VISIBLE) {
                    progressBar.setVisibility(View.GONE);
                    recipeDialog.setWarnText("음료기록 데이터가 부족합니다.");
                    recipeDialog.show();
                }
            }
        };
        timeoutHandler.postDelayed(timeoutRunnable, 10000); // 10초 후 실행

        retrofitModule.getApiClient().getbever(req).enqueue(new Callback<List<RESBever>>() {
            @Override
            public void onResponse(Call<List<RESBever>> call, Response<List<RESBever>> response) {
                progressBar.setVisibility(View.GONE);
                timeoutHandler.removeCallbacks(timeoutRunnable); // 타임아웃 취소

                if (response.body() != null && !response.body().isEmpty()) {
                    List<RESBever> resBeverList = response.body();
                    for (RESBever resBever : resBeverList) {
                        int firstRate = Math.round(Float.parseFloat(resBever.getFirstRate()) * 10);
                        int secondRate = Math.round(Float.parseFloat(resBever.getSecondRate()) * 10);
                        int thirdRate = Math.round(Float.parseFloat(resBever.getThirdRate()) * 10);

                        int recipeNum = 0;
                        if (!resBever.getFirstBever().isEmpty()) {
                            recipeNum++;
                        }
                        if (!resBever.getSecondBever().isEmpty()) {
                            recipeNum++;
                        }
                        if (!resBever.getThirdBever().isEmpty()) {
                            recipeNum++;
                        }

                        int[] recipeCount = new int[recipeNum];
                        String[] recipe = new String[recipeNum];

                        if (recipeNum == 2) {
                            recipeCount[0] = firstRate;
                            recipeCount[1] = secondRate;
                            recipe[0] = resBever.getFirstBever();
                            recipe[1] = resBever.getSecondBever();
                        } else if (recipeNum == 3) {
                            recipeCount[0] = firstRate;
                            recipeCount[1] = secondRate;
                            recipeCount[2] = thirdRate;
                            recipe[0] = resBever.getFirstBever();
                            recipe[1] = resBever.getSecondBever();
                            recipe[2] = resBever.getThirdBever();
                        } else {
                            Log.e("minfrank", "망함");
                        }

                        BeverRecipe beverRecipe = new BeverRecipe(recipe, recipeCount, true);
                        filteredList.add(beverRecipe);
                    }
                    recipeDialog.setRecipeList(filteredList);
                    recipeRecyclerAdapter.setList(filteredList);
                    recipeRecyclerAdapter.notifyDataSetChanged();
                    recipeDialog.show();
                } else {
                    recipeDialog.setWarnText("음료기록 데이터가 부족합니다.");
                    recipeDialog.show();
                }
            }

            @Override
            public void onFailure(Call<List<RESBever>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                timeoutHandler.removeCallbacks(timeoutRunnable); // 타임아웃 취소

                Log.e("minfrank", "네트워크 오류: " + t.getMessage());
                recipeDialog.setWarnText("네트워크 오류가 발생했습니다.");
                recipeDialog.show();
            }
        });
    }

    private final RecipeDialogAdapter.OnClickRecipeItem onClickRecipeItem = new RecipeDialogAdapter.OnClickRecipeItem() {
        @Override
        public void onClickRecipeItem(int num1, int num2, int num3) {
            checkAndSaveData(mAuth.getCurrentUser().getUid(), num1 * weight, num2 * weight, num3 * weight);
        }
    };

    private final View.OnClickListener onClickCommDialogConfirmButton = v -> recipeDialog.dismiss();

    private void checkAndSaveData(String userId, int a, int b, int c) {
        DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        DatabaseReference machineRef = database.child("Machine");

        machineRef.runTransaction(new Transaction.Handler() {
            private long remainingTime = 0;

            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                long currentTimestamp = System.currentTimeMillis();
                if (mutableData.getValue() != null) {
                    Map<String, Object> data = (Map<String, Object>) mutableData.getValue();
                    int gameMode = ((Long) data.get("gameMode")).intValue();
                    long lastTimestamp = (long) data.get("timestamp");
                    int lastDrinkA = ((Long) data.get("drinkA")).intValue();
                    int lastDrinkB = ((Long) data.get("drinkB")).intValue();
                    int lastDrinkC = ((Long) data.get("drinkC")).intValue();
                    int maxLastDrink = Math.max(lastDrinkA, Math.max(lastDrinkB, lastDrinkC));
                    long timeLimit = lastTimestamp + (maxLastDrink * 200) + 1500;

                    if (gameMode == 0) {
                        if (currentTimestamp > timeLimit) {
                            mutableData.child("drinkA").setValue(a);
                            mutableData.child("drinkB").setValue(b);
                            mutableData.child("drinkC").setValue(c);
                            mutableData.child("timestamp").setValue(currentTimestamp);
                            return Transaction.success(mutableData);
                        } else {
                            remainingTime = timeLimit - currentTimestamp;
                            return Transaction.abort();
                        }
                    } else {
                        remainingTime = -1; // Game mode 상태 표시
                        return Transaction.abort();
                    }
                } else {
//                    mutableData.child("drinkA").setValue(a);  //초기 데이터는 무조건 있다고 가정
//                    mutableData.child("drinkB").setValue(b);
//                    mutableData.child("drinkC").setValue(c);
//                    mutableData.child("timestamp").setValue(currentTimestamp);
                    return Transaction.success(mutableData);
                }
            }

            @Override
            public void onComplete(DatabaseError error, boolean committed, DataSnapshot currentData) {
                if (error != null) {
                    Log.e(TAG, "Failed to run transaction", error.toException());
                    Toast.makeText(MakeDrinksActivity.this, "데이터를 처리하는 동안 오류가 발생했습니다", Toast.LENGTH_SHORT).show();
                } else if (committed) {
                    Log.d(TAG, "Transaction completed successfully");
                    saveDataAndShowDialog(userId, a, b, c);
                } else {
                    if (remainingTime > 0) {
                        Toast.makeText(MakeDrinksActivity.this, "기계가 사용 중 입니다. 남은 시간 : " + remainingTime / 1000 + "초", Toast.LENGTH_SHORT).show();
                    } else if (remainingTime == -1) {
                        Toast.makeText(MakeDrinksActivity.this, "기기가 게임모드 상태입니다.", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(MakeDrinksActivity.this, "기계가 사용 중 입니다.", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    private void saveDataAndShowDialog(String userId, int a, int b, int c) {
        //saveDataToFirebase(userId, a, b, c);
        saveDataSequentially(userId, a, b, c);
        showProgressBarDialog(a, b, c);
    }

    private void setupSeekBar(SeekBar primarySeekBar, TextView primaryTextView, SeekBar otherSeekBar1, SeekBar otherSeekBar2) {
        primarySeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    int total = otherSeekBar1.getProgress() + otherSeekBar2.getProgress() + progress;
                    if (total > MAX_DRINKS) {
                        seekBar.setProgress(progress - (total - MAX_DRINKS));
                    }
                    primaryTextView.setText(String.format("%d.%d잔", seekBar.getProgress() / 2, ((seekBar.getProgress() % 2) * 5)));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    private void showProgressBarDialog(int drinkA, int drinkB, int drinkC) {
        final Dialog dialog = new Dialog(MakeDrinksActivity.this);
        dialog.setContentView(R.layout.drinks_progress_bar);
        dialog.setCancelable(false);

        final TextView textViewStatus = dialog.findViewById(R.id.textViewStatus);
        ProgressBar progressBar = dialog.findViewById(R.id.progressBar);

        int progressBarColor = Color.parseColor("#0061A8");
        progressBar.setProgressTintList(ColorStateList.valueOf(progressBarColor));

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        int progressA = drinkA;
        int progressB = drinkB;
        int progressC = drinkC;

        final int maxProgressTime = (Math.max(progressA * 200, Math.max(progressB * 172, progressC * 200))) + 1000;
        progressBar.setMax(maxProgressTime);

        CountDownTimer timer = new CountDownTimer(maxProgressTime, 10) {
            int dotCount = 0;
            int textUpdateCount = 1;

            public void onTick(long millisUntilFinished) {
                int progressStatus = maxProgressTime - (int) millisUntilFinished;
                progressBar.setProgress(progressStatus);

                if (progressStatus / 1000 == textUpdateCount) {
                    String dots = new String(new char[dotCount + 1]).replace("\0", ".");
                    textViewStatus.setText("음료 나오는 중" + dots);
                    dotCount = (dotCount + 1) % 3;
                    textUpdateCount++;
                }
            }

            public void onFinish() {
                textViewStatus.setText("완료!");

                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    seekBarDrinkA.setProgress(0);
                    seekBarDrinkB.setProgress(0);
                    seekBarDrinkC.setProgress(0);

                    DrinkA_num.setText("0.0잔");
                    DrinkB_num.setText("0.0잔");
                    DrinkC_num.setText("0.0잔");

                    dialog.dismiss();
                }, 500); // 500ms 지연
            }
        };
        timer.start();

        dialog.show();
    }

    private void saveDataToFirebase(String userId, int drinkA, int drinkB, int drinkC) {
        DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        DatabaseReference machineRef = database.child("Machine");

        Map<String, Object> data = new HashMap<>();
        data.put("drinkA", drinkA);
        data.put("drinkB", drinkB);
        data.put("drinkC", drinkC);
        data.put("timestamp", System.currentTimeMillis());

        machineRef.updateChildren(data).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d(TAG, "Data uploaded successfully");
            } else {
                Log.e(TAG, "Failed to upload data", task.getException());
            }
        });
    }

    private void saveDataSequentially(String userId, int drinkA, int drinkB, int drinkC) {
        DatabaseReference database = FirebaseDatabase.getInstance().getReference();

        DatabaseReference dataRef = database.child("Data").child(userId);
        DatabaseReference indexRef = database.child("Data").child(userId).child("lastIndex");

        indexRef.runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                Integer currentIndex = currentData.getValue(Integer.class);
                if (currentIndex == null) {
                    currentIndex = 0;
                } else {
                    currentIndex += 1;
                }
                currentData.setValue(currentIndex);
                return Transaction.success(currentData);
            }

            @Override
            public void onComplete(DatabaseError error, boolean committed, DataSnapshot currentData) {
                if (committed) {
                    int newIndex = currentData.getValue(Integer.class);
                    Map<String, Object> data = new HashMap<>();

                    // 같은 키가 존재하면 더하도록 바꿈
                    String drinkAKey = textViewDrinkA.getText().toString();
                    String drinkBKey = textViewDrinkB.getText().toString();
                    String drinkCKey = textViewDrinkC.getText().toString();

                    if (drinkA != 0) {
                        if (data.containsKey(drinkAKey)) {
                            int currentValue = (int) data.get(drinkAKey);
                            data.put(drinkAKey, currentValue + drinkA);
                        } else {
                            data.put(drinkAKey, drinkA);
                        }
                    }

                    if (drinkB != 0) {
                        if (data.containsKey(drinkBKey)) {
                            int currentValue = (int) data.get(drinkBKey);
                            data.put(drinkBKey, currentValue + drinkB);
                        } else {
                            data.put(drinkBKey, drinkB);
                        }
                    }

                    if (drinkC != 0) {
                        if (data.containsKey(drinkCKey)) {
                            int currentValue = (int) data.get(drinkCKey);
                            data.put(drinkCKey, currentValue + drinkC);
                        } else {
                            data.put(drinkCKey, drinkC);
                        }
                    }

                    dataRef.child(String.valueOf(newIndex)).setValue(data);
                } else {
                    Log.e("Firebase", "Failed to update index for data saving.");
                }
            }
        });
    }
}
