package com.example.refrigerator.entity;

import android.os.Parcel;
import android.os.Parcelable;

/*
Parcelable 타입의 객체만 Intent 을 통해 Activity 간 데이터를 넘길 수 있음
 */
public class Food implements Parcelable {

    private String code;                // 코드
    private String name;                // 식품명
    private String expirationDate;      // 유통기한 (유효기간)

    // 파이어 스토어를 사용하기 위해 필요한 생성자
    public Food() {}

    public Food(Parcel in) {
        readFromParcel(in);
    }

    public Food(String code, String name, String expirationDate) {
        this.code = code;
        this.name = name;
        this.expirationDate = expirationDate;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getExpirationDate() {
        return expirationDate;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.code);
        dest.writeString(this.name);
        dest.writeString(this.expirationDate);
    }

    private void readFromParcel(Parcel in){
        this.code = in.readString();
        this.name = in.readString();
        this.expirationDate = in.readString();
    }

    public static final Creator CREATOR = new Creator() {
        public Food createFromParcel(Parcel in) {
            return new Food(in);
        }

        public Food[] newArray(int size) {
            return new Food[size];
        }
    };
}
