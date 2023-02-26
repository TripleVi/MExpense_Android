package com.example.mexpense.entities;

import android.content.ContentValues;
import android.database.Cursor;

import androidx.annotation.NonNull;

import com.example.mexpense.repositories.TripRepository;
import com.example.mexpense.utilities.Constants;
import com.google.android.material.datepicker.MaterialDatePicker;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TripEntity {
    private long id;
    private String destination;
    private long startDate;
    private long endDate;
    private String description;
    private boolean requiredRiskAssessment;
    private double total;
    private TripCategoryEntity category;
    private List<ExpenseEntity> expenses;

    public TripEntity() {
        id = Constants.NEW_ID;
        destination = Constants.EMPTY_STRING;
        startDate = MaterialDatePicker.todayInUtcMilliseconds();
        endDate = MaterialDatePicker.todayInUtcMilliseconds();
        description = Constants.EMPTY_STRING;
        requiredRiskAssessment = false;
        total = 0;
        category = new TripCategoryEntity();
        expenses = new ArrayList<>();
    }

    public TripEntity(long id, @NonNull String des, long sDate, long eDate, @NonNull String desc, boolean risk, @NonNull TripCategoryEntity category) {
        this();
        setId(id);
        setDestination(des);
        setStartDate(sDate);
        setEndDate(eDate);
        setDescription(desc);
        setRequiredRiskAssessment(risk);
        setCategory(category);
    }

    public TripEntity(@NonNull String des, long sDate, long eDate, @NonNull String desc, boolean risk, @NonNull  TripCategoryEntity category) {
        this(Constants.NEW_ID, des, sDate, eDate, desc, risk, category);
    }

    @NonNull
    public static TripEntity fromCursor(@NonNull Cursor c) {
        long id = c.getLong(c.getColumnIndexOrThrow(TripRepository.COLUMN_ID));
        String destination = c.getString(c.getColumnIndexOrThrow(TripRepository.COLUMN_DESTINATION));
        long startDate = c.getLong(c.getColumnIndexOrThrow(TripRepository.COLUMN_START_DATE));
        long endDate = c.getLong(c.getColumnIndexOrThrow(TripRepository.COLUMN_END_DATE));
        String description = c.getString(c.getColumnIndexOrThrow(TripRepository.COLUMN_DESCRIPTION));
        boolean requiredRiskAssessment = c.getShort(c.getColumnIndexOrThrow(TripRepository.COLUMN_REQUIRED_RISK_ASSESSMENT)) != 0;
        double total = c.getDouble(c.getColumnIndexOrThrow(TripRepository.COLUMN_TOTAL));
        long categoryId = c.getLong(c.getColumnIndexOrThrow(TripRepository.COLUMN_CATEGORY_ID));
        TripCategoryEntity category = new TripCategoryEntity();
        category.setId(categoryId);
        TripEntity trip = new TripEntity(id, destination, startDate, endDate, description, requiredRiskAssessment, category);
        trip.setTotal(total);
        return trip;
    }

    @NonNull
    public ContentValues toContentValuesWithoutId() {
        ContentValues rowValues = new ContentValues();
        rowValues.put(TripRepository.COLUMN_DESTINATION, destination);
        rowValues.put(TripRepository.COLUMN_START_DATE, startDate);
        rowValues.put(TripRepository.COLUMN_END_DATE, endDate);
        rowValues.put(TripRepository.COLUMN_DESCRIPTION, description);
        rowValues.put(TripRepository.COLUMN_REQUIRED_RISK_ASSESSMENT, requiredRiskAssessment);
        rowValues.put(TripRepository.COLUMN_TOTAL, total);
        rowValues.put(TripRepository.COLUMN_CATEGORY_ID, category.getId());
        return rowValues;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @NonNull
    public String getDestination() {
        return destination;
    }

    public void setDestination(@NonNull String destination) {
        this.destination = Objects.requireNonNull(destination);
    }

    public long getStartDate() {
        return startDate;
    }

    public void setStartDate(long startDate) {
        this.startDate = startDate;
    }

    public long getEndDate() {
        return endDate;
    }

    public void setEndDate(long endDate) {
        this.endDate = endDate;
    }

    @NonNull
    public String getDescription() {
        return description;
    }

    public void setDescription(@NonNull String description) {
        this.description = Objects.requireNonNull(description);
    }

    public boolean getRequiredRiskAssessment() {
        return requiredRiskAssessment;
    }

    public void setRequiredRiskAssessment(boolean requiredRiskAssessment) {
        this.requiredRiskAssessment = requiredRiskAssessment;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) throws IllegalArgumentException {
        if(total < 0) throw new IllegalArgumentException("Total cannot be less than 0!");
        this.total = total;
    }

    @NonNull
    public TripCategoryEntity getCategory() {
        return category;
    }

    public void setCategory(@NonNull TripCategoryEntity category) {
        this.category = Objects.requireNonNull(category);
    }

    @NonNull
    public List<ExpenseEntity> getExpenses() {
        return expenses;
    }

    public void setExpenses(@NonNull List<ExpenseEntity> expenses) {
        this.expenses = expenses;
    }

    @NonNull
    @Override
    public String toString() {
        return "TripEntity{" +
                "id=" + id +
                ", destination='" + destination + '\'' +
                ", startDate='" + startDate + '\'' +
                ", endDate='" + endDate + '\'' +
                ", description='" + description + '\'' +
                ", riskAssessment=" + requiredRiskAssessment +
                ", total=" + total +
                ", category=" + category +
                '}';
    }
}
