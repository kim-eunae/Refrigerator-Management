package com.example.refrigerator.popupwindow;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.refrigerator.R;
import com.example.refrigerator.entity.UserFood;
import com.example.refrigerator.layout.FoodLayout;
import com.example.refrigerator.util.Constants;
import com.example.refrigerator.util.GlobalVariable;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class FoodListPopup extends PopupWindow {
    private static String TAG = FoodListPopup.class.getSimpleName();

    private Context context;
    private LinearLayout layFood;

    public FoodListPopup(View view, String date) {
        super(view, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        this.context = view.getContext();

        this.layFood = view.findViewById(R.id.layFood);

        ((TextView) view.findViewById(R.id.txtDate)).setText(date);

        view.findViewById(R.id.layBlock).setOnClickListener(mClickListener);
        view.findViewById(R.id.layBody).setOnClickListener(mClickListener);

        // 식품 리스트
        listFood(date);
    }

    /* 식품 리스트 */
    private void listFood(String date) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        // 유통기한 일자로 검색
        Query query = db.collection(Constants.FirestoreCollectionName.USER)
                .document(GlobalVariable.documentId).collection(Constants.FirestoreCollectionName.USER_FOOD)
                .whereEqualTo("expirationDate", date)
                .orderBy("name");
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    if (task.getResult() != null) {
                        // 식품 목록
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            UserFood food = document.toObject(UserFood.class);

                            // 식품 추가
                            layFood.addView(new FoodLayout(context, food.getCode(), food.getName(), food.getExpirationDate()),
                                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
                        }
                    }
                } else {
                    // 오류
                    Log.d(TAG, "error:" + task.getException().toString());
                }
            }
        });
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
            }
        }
    };
}
