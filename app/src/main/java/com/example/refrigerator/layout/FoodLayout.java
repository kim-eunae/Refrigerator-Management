package com.example.refrigerator.layout;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.refrigerator.R;

public class FoodLayout extends LinearLayout {
    private static String TAG = FoodLayout.class.getSimpleName();

    public FoodLayout(Context context, String code, String name, String expirationDate) {
        super(context);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final LinearLayout layout = (LinearLayout)inflater.inflate(R.layout.layout_food, null);

        addView(layout, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);

        String food = "[" + code + "] " + name;
        ((TextView) findViewById(R.id.txtFood)).setText(food);                      // 코드 + 식픔

        ((TextView) findViewById(R.id.txtExpirationDate)).setText(expirationDate);  // 유통기한
    }
}
