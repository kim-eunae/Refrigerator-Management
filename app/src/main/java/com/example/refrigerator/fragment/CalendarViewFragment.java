package com.example.refrigerator.fragment;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.refrigerator.R;
import com.example.refrigerator.adapter.CalendarAdapter;
import com.example.refrigerator.entity.CalendarDay;
import com.example.refrigerator.entity.UserFood;
import com.example.refrigerator.fragment.abstracts.IFragment;
import com.example.refrigerator.listener.IAdapterOnClickListener;
import com.example.refrigerator.popupwindow.CodeInputPopup;
import com.example.refrigerator.popupwindow.FoodListPopup;
import com.example.refrigerator.util.Constants;
import com.example.refrigerator.util.GlobalVariable;
import com.example.refrigerator.util.MarginDecoration;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class CalendarViewFragment extends Fragment implements IFragment {
    private static String TAG = CalendarViewFragment.class.getSimpleName();

    private RecyclerView recyclerView;
    private CalendarAdapter adapter;

    private LinearLayout layLoading;

    // 페이지 위치
    private int position;

    // Calendar 객체의 현재 년/월/일 (월은 +1 해야 정확한 월이 구해짐)
    private int currentYear, currentMonth, currentDay;

    // 선택 Calendar
    private Calendar selectedCalendar;

    // 선택년월(yyyy-mm)
    private String selectedMonth;

    // 달력 일 레이아웃 사이즈
    private int layoutWidth;
    private int layoutHeight;

    private static final int GRID_ROW = 6;
    private static final int GRID_COL = 7;

    public static CalendarViewFragment getInstance(int position) {
        CalendarViewFragment fragment = new CalendarViewFragment();

        Bundle bundle = new Bundle();
        bundle.putInt("position", position);

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
            this.position = bundle.getInt("position", Integer.MIN_VALUE);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_calendar_view, container, false);

        // 리사이클러뷰 설정
        this.recyclerView = view.findViewById(R.id.recyclerView);
        RecyclerView.LayoutManager lm = new GridLayoutManager(getContext(), GRID_COL);
        this.recyclerView.setLayoutManager(lm);

        this.recyclerView.addItemDecoration(new MarginDecoration(getContext(), 0));
        this.recyclerView.setHasFixedSize(true);

        // 로딩 레이아웃
        this.layLoading = view.findViewById(R.id.layLoading);

        view.post(new Runnable() {
            @Override
            public void run() {
                if (position == 1) {
                    // 가운데 페이지이면 (첫 실행시 한번만 실행됨)
                    Calendar calendar = Calendar.getInstance();

                    // 첫 실행시 현재 년/월/일 구하기
                    currentYear = calendar.get(Calendar.YEAR);
                    currentMonth = calendar.get(Calendar.MONTH);
                    currentDay = calendar.get(Calendar.DAY_OF_MONTH);

                    // 달력 일 레이아웃 구하기
                    /* 해상도 */
                    Display display = getActivity().getWindowManager().getDefaultDisplay();
                    Point size = new Point();
                    display.getSize(size);

                    layoutWidth = (size.x / GRID_COL);

                    int height = recyclerView.getHeight();
                    layoutHeight = (height / GRID_ROW);

                    // 달력 생성
                    createCalendar(calendar);
                }
            }
        });

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == Constants.RequestCode.LIST) {
                // 리스트 정보가 변경되었으면 달력 새로고침
                createCalendar(this.selectedCalendar);
            }
        }
    }

    @Override
    public void task(int kind, Bundle bundle) {
        if (kind == Constants.FragmentTaskKind.REFRESH) {
            long timeMillis = bundle.getLong("time_millis");
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(timeMillis);

            // 달력 만들기
            createCalendar(calendar);
        }
    }

    /* 달력 만들기 */
    private void createCalendar(Calendar calendar) {
        ArrayList<CalendarDay> days = new ArrayList<>();
        // 6라인
        int max = (GRID_COL * GRID_ROW);

        // 일 초기화
        for (int i=0; i<max; i++) {
            days.add(new CalendarDay("", 0, false));
        }

        // 해당월 1일의 요일 구하기 위한 Calendar 객체
        Calendar c = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        // 해당 년/월의 1일로 설정
        c.set(year, month, 1);
        // 요일 (1(일요일) ... 7(토요일)
        int week = c.get(Calendar.DAY_OF_WEEK);

        // 월 최대일
        int dayMax = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

        int w = week;
        int day = 1;
        for (int i=(week - 1); i<max; i++) {
            if (day > dayMax) {
                break;
            }

            days.get(i).day = String.valueOf(day);

            // 요일
            if (w % 7 == 0) {
                days.get(i).week = w;
                w = 0;
            } else {
                days.get(i).week = w;
            }

            // 오늘인지 체크
            if (year == this.currentYear && month == this.currentMonth && day == currentDay) {
                days.get(i).today = true;
            }

            w++;
            day++;
        }

        // 선택 Calendar
        this.selectedCalendar = calendar;

        // 선택 년월
        this.selectedMonth = year + "-" +  String.format(Locale.getDefault(), "%02d", (month + 1));

        // 유통기한 마지막 날자인 식품 존재여부 표시 (리스트 구성된 다음에 변경사항 적용됨)
        setData(days, this.selectedMonth);

        this.adapter = new CalendarAdapter(mAdapterListener, days, this.layoutWidth, this.layoutHeight);
        this.recyclerView.setAdapter(this.adapter);

        this.layLoading.setVisibility(View.GONE);
    }

    /* 유통기한 마지막 날자인 식품 존재여부 표시 */
    private void setData(final ArrayList<CalendarDay> days, String month) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // 기간
        String date1 = month + "-01";
        String date2 = month + "-31";

        CollectionReference reference = db.collection(Constants.FirestoreCollectionName.USER)
                .document(GlobalVariable.documentId).collection(Constants.FirestoreCollectionName.USER_FOOD);

        // 유통기한 마지막 날자인 식품 얻기
        Query query = reference.whereGreaterThanOrEqualTo("expirationDate", date1)
                .whereLessThanOrEqualTo("expirationDate", date2)
                .orderBy("expirationDate");

        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    if (task.getResult() != null) {

                        // 달력의 시작 위치
                        int pos = 0;
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            // 추가한 식품
                            UserFood food = document.toObject(UserFood.class);

                            for (int i = pos; i < days.size(); i++) {
                                if (!TextUtils.isEmpty(days.get(i).day)) {
                                    String day = food.getExpirationDate().substring(8, 10);
                                    if (day.startsWith("0")) {
                                        day = day.substring(1, 2);
                                    }

                                    // 달력의 해당 날자이면
                                    if (day.equals(days.get(i).day)) {
                                        days.get(i).count++;
                                        pos = i;
                                        break;
                                    }
                                }
                            }
                        }

                        // 리스트에 적용
                        adapter.notifyDataSetChanged();
                    }
                } else {
                    Log.d(TAG, "error:" + task.getException().toString());
                    Toast.makeText(getContext(), getString(R.string.msg_error), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /* 유통기한 마지막 날자이 식품 팝업창 호출 */
    private void onPopupFoodList(String date) {
        View popupView = View.inflate(getContext(), R.layout.popup_food_list, null);
        FoodListPopup popup = new FoodListPopup(popupView, date);
        // Back 키 눌렸을때 닫기 위함
        popup.setFocusable(true);
        popup.showAtLocation(popupView, Gravity.CENTER, 0, 0);
    }

    private IAdapterOnClickListener mAdapterListener = new IAdapterOnClickListener() {
        @Override
        public void onItemClick(Bundle bundle, int id) {
            // 일 클릭
            String day = bundle.getString("day");

            if (TextUtils.isEmpty(day)) {
                return;
            }

            if (bundle.getInt("count", 0) == 0) {
                return;
            }

            // 선택일자
            String date = selectedMonth + "-" + String.format(Locale.getDefault(), "%02d", Integer.parseInt(day));
            Log.d(TAG, "date:" + date);

            // 유통기한 마지막 날자이 식품 팝업창 호출
            onPopupFoodList(date);
        }
    };
}
