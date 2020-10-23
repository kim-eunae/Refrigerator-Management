package com.example.refrigerator.util;

public class Constants {

    public static final int DEFAULT_NOTIFICATION_HOUR = 8;                      // 디폴트 알림 시간

    /* SharedPreferences 관련 상수 */
    public static class SharedPreferencesName {
        public static final String USER_DOCUMENT_ID = "user_document_id";       // 사용자(회원) Fire store Document ID

        public static final String ALARM_NO = "alarm_no";                       // 알람번호
        public static final String ALARM_SILENT = "alarm_silent";               // 알람 무음여부
        public static final String ALARM_VIBRATION = "alarm_vibration";         // 알람 진동여부
    }

    /* Activity 요청 코드 */
    public static class RequestCode {
        public static final int JOIN = 0;                       // 회원가입
        public static final int ADD = 1;                        // 등록
        public static final int EDIT = 2;                       // 편집(수정)
        public static final int LIST = 3;                       // 리스트
    }

    /* 액티비티에서 프레그먼트에 요청할 작업 종류 */
    public static class FragmentTaskKind {
        public static final int REFRESH = 0;                    // 새로고침
        public static final int INFO = 1;                       // 정보보기
    }

    /* Fire store Collection 이름 */
    public static class FirestoreCollectionName {
        public static final String USER = "users";              // 사용자(회원)
        public static final String FOOD = "foods";              // 식품
        public static final String USER_FOOD = "userFoods";     // 사용자(회원)이 추가한 식품
    }

    /* 클릭 모드 */
    public static class ClickMode {
        public static final int NORMAL = 0;                             // 클릭
        public static final int LONG = 1;                               // 롱클릭
    }

    /* 로딩 딜레이 */
    public static class LoadingDelay {
        public static final int SHORT = 300;
        public static final int LONG = 1000;
    }
}
