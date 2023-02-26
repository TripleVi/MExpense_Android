package com.example.mexpense.fragments.expense;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.mexpense.entities.ExpenseEntity;

public class ExpenseDetailsViewModel extends ViewModel {
    MutableLiveData<ExpenseEntity> expenseLiveData = new MutableLiveData<>();
}
