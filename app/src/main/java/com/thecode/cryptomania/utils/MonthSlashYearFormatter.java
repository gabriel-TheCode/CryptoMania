package com.thecode.cryptomania.utils;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MonthSlashYearFormatter extends ValueFormatter {

    @Override
    public String getFormattedValue(float unixSeconds, AxisBase axis) {
        Date date = new Date((long)unixSeconds);
        SimpleDateFormat sdf = new SimpleDateFormat("MMM/yy", Locale.ENGLISH);
        return sdf.format(date);
    }
}