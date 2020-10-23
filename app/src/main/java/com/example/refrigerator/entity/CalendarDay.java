package com.example.refrigerator.entity;

public class CalendarDay {
    public String day;
    public int week;

    public int count;                           // 유통기한 마지막 날자인 식품수

    public boolean today;

    public CalendarDay(String day, int week, boolean today) {
        this.day = day;
        this.week = week;
        this.today = today;

        this.count = 0;
    }
}