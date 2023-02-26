package com.example.mexpense;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.example.mexpense.repositories.DatabaseHelper;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DatabaseHelper.connect(this);
        setContentView(R.layout.activity_main);
    }
}