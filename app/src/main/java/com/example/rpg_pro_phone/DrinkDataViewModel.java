package com.example.rpg_pro_phone;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.HashMap;
import java.util.Map;

public class DrinkDataViewModel extends ViewModel {
    private final MutableLiveData<Map<String, String>> drinkData = new MutableLiveData<>(new HashMap<>());

    public LiveData<Map<String, String>> getDrinkData() {
        return drinkData;
    }

    public void updateDrinkData(String key, String value) {
        Map<String, String> currentData = drinkData.getValue();
        if (currentData != null) {
            currentData.put(key, value);
            drinkData.setValue(currentData);
        }
    }

    public void setDrinkData(Map<String, String> data) {
        drinkData.setValue(data);
    }
}