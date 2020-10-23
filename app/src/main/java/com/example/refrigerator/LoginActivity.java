package com.example.refrigerator;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.refrigerator.entity.User;
import com.example.refrigerator.util.Constants;
import com.example.refrigerator.util.GlobalVariable;
import com.example.refrigerator.util.SharedPreferencesUtils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class LoginActivity extends AppCompatActivity {
    private static String TAG = LoginActivity.class.getSimpleName();

    private LinearLayout layLoading;                // 로딩 레이아웃

    private EditText editUserId, editPassword;

    private InputMethodManager imm;                 // 키보드를 숨기기 위해 필요함

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // 로딩 레이아웃
        this.layLoading = findViewById(R.id.layLoading);
        ((ProgressBar) findViewById(R.id.progressBar)).setIndeterminateTintList(ColorStateList.valueOf(Color.WHITE));

        this.editUserId = findViewById(R.id.editUserId);
        this.editUserId.setHint("ID");

        this.editPassword = findViewById(R.id.editPassword);
        this.editPassword.setHint("비밀번호");

        findViewById(R.id.btnLogin).setOnClickListener(mClickListener);
        findViewById(R.id.btnFindUserId).setOnClickListener(mClickListener);
        findViewById(R.id.btnFindPassword).setOnClickListener(mClickListener);
        findViewById(R.id.btnJoin).setOnClickListener(mClickListener);
        findViewById(R.id.layLoading).setOnClickListener(mClickListener);

        // 키보드를 숨기기 위해 필요함
        this.imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        this.editUserId.requestFocus();
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == Constants.RequestCode.JOIN) {
                // 회원가입 이후 로그인하기
                if (data != null) {
                    this.editUserId.setText(data.getStringExtra("userId"));
                    this.editPassword.setText(data.getStringExtra("password"));

                    this.layLoading.setVisibility(View.VISIBLE);
                    // 로딩 레이아웃을 표시하기 위해 딜레이를 줌
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            // 로그인
                            login();
                        }
                    }, Constants.LoadingDelay.SHORT);
                }
            }
        }
    }

    /* 입력 데이터 체크 */
    private boolean checkData() {
        // 아이디 입력 체크
        String userId = this.editUserId.getText().toString();
        if (TextUtils.isEmpty(userId)) {
            Toast.makeText(this, getString(R.string.msg_user_id_check_empty), Toast.LENGTH_SHORT).show();
            this.editUserId.requestFocus();
            return false;
        }

        // 비밀번호 입력 체크
        String password = this.editPassword.getText().toString();
        if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, getString(R.string.msg_password_check_empty), Toast.LENGTH_SHORT).show();
            this.editPassword.requestFocus();
            return false;
        }

        // 키보드 숨기기
        this.imm.hideSoftInputFromWindow(this.editPassword.getWindowToken(), 0);

        return true;
    }

    /* 로그인 */
    private void login() {
        final String userId = this.editUserId.getText().toString();
        final String password = this.editPassword.getText().toString();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference reference = db.collection(Constants.FirestoreCollectionName.USER);

        // 로그인
        Query query = reference.whereEqualTo("userId", userId).limit(1);
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                layLoading.setVisibility(View.GONE);

                if (task.isSuccessful()) {
                    if (task.getResult() != null) {
                        if (task.getResult().size() == 0) {
                            // 로그인 실패 (회원이 아님)
                            Toast.makeText(LoginActivity.this, getString(R.string.msg_login_user_none), Toast.LENGTH_SHORT).show();
                        } else {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, document.getId() + " => " + document.getData());

                                User user = document.toObject(User.class);
                                if (user.getPassword().equals(password)) {
                                    // 로그인 성공

                                    if (user.isWithdrawal()) {
                                        // 회원탈퇴한 계정
                                        Toast.makeText(LoginActivity.this, getString(R.string.msg_account_withdrawal), Toast.LENGTH_SHORT).show();
                                    } else {
                                        // Document Id 저장
                                        GlobalVariable.documentId = document.getId();

                                        // 사용자 객체 생성
                                        GlobalVariable.user = user;

                                        // SharedPreferences 에 알림 진동 및 무음 여부 저장
                                        SharedPreferencesUtils.getInstance(LoginActivity.this).put(Constants.SharedPreferencesName.ALARM_SILENT, user.isSilent());
                                        SharedPreferencesUtils.getInstance(LoginActivity.this).put(Constants.SharedPreferencesName.ALARM_VIBRATION, user.isVibration());

                                        // SharedPreferences 에 록그인 정보 저장 (자동 로그인 기능)
                                        SharedPreferencesUtils.getInstance(LoginActivity.this)
                                                .put(Constants.SharedPreferencesName.USER_DOCUMENT_ID, GlobalVariable.documentId);

                                        // 메인 화면으로 이동
                                        goMain();
                                    }
                                } else {
                                    // 로그인 실패 (비밀번호 틀림)
                                    Toast.makeText(LoginActivity.this, getString(R.string.msg_login_password_wrong), Toast.LENGTH_SHORT).show();
                                }
                                break;
                            }
                        }
                    } else {
                        // 오류
                        Toast.makeText(LoginActivity.this, getString(R.string.msg_error), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // 오류
                    Toast.makeText(LoginActivity.this, getString(R.string.msg_error), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /* 메인화면으로 이동 */
    private void goMain() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);

        finish();
    }

    private View.OnClickListener mClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent;
            switch (v.getId()) {
                case R.id.btnLogin:
                    // 로그인
                    if (checkData()) {
                        layLoading.setVisibility(View.VISIBLE);
                        // 로딩 레이아웃을 표시하기 위해 딜레이를 줌
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                // 로그인
                                login();
                            }
                        }, Constants.LoadingDelay.SHORT);
                    }
                    break;
                case R.id.btnFindUserId:
                    // ID 찾기
                    intent = new Intent(LoginActivity.this, FindUserIdActivity.class);
                    startActivity(intent);
                    break;
                case R.id.btnFindPassword:
                    // 비밀번호 찾기
                    intent = new Intent(LoginActivity.this, FindPasswordActivity.class);
                    startActivity(intent);
                    break;
                case R.id.btnJoin:
                    // 회원가입
                    intent = new Intent(LoginActivity.this, JoinActivity.class);
                    startActivityForResult(intent, Constants.RequestCode.JOIN);
                    break;
                case R.id.layLoading:
                    // 로딩중 클릭 방지
                    break;
            }
        }
    };
}
