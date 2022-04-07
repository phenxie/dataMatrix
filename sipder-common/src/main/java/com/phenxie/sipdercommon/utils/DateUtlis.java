package com.phenxie.sipdercommon.utils;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class DateUtlis {

    public static String  getDateString1(Calendar c){
        SimpleDateFormat formater = new SimpleDateFormat(
                "yyyy-MM/dd");
        return formater.format(c.getTime());
    }
    public static String  getDateString2(Calendar c){
        SimpleDateFormat formater = new SimpleDateFormat(
                "yyyyMMdd");
        return formater.format(c.getTime());
    }
    public static String  getDateString3(Calendar c){
        SimpleDateFormat formater = new SimpleDateFormat(
                "yyyy-MM-dd");
        return formater.format(c.getTime());
    }
    public static String  yyyyMMdd(Calendar c){
        SimpleDateFormat formater = new SimpleDateFormat(
                "yyyyMMdd");
        return formater.format(c.getTime());
    }
    public static Calendar CreateCalendar(int year){
        Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR,year);
        c.set(Calendar.MONTH,0);
        c.set(Calendar.DAY_OF_YEAR, 1);
        return c;
    }
}
