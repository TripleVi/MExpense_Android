package com.example.mexpense.repositories;

import android.database.Cursor;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.mexpense.entities.ExpenseCategoryEntity;

import java.util.ArrayList;
import java.util.List;

public class ExpenseCategoryRepository {
    public static final String TABLE_NAME = "expense_categories";

    public static final String COLUMN_ID = "id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_ICON_ID = "icon_id";

    public static final String TAG = "ExpenseCategoryRepository";

    public static final String CREATE_TABLE = String.format(
            "CREATE TABLE %s ("
                    + "%s INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "%s TEXT NOT NULL, "
                    + "%s INTEGER NOT NULL)",
            TABLE_NAME, COLUMN_ID, COLUMN_NAME, COLUMN_ICON_ID
    );

    @NonNull
    public List<ExpenseCategoryEntity> getAll() {
        List<ExpenseCategoryEntity> categories = new ArrayList<>();
        String query = String.format("SELECT * FROM %s", TABLE_NAME);
        try {
            Cursor c = DatabaseHelper.getInstance().rawQuery(query, null);
            while(c.moveToNext()) {
                ExpenseCategoryEntity category = ExpenseCategoryEntity.fromCursor(c);
                Log.d(TAG, "Getting row => " + category);
                categories.add(category);
            }
            c.close();
        }catch (Exception e) {
            Log.e(TAG, "Error getting row => " + e);
        }
        return categories;
    }

    @Nullable
    public ExpenseCategoryEntity getOneById(long id) {
        String query = String.format("SELECT * FROM %s WHERE %s = ?", TABLE_NAME, COLUMN_ID);
        ExpenseCategoryEntity category = null;
        try {
            Cursor c = DatabaseHelper.getInstance().rawQuery(query, new String[] {id+""});
            if(c.moveToFirst()) {
                category = ExpenseCategoryEntity.fromCursor(c);
                Log.d(TAG, "Getting row => " + category);
            }
            c.close();
        }catch (Exception e) {
            Log.e(TAG, "Error getting row => " + e);
        }
        return category;
    }

    public boolean insertOne(@NonNull ExpenseCategoryEntity category) {
        try {
            long result = DatabaseHelper.getInstance().insertOrThrow(TABLE_NAME, null, category.toContentValuesWithoutId());
            Log.d(TAG, "Inserted row with id " + result);
            return true;
        }catch (Exception e) {
            Log.e(TAG, "Error inserting row => " + e);
            return false;
        }
    }
}
