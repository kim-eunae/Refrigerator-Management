package com.example.refrigerator.entity;

public class User {

    private String userId;              // 아이디
    private String password;            // 비밀번호

    private String name;                // 이름
    private String phone;               // 휴대번호
    private String email;               // 이메일

    private boolean vibration;          // 알림 진동여부
    private boolean silent;             // 알림 무음여부

    private boolean withdrawal;         // 탈퇴 여부

    private long joinTimeMillis;        // 가입일시를 millisecond 로 표현

    // 파이어 스토어를 사용하기 위해 필요한 생성자
    public User() {}

    public User(String userId, String password, String name, String phone, String email, long joinTimeMillis) {
        this.userId = userId;
        this.password = password;

        this.name = name;
        this.phone = phone;
        this.email = email;

        this.vibration = false;
        this.silent = false;

        this.withdrawal = false;
    }

    public String getUserId() {
        return userId;
    }

    public String getPassword() {
        return password;
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }

    public String getEmail() {
        return email;
    }

    public long getJoinTimeMillis() {
        return joinTimeMillis;
    }

    public boolean isVibration() {
        return vibration;
    }

    public boolean isSilent() {
        return silent;
    }

    public boolean isWithdrawal() {
        return withdrawal;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setVibration(boolean vibration) {
        this.vibration = vibration;
    }

    public void setSilent(boolean silent) {
        this.silent = silent;
    }

    public void setWithdrawal(boolean withdrawal) {
        this.withdrawal = withdrawal;
    }

}
