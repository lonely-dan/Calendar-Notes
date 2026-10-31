package com.example.calendarmemo;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;

import com.example.calendarmemo.db.MemoDao;
import com.example.calendarmemo.util.DateUtils;
import com.example.calendarmemo.util.UiUtils;

import java.util.Calendar;

/**
 * 启动页：保持克制，只保留一个“记录事件”主按钮。
 *
 * <p>点击后携带今天日期进入日历记事本，用户可在日历中选择日期并新增记录。
 * 页面底部的姓名、班级、学号来自静态字符串资源，运行时没有编辑入口。</p>
 */
public class HomeActivity extends AppCompatActivity {

    private TextView tvHomeMonth;
    private TextView tvHomeYear;
    private TextView tvHomeDay;
    private TextView tvHomeWeekday;
    private TextView tvTodayStatus;
    private MemoDao memoDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        View root = findViewById(R.id.rootHome);
        UiUtils.applyEdgeToEdge(this, root, false);

        tvHomeMonth = findViewById(R.id.tvHomeMonth);
        tvHomeYear = findViewById(R.id.tvHomeYear);
        tvHomeDay = findViewById(R.id.tvHomeDay);
        tvHomeWeekday = findViewById(R.id.tvHomeWeekday);
        tvTodayStatus = findViewById(R.id.tvTodayStatus);
        memoDao = new MemoDao(this);

        Button recordButton = findViewById(R.id.btnRecordEvent);
        recordButton.setOnClickListener(v -> openCalendar());

        renderToday();
        if (savedInstanceState == null) {
            playEntranceAnimation();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        renderToday();
    }

    private void renderToday() {
        Calendar today = DateUtils.today();
        tvHomeMonth.setText(getString(R.string.month_only, today.get(Calendar.MONTH) + 1));
        tvHomeYear.setText(String.valueOf(today.get(Calendar.YEAR)));
        tvHomeDay.setText(String.valueOf(today.get(Calendar.DAY_OF_MONTH)));
        tvHomeWeekday.setText(DateUtils.weekday(this, today));

        if (memoDao != null) {
            int count = memoDao.countByDate(DateUtils.keyOf(today));
            tvTodayStatus.setText(count == 0
                    ? getString(R.string.home_status_empty)
                    : getString(R.string.home_status_count, count));
        }
    }

    private void openCalendar() {
        Intent intent = new Intent(this, CalendarActivity.class);
        // 第一次页面跳转即通过 Intent 携带日期，日历页据此定位到今天。
        intent.putExtra(Constants.EXTRA_SELECTED_DATE, DateUtils.todayKey());
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    @Override
    protected void onDestroy() {
        if (memoDao != null) {
            memoDao.close();
        }
        super.onDestroy();
    }

    private void playEntranceAnimation() {
        View day = findViewById(R.id.tvHomeDay);
        View greeting = findViewById(R.id.tvHomeWeekday);
        View button = findViewById(R.id.btnRecordEvent);
        View[] views = {day, greeting, button};
        for (int i = 0; i < views.length; i++) {
            View view = views[i];
            view.setAlpha(0f);
            view.setTranslationY(UiUtils.dp(this, 14));
            view.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setStartDelay(120L + i * 90L)
                    .setDuration(380L)
                    .start();
        }
    }
}
