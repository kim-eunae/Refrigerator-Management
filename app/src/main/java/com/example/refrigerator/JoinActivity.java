package com.example.refrigerator;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
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
import com.example.refrigerator.util.Utils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

public class JoinActivity extends AppCompatActivity {
    private static String TAG = JoinActivity.class.getSimpleName();

    private LinearLayout layLoading;                // 로딩 레이아웃

    private EditText editUserId, editPassword1, editPassword2;
    private EditText editName, editPhone, editEmail;

    private InputMethodManager imm;                 // 키보드를 숨기기 위해 필요함

    private final static int MIN_SIZE = 6;          // 아이디/비밀번호 최소 자리수

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join);

        // 로딩 레이아웃
        this.layLoading = findViewById(R.id.layLoading);
        ((ProgressBar) findViewById(R.id.progressBar)).setIndeterminateTintList(ColorStateList.valueOf(Color.WHITE));

        this.editName = findViewById(R.id.editName);
        this.editName.setHint("Name");

        this.editPhone = findViewById(R.id.editPhone);
        this.editPhone.setHint("Phone-Number");

        this.editEmail = findViewById(R.id.editEmail);
        this.editEmail.setHint("E-mail");

        this.editUserId = findViewById(R.id.editUserId);
        this.editUserId.setHint("ID (6글자 이상)");

        this.editPassword1 = findViewById(R.id.editPassword1);
        this.editPassword1.setHint("비밀번호 (6글자 이상)");

        this.editPassword2 = findViewById(R.id.editPassword2);
        this.editPassword2.setHint("비밀번호 확인");

        findViewById(R.id.btnJoin).setOnClickListener(mClickListener);
        findViewById(R.id.layLoading).setOnClickListener(mClickListener);

        // 키보드를 숨기기 위해 필요함
        this.imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        this.editUserId.requestFocus();
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

        // 아이디 자리수 체크
        if (userId.length() < MIN_SIZE) {
            Toast.makeText(this, getString(R.string.msg_user_id_check_length), Toast.LENGTH_SHORT).show();
            this.editUserId.requestFocus();
            return false;
        }

        // 이메일 입력 체크
        String email = this.editEmail.getText().toString();
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, getString(R.string.msg_email_check_empty), Toast.LENGTH_SHORT).show();
            this.editEmail.requestFocus();
            return false;
        }

        // 이메일 유효성 체크
        if (!Utils.isEmail(email)) {
            Toast.makeText(this, getString(R.string.msg_email_check_wrong), Toast.LENGTH_SHORT).show();
            this.editEmail.requestFocus();
            return false;
        }

        // 비밀번호 입력 체크
        String password1 = this.editPassword1.getText().toString();
        if (TextUtils.isEmpty(password1)) {
            Toast.makeText(this, getString(R.string.msg_password_check_empty), Toast.LENGTH_SHORT).show();
            this.editPassword1.requestFocus();
            return false;
        }

        // 비밀번호 자리수 체크
        if (password1.length() < MIN_SIZE) {
            Toast.makeText(this, getString(R.string.msg_password_check_length), Toast.LENGTH_SHORT).show();
            this.editPassword1.requestFocus();
            return false;
        }

        // 비밀번호 확인 체크
        String password2 = this.editPassword2.getText().toString();
        if (!password1.equals(password2)) {
            Toast.makeText(this, getString(R.string.msg_password_check_confirm), Toast.LENGTH_SHORT).show();
            this.editPassword2.requestFocus();
            return false;
        }

        // 이름 입력 체크
        String name = this.editName.getText().toString();
        if (TextUtils.isEmpty(name)) {
            Toast.makeText(this, getString(R.string.msg_user_name_check_empty), Toast.LENGTH_SHORT).show();
            this.editName.requestFocus();
            return false;
        }

        // 휴대번호 입력 체크
        String phone = this.editPhone.getText().toString();
        if (TextUtils.isEmpty(phone)) {
            Toast.makeText(this, getString(R.string.msg_phone_number_check_empty), Toast.LENGTH_SHORT).show();
            this.editPhone.requestFocus();
            return false;
        }

        // 휴대번호 유효성 체크
        if (!Utils.isPhoneNumber(phone)) {
            Toast.makeText(this, getString(R.string.msg_phone_number_check_wrong), Toast.LENGTH_SHORT).show();
            this.editPhone.requestFocus();
            return false;
        }

        // 키보드 숨기기
        this.imm.hideSoftInputFromWindow(this.editPhone.getWindowToken(), 0);

        return true;
    }

    /* 회원가입 */
    private void join() {
        String userId = this.editUserId.getText().toString();
        String password = this.editPassword1.getText().toString();
        String name = this.editName.getText().toString();
        String phone = this.editPhone.getText().toString();
        String email = this.editEmail.getText().toString();

        // 회원정보
        final User user = new User(userId, password, name, phone, email, System.currentTimeMillis());

        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference reference = db.collection(Constants.FirestoreCollectionName.USER);

        // 아이디 중복 체크
        Query query = reference.whereEqualTo("userId", userId);
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    if (task.getResult() != null) {
                        if (task.getResult().size() == 0) {
                            // 아이디 중복 아님

                            // 회원가입 하기 (자동 문서 ID 값 생성 (컬렉션에 add 하면 document 가 자동 생성됨))
                            db.collection(Constants.FirestoreCollectionName.USER)
                                    .add(user)
                                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                        @Override
                                        public void onSuccess(DocumentReference documentReference) {
                                            // 성공
                                            layLoading.setVisibility(View.GONE);

                                            // 로그인 Activity 에 전달 (바로 로그인 되게 하기 위함)
                                            Intent intent = new Intent();
                                            intent.putExtra("userId", user.getUserId());
                                            intent.putExtra("password", user.getPassword());
                                            setResult(Activity.RESULT_OK, intent);

                                            finish();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            // 회원가입 실패
                                            layLoading.setVisibility(View.GONE);
                                            Toast.makeText(JoinActivity.this, getString(R.string.msg_error), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        } else {
                            // 아이디 중복
                            layLoading.setVisibility(View.GONE);
                            Toast.makeText(JoinActivity.this, getString(R.string.msg_user_id_check_overlap), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        layLoading.setVisibility(View.GONE);
                        Toast.makeText(JoinActivity.this, getString(R.string.msg_error), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // 오류
                    layLoading.setVisibility(View.GONE);
                    Toast.makeText(JoinActivity.this, getString(R.string.msg_error), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /* 클릭 리스너 */
    private View.OnClickListener mClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btnJoin:
                    // 회원가입
                    if (checkData()) {
                        layLoading.setVisibility(View.VISIBLE);
                        // 로딩 레이아웃을 표시하기 위해 딜레이를 줌
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                // 아이디 중복체크 후 가입
                                join();
                            }
                        }, Constants.LoadingDelay.SHORT);
                    }

                    break;
                case R.id.layLoading:
                    // 로딩중 클릭 방지
                    break;
            }
        }
    };
}
