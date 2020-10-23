package com.example.refrigerator.popupwindow;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import com.example.refrigerator.R;
import com.example.refrigerator.listener.IClickListener;

public class FoodAddMethodSelectPopup extends PopupWindow {

    private IClickListener listener;

    public FoodAddMethodSelectPopup(View view) {
        super(view, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);

        view.findViewById(R.id.layBlock).setOnClickListener(mClickListener);
        view.findViewById(R.id.layBody).setOnClickListener(mClickListener);
        view.findViewById(R.id.layQRCode).setOnClickListener(mClickListener);
        view.findViewById(R.id.layCode).setOnClickListener(mClickListener);
    }

    /* 리스너 등록 */
    public void setClickListener(IClickListener listener) {
        this.listener = listener;
    }

    /* 클릭 리스너 */
    private View.OnClickListener mClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.layBlock:
                    // 여백 클릭
                    dismiss();
                    break;
                case R.id.layBody:
                    // 닫기 방지
                    break;
                case R.id.layQRCode:
                    // QR 코드로 촬영하기
                    if (listener != null) {
                        listener.onClick(null, R.id.layQRCode);
                    }

                    dismiss();
                    break;
                case R.id.layCode:
                    // 코드 직접 입력하기
                    if (listener != null) {
                        listener.onClick(null, R.id.layCode);
                    }

                    dismiss();
                    break;
            }
        }
    };
}
