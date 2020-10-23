package com.example.refrigerator.util;

import android.app.Activity;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Pattern;

public class Utils {

    /* 현재날자 구하기 */
    public static String getCurrentDate() {
        return getCurrentDate("yyyy-MM-dd");
    }

    /* 현재날자 구하기 */
    public static String getCurrentDate(String format) {
        return getDate(format, System.currentTimeMillis());
    }

    /* 날자 구하기 */
    public static String getDate(String format, long timeMillis) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(format, Locale.getDefault());
        Date date = new Date(timeMillis);

        return dateFormat.format(date);
    }

    /* Calendar 얻기 */
    public static Calendar getCalendar(String date, String format) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(format, Locale.getDefault());
        Calendar calendar = Calendar.getInstance();

        try {
            Date d = dateFormat.parse(date);
            if (d != null) {
                calendar.setTime(d);
            }
        } catch (ParseException ignored) {}

        return calendar;
    }

    /* 두날자 사이의 차이 구하기 */
    public static int diffDate(String date1, String date2) {
        Calendar calendar1 = Utils.getCalendar(date1, "yyyy-MM-dd");
        Calendar calendar2 = Utils.getCalendar(date2, "yyyy-MM-dd");

        try {
            long day1 = calendar1.getTimeInMillis() / 86400000;    //->(24 * 60 * 60 * 1000) 24시간 60분 60초 * (ms초->초 변환 1000)
            long day2 = calendar2.getTimeInMillis() / 86400000;
            long count = day2 - day1;                              // day2 에서 day1 날자를 빼주게 됩니다.
            return (int) count;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    /* 휴대번호 체크 */
    public static boolean isPhoneNumber(String number) {
        String regEx = "^(010)-?(\\d{4})-?(\\d{4})$";
        if (number.indexOf("010") != 0) {
            regEx = "^(01(?:1|[6-9]))-?(\\d{3})-?(\\d{4})$";
        }

        return Pattern.matches(regEx, number);
    }

    /* 이메일 체크 */
    public static boolean isEmail(String email) {
        String regEx = "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

        return Pattern.matches(regEx, email);
    }

    /* 휴대번호 얻기 */
    public static String getPhoneNumber(Activity activity) {
        String number = "";

        try {
            TelephonyManager tel = (TelephonyManager) activity.getSystemService(Activity.TELEPHONY_SERVICE);
            number = tel.getLine1Number();

            if (!TextUtils.isEmpty(number)) {
                // "-", "+" 제거
                number = number.replace("-", "").replace("+", "");

                if (number.indexOf("82") == 0) {
                    number = "0" + number.substring(2);
                }
            }
        } catch (SecurityException ignored) {
        } catch (Exception e) {
        }

        return number;
    }

}
