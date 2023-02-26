package com.example.mexpense.utilities;

import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import androidx.fragment.app.FragmentActivity;

public final class Utilities {
    private Utilities() {}

    public static String capitalizeWords(String text) {
        String[] arr = text.split(" ");
        String result = "";
        int i = 0;
        for(; i < arr.length-1; i++) {
            result += capitalizeWord(arr[i]) + " ";
        }
        result += capitalizeWord(arr[i]);
        return result;
    }

    public static String capitalizeWord(String text) {
        return text.substring(0, 1).toUpperCase() + text.substring(1).toLowerCase();
    }

    public static void hideKeyboard(View view, FragmentActivity act) {
        InputMethodManager imm = (InputMethodManager) act.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}
