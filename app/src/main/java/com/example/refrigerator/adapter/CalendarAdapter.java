package com.example.refrigerator.adapter;

import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.refrigerator.R;
import com.example.refrigerator.entity.CalendarDay;
import com.example.refrigerator.listener.IAdapterOnClickListener;

import java.util.ArrayList;

public class CalendarAdapter extends RecyclerView.Adapter<CalendarAdapter.ViewHolder> {
    private IAdapterOnClickListener listener;
    private ArrayList<CalendarDay> items;

    // 레이아웃 사이즈
    private int layoutWidth;
    private int layoutHeight;

    public CalendarAdapter(IAdapterOnClickListener listener, ArrayList<CalendarDay> items, int layoutWidth, int layoutHeight) {
        this.listener = listener;
        this.items = items;
        this.layoutWidth = layoutWidth;
        this.layoutHeight = layoutHeight;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_calendar_day, null);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // 레이아웃 사이즈 조절
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(this.layoutWidth, this.layoutHeight);
        holder.layDay.setLayoutParams(params);

        holder.txtDay.setText(this.items.get(position).day);

        if (this.items.get(position).today) {
            holder.imgToday.setVisibility(View.VISIBLE);
        } else {
            holder.imgToday.setVisibility(View.INVISIBLE);
        }

        switch (this.items.get(position).week) {
            case 1:
                // 일요일
                holder.txtDay.setTextColor(ContextCompat.getColor(holder.txtDay.getContext(), R.color.red_text_color));
                break;
            case 7:
                // 토요일
                holder.txtDay.setTextColor(ContextCompat.getColor(holder.txtDay.getContext(), R.color.blue_text_color));
                break;
            default:
                holder.txtDay.setTextColor(ContextCompat.getColor(holder.txtDay.getContext(), R.color.default_text_color));
                break;
        }

        if (this.items.get(position).week == 7) {
            // 맨 마지막 라인은 표시 안함
            holder.viewL.setVisibility(View.INVISIBLE);
        } else {
            holder.viewL.setVisibility(View.VISIBLE);
        }

        // 유통기한 마지막 날자인 식품이 있는지 체크
        if (this.items.get(position).count > 0) {
            // 있음
            holder.imgState.setVisibility(View.VISIBLE);
        } else {
            // 없음
            holder.imgState.setVisibility(View.INVISIBLE);
        }

        if (TextUtils.isEmpty(this.items.get(position).day)) {
            holder.layDay.setBackgroundColor(Color.WHITE);
        } else {
            holder.layDay.setBackgroundResource(R.drawable.list_item_selector);
        }
    }

    @Override
    public int getItemCount() {
        return this.items.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public LinearLayout layDay;
        public TextView txtDay;
        public ImageView imgToday, imgState;

        // 세로 라인
        public View viewL;

        public ViewHolder(View view) {
            super(view);

            this.layDay = view.findViewById(R.id.layDay);
            this.txtDay = view.findViewById(R.id.txtDay);
            this.imgToday = view.findViewById(R.id.imgToday);
            this.imgState = view.findViewById(R.id.imgState);
            this.viewL = view.findViewById(R.id.viewL);

            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            Bundle bundle = new Bundle();
            int position = getAdapterPosition();

            bundle.putString("day", items.get(position).day);
            bundle.putInt("count", items.get(position).count);
            listener.onItemClick(bundle, v.getId());
        }
    }
}
