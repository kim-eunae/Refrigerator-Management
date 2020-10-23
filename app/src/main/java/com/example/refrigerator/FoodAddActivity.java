package com.example.refrigerator;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.example.refrigerator.entity.Food;
import com.example.refrigerator.fragment.FoodAddFragment;
import com.example.refrigerator.fragment.abstracts.IFragment;
import com.example.refrigerator.util.Constants;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class FoodAddActivity extends AppCompatActivity {
    private static String TAG = FoodAddActivity.class.getSimpleName();

    private Fragment fragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_normal);

        // 식품 정보
        Intent intent = getIntent();
        Food food = intent.getParcelableExtra("food");

        // 제목 표시
        setTitle(getString(R.string.activity_title_food_add));

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        this.fragment = FoodAddFragment.getInstance(food);
        getSupportFragmentManager().beginTransaction().add(R.id.layContent, this.fragment).commit();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (requestCode == IntentIntegrator.REQUEST_CODE) {
            if(result != null) {
                if(result.getContents() == null) {
                    Toast.makeText(this, getString(R.string.msg_qr_code_scan_failure), Toast.LENGTH_LONG).show();
                } else {
                    // QR 코드 인식 성공
                    String code = result.getContents();
                    Log.d(TAG, "qr code:" + code);

                    Bundle bundle = new Bundle();
                    bundle.putString("code", code);
                    // 식품정보
                    ((IFragment) this.fragment).task(Constants.FragmentTaskKind.INFO, bundle);
                }
            } else {
                super.onActivityResult(requestCode, resultCode, data);
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            // 종료
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
