package com.example.refrigerator;

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
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.refrigerator.entity.User;
import com.example.refrigerator.util.Constants;
import com.example.refrigerator.util.Utils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class FindUserIdActivity extends AppCompatActivity {
    private static String TAG = FindUserIdActivity.class.getSimpleName();

    private LinearLayout layLoading;                // 로딩 레이아웃

    private LinearLayout layUserId;
    private EditText editEmail, editName, editPhone;
    private TextView txtUserId;

    private InputMethodManager imm;                 // 키보드를 숨기기 위해 필요함

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_user_id);

        // 로딩 레이아웃
        this.layLoading = findViewById(R.id.layLoading);
        ((ProgressBar) findViewById(R.id.progressBar)).setIndeterminateTintList(ColorStateList.valueOf(Color.WHITE));

        this.editEmail = findViewById(R.id.editEmail);
        this.editEmail.setHint("E-mail");

        this.editName = findViewById(R.id.editName);
        this.editName.setHint("Name");

        this.editPhone = findViewById(R.id.editPhone);
        this.editPhone.setHint("Phone-Number");

        this.layUserId = findViewById(R.id.layUserId);
        this.txtUserId = findViewById(R.id.txtUserId);

        findViewById(R.id.btnFind).setOnClickListener(mClickListener);
        findViewById(R.id.btnFindPassword).setOnClickListener(mClickListener);
        findViewById(R.id.layLoading).setOnClickListener(mClickListener);

        // 키보드를 숨기기 위해 필요함
        this.imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        this.editEmail.requestFocus();
    }

    /* 입력 데이터 체크 */
    private boolean checkData() {
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

    /* ID 찾기 */
    private void findUserId() {
        final String email = this.editEmail.getText().toString();
        final String name = this.editName.getText().toString();
        final String phone = this.editPhone.getText().toString();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference reference = db.collection(Constants.FirestoreCollectionName.USER);

        // ID 찾기
        Query query = reference.whereEqualTo("email", email)
                .whereEqualTo("name", name)
                .whereEqualTo("phone", phone).limit(1);
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                layLoading.setVisibility(View.GONE);

                if (task.isSuccessful()) {
                    if (task.getResult().size() == 0) {
                        // 찾기 실패
                        layUserId.setVisibility(View.GONE);
                        Toast.makeText(FindUserIdActivity.this, getString(R.string.msg_find_failure), Toast.LENGTH_SHORT).show();
                    } else {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Log.d(TAG, document.getId() + " => " + document.getData());

                            // 사용자 객체 생성
                            User user = document.toObject(User.class);

                            txtUserId.setText(user.getUserId());
                            layUserId.setVisibility(View.VISIBLE);
                            break;
                        }
                    }
                } else {
                    // 오류
                    Toast.makeText(FindUserIdActivity.this, getString(R.string.msg_error), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /* 클릭 리스너 */
    private View.OnClickListener mClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btnFind:
                    // ID 찾기
                    if (checkData()) {
                        layLoading.setVisibility(View.VISIBLE);
                        // 로딩 레이아웃을 표시하기 위해 딜레이를 줌
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                // ID 찾기
                                findUserId();
                            }
                        }, Constants.LoadingDelay.SHORT);
                    }
                    break;
                case R.id.btnFindPassword:
                    // 비밀번호 찾기
                    Intent intent = new Intent(FindUserIdActivity.this, FindPasswordActivity.class);
                    startActivity(intent);
                    finish();
                    break;
                case R.id.layLoading:
                    // 로딩중 클릭 방지
                    break;
            }
        }
    };
}
