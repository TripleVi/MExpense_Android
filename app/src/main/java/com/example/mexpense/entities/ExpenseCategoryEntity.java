package com.example.mexpense.entities;

import android.content.ContentValues;
import android.database.Cursor;

import androidx.annotation.NonNull;

import com.example.mexpense.repositories.ExpenseCategoryRepository;
import com.example.mexpense.utilities.Constants;

import java.util.Objects;

public class ExpenseCategoryEntity {
    private long id;
    private String name;
    private int iconId;

    public ExpenseCategoryEntity() {
        id = Constants.NEW_ID;
        name = Constants.EMPTY_STRING;
        iconId = 0;
    }

    public ExpenseCategoryEntity(long id, @NonNull String name, int iconId) {
        setId(id);
        setName(name);
        setIconId(iconId);
    }

    @NonNull
    public static ExpenseCategoryEntity fromCursor(@NonNull Cursor c) {
        long id = c.getLong(c.getColumnIndexOrThrow(ExpenseCategoryRepository.COLUMN_ID));
        String name = c.getString(c.getColumnIndexOrThrow(ExpenseCategoryRepository.COLUMN_NAME));
        int iconId = c.getInt(c.getColumnIndexOrThrow(ExpenseCategoryRepository.COLUMN_ICON_ID));
        return new ExpenseCategoryEntity(id, name, iconId);
    }

    @NonNull
    public ContentValues toContentValuesWithoutId() {
        ContentValues rowValues = new ContentValues();
        rowValues.put(ExpenseCategoryRepository.COLUMN_NAME, name);
        rowValues.put(ExpenseCategoryRepository.COLUMN_ICON_ID, iconId);
        return rowValues;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @NonNull
    public String getName() {
        return name;
    }

    public void setName(@NonNull String name) {
        this.name = Objects.requireNonNull(name);
    }

    public int getIconId() {
        return iconId;
    }

    public void setIconId(int iconId) {
        this.iconId = iconId;
    }

    @NonNull
    @Override
    public String toString() {
        return "ExpenseCategoryEntity{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", iconId=" + iconId +
                '}';
    }
}
