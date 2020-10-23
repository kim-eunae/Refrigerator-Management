package com.example.refrigerator.fragment;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.refrigerator.FoodAddActivity;
import com.example.refrigerator.JoinActivity;
import com.example.refrigerator.MainActivity;
import com.example.refrigerator.R;
import com.example.refrigerator.entity.Food;
import com.example.refrigerator.entity.User;
import com.example.refrigerator.entity.UserFood;
import com.example.refrigerator.fragment.abstracts.IFragment;
import com.example.refrigerator.listener.IClickListener;
import com.example.refrigerator.popupwindow.CodeInputPopup;
import com.example.refrigerator.receiver.AlarmReceiver;
import com.example.refrigerator.util.Constants;
import com.example.refrigerator.util.GlobalVariable;
import com.example.refrigerator.util.SharedPreferencesUtils;
import com.example.refrigerator.util.Utils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import static android.content.Context.ALARM_SERVICE;

public class FoodAddFragment extends Fragment implements IFragment {
    private static final String TAG = FoodAddFragment.class.getSimpleName();

    private LinearLayout layLoading;

    private TextView txtCode, txtFoodName, txtExpirationDate;

    private Food food;                      // 식품 객체

    /* Activity 에서 Fragment 로 값 넘기기 위함 */
    public static FoodAddFragment getInstance(Food food) {
        FoodAddFragment fragment = new FoodAddFragment();

        Bundle bundle = new Bundle();
        bundle.putParcelable("food", food);

        // Argument 에 값 설정
        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Argument 에서 값 얻기
        Bundle bundle = getArguments();

        if (bundle != null) {
            this.food = bundle.getParcelable("food");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_food_add, container, false);

        // 로딩 레이아웃
        this.layLoading = view.findViewById(R.id.layLoading);
        ((ProgressBar) view.findViewById(R.id.progressBar)).setIndeterminateTintList(ColorStateList.valueOf(Color.WHITE));

        this.txtCode = view.findViewById(R.id.txtCode);
        this.txtFoodName = view.findViewById(R.id.txtFoodName);
        this.txtExpirationDate = view.findViewById(R.id.txtExpirationDate);

        view.findViewById(R.id.btnAdd).setOnClickListener(mClickListener);
        view.findViewById(R.id.btnQRCode).setOnClickListener(mClickListener);
        view.findViewById(R.id.btnCode).setOnClickListener(mClickListener);
        view.findViewById(R.id.layLoading).setOnClickListener(mClickListener);

        // 식품 정보 표시
        displayFood();

        return view;
    }

    @Override
    public void task(int kind, Bundle bundle) {
        if (kind == Constants.FragmentTaskKind.INFO) {
            String code = bundle.getString("code");
            Log.d(TAG, "qr code:" + code);

            // 식품정보
            infoFood(code);
        }
    }

    /* 제품식품 정보 표시 */
    private void displayFood() {
        this.txtCode.setText(this.food.getCode());                      // 코드
        this.txtFoodName.setText(this.food.getName());                  // 식품명
        this.txtExpirationDate.setText(this.food.getExpirationDate());  // 유통기한
    }

    /* 식품 정보 */
    private void infoFood(String code) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference reference = db.collection(Constants.FirestoreCollectionName.FOOD);

        // 식품찾기
        Query query = reference.whereEqualTo("code", code).limit(1);
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    if (task.getResult().size() == 0) {
                        // 찾기 실패
                        Toast.makeText(getContext(), getString(R.string.msg_food_empty), Toast.LENGTH_SHORT).show();
                    } else {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Log.d(TAG, document.getId() + " => " + document.getData());

                            // 식품 객체 생성
                            food = document.toObject(Food.class);

                            // 식품 정보 표시
                            displayFood();
                            break;
                        }
                    }
                } else {
                    // 오류
                    Toast.makeText(getContext(), getString(R.string.msg_error), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /* 날자 체크 */
    private boolean checkDate() {
        // 현재일
        Calendar calendarNow = Utils.getCalendar(Utils.getCurrentDate(), "yyyy-MM-dd");
        // 유통기한 날자
        Calendar calendar = Utils.getCalendar(this.food.getExpirationDate(), "yyyy-MM-dd");

        // 현재일보다 이전이면
        if (calendar.before(calendarNow)) {
            Toast.makeText(getContext(), getString(R.string.msg_expiration_date_expired), Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    /* 식품 추가 (이미 등록된 식품인지 먼저 체크) */
    private void addFood() {
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference reference = db.collection(Constants.FirestoreCollectionName.USER)
                .document(GlobalVariable.documentId).collection(Constants.FirestoreCollectionName.USER_FOOD);

        // 식품 중복 체크
        Query query = reference.whereEqualTo("code", this.food.getCode());
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    if (task.getResult() != null) {
                        if (task.getResult().size() == 0) {
                            // 식품 중복 아님

                            // 알람 번호
                            int alarmNo = SharedPreferencesUtils.getInstance(getContext()).get(Constants.SharedPreferencesName.ALARM_NO, 1);

                            // 현재일
                            Calendar calendarNow = Utils.getCalendar(Utils.getCurrentDate(), "yyyy-MM-dd");
                            // 유통기한 날자
                            Calendar calendar = Utils.getCalendar(food.getExpirationDate(), "yyyy-MM-dd");
                            // 3일전
                            calendar.add(Calendar.DATE, -3);

                            int alarmNo3 = 0;
                            if (calendarNow.before(calendar)) {
                                // 유통기한 3일전 알람설정
                                setAlarm(alarmNo, 3);
                                alarmNo3 = alarmNo;
                                alarmNo++;
                            }

                            // 1일전
                            calendar.add(Calendar.DATE, 2);

                            int alarmNo1 = 0;
                            if (calendarNow.before(calendar)) {
                                // // 유통기한 1일전 알람설정
                                setAlarm(alarmNo, 1);
                                alarmNo1 = alarmNo;
                            }

                            // 추가할 식품
                            UserFood userFood = new UserFood(food.getCode(), food.getName(), food.getExpirationDate(),
                                    alarmNo1, alarmNo3, System.currentTimeMillis());

                            // 식품 추가하기
                            db.collection(Constants.FirestoreCollectionName.USER)
                                    .document(GlobalVariable.documentId).collection(Constants.FirestoreCollectionName.USER_FOOD)
                                    .add(userFood)
                                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                        @Override
                                        public void onSuccess(DocumentReference documentReference) {
                                            // 성공
                                            layLoading.setVisibility(View.GONE);

                                            Toast.makeText(getContext(), getString(R.string.msg_food_add_complete), Toast.LENGTH_SHORT).show();

                                            // MainFragment 에 전달
                                            getActivity().setResult(Activity.RESULT_OK);
                                            getActivity().finish();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            // 추가 실패
                                            layLoading.setVisibility(View.GONE);
                                            Toast.makeText(getContext(), getString(R.string.msg_error), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        } else {
                            // 식품 중복
                            layLoading.setVisibility(View.GONE);
                            Toast.makeText(getContext(), getString(R.string.msg_food_check_overlap), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        layLoading.setVisibility(View.GONE);
                        Toast.makeText(getContext(), getString(R.string.msg_error), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // 오류
                    layLoading.setVisibility(View.GONE);
                    Toast.makeText(getContext(), getString(R.string.msg_error), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /* 알람 설정 */
    private void setAlarm(int alarmNo, int day) {
        /* 유통기한 날자
        Calendar calendar = Utils.getCalendar(food.getExpirationDate(), "yyyy-MM-dd");
        calendar.add(Calendar.DATE, (-day));

        // 알람 시간 설정
        calendar.set(Calendar.HOUR_OF_DAY, Constants.DEFAULT_NOTIFICATION_HOUR);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        */

        // 테스트를 위해 현재시간에서 1분 / 2분 이후로 설정
        Calendar calendar = Calendar.getInstance();
        if (day == 3) {
            calendar.add(Calendar.MINUTE, 1);
        } else {
            calendar.add(Calendar.MINUTE, 2);
        }

        String message = this.food.getName() + "의 생명이 " + day + "일 남았습니다!";

        // 알람 설정 ===============================================================================
        Intent alarmIntent = new Intent(getContext(), AlarmReceiver.class);
        alarmIntent.putExtra("alarm_no", alarmNo);      // 알람번호 전달
        alarmIntent.putExtra("message", message);       // 식품 + 유통기한 전달

        // no : requestCode
        PendingIntent alarmPendingIntent = PendingIntent.getBroadcast(getContext(), alarmNo, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager) getActivity().getSystemService(ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), alarmPendingIntent);
        // =========================================================================================

        // 알람 번호 증가
        SharedPreferencesUtils.getInstance(getContext()).put(Constants.SharedPreferencesName.ALARM_NO, (alarmNo + 1));

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Log.d(TAG, "alarm date:" + dateFormat.format(calendar.getTime()));
    }

    /* 코드 입력 팝업창 호출 */
    private void onPopupCodeInput() {
        View popupView = View.inflate(getContext(), R.layout.popup_code_input, null);
        CodeInputPopup popup = new CodeInputPopup(popupView);
        popup.setClickListener(mCListener);
        // Back 키 눌렸을때 닫기 위함
        popup.setFocusable(true);
        popup.showAtLocation(popupView, Gravity.CENTER, 0, 0);
    }

    /* 팝업창에서 사용할 클릭 리스너 */
    private IClickListener mCListener = new IClickListener() {
        @Override
        public void onClick(Bundle bundle, int id) {
            if (id == R.id.btnOk) {
                // 코드 입력후
                String code = bundle.getString("code");
                Log.d(TAG, "code:" + code);

                // 식품정보
                infoFood(code);
            }
        }
    };

    private View.OnClickListener mClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btnAdd:
                    // 식품추가

                    // 날자 체크
                    if (checkDate()) {
                        layLoading.setVisibility(View.VISIBLE);
                        // 로딩 레이아웃을 표시하기 위해 딜레이를 줌
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                // 식품 추가 (이미 등록된 식품인지 먼저 체크)
                                addFood();
                            }
                        }, Constants.LoadingDelay.SHORT);
                    }
                    break;
                case R.id.btnQRCode:
                    // QR 코드로 촬영하기
                    IntentIntegrator intentIntegrator = new IntentIntegrator(getActivity());
                    // QR 코드 인식시 소리
                    intentIntegrator.setBeepEnabled(true);
                    intentIntegrator.initiateScan();
                    break;
                case R.id.btnCode:
                    // 코드 직접 입력하기
                    onPopupCodeInput();
                    break;
                case R.id.layLoading:
                    // 로딩중 클릭 방지
                    break;
            }
        }
    };
}
