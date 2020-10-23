package com.example.refrigerator.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.refrigerator.R;
import com.example.refrigerator.util.Constants;
import com.example.refrigerator.util.GlobalVariable;
import com.example.refrigerator.util.Utils;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileEditFragment extends Fragment {
    private static final String TAG = ProfileEditFragment.class.getSimpleName();

    private LinearLayout layLoading;

    private EditText editName, editEmail, editPhone;

    private InputMethodManager imm;                 // 키보드를 숨기기 위해 필요함

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile_edit, container, false);

        // 로딩 레이아웃
        this.layLoading = view.findViewById(R.id.layLoading);
        ((ProgressBar) view.findViewById(R.id.progressBar)).setIndeterminateTintList(ColorStateList.valueOf(Color.WHITE));

        this.editName = view.findViewById(R.id.editName);
        this.editName.setHint("Name");

        this.editPhone = view.findViewById(R.id.editPhone);
        this.editPhone.setHint("Phone-Number");

        this.editEmail = view.findViewById(R.id.editEmail);
        this.editEmail.setHint("E-mail");

        view.findViewById(R.id.btnSave).setOnClickListener(mClickListener);
        view.findViewById(R.id.layLoading).setOnClickListener(mClickListener);

        // 키보드를 숨기기 위해 필요함
        this.imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);

        this.editName.requestFocus();

        // 회원정보
        infoUser(view);

        return view;
    }

    /* 회원정보 */
    private void infoUser(View view) {
        // 아이디는 수정 못함
        ((TextView) view.findViewById(R.id.txtUserId)).setText(GlobalVariable.user.getUserId());

        this.editName.setText(GlobalVariable.user.getName());           // 이름
        this.editPhone.setText(GlobalVariable.user.getPhone());         // 휴대번호
        this.editEmail.setText(GlobalVariable.user.getEmail());         // 이메일
    }

    /* 입력 데이터 체크 */
    private boolean checkData() {
        // 이름 입력 체크
        String name = this.editName.getText().toString();
        if (TextUtils.isEmpty(name)) {
            Toast.makeText(getContext(), getString(R.string.msg_user_name_check_empty), Toast.LENGTH_SHORT).show();
            this.editName.requestFocus();
            return false;
        }

        // 이메일 입력 체크
        String email = this.editEmail.getText().toString();
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(getContext(), getString(R.string.msg_email_check_empty), Toast.LENGTH_SHORT).show();
            this.editEmail.requestFocus();
            return false;
        }

        // 이메일 유효성 체크
        if (!Utils.isEmail(email)) {
            Toast.makeText(getContext(), getString(R.string.msg_email_check_wrong), Toast.LENGTH_SHORT).show();
            this.editEmail.requestFocus();
            return false;
        }

        // 휴대번호 입력 체크
        String phone = this.editPhone.getText().toString();
        if (TextUtils.isEmpty(phone)) {
            Toast.makeText(getContext(), getString(R.string.msg_phone_number_check_empty), Toast.LENGTH_SHORT).show();
            this.editPhone.requestFocus();
            return false;
        }

        // 휴대번호 유효성 체크
        if (!Utils.isPhoneNumber(phone)) {
            Toast.makeText(getContext(), getString(R.string.msg_phone_number_check_wrong), Toast.LENGTH_SHORT).show();
            this.editPhone.requestFocus();
            return false;
        }

        // 키보드 숨기기
        this.imm.hideSoftInputFromWindow(this.editPhone.getWindowToken(), 0);

        return true;
    }

    /* 변경된 정보 저장 */
    protected void save() {
        final String name = this.editName.getText().toString();
        final String phone = this.editPhone.getText().toString();
        final String email = this.editEmail.getText().toString();

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        DocumentReference reference = db.collection(Constants.FirestoreCollectionName.USER)
                .document(GlobalVariable.documentId);
        reference.update("name", name, "phone", phone, "email", email)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // 성공
                        layLoading.setVisibility(View.GONE);

                        GlobalVariable.user.setName(name);
                        GlobalVariable.user.setPhone(phone);
                        GlobalVariable.user.setEmail(email);

                        Toast.makeText(getContext(), getString(R.string.msg_info_update), Toast.LENGTH_SHORT).show();

                        // SettingFragment 에 전달
                        getActivity().setResult(Activity.RESULT_OK);
                        getActivity().finish();
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

    /* 클릭 리스너 */
    private View.OnClickListener mClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btnSave:
                    // 회원정보 수정
                    if (checkData()) {
                        layLoading.setVisibility(View.VISIBLE);
                        // 로딩 레이아웃을 표시하기 위해 딜레이를 줌
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                // 변경된 정보 저장
                                save();
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
