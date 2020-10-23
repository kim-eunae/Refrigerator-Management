package com.example.refrigerator;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.refrigerator.entity.User;
import com.example.refrigerator.util.Constants;
import com.example.refrigerator.util.GlobalVariable;
import com.example.refrigerator.util.SharedPreferencesUtils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions; //자동 로그인 권한 요청

@RuntimePermissions  //자동 로그인 권한 요청
public class IntroActivity extends AppCompatActivity {
    private static String TAG = IntroActivity.class.getSimpleName();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);

        // 인트로 화면을 일정시간 보여줌
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // 초기화
                IntroActivityPermissionsDispatcher.initWithPermissionCheck(IntroActivity.this);
            }
        }, Constants.LoadingDelay.LONG);
    }

    @Override
    public void onBackPressed() {
        // 백키 눌려도 종료 안되게 하기 위함
        //super.onBackPressed();
    }

    ///////////////////////////////////////////// 여기서 부터 자동 로그인 권한 요청 코드////////////////////////////////////////////////////////////

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            IntroActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }              ///자동 로그인 기능

    /* 초기화 */
    @NeedsPermission(Manifest.permission.READ_PHONE_STATE) //Read_Phone_State를 사용하는 권한을 허용할 것인지, 권한을 물어보는 것
    void init() {
        // 자동 로그인 체크시 사용자 등록 Doc ID
        String id = SharedPreferencesUtils.getInstance(this)
                .get(Constants.SharedPreferencesName.USER_DOCUMENT_ID);

        Log.d(TAG, "id: " + id);

        // 자동 로그인 상태이면
        if (!TextUtils.isEmpty(id)) {
            // 로그인
            login(id);
        } else {
            // 로그인 화면으로 이동
            goLogin();
        }
    }

    @OnShowRationale(Manifest.permission.READ_PHONE_STATE)
    void showRationale(final PermissionRequest request) {
        new AlertDialog.Builder(this)
                .setPositiveButton(getString(R.string.dialog_allow), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(@NonNull DialogInterface dialog, int which) {
                        request.proceed();
                    }
                })
                .setNegativeButton(getString(R.string.dialog_deny), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(@NonNull DialogInterface dialog, int which) {
                        request.cancel();
                    }
                })
                .setCancelable(false)
                .setMessage(getString(R.string.permission_rationale_app_use))
                .show();
    }

    @OnPermissionDenied(Manifest.permission.READ_PHONE_STATE)
    void showDenied() {
        Toast.makeText(this, getString(R.string.permission_rationale_app_use), Toast.LENGTH_LONG).show();
    }

    @OnNeverAskAgain(Manifest.permission.READ_PHONE_STATE)
    void showNeverAsk() {
        Toast.makeText(this, getString(R.string.permission_rationale_app_use), Toast.LENGTH_LONG).show();
    }

    ////////////////////////////////////////자동 로그인 권한 코드 종료//////////////////////////////////////////////////////////////

    /* 자동 로그인 기능 */
    private void login(final String id) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference reference = db.collection(Constants.FirestoreCollectionName.USER).document(id);
        reference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() { //get()을 사용하여 단일 문서의 내용을 검색
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    // 성공
                    DocumentSnapshot document = task.getResult();
                    if (document != null) {
                        // 사용자 객체 생성
                        User user = document.toObject(User.class);

                        if (user.isWithdrawal()) {
                            // 회원탈퇴한 계정
                            goLogin();
                        } else {
                            GlobalVariable.documentId = document.getId(); //GlobalVariable는 전역변수
                            GlobalVariable.user = user;

                            // SharedPreferences 에 알림 진동 및 무음 여부 저장
                            SharedPreferencesUtils.getInstance(IntroActivity.this).put(Constants.SharedPreferencesName.ALARM_SILENT, user.isSilent());
                            SharedPreferencesUtils.getInstance(IntroActivity.this).put(Constants.SharedPreferencesName.ALARM_VIBRATION, user.isVibration());

                            // 메인으로 이동
                            goMain();
                        }
                    } else {
                        // 로그인 화면으로 이동
                        goLogin();
                    }
                } else {
                    // 로그인 화면으로 이동
                    goLogin();
                }
            }
        });
    }

    /* 로그인화면으로 이동 */
    private void goLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);

        finish();
    }

    /* 메인화면으로 이동 */
    private void goMain() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);

        finish();
    }
}