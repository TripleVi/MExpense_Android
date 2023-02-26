package com.example.mexpense.fragments.search;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.mexpense.entities.TripEntity;

import java.util.List;

public class SearchViewModel extends ViewModel {
    MutableLiveData<List<TripEntity>> liveData;

    public SearchViewModel() {
        liveData = new MutableLiveData<>();
    }
}