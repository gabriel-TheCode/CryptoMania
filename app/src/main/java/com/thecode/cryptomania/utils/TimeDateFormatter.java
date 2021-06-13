package com.thecode.cryptomania.utils;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Ryan on 1/14/2018.
 */

public class TimeDateFormatter extends ValueFormatter {

    @Override
    public String getFormattedValue(float unixSeconds, AxisBase axis) {
        Date date = new Date((long)unixSeconds);
        SimpleDateFormat sdf = new SimpleDateFormat("HH:MM", Locale.ENGLISH);
        return sdf.format(date);
    }
}