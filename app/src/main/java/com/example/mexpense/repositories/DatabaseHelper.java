package com.example.mexpense.repositories;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import com.example.mexpense.R;

import com.example.mexpense.entities.ExpenseCategoryEntity;
import com.example.mexpense.entities.TripCategoryEntity;

public class DatabaseHelper extends SQLiteOpenHelper {
    public static final String DB_NAME = "MExpense";
    public static final int DB_VERSION = 1;
    public static final String TAG = "DatabaseHelper";

    private static SQLiteDatabase db;

    private DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    public static void connect(Context context) {
        if(db == null) {
            boolean doesExist = context.getDatabasePath(DB_NAME).exists();
            db = (new DatabaseHelper(context)).getWritableDatabase();
            if(!doesExist) {
                insertTripCategories();
                insertExpenseCategories();
            }
        }
    }

    public static SQLiteDatabase getInstance() throws NullPointerException {
        if(db == null) throw new NullPointerException("Connect to database before getting a database instance");
        return db;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TripCategoryRepository.CREATE_TABLE);
        db.execSQL(TripRepository.CREATE_TABLE);
        db.execSQL(ExpenseCategoryRepository.CREATE_TABLE);
        db.execSQL(ExpenseRepository.CREATE_TABLE);
        Log.i(TripRepository.TAG, TripRepository.TABLE_NAME + " table was created.");
        Log.i(TripCategoryRepository.TAG, TripCategoryRepository.TABLE_NAME + " table was created.");
        Log.i(ExpenseCategoryRepository.TAG, ExpenseCategoryRepository.TABLE_NAME + " table was created.");
        Log.i(ExpenseRepository.TAG, ExpenseRepository.TABLE_NAME + " table was created.");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
//         db.execSQL("DROP TABLE IF EXISTS " + TripRepository.TABLE_NAME);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        db.execSQL("PRAGMA foreign_keys=ON");
    }

    private static void insertTripCategories() {
        final TripCategoryRepository repo = new TripCategoryRepository();
        repo.insertOne(new TripCategoryEntity(-1, "conference", R.drawable.ic_conference_40));
        repo.insertOne(new TripCategoryEntity(-1, "event", R.drawable.ic_event_40));
        repo.insertOne(new TripCategoryEntity(-1, "internal meeting", R.drawable.ic_internal_meeting_40));
        repo.insertOne(new TripCategoryEntity(-1, "client meeting", R.drawable.ic_client_meeting_40));
//        repo.insertOne(new TripCategoryEntity(-1, "partner meeting", R.drawable.ic_partner_meeting_40));
        repo.insertOne(new TripCategoryEntity(-1, "company branch visit", R.drawable.ic_company_visit_40));
        repo.insertOne(new TripCategoryEntity(-1, "exhibition", R.drawable.ic_exhibition_40));
        repo.insertOne(new TripCategoryEntity(-1, "other", R.drawable.ic_other_40));
    }

    private static void insertExpenseCategories() {
        ExpenseCategoryRepository repo = new ExpenseCategoryRepository();
        repo.insertOne(new ExpenseCategoryEntity(-1, "food and beverages", R.drawable.ic_food_beverages_40));
        repo.insertOne(new ExpenseCategoryEntity(-1, "transportation", R.drawable.ic_taxi_40));
//        repo.insertOne(new ExpenseCategoryEntity(-1, "flight", R.drawable.ic_flight_40));
//        repo.insertOne(new ExpenseCategoryEntity(-1, "train", R.drawable.ic_train_40));
//        repo.insertOne(new ExpenseCategoryEntity(-1, "bus", R.drawable.ic_bus_40));
//        repo.insertOne(new ExpenseCategoryEntity(-1, "subway", R.drawable.ic_subway_40));
        repo.insertOne(new ExpenseCategoryEntity(-1, "accommodation", R.drawable.ic_accommodation_40));
        repo.insertOne(new ExpenseCategoryEntity(-1, "telephone", R.drawable.ic_telephone_40));
        repo.insertOne(new ExpenseCategoryEntity(-1, "clothes", R.drawable.ic_clothes_40));
        repo.insertOne(new ExpenseCategoryEntity(-1, "entertainment", R.drawable.ic_entertainment_40));
        repo.insertOne(new ExpenseCategoryEntity(-1, "other", R.drawable.ic_other_40));
    }
}
