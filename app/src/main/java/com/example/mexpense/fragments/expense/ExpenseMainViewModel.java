package com.example.mexpense.fragments.expense;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.mexpense.entities.TripEntity;

public class ExpenseMainViewModel extends ViewModel {
    MutableLiveData<TripEntity> tripLiveData = new MutableLiveData<>();
}