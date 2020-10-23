package com.example.refrigerator.entity;

public class UserFoodItem {

    public String id;               // UserFood Doc ID
    public UserFood food;           // UserFood 객체

    public UserFoodItem(String id, UserFood food) {
        this.id = id;
        this.food = food;
    }
}
