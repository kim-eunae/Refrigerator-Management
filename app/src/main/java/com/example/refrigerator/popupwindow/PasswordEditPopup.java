package com.example.refrigerator.popupwindow;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout.LayoutParams;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.example.refrigerator.R;
import com.example.refrigerator.listener.IClickListener;

public class PasswordEditPopup extends PopupWindow {

    private Context context;
    private IClickListener listener;

    private EditText editPassword, editPassword1, editPassword2;
    private TextView txtMessage;

    // 아이디/비밀번호 최소 자리수
    private final static int MIN_SIZE = 6;

    public PasswordEditPopup(View view) {
        super(view, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        this.context = view.getContext();

        this.editPassword = view.findViewById(R.id.editPassword);
        this.editPassword.setHint("현재 비밀번호");

        this.editPassword1 = view.findViewById(R.id.editPassword1);
        this.editPassword1.setHint("새 비밀번호");

        this.editPassword2 = view.findViewById(R.id.editPassword2);
        this.editPassword2.setHint("비밀번호 확인");

        // 오류 표시
        this.txtMessage = view.findViewById(R.id.txtMessage);
        this.txtMessage.setText("");

        view.findViewById(R.id.btnOk).setOnClickListener(mClickListener);
        view.findViewById(R.id.btnCancel).setOnClickListener(mClickListener);
    }

    /* 리스너 등록 */
    public void setClickListener(IClickListener listener) {
        this.listener = listener;
    }

    /* 입력 데이터 체크 */
    private boolean checkData() {
        // 현재 비밀번호 입력 체크
        String password = this.editPassword.getText().toString();
        if (TextUtils.isEmpty(password)) {
            this.txtMessage.setText(this.context.getString(R.string.msg_password_check_empty));
            this.editPassword.requestFocus();
            return false;
        }

        // 새 비밀번호 입력 체크
        String password1 = this.editPassword1.getText().toString();
        if (TextUtils.isEmpty(password1)) {
            this.txtMessage.setText(this.context.getString(R.string.msg_password_check_empty));
            this.editPassword1.requestFocus();
            return false;
        }

        // 새 비밀번호 자리수 체크
        if (password1.length() < MIN_SIZE) {
            this.txtMessage.setText(this.context.getString(R.string.msg_password_check_length));
            this.editPassword1.requestFocus();
            return false;
        }

        // 비밀번호 확인 체크
        String password2 = this.editPassword2.getText().toString();
        if (!password1.equals(password2)) {
            this.txtMessage.setText(this.context.getString(R.string.msg_password_check_confirm));
            this.editPassword2.requestFocus();
            return false;
        }

        this.txtMessage.setText("");

        return true;
    }

    /* 클릭 리스너 */
    private View.OnClickListener mClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btnOk:
                    // 비밀번호 변경
                    if (!checkData()) {
                        return;
                    }

                    if (listener != null) {
                        Bundle bundle = new Bundle();
                        bundle.putString("old_password", editPassword.getText().toString());
                        bundle.putString("new_password", editPassword1.getText().toString());

                        listener.onClick(bundle, v.getId());
                    }

                    dismiss();
                    break;
                case R.id.btnCancel:
                    dismiss();
                    break;
            }
        }
    };
}
