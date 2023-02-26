package com.example.mexpense.fragments.trip;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.mexpense.entities.TripEntity;

import java.util.List;

public class TripMainViewModel extends ViewModel {
    public MutableLiveData<List<TripEntity>> liveData = new MutableLiveData<>();
}