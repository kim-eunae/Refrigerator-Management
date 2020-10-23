package com.example.refrigerator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.refrigerator.entity.Food;
import com.example.refrigerator.fragment.CalendarFragment;
import com.example.refrigerator.fragment.HistoryFragment;
import com.example.refrigerator.fragment.SettingFragment;
import com.example.refrigerator.fragment.abstracts.IFragment;
import com.example.refrigerator.listener.IClickListener;
import com.example.refrigerator.popupwindow.CodeInputPopup;
import com.example.refrigerator.popupwindow.FoodAddMethodSelectPopup;
import com.example.refrigerator.util.Constants;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class MainActivity extends AppCompatActivity {
    private static String TAG = MainActivity.class.getSimpleName();

    private BackPressHandler backPressHandler;

    private Fragment fragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 제목 표시
        setTitle(getString(R.string.activity_title_main));

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // 네비게이션 뷰 (하단에 표시되는 메뉴)
        BottomNavigationView bottomNavigationView = findViewById(R.id.navigationView);
        bottomNavigationView.setSelectedItemId(R.id.menu_button_calendar);
        bottomNavigationView.setOnNavigationItemSelectedListener(mItemSelectedListener);

        // 종료 핸들러
        this.backPressHandler = new BackPressHandler(this);

        // Fragment 메니저를 이용해서 layContent 레이아웃에 Fragment 넣기
        this.fragment = new CalendarFragment();
        getSupportFragmentManager().beginTransaction().add(R.id.layContent, this.fragment).commit();
    }

    @Override
    public void onBackPressed() {
        this.backPressHandler.onBackPressed();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (requestCode == IntentIntegrator.REQUEST_CODE) {
            if(result != null) {
                if(result.getContents() == null) {
                    Toast.makeText(this, getString(R.string.msg_qr_code_scan_failure), Toast.LENGTH_LONG).show();
                } else {
                    // QR 코드 인식 성공
                    String code = result.getContents();
                    Log.d(TAG, "qr code:" + code);

                    // 식품정보
                    infoFood(code);
                }
            } else {
                super.onActivityResult(requestCode, resultCode, data);
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == Constants.RequestCode.ADD) {
                // 식품이 추가된 후
                Log.d(TAG, "add");
                ((IFragment) this.fragment).task(Constants.FragmentTaskKind.REFRESH, null);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // main 메뉴 생성
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_plus) {
            // 추가
            // 식품 선택 팝업창 호출
            onPopupFoodAddMethodSelect();
            return true;
        }

        return super.onOptionsItemSelected(item);
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
                        Toast.makeText(MainActivity.this, getString(R.string.msg_food_empty), Toast.LENGTH_SHORT).show();
                    } else {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Log.d(TAG, document.getId() + " => " + document.getData());

                            // 식품 객체 생성
                            Food food = document.toObject(Food.class);

                            // 식품추가 Activity 로 이동
                            Intent intent = new Intent(MainActivity.this, FoodAddActivity.class);
                            intent.putExtra("food", food);      // 식품 객체 넘김
                            startActivityForResult(intent, Constants.RequestCode.ADD);
                            break;
                        }
                    }
                } else {
                    // 오류
                    Toast.makeText(MainActivity.this, getString(R.string.msg_error), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /* 식품 선택 팝업창 호출 */
    private void onPopupFoodAddMethodSelect() {
        View popupView = View.inflate(this, R.layout.popup_food_add_method_select, null);
        FoodAddMethodSelectPopup popup = new FoodAddMethodSelectPopup(popupView);
        popup.setClickListener(mCListener);
        // Back 키 눌렸을때 닫기 위함
        popup.setFocusable(true);
        popup.showAtLocation(popupView, Gravity.CENTER, 0, 0);
    }

    /* 코드 입력 팝업창 호출 */
    private void onPopupCodeInput() {
        View popupView = View.inflate(this, R.layout.popup_code_input, null);
        CodeInputPopup popup = new CodeInputPopup(popupView);
        popup.setClickListener(mCListener);
        // Back 키 눌렸을때 닫기 위함
        popup.setFocusable(true);
        popup.showAtLocation(popupView, Gravity.CENTER, 0, 0);
    }

    /* 종료 */
    public void end() {
        moveTaskToBack(true);
        finish();
    }

    /* 팝업창에서 사용할 클릭 리스너 */
    private IClickListener mCListener = new IClickListener() {
        @Override
        public void onClick(Bundle bundle, int id) {
            switch (id) {
                case R.id.layQRCode:
                    // QR 코드로 촬영하기
                    IntentIntegrator intentIntegrator = new IntentIntegrator(MainActivity.this);
                    // QR 코드 인식시 소리
                    intentIntegrator.setBeepEnabled(true);
                    intentIntegrator.initiateScan();
                    break;
                case R.id.layCode:
                    // 코드 직접 입력하기
                    onPopupCodeInput();
                    break;
                case R.id.btnOk:
                    // 코드 입력후
                    String code = bundle.getString("code");
                    Log.d(TAG, "code:" + code);

                    // 식품정보
                    infoFood(code);
                    break;
            }
        }
    };

    /* BottomNavigationView 선택 리스너 */
    private BottomNavigationView.OnNavigationItemSelectedListener mItemSelectedListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
            switch (menuItem.getItemId()) {
                case R.id.menu_button_history:
                    // 내역
                    setTitle(getString(R.string.activity_title_history));
                    fragment = new HistoryFragment();
                    break;
                case R.id.menu_button_calendar:
                    // 달력
                    setTitle(getString(R.string.activity_title_main));
                    fragment = new CalendarFragment();
                    break;
                case R.id.menu_button_setting:
                    // 설정
                    setTitle(getString(R.string.activity_title_setting));
                    fragment = new SettingFragment();
                    break;
            }

            getSupportFragmentManager().beginTransaction().replace(R.id.layContent, fragment).commit();
            return true;
        }
    };

    /* Back Press Class */
    private class BackPressHandler {
        private Context context;
        private Toast toast;

        private final long FINISH_INTERVAL_TIME = 2000;
        private long backPressedTime = 0;

        public BackPressHandler(Context context) {
            this.context = context;
        }

        public void onBackPressed() {
            if (System.currentTimeMillis() > this.backPressedTime + FINISH_INTERVAL_TIME) {
                this.backPressedTime = System.currentTimeMillis();

                this.toast = Toast.makeText(this.context, R.string.msg_back_press_end, Toast.LENGTH_SHORT);
                this.toast.show();

                return;
            }

            if (System.currentTimeMillis() <= this.backPressedTime + FINISH_INTERVAL_TIME) {
                // 종료
                end();
                this.toast.cancel();
            }
        }
    }
}