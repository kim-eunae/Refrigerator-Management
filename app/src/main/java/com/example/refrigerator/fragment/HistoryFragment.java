package com.example.refrigerator.fragment;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;

import com.example.refrigerator.R;
import com.example.refrigerator.adapter.HistoryAdapter;
import com.example.refrigerator.entity.UserFood;
import com.example.refrigerator.entity.UserFoodItem;
import com.example.refrigerator.fragment.abstracts.IFragment;
import com.example.refrigerator.listener.IAdapterOnClickListener;
import com.example.refrigerator.receiver.AlarmReceiver;
import com.example.refrigerator.util.Constants;
import com.example.refrigerator.util.GlobalVariable;
import com.example.refrigerator.util.Utils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

import static android.content.Context.ALARM_SERVICE;

public class HistoryFragment extends Fragment implements IFragment {
    private static final String TAG = HistoryFragment.class.getSimpleName();

    private LinearLayout layLoading;

    private RecyclerView recyclerView;
    private HistoryAdapter adapter;

    private ArrayList<UserFoodItem> items;

    // 데이터 없을때 표시할 레이아웃
    private LinearLayout layNoData;

    private EditText editKeyword;
    private TextView txtCount;

    private InputMethodManager imm;                 // 키보드를 숨기기 위해 필요함

    private int selectedPosition = -1;              // 내역 리스트 위치

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history, container, false);

        // 로딩 레이아웃
        this.layLoading = view.findViewById(R.id.layLoading);
        ((ProgressBar) view.findViewById(R.id.progressBar)).setIndeterminateTintList(ColorStateList.valueOf(Color.WHITE));

        // 리사이클러뷰
        this.recyclerView = view.findViewById(R.id.recyclerView);
        this.recyclerView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));

        this.layNoData = view.findViewById(R.id.layNoData);

        this.editKeyword = view.findViewById(R.id.editKeyword);
        this.editKeyword.setImeOptions(EditorInfo.IME_ACTION_DONE);
        this.editKeyword.setHint("식품");

        this.txtCount = view.findViewById(R.id.txtCount);
        this.txtCount.setText("");

        // 키보드를 숨기기 위해 필요함
        this.imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);

        view.findViewById(R.id.btnSearch).setOnClickListener(mClickListener);

        view.post(new Runnable() {
            @Override
            public void run() {
                // 추가한 식품 전체 목록 (유통기한 지난 식품은 제외)
                searchFood("");
            }
        });

        return view;
    }

    @Override
    public void task(int kind, Bundle bundle) {
        if (kind == Constants.FragmentTaskKind.REFRESH) {
            // 내역 새로고침
            searchFood(this.editKeyword.getText().toString());
        }
    }

    /* 추가한 식품 검색 (유통기한 지난 식품은 제외) */
    private void searchFood(final String keyword) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        // 추가한 식품 목록 (유통기한 지난 식품은 제외)
        Query query = db.collection(Constants.FirestoreCollectionName.USER)
                .document(GlobalVariable.documentId).collection(Constants.FirestoreCollectionName.USER_FOOD)
                .whereGreaterThanOrEqualTo("expirationDate", Utils.getCurrentDate())
                .orderBy("expirationDate");
                //.orderBy("expirationDate", Query.Direction.DESCENDING);
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    if (task.getResult() != null) {
                        items = new ArrayList<>();

                        // 식품 목록
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            UserFood food = document.toObject(UserFood.class);

                            // 검색어가 있으면
                            if (!TextUtils.isEmpty(keyword)) {
                                // 식품명에 검색어가 포함되어 있는지 체크
                                if (!food.getName().contains(keyword)) {
                                    food = null;
                                }
                            }

                            if (food != null) {
                                // 식품 추가
                                items.add(new UserFoodItem(document.getId(), food));
                            }
                        }

                        if (items.size() == 0) {
                            // 식품 목록 없으면
                            layNoData.setVisibility(View.VISIBLE);
                        } else {
                            layNoData.setVisibility(View.GONE);
                        }

                        txtCount.setText(items.size() + "건");

                        // 리스트에 어뎁터 설정
                        adapter = new HistoryAdapter(mAdapterListener, items);
                        recyclerView.setAdapter(adapter);
                    }
                } else {
                    // 오류
                    Log.d(TAG, "error:" + task.getException().toString());
                }
            }
        });
    }

    /* 식품 삭제 */
    private void deleteFood(String foodId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        // 회원 식품 document 참조
        DocumentReference reference = db.collection(Constants.FirestoreCollectionName.USER)
                .document(GlobalVariable.documentId).collection(Constants.FirestoreCollectionName.USER_FOOD)
                .document(foodId);
        // 식품 삭제
        reference.delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // 성공
                        layLoading.setVisibility(View.GONE);

                        if (items.get(selectedPosition).food.getAlarmNo1() > 0) {
                            // 1일전 알람 취소하기
                            cancelAlarm(items.get(selectedPosition).food.getAlarmNo1());
                        }

                        if (items.get(selectedPosition).food.getAlarmNo3() > 0) {
                            // 3일전 알람 취소하기
                            cancelAlarm(items.get(selectedPosition).food.getAlarmNo3());
                        }

                        // 리스트에서 삭제
                        adapter.remove(selectedPosition);
                        txtCount.setText(items.size() + "건");

                        if (items.size() == 0) {
                            layNoData.setVisibility(View.VISIBLE);
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // 실패
                layLoading.setVisibility(View.GONE);
                Toast.makeText(getContext(), getString(R.string.msg_error), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /* 알람 취소  */
    private void cancelAlarm(int alarmNo) {
        AlarmManager alarmManager = (AlarmManager) getActivity().getSystemService(ALARM_SERVICE);
        Intent intent = new Intent(getActivity(), AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getActivity(), alarmNo,
                intent, 0);
        alarmManager.cancel(pendingIntent);
    }

    /* 식품 클릭 리스너 */
    private IAdapterOnClickListener mAdapterListener = new IAdapterOnClickListener() {
        @Override
        public void onItemClick(Bundle bundle, int id) {
            int mode = bundle.getInt("click_mode");

            if (mode == Constants.ClickMode.LONG) {
                // 롱클릭 (삭제하기)
                selectedPosition = bundle.getInt("position");

                final String foodId = bundle.getString("id");

                new AlertDialog.Builder(getContext())
                        .setPositiveButton(getString(R.string.dialog_ok), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(@NonNull DialogInterface dialog, int which) {
                                layLoading.setVisibility(View.VISIBLE);
                                // 로딩 레이아웃을 표시하기 위해 딜레이를 줌
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        // 식품 삭제
                                        deleteFood(foodId);
                                    }
                                }, Constants.LoadingDelay.SHORT);
                            }
                        })
                        .setNegativeButton(getString(R.string.dialog_cancel), null)
                        .setCancelable(false)
                        .setTitle(getString(R.string.dialog_title_food_delete))
                        .setMessage(getString(R.string.dialog_msg_food_delete))
                        .show();
            }
        }
    };

    /* 클릭 리스너 */
    private View.OnClickListener mClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.btnSearch) {
                // 검색
                // 키보드 숨기기
                imm.hideSoftInputFromWindow(editKeyword.getWindowToken(), 0);

                // 식품 검색
                searchFood(editKeyword.getText().toString());
            }
        }
    };
}
