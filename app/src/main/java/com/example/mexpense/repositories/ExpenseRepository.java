package com.example.mexpense.repositories;

import android.database.Cursor;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.mexpense.entities.ExpenseCategoryEntity;
import com.example.mexpense.entities.ExpenseEntity;
import com.example.mexpense.entities.TripEntity;
import com.example.mexpense.utilities.Constants;

import java.util.ArrayList;
import java.util.List;

public class ExpenseRepository {
    public static final String TABLE_NAME = "expenses";

    public static final String COLUMN_ID = "id";
    public static final String COLUMN_DATE = "date";
    public static final String COLUMN_COST = "cost";
    public static final String COLUMN_COMMENTS = "comments";
    public static final String COLUMN_LATITUDE = "latitude";
    public static final String COLUMN_LONGITUDE = "longitude";
    public static final String COLUMN_IMAGE_NAME = "image_name";
    public static final String COLUMN_CATEGORY_ID = "category_id";
    public static final String COLUMN_TRIP_ID = "trip_id";

    public static final String TAG = "ExpenseRepository";

    public static final String CREATE_TABLE = String.format(
            "CREATE TABLE %s("
                    + "%s INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "%s INTEGER NOT NULL, "
                    + "%s REAL NOT NULL, "
                    + "%s TEXT, "
                    + "%s REAL, "
                    + "%s REAL, "
                    + "%s TEXT, "
                    + "%s INTEGER NOT NULL, "
                    + "%s INTEGER NOT NULL REFERENCES trips(id) ON DELETE CASCADE)",
            TABLE_NAME, COLUMN_ID, COLUMN_DATE, COLUMN_COST, COLUMN_COMMENTS, COLUMN_LATITUDE, COLUMN_LONGITUDE, COLUMN_IMAGE_NAME, COLUMN_CATEGORY_ID, COLUMN_TRIP_ID
    );

    @NonNull
    public List<ExpenseEntity> getAll() {
        List<ExpenseEntity> expenses = new ArrayList<>();
        String query = String.format(
                "SELECT t1.*, t2.%s, t2.%s, t3.%s, t3.%s, t3.%s, t3.%s, t3.%s, t3.%s "
                        + "FROM %s t1 "
                        + "LEFT JOIN %s t2 ON t1.%s = t2.%s "
                        + "LEFT JOIN %s t3 ON t1.%s = t3.%s",
                ExpenseCategoryRepository.COLUMN_NAME, ExpenseCategoryRepository.COLUMN_ICON_ID, TripRepository.COLUMN_DESTINATION, TripRepository.COLUMN_START_DATE, TripRepository.COLUMN_END_DATE, TripRepository.COLUMN_DESCRIPTION, TripRepository.COLUMN_REQUIRED_RISK_ASSESSMENT, TripRepository.COLUMN_TOTAL, TABLE_NAME, ExpenseCategoryRepository.TABLE_NAME, COLUMN_CATEGORY_ID, COLUMN_ID, TripRepository.TABLE_NAME, COLUMN_TRIP_ID, COLUMN_ID
        );
        try {
            Cursor c = DatabaseHelper.getInstance().rawQuery(query, null);
            while(c.moveToNext()) {
                ExpenseEntity expense = ExpenseEntity.fromCursor(c);
                ExpenseCategoryEntity category = ExpenseCategoryEntity.fromCursor(c);
                category.setId(expense.getCategory().getId());
                expense.setCategory(category);
                TripEntity trip = TripEntity.fromCursor(c);
                trip.setId(expense.getTrip().getId());
                expense.setTrip(trip);
                expenses.add(expense);
                Log.d(TAG, "Getting row => " + expense);
            }
            c.close();
        }catch (Exception e) {
            Log.e(TAG, "Error getting row => " + e);
        }
        return expenses;
    }

    @NonNull
    public String uploadToCloud() {
        StringBuilder result = new StringBuilder("{\"userId\":\"vuongvvgch190692\", \"detailList\":[");
        String query =
                "SELECT t1.id, t1.date, t1.cost, t2.name type, t3.destination, t3.start_date, t3.description, t3.required_risk_assessment, t3.total, t4.name "
                        + "FROM expenses t1 "
                        + "LEFT JOIN expense_categories t2 ON t1.category_id = t2.id "
                        + "LEFT JOIN trips t3 ON t1.trip_id = t3.id "
                        + "LEFT JOIN trip_categories t4 ON t3.category_id = t4.id";
        try {
            Cursor c = DatabaseHelper.getInstance().rawQuery(query, null);
            while(c.moveToNext()) {
                String expenseJson = String.format("{\"id\":\"%s\", \"name\":\"%s\", \"destination\":\"%s\", \"start_date\":\"%s\", \"description\":\"%s\", \"required_risk_assessment\":\"%s\", \"total\":\"%s\", \"type\":\"%s\", \"date\":\"%s\", \"cost\":\"%s\"}",
                        c.getLong(0), c.getString(9), c.getString(4), c.getLong(5), c.getString(6), c.getShort(7) != 0, c.getDouble(8), c.getString(3), c.getLong(1), c.getDouble(2)
                );
                if(c.isFirst()) {
                    result.append(expenseJson);
                }else {
                    result.append(",").append(expenseJson);
                }
            }
            result.append("]}");
            c.close();
            return result.toString();
        }catch (Exception e) {
            Log.e(TAG, "Error getting rows => " + e);
            return Constants.EMPTY_STRING;
        }
    }

    @NonNull
    public List<ExpenseEntity> getAllByTripId(long id) {
        List<ExpenseEntity> expenses = new ArrayList<>();
        String query = String.format(
                "SELECT t1.*, t2.%s, t2.%s "
                        + "FROM %s t1 "
                        + "LEFT JOIN %s t2 ON t1.%s = t2.%s "
                        + "WHERE t1.%s = %s "
                        + "ORDER BY t1.%s DESC, t1.%s DESC",
                ExpenseCategoryRepository.COLUMN_NAME, ExpenseCategoryRepository.COLUMN_ICON_ID, TABLE_NAME, ExpenseCategoryRepository.TABLE_NAME, COLUMN_CATEGORY_ID, COLUMN_ID, COLUMN_TRIP_ID, id, COLUMN_DATE, COLUMN_ID
        );
        try {
            Cursor c = DatabaseHelper.getInstance().rawQuery(query, null);
            while(c.moveToNext()) {
                ExpenseEntity expense = ExpenseEntity.fromCursor(c);
                ExpenseCategoryEntity category = ExpenseCategoryEntity.fromCursor(c);
                category.setId(expense.getCategory().getId());
                expense.setCategory(category);
                expenses.add(expense);
                Log.d(TAG, "Getting row => " + expense);
            }
            c.close();
        }catch (Exception e) {
            Log.e(TAG, "Error getting row => " + e);
        }
        return expenses;
    }

    @Nullable
    public ExpenseEntity getOneById(long id) {
        String query = String.format(
                "SELECT t1.*, t2.%s, t2.%s, t3.%s, t3.%s, t3.%s, t3.%s, t3.%s, t3.%s, t3.%s "
                        + "FROM %s t1 "
                        + "LEFT JOIN %s t2 ON t1.%s = t2.%s "
                        + "LEFT JOIN %s t3 ON t1.%s = t3.%s "
                        + "WHERE t1.%s = %s",
                ExpenseCategoryRepository.COLUMN_NAME, ExpenseCategoryRepository.COLUMN_ICON_ID, TripRepository.COLUMN_DESTINATION, TripRepository.COLUMN_START_DATE, TripRepository.COLUMN_END_DATE, TripRepository.COLUMN_DESCRIPTION, TripRepository.COLUMN_REQUIRED_RISK_ASSESSMENT, TripRepository.COLUMN_TOTAL, TripRepository.COLUMN_CATEGORY_ID, TABLE_NAME, ExpenseCategoryRepository.TABLE_NAME, COLUMN_CATEGORY_ID, COLUMN_ID, TripRepository.TABLE_NAME, COLUMN_TRIP_ID, COLUMN_ID, COLUMN_ID, id
        );
        ExpenseEntity expense = null;
        try {
            Cursor c = DatabaseHelper.getInstance().rawQuery(query, null);
            if(c.moveToFirst()) {
                expense = ExpenseEntity.fromCursor(c);
                ExpenseCategoryEntity category = ExpenseCategoryEntity.fromCursor(c);
                category.setId(expense.getCategory().getId());
                expense.setCategory(category);
                TripEntity trip = TripEntity.fromCursor(c);
                trip.setId(expense.getTrip().getId());
                expense.setTrip(trip);
                Log.d(TAG, "Getting row => " + expense);
            }
            c.close();
        }catch (Exception e) {
            Log.e(TAG, "Error getting row => " + e);
        }
        return expense;
    }

    public boolean insertOne(@NonNull ExpenseEntity expense) {
        try {
            long result = DatabaseHelper.getInstance().insertOrThrow(TABLE_NAME, null, expense.toContentValuesWithoutId());
            Log.d(TAG, "Inserted row with id " + result);
            return (new TripRepository()).updateOne(expense.getTrip());
        }catch (Exception e) {
            Log.e(TAG, "Error inserting row => " + e);
            return false;
        }
    }

    public boolean updateOne(@NonNull ExpenseEntity expense) {
        try {
            String whereClause = String.format("%s = %s", COLUMN_ID, expense.getId());
            int number =  DatabaseHelper.getInstance().update(TABLE_NAME, expense.toContentValuesWithoutId(), whereClause, null);
            Log.i(TAG, "Updating row => the number of rows affected is " + number);
            boolean isSuccess = (new TripRepository()).updateOne(expense.getTrip());
            return number != 0 && isSuccess;
        }catch (Exception e) {
            Log.e(TAG, "Error updating row => " + e);
            return false;
        }
    }

    public boolean deleteOneById(@NonNull ExpenseEntity expense) {
        try {
            int number = DatabaseHelper.getInstance().delete(TABLE_NAME, COLUMN_ID + " = " + expense.getId(), null);
            Log.i(TAG, "Deleting row => the number of rows affected is " + number);
            boolean isSuccess = (new TripRepository()).updateOne(expense.getTrip());
            return number != 0 && isSuccess;
        }catch (Exception e) {
            Log.e(TAG, "Error deleting row => " + e);
            return false;
        }
    }

    public boolean deleteAll(@NonNull TripEntity trip) {
        try {
            int number = DatabaseHelper.getInstance().delete(TABLE_NAME, null, null);
            Log.i(TAG, "Deleting all rows => the number of rows affected is " + number);
            trip.setTotal(0);
            boolean isSuccess = (new TripRepository()).updateOne(trip);
            return number != 0 && isSuccess;
        }catch (Exception e) {
            Log.e(TAG, "Error deleting all rows => " + e);
            return false;
        }
    }
}
