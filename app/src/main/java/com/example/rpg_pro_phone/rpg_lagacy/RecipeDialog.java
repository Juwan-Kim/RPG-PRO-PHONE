package com.example.rpg_pro_phone.rpg_lagacy;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rpg_pro_phone.R;

import java.util.ArrayList;

public class RecipeDialog extends Dialog {
    private Context context;
    private TextView Confirm;


    private View.OnClickListener Confirm_Btn;

    public TextView Title;
    public TextView tvWarn;
    public String title;

    public RecyclerView recipeRecyclerView;
    public RecipeDialogAdapter recipeRecyclerAdapter;
    private ArrayList<BeverRecipe> recipeList;
    private RecipeDialogAdapter.OnClickRecipeItem onClickRecipeItem;

    private RadioButton radioButton, radioButton2, radioButton3;

    private OnClickRadioButton onClickRadioButton;

    private String warnText;


    public RecipeDialog(@NonNull Context context, View.OnClickListener Confirm_Btn, String title, ArrayList<BeverRecipe> recipeList) {
        super(context);
        //생성자에서 리스너 및 텍스트 초기화
        this.context = context;
        this.Confirm_Btn = Confirm_Btn;
        this.title = title;
        this.recipeList = recipeList;

    }

    public RecipeDialogAdapter getRecipeRecyclerAdapter() {
        return recipeRecyclerAdapter;
    }

    public void setRecipeRecyclerAdapter(RecipeDialogAdapter recipeRecyclerAdapter) {
        this.recipeRecyclerAdapter = recipeRecyclerAdapter;
    }

    public ArrayList<BeverRecipe> getRecipeList() {
        return recipeList;
    }

    public void setRecipeList(ArrayList<BeverRecipe> recipeList) {
        this.recipeList = recipeList;
    }

    public void setOnClickRadioButton(OnClickRadioButton onClickRadioButton) {
        this.onClickRadioButton = onClickRadioButton;
    }

    // 새로운 메서드 추가
    public void setWarnText(String text) {
        if (text != null) {
            warnText = text;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //다이얼로그의 꼭짓점이 짤리는부분 방지.
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        setContentView(R.layout.dialog_recipe);

        Confirm=findViewById(R.id.Confirm);
        Title = findViewById(R.id.title);
        tvWarn = findViewById(R.id.warn);

        if(warnText != null){
            tvWarn.setText(warnText);
        }

        recipeRecyclerView = findViewById(R.id.recipe_list);
        recipeRecyclerView.setAdapter(recipeRecyclerAdapter);

        if(recipeList.size() == 0) {
            recipeRecyclerView.setVisibility(View.GONE);
            tvWarn.setVisibility(View.VISIBLE);
        } else {
            recipeRecyclerView.setVisibility(View.VISIBLE);
            tvWarn.setVisibility(View.GONE);
        }

        radioButton = findViewById(R.id.radioButton);
        radioButton2 = findViewById(R.id.radioButton2);
        radioButton3 = findViewById(R.id.radioButton3);

        radioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickRadioButton.onClickButton();
            }
        });

        radioButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickRadioButton.onClickButton2();

            }
        });

        radioButton3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickRadioButton.onClickButton3();

            }
        });

        Confirm.setOnClickListener(Confirm_Btn);

        //타이틀과 바디의 글씨 재셋팅
        Title.setText(this.title);

        radioButton.performClick();
    }


    public interface OnClickRadioButton {
        public void onClickButton();
        public void onClickButton2();
        public void onClickButton3();

    }

}