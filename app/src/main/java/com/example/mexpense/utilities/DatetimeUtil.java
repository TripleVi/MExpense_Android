package com.example.mexpense.utilities;

import androidx.annotation.Nullable;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public final class DatetimeUtil {
    public static String getDate(long UTCMilliseconds, @Nullable DateTimeFormatter formatter) {
//        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
//        calendar.setTimeInMillis(UTCMilliseconds);
//        int mYear = calendar.get(Calendar.YEAR);
//        int mMonth = calendar.get(Calendar.MONTH);
//        int mDay = calendar.get(Calendar.DAY_OF_MONTH);

        String date = new Date(UTCMilliseconds).toString(); //Tue Sep 20 07:00:00 GMT+07:00 2022
        LocalDateTime datetime = LocalDateTime.parse(date, DateTimeFormatter.ofPattern("EEE LLL dd HH:mm:ss O yyyy"));
        if(formatter == null) formatter = DateTimeFormatter.ofPattern("LLL dd, yyyy");
        return datetime.format(formatter);
    }
}
