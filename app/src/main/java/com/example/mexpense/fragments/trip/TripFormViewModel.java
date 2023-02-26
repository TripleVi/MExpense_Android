package com.example.mexpense.fragments.trip;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.mexpense.entities.TripCategoryEntity;
import com.example.mexpense.entities.TripEntity;

import java.util.List;

public class TripFormViewModel extends ViewModel {
    MutableLiveData<TripEntity> tripLiveData = new MutableLiveData<>();
    MutableLiveData<List<TripCategoryEntity>> tripCategoryLiveData = new MutableLiveData<>();
}