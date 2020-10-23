package com.example.refrigerator.fragment;

import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.example.refrigerator.R;
import com.example.refrigerator.adapter.MyFragmentPagerAdapter;
import com.example.refrigerator.fragment.abstracts.IFragment;
import com.example.refrigerator.util.Constants;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class CalendarFragment extends Fragment implements IFragment, ViewPager.OnPageChangeListener {
    private static String TAG = CalendarFragment.class.getSimpleName();

    private Calendar calendar;

    private List<Fragment> fragments;

    private ViewPager viewPager;
    private TextView txtYearMonth;

    private int pagePosition = 1;                   // 디폴트 포지션

    private static final int PAGE_MIDDLE = 1;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_calendar, container, false);

        this.viewPager = view.findViewById(R.id.viewPager);

        // 유지되는 페이지수를 설정
        // (3개의 페이지를 초반에 미리로딩한다. 페이지를 이동할때 마다 View 를 지우고 새로만드는 작업은 하지않게 된다)
        this.viewPager.setOffscreenPageLimit(3);

        // 달력 3개를 생성 (이전달, 현재달, 다음달)
        this.fragments = new ArrayList<>();
        for (int i=0; i<3; i++) {
            this.fragments.add(CalendarViewFragment.getInstance(i));
        }

        MyFragmentPagerAdapter adapter = new MyFragmentPagerAdapter(getChildFragmentManager(), this.fragments);
        this.viewPager.setAdapter(adapter);
        this.viewPager.addOnPageChangeListener(this);

        this.txtYearMonth = view.findViewById(R.id.txtYearMonth);

        view.findViewById(R.id.btnPrev).setOnClickListener(mClickListener);
        view.findViewById(R.id.btnNext).setOnClickListener(mClickListener);

        this.calendar = Calendar.getInstance();
        this.txtYearMonth.setText(DateFormat.format("yyyy.MM", calendar));

        this.viewPager.setCurrentItem(PAGE_MIDDLE, false);

        return view;
    }

    @Override
    public void task(int kind, Bundle bundle) {
        if (kind == Constants.FragmentTaskKind.REFRESH) {
            // 달력 새로고침
            Bundle b = new Bundle();
            b.putLong("time_millis", this.calendar.getTimeInMillis());
            ((IFragment) this.fragments.get(PAGE_MIDDLE)).task(Constants.FragmentTaskKind.REFRESH, b);
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        if (state == ViewPager.SCROLL_STATE_IDLE) {
            // 스크롤이 정지되어 있는 상태
            if (this.pagePosition < PAGE_MIDDLE) {
                // 이전달
                prevMonth();
            } else if (this.pagePosition > PAGE_MIDDLE) {
                // 다음달
                nextMonth();
            } else {
                return;
            }

            // 페이지를 다시 가운데로 맞춘다 (3페이지로 계속 이전 / 다음 할 수 있게 하기위함)
            this.viewPager.setCurrentItem(PAGE_MIDDLE, false);

            // 달력 만들기
            Bundle bundle = new Bundle();
            bundle.putLong("time_millis", this.calendar.getTimeInMillis());
            ((IFragment) this.fragments.get(PAGE_MIDDLE)).task(Constants.FragmentTaskKind.REFRESH, bundle);
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

    @Override
    public void onPageSelected(int position) {
        this.pagePosition = position;
    }

    /* 이전달 */
    private void prevMonth() {
        int month = this.calendar.get(Calendar.MONTH);
        int min = this.calendar.getActualMinimum(Calendar.MONTH);

        if (month == min) {
            this.calendar.set(this.calendar.get(Calendar.YEAR) - 1, this.calendar.getActualMaximum(Calendar.MONTH), 1);
        } else {
            this.calendar.set(Calendar.MONTH, month - 1);
        }

        this.txtYearMonth.setText(DateFormat.format("yyyy.MM", calendar));
    }

    /* 다음달 */
    private void nextMonth() {
        int month = this.calendar.get(Calendar.MONTH);
        int max = this.calendar.getActualMaximum(Calendar.MONTH);

        if (month == max) {
            this.calendar.set(this.calendar.get(Calendar.YEAR) + 1, this.calendar.getActualMinimum(Calendar.MONTH), 1);
        } else {
            this.calendar.set(Calendar.MONTH, month + 1);
        }

        this.txtYearMonth.setText(DateFormat.format("yyyy.MM", calendar));
    }

    private View.OnClickListener mClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btnPrev:
                    // 이전달
                    viewPager.setCurrentItem(PAGE_MIDDLE - 1, true);
                    break;
                case R.id.btnNext:
                    // 다음달
                    viewPager.setCurrentItem(PAGE_MIDDLE + 1, true);
                    break;
            }
        }
    };
}
