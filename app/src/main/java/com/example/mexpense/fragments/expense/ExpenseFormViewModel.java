package com.example.mexpense.fragments.expense;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.mexpense.entities.ExpenseCategoryEntity;
import com.example.mexpense.entities.ExpenseEntity;
import com.example.mexpense.entities.TripEntity;

import java.util.List;

public class ExpenseFormViewModel extends ViewModel {
    MutableLiveData<ExpenseEntity> expenseLiveData = new MutableLiveData<>();
    MutableLiveData<List<ExpenseCategoryEntity>> expenseCategoryLiveData = new MutableLiveData<>();
    MutableLiveData<TripEntity> tripLiveData = new MutableLiveData<>();
}