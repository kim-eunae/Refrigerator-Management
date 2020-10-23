package com.example.refrigerator.entity;

public class UserFood {

    private String code;                // 코드
    private String name;                // 식품명
    private String expirationDate;      // 유통기한 (유효기간)

    private int alarmNo1;               // 1일 남았을 때 알람번호
    private int alarmNo3;               // 3일 남았을 때 알람번호

    private long createTimeMillis;      // 생성일시를 millisecond 로 표현

    // 파이어 스토어를 사용하기 위해 필요한 생성자
    public UserFood() {}

    public UserFood(String code, String name, String expirationDate,
                    int alarmNo1, int alarmNo3, long createTimeMillis) {
        this.code = code;
        this.name = name;
        this.expirationDate = expirationDate;
        this.alarmNo1 = alarmNo1;
        this.alarmNo3 = alarmNo3;
        this.createTimeMillis = createTimeMillis;
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

    public int getAlarmNo1() {
        return alarmNo1;
    }

    public int getAlarmNo3() {
        return alarmNo3;
    }

    public long getCreateTimeMillis() {
        return createTimeMillis;
    }
}
