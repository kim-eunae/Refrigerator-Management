package com.example.refrigerator.fragment;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.refrigerator.LoginActivity;
import com.example.refrigerator.ProfileEditActivity;
import com.example.refrigerator.R;
import com.example.refrigerator.fragment.abstracts.IFragment;
import com.example.refrigerator.listener.IClickListener;
import com.example.refrigerator.popupwindow.PasswordEditPopup;
import com.example.refrigerator.util.Constants;
import com.example.refrigerator.util.GlobalVariable;
import com.example.refrigerator.util.SharedPreferencesUtils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

public class SettingFragment extends Fragment implements IFragment {
    private static final String TAG = SettingFragment.class.getSimpleName();

    private LinearLayout layLoading;

    private TextView txtUserName, txtPhone, txtEmail;
    private CheckBox ckVibration, ckSilent;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_setting, container, false);

        // 로딩 레이아웃
        this.layLoading = view.findViewById(R.id.layLoading);
        ((ProgressBar) view.findViewById(R.id.progressBar)).setIndeterminateTintList(ColorStateList.valueOf(Color.WHITE));

        this.txtUserName = view.findViewById(R.id.txtUserName);
        this.txtPhone = view.findViewById(R.id.txtPhone);
        this.txtEmail = view.findViewById(R.id.txtEmail);

        this.ckVibration = view.findViewById(R.id.ckVibration);
        this.ckSilent = view.findViewById(R.id.ckSilent);

        view.findViewById(R.id.imgProfileEdit).setOnClickListener(mClickListener);
        view.findViewById(R.id.layPasswordEdit).setOnClickListener(mClickListener);
        view.findViewById(R.id.layLogout).setOnClickListener(mClickListener);
        view.findViewById(R.id.layWithdrawal).setOnClickListener(mClickListener);
        view.findViewById(R.id.layLoading).setOnClickListener(mClickListener);

        // 초기화
        init();

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == Constants.RequestCode.EDIT) {
                // 프로필 수정 이후
                infoUser();
            }
        }
    }

    @Override
    public void task(int kind, Bundle bundle) {
    }

    /* 초기화 */
    private void init() {
        // 회원정보
        infoUser();

        // 알림설정 정보
        this.ckVibration.setChecked(GlobalVariable.user.isVibration()); // 진동
        this.ckSilent.setChecked(GlobalVariable.user.isSilent());       // 무음

        this.ckVibration.setOnCheckedChangeListener(mmCheckedChangeListener);
        this.ckSilent.setOnCheckedChangeListener(mmCheckedChangeListener);
    }

    /* 회원정보 */
    private void infoUser() {
        // 회원 이름 + (아이디)
        String name = GlobalVariable.user.getName() + " (" + GlobalVariable.user.getUserId() + ")";
        this.txtUserName.setText(name);

        this.txtPhone.setText(GlobalVariable.user.getPhone());          // 휴대번호
        this.txtEmail.setText(GlobalVariable.user.getEmail());          // 이메일
    }

    /* 진동 on/off 설정 */
    private void setVibration(final boolean vibration) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        // 회원 document 참조
        DocumentReference reference = db.collection(Constants.FirestoreCollectionName.USER)
                .document(GlobalVariable.documentId);
        // 진동 on/off 설정
        reference.update("vibration", vibration)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // 성공
                        // 진동 on/off 설정
                        GlobalVariable.user.setVibration(vibration);

                        // SharedPreferences 에 알림 진동 여부 저장
                        SharedPreferencesUtils.getInstance(getContext()).put(Constants.SharedPreferencesName.ALARM_VIBRATION, vibration);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // 실패
                        Toast.makeText(getContext(), getString(R.string.msg_error), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /* 무음 on/off 설정 */
    private void setSilent(final boolean silent) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        // 회원 document 참조
        DocumentReference reference = db.collection(Constants.FirestoreCollectionName.USER)
                .document(GlobalVariable.documentId);
        // 무음 on/off 설정
        reference.update("silent", silent)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // 성공
                        // 무음 on/off 설정
                        GlobalVariable.user.setVibration(silent);

                        // SharedPreferences 에 알림 무음 여부 저장
                        SharedPreferencesUtils.getInstance(getContext()).put(Constants.SharedPreferencesName.ALARM_SILENT, silent);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // 실패
                        Toast.makeText(getContext(), getString(R.string.msg_error), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /* 비밀번호 변경 */
    private void modifyPassword(final String password) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        // 회원 document 참조
        DocumentReference reference = db.collection(Constants.FirestoreCollectionName.USER)
                .document(GlobalVariable.documentId);
        // 비밀번호 변경
        reference.update("password", password)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // 성공
                        layLoading.setVisibility(View.GONE);

                        // 비밀번호 변경
                        GlobalVariable.user.setPassword(password);
                        Toast.makeText(getContext(), getString(R.string.msg_password_modify_complete), Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // 실패
                        layLoading.setVisibility(View.GONE);
                        Toast.makeText(getContext(), getString(R.string.msg_error), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /* 회원탈퇴 */
    private void doWithdrawal() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        // 회원 document 참조
        DocumentReference reference = db.collection(Constants.FirestoreCollectionName.USER)
                .document(GlobalVariable.documentId);
        // 회원탈퇴
        reference.update("withdrawal", true)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // 성공
                        layLoading.setVisibility(View.GONE);

                        // 로그아웃 시킴
                        logout();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // 실패
                        layLoading.setVisibility(View.GONE);
                        Toast.makeText(getContext(), getString(R.string.msg_error), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /* 로그아웃 */
    private void logout() {
        // Document Id 값 clear
        SharedPreferencesUtils.getInstance(getContext()).
                put(Constants.SharedPreferencesName.USER_DOCUMENT_ID, "");

        // 로그인화면으로 이동
        Intent intent = new Intent(getContext(), LoginActivity.class);
        startActivity(intent);

        getActivity().finish();
    }

    /* 비밀번호 변경 팝업창 호출 */
    private void onPopupPasswordEdit() {
        View popupView = View.inflate(getContext(), R.layout.popup_password_edit, null);
        PasswordEditPopup popup = new PasswordEditPopup(popupView);
        popup.setClickListener(mCListener);
        // Back 키 눌렸을때 닫기 위함 (입력박스 활성화)
        popup.setFocusable(true);
        popup.showAtLocation(popupView, Gravity.CENTER, 0, 0);
    }

    /* 팝업창에서 사용할 클릭 리스너 */
    private IClickListener mCListener = new IClickListener() {
        @Override
        public void onClick(Bundle bundle, int id) {
            if (id == R.id.btnOk) {
                // 비밀번호 수정
                String oldPassword = bundle.getString("old_password");
                final String newPassword = bundle.getString("new_password");

                // 현재 비밀번호 확인
                if (oldPassword.equals(GlobalVariable.user.getPassword())) {
                    layLoading.setVisibility(View.VISIBLE);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            // 비밀번호 변경
                            modifyPassword(newPassword);
                        }
                    }, Constants.LoadingDelay.SHORT);
                } else {
                    // 현재 비밀번호 틀림
                    Toast.makeText(getContext(), getString(R.string.msg_password_check_failure), Toast.LENGTH_SHORT).show();
                }
            }
        }
    };

    private CompoundButton.OnCheckedChangeListener mmCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            switch (buttonView.getId()) {
                case R.id.ckVibration:
                    // 진동 on/off 설정
                    setVibration(isChecked);
                    break;
                case R.id.ckSilent:
                    // 무음 on/off 설정
                    setSilent(isChecked);
                    break;
            }
        }
    };

    private View.OnClickListener mClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.imgProfileEdit:
                    // 회원정보 수정
                    Intent intent = new Intent(getContext(), ProfileEditActivity.class);
                    startActivityForResult(intent, Constants.RequestCode.EDIT);
                    break;
                case R.id.layPasswordEdit:
                    // 비밀번호 변경
                    onPopupPasswordEdit();
                    break;
                case R.id.layLogout:
                    // 로그아웃
                    new AlertDialog.Builder(getContext())
                            .setPositiveButton(getString(R.string.dialog_ok), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(@NonNull DialogInterface dialog, int which) {
                                    // 로그아웃
                                    logout();
                                }
                            })
                            .setNegativeButton(getString(R.string.dialog_cancel), null)
                            .setCancelable(false)
                            .setTitle(getString(R.string.dialog_title_logout))
                            .setMessage(getString(R.string.dialog_msg_logout))
                            .show();
                    break;
                case R.id.layWithdrawal:
                    // 회원 탈퇴

                    new AlertDialog.Builder(getContext())
                            .setPositiveButton(getString(R.string.dialog_ok), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(@NonNull DialogInterface dialog, int which) {
                                    layLoading.setVisibility(View.VISIBLE);
                                    // 로딩 레이아웃을 표시하기 위해 딜레이를 줌
                                    new Handler().postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            // 회원탈퇴
                                            doWithdrawal();
                                        }
                                    }, Constants.LoadingDelay.SHORT);
                                }
                            })
                            .setNegativeButton(getString(R.string.dialog_cancel), null)
                            .setCancelable(false)
                            .setTitle(getString(R.string.dialog_title_withdrawal))
                            .setMessage(getString(R.string.dialog_msg_withdrawal))
                            .show();
                    break;
                case R.id.layLoading:
                    // 로딩중 클릭 방지
                    break;
            }
        }
    };
}
