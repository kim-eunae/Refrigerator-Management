package com.example.refrigerator;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.refrigerator.fragment.ProfileEditFragment;

public class ProfileEditActivity extends AppCompatActivity {
    private static String TAG = ProfileEditActivity.class.getSimpleName();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_normal);

        // 제목 표시
        setTitle(getString(R.string.activity_title_profile_edit));

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getSupportFragmentManager().beginTransaction().add(R.id.layContent, new ProfileEditFragment()).commit();
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