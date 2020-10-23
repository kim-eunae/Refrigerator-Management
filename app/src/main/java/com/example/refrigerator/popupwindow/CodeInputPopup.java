package com.example.refrigerator.popupwindow;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.example.refrigerator.R;
import com.example.refrigerator.listener.IClickListener;

public class CodeInputPopup extends PopupWindow {

    private IClickListener listener;

    private EditText editCode;
    private TextView txtMessage;

    public CodeInputPopup(View view) {
        super(view, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);

        this.editCode = view.findViewById(R.id.editCode);
        this.editCode.setHint("CODE");

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
        // 입력 체크
        String code = this.editCode.getText().toString();
        if (TextUtils.isEmpty(code)) {
            this.txtMessage.setText(this.txtMessage.getContext().getString(R.string.msg_code_check_empty));
            this.editCode.requestFocus();
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
                    // 입력 데이터 체크
                    if (!checkData()) {
                        return;
                    }

                    if (listener != null) {
                        Bundle bundle = new Bundle();
                        bundle.putString("code", editCode.getText().toString());

                        listener.onClick(bundle, v.getId());
                    }

                    dismiss();
                    break;
                case R.id.btnCancel:
                    // 취소
                    dismiss();
                    break;
            }
        }
    };
}