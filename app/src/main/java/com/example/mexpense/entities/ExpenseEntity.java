package com.example.mexpense.entities;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.mexpense.repositories.ExpenseRepository;
import com.example.mexpense.utilities.Constants;
import com.google.android.material.datepicker.MaterialDatePicker;

import java.util.Map;
import java.util.Objects;

public class ExpenseEntity {
    private long id;
    private long date;
    private double cost;
    private String comments;
    private Double latitude;
    private Double longitude;
    private String imgName;
    private ExpenseCategoryEntity category;
    private TripEntity trip;

    public ExpenseEntity() {
        id = Constants.NEW_ID;
        date = MaterialDatePicker.todayInUtcMilliseconds();
        cost = 0;
        comments = Constants.EMPTY_STRING;
        category = new ExpenseCategoryEntity();
        trip = new TripEntity();
    }

    public ExpenseEntity(long id, long date, double cost, @NonNull String comments, @Nullable Double latitude, @Nullable Double longitude, String imgName, @NonNull ExpenseCategoryEntity category, @NonNull TripEntity trip) {
        setId(id);
        setDate(date);
        setCost(cost);
        setComments(comments);
        setLatitude(latitude);
        setLongitude(longitude);
        setImgName(imgName);
        setCategory(category);
        setTrip(trip);
    }

    public ExpenseEntity(long date, double cost, @NonNull String comments, @Nullable Double latitude, @Nullable Double longitude, String imgName, @NonNull ExpenseCategoryEntity category, @NonNull TripEntity trip) {
        this(Constants.NEW_ID, date, cost, comments, latitude, longitude, imgName, category, trip);
    }

    @NonNull
    public static ExpenseEntity fromCursor(@NonNull Cursor c) {
        long id = c.getLong(c.getColumnIndexOrThrow(ExpenseRepository.COLUMN_ID));
        long date = c.getLong(c.getColumnIndexOrThrow(ExpenseRepository.COLUMN_DATE));
        double cost = c.getDouble(c.getColumnIndexOrThrow(ExpenseRepository.COLUMN_COST));
        String comment = c.getString(c.getColumnIndexOrThrow(ExpenseRepository.COLUMN_COMMENTS));
        int index = c.getColumnIndexOrThrow(ExpenseRepository.COLUMN_LATITUDE);
        Double latitude = c.isNull(index) ? null : c.getDouble(index);
        index = c.getColumnIndexOrThrow(ExpenseRepository.COLUMN_LONGITUDE);
        Double longitude = c.isNull(index) ? null : c.getDouble(index);
        String imgName = c.getString(c.getColumnIndexOrThrow(ExpenseRepository.COLUMN_IMAGE_NAME));
        long categoryId = c.getLong(c.getColumnIndexOrThrow(ExpenseRepository.COLUMN_CATEGORY_ID));
        ExpenseCategoryEntity category = new ExpenseCategoryEntity();
        category.setId(categoryId);
        long tripId = c.getLong(c.getColumnIndexOrThrow(ExpenseRepository.COLUMN_TRIP_ID));
        TripEntity trip = new TripEntity();
        trip.setId(tripId);
        return new ExpenseEntity(id, date, cost, comment, latitude, longitude, imgName, category, trip);
    }

    @NonNull
    public ContentValues toContentValuesWithoutId() {
        ContentValues rowValues = new ContentValues();
        rowValues.put(ExpenseRepository.COLUMN_DATE, date);
        rowValues.put(ExpenseRepository.COLUMN_COST, cost);
        rowValues.put(ExpenseRepository.COLUMN_COMMENTS, comments);
        rowValues.put(ExpenseRepository.COLUMN_LATITUDE, latitude);
        rowValues.put(ExpenseRepository.COLUMN_LONGITUDE, longitude);
        rowValues.put(ExpenseRepository.COLUMN_IMAGE_NAME, imgName);
        rowValues.put(ExpenseRepository.COLUMN_CATEGORY_ID, category.getId());
        rowValues.put(ExpenseRepository.COLUMN_TRIP_ID, trip.getId());
        return rowValues;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public double getCost() {
        return cost;
    }

    public void setCost(double cost) {
        if(cost < 0) throw new IllegalArgumentException("Cost cannot less than 0.");
        this.cost = cost;
    }

    @NonNull
    public String getComments() {
        return comments;
    }

    public void setComments(@NonNull String comments) {
        this.comments = Objects.requireNonNull(comments);
    }

    public String getImgName() {
        return imgName;
    }

    public void setImgName(String imgName) {
        this.imgName = imgName;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    @NonNull
    public ExpenseCategoryEntity getCategory() {
        return category;
    }

    public void setCategory(@NonNull ExpenseCategoryEntity category) {
        this.category = Objects.requireNonNull(category);
    }

    @NonNull
    public TripEntity getTrip() {
        return trip;
    }

    public void setTrip(@NonNull TripEntity trip) {
//        this.trip = Objects.requireNonNull(trip);
        this.trip = trip;
    }

    @NonNull
    @Override
    public String toString() {
        return "ExpenseEntity{" +
                "id=" + id +
                ", date='" + date + '\'' +
                ", cost='" + cost + '\'' +
                ", comments='" + comments + '\'' +
                ", latitude='" + latitude + '\'' +
                ", longitude='" + longitude + '\'' +
                ", imgUri='" + imgName + '\'' +
                ", category=" + category +
                ", trip=" + trip +
                '}';
    }
}
