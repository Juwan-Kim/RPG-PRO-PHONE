package com.example.rpg_pro_phone.rpg_lagacy;

public class BeverRecipe {
    private String[] recipe;
    private int[] recipeCount;

    private boolean isAi;

    public BeverRecipe(String[] recipe, int[] recipeCount, boolean isAi) {
        this.recipe = recipe;
        this.recipeCount = recipeCount;
        this.isAi = isAi;
    }

    public String[] getRecipe() {
        return recipe;
    }

    public int[] getRecipeCount() {
        return recipeCount;
    }

    public boolean isAi() {
        return isAi;
    }

    public void setAi(boolean ai) {
        isAi = ai;
    }
}
