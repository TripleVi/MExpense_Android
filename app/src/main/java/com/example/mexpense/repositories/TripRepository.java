package com.example.mexpense.repositories;

import android.database.Cursor;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.mexpense.entities.ExpenseEntity;
import com.example.mexpense.entities.TripCategoryEntity;
import com.example.mexpense.entities.TripEntity;

import java.util.ArrayList;
import java.util.List;

public class TripRepository {
    public static final String TABLE_NAME = "trips";

    public static final String COLUMN_ID = "id";
    public static final String COLUMN_DESTINATION = "destination";
    public static final String COLUMN_START_DATE = "start_date";
    public static final String COLUMN_END_DATE = "end_date";
    public static final String COLUMN_DESCRIPTION = "description";
    public static final String COLUMN_REQUIRED_RISK_ASSESSMENT = "required_risk_assessment";
    public static final String COLUMN_TOTAL = "total";
    public static final String COLUMN_CATEGORY_ID = "category_id";

    public static final String TAG = "TripRepository";

    public static final String CREATE_TABLE = String.format(
            "CREATE TABLE %s("
                    + "%s INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "%s TEXT NOT NULL, "
                    + "%s INTEGER NOT NULL, "
                    + "%s INTEGER NOT NULL, "
                    + "%s TEXT, "
                    + "%s INTEGER NOT NULL, "
                    + "%s REAL NOT NULL, "
                    + "%s INTEGER NOT NULL)",
            TABLE_NAME, COLUMN_ID, COLUMN_DESTINATION, COLUMN_START_DATE, COLUMN_END_DATE, COLUMN_DESCRIPTION, COLUMN_REQUIRED_RISK_ASSESSMENT, COLUMN_TOTAL, COLUMN_CATEGORY_ID
    );

    @NonNull
    public List<TripEntity> getAll(String whereClause, String[] selectionArgs) {
        List<TripEntity> trips = new ArrayList<>();
        String query = String.format(
                "SELECT t1.*, t2.%s, t2.%s "
                        + "FROM %s t1 "
                        + "LEFT JOIN %s t2 ON t1.%s = t2.%s "
                        + "%s "
                        + "ORDER BY t1.%s DESC, t1.%s DESC",
                TripCategoryRepository.COLUMN_NAME, TripCategoryRepository.COLUMN_ICON_ID, TABLE_NAME, TripCategoryRepository.TABLE_NAME, COLUMN_CATEGORY_ID, TripCategoryRepository.COLUMN_ID, whereClause, COLUMN_START_DATE, COLUMN_ID
        );
        try {
            Cursor c  = DatabaseHelper.getInstance().rawQuery(query, selectionArgs);
            Log.i(TAG, "Cursor: " + c);
            while(c.moveToNext()) {
                TripEntity trip = TripEntity.fromCursor(c);
                String cName = c.getString(c.getColumnIndexOrThrow(TripCategoryRepository.COLUMN_NAME));
                int cIconId = c.getInt(c.getColumnIndexOrThrow(TripCategoryRepository.COLUMN_ICON_ID));
                trip.getCategory().setName(cName);
                trip.getCategory().setIconId(cIconId);
                trips.add(trip);
                Log.d(TAG, "Getting row => " + trip);
            }
            c.close();
        }catch (Exception e) {
            Log.e(TAG, "Error getting row => " + e);
        }
        return trips;
    }

    @NonNull
    public List<TripEntity> searchByNameOrDestination(String query) {
        String where = "WHERE t2." + TripCategoryRepository.COLUMN_NAME + " LIKE ? OR t1." + COLUMN_DESTINATION + " LIKE ?";
        return getAll(where, new String[] {"%"+query+"%", "%"+query+"%"});
    }

    @Nullable
    public TripEntity getOneById(long id) {
        String query = String.format(
                "SELECT t1.*, t2.%s, t2.%s "
                        + "FROM %s t1 "
                        + "LEFT JOIN %s t2 ON t1.%s = t2.%s "
                        + "WHERE t1.%s = ?",
                TripCategoryRepository.COLUMN_NAME, TripCategoryRepository.COLUMN_ICON_ID, TABLE_NAME,
                TripCategoryRepository.TABLE_NAME, COLUMN_CATEGORY_ID, COLUMN_ID, COLUMN_ID
        );
        TripEntity trip = null;
        try {
            Cursor c = DatabaseHelper.getInstance().rawQuery(query, new String[] {id+""});
            if(c.moveToFirst()) {
                trip = TripEntity.fromCursor(c);
                TripCategoryEntity category = TripCategoryEntity.fromCursor(c);
                category.setId(trip.getCategory().getId());
                trip.setCategory(category);
                List<ExpenseEntity> expenses = (new ExpenseRepository()).getAllByTripId(trip.getId());
                trip.setExpenses(expenses);
                Log.d(TAG, "Getting row => " + trip);
            }
            c.close();
        }catch (Exception e) {
            Log.e(TAG, "Error getting row => " + e);
        }
        return trip;
    }

    public boolean insertOne(@NonNull TripEntity trip) {
        try {
            long result = DatabaseHelper.getInstance().insertOrThrow(TABLE_NAME, null, trip.toContentValuesWithoutId());
            Log.d(TAG, "Inserted row with id " + result);
            return true;
        }catch (Exception e) {
            Log.e(TAG, "Error inserting row => " + e);
            return false;
        }
    }

    public boolean updateOne(@NonNull TripEntity trip) {
        try {
            int number =  DatabaseHelper.getInstance().update(TABLE_NAME, trip.toContentValuesWithoutId(), COLUMN_ID + " = " + trip.getId(), null);
            Log.i(TAG, "Updating row => the number of rows affected is " + number);
            return number != 0;
        }catch (Exception e) {
            Log.e(TAG, "Error updating row => " + e);
            return false;
        }
    }

    public boolean deleteOneById(long id) {
        try {
            int number = DatabaseHelper.getInstance().delete(TABLE_NAME, COLUMN_ID + " = " + id, null);
            Log.i(TAG, "Deleting row => the number of rows affected is " + number);
            return number != 0;
        }catch (Exception e) {
            Log.e(TAG, "Error deleting row => " + e);
            return false;
        }
    }

    public boolean deleteAll() {
        try {
            int number = DatabaseHelper.getInstance().delete(TABLE_NAME, null, null);
            Log.i(TAG, "Deleting all rows => the number of rows affected is " + number);
            return number != 0;
        }catch (Exception e) {
            Log.e(TAG, "Error deleting all rows => " + e);
            return false;
        }
    }
}
