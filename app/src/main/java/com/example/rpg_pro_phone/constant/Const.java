package com.example.rpg_pro_phone.constant;


import com.example.rpg_pro_phone.rpg_lagacy.BeverRecipe;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Const {

    public static String BASE_URL = "http://18.118.95.155:8000/";

    public static final List<String> drinks = new ArrayList<>(Arrays.asList("소주", "맥주", "사이다", "콜라", "박카스", "비타500", "막걸리", "밀키스", "파워에이드", "요구르트"));
    public static final ArrayList<BeverRecipe> RECIPE_LIST = new ArrayList<BeverRecipe>(
            Arrays.asList(                                                                                  //다 합쳐서 10이 되게 8~12정도로
                    new BeverRecipe(new String[]{"소주","포도주스","핫식스"}, new int[]{2,4,6}, false),  // 1:2:3
                    new BeverRecipe(new String[]{"소주","데미소다","핫식스"}, new int[]{2,6,4}, false),  // 1:3:2
                    new BeverRecipe(new String[]{"소주","요구르트"}, new int[]{3,6}, false),            // 1:2
                    new BeverRecipe(new String[]{"소주","맥주","밀키스"}, new int[]{3,3,3}, false),     // 1:1:1
                    new BeverRecipe(new String[]{"소주","맥주", "콜라"}, new int[]{4,2,2}, false),      // 2:1:1
                    new BeverRecipe(new String[]{"맥주","토마토주스"}, new int[]{3,6}, false),          // 1:2
                    new BeverRecipe(new String[]{"소주","레스비"}, new int[]{3,6}, false),             // 1:2
                    new BeverRecipe(new String[]{"소주","맥주", "사이다"}, new int[]{2,4,4}, false),    // 1:2:2
                    new BeverRecipe(new String[]{"소주","봉봉", "사이다"}, new int[]{2,2,6}, false),    // 1:1:3
                    new BeverRecipe(new String[]{"소주","막걸리", "사이다"}, new int[]{3,3,3}, false),   // 1:1:1
                    new BeverRecipe(new String[]{"소주","아침햇쌀", "사이다"}, new int[]{2,2,4}, false),  // 1:1:2
                    new BeverRecipe(new String[]{"소주","맥주"}, new int[]{3,7}, false)                 //3:7

            )
    );


}
