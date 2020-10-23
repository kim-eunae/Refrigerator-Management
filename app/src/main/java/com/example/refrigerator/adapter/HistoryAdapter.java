package com.example.refrigerator.adapter;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.refrigerator.R;
import com.example.refrigerator.entity.UserFoodItem;
import com.example.refrigerator.listener.IAdapterOnClickListener;
import com.example.refrigerator.util.Constants;
import com.example.refrigerator.util.Utils;

import java.util.ArrayList;
import java.util.Calendar;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {
    private static final String TAG = HistoryAdapter.class.getSimpleName();

    private IAdapterOnClickListener listener;
    private ArrayList<UserFoodItem> items;

    public HistoryAdapter(IAdapterOnClickListener listener, ArrayList<UserFoodItem> items) {
        this.listener = listener;
        this.items = items;
    }

    /* 삭제 */
    public UserFoodItem remove(int position){
        UserFoodItem data = null;

        if (position < getItemCount()) {
            data = this.items.get(position);
            // 식품 삭제
            this.items.remove(position);
            // 삭제된 식품을 리스트에 적용하기 위함
            notifyItemRemoved(position);
        }

        return data;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_history, null);

        // Item 사이즈 조절
        RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        view.setLayoutParams(lp);

        // ViewHolder 생성
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String food = "[" + this.items.get(position).food.getCode() + "] " + this.items.get(position).food.getName();
        holder.txtFood.setText(food);                                   // 코드 + 식픔

        // 유통기한 남은 일수 구하기
        int day = Utils.diffDate(Utils.getCurrentDate(), this.items.get(position).food.getExpirationDate());
        String expirationDate = this.items.get(position).food.getExpirationDate() + " (" + day + "일전)";
        holder.txtExpirationDate.setText(expirationDate);               // 유통기한

        // 현재일보다 이전이면
        if (day < 2) {
            // 글자색을 빨간색으로 변경
            holder.txtFood.setTextColor(ContextCompat.getColor(holder.txtFood.getContext(), R.color.red_text_color));
            holder.txtExpirationDate.setTextColor(ContextCompat.getColor(holder.txtExpirationDate.getContext(), R.color.red_text_color));
        } else {
            holder.txtFood.setTextColor(ContextCompat.getColor(holder.txtFood.getContext(), R.color.default_text_color));
            holder.txtExpirationDate.setTextColor(ContextCompat.getColor(holder.txtExpirationDate.getContext(), R.color.default_text_color));
        }
    }

    @Override
    public int getItemCount() {
        return this.items.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnLongClickListener {
        private TextView txtFood, txtExpirationDate;

        private ViewHolder(View view) {
            super(view);

            this.txtFood = view.findViewById(R.id.txtFood);
            this.txtExpirationDate = view.findViewById(R.id.txtExpirationDate);

            view.setOnLongClickListener(this);
        }

        @Override
        public boolean onLongClick(View view) {
            // 롱클릭시 삭제 처리 하기
            Bundle bundle = new Bundle();
            int position = getAdapterPosition();

            bundle.putInt("position", position);
            bundle.putInt("click_mode", Constants.ClickMode.LONG);
            bundle.putString("id", items.get(position).id);
            listener.onItemClick(bundle, view.getId());

            // 다른데서는 처리할 필요없음 true
            return true;
        }
    }
}
