package com.example.calendarmemo;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.calendarmemo.adapter.CalendarDayAdapter;
import com.example.calendarmemo.adapter.MemoListAdapter;
import com.example.calendarmemo.db.MemoDao;
import com.example.calendarmemo.model.Festival;
import com.example.calendarmemo.model.Memo;
import com.example.calendarmemo.util.DateUtils;
import com.example.calendarmemo.util.FestivalUtils;
import com.example.calendarmemo.util.UiUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

/**
 * 日历记事本：月历 + 当日备忘录列表。
 *
 * <p>页面职责：</p>
 * <ul>
 *     <li>使用 GridView 展示可点击月历，支持上下月切换与“回到今天”；</li>
 *     <li>点击日期后，ListView 显示该日期的备忘录；</li>
 *     <li>通过 Activity Result 接收编辑页保存后的日期，自动定位并高亮；</li>
 *     <li>支持分类 Spinner 过滤、只看重要 CheckBox、长按删除。</li>
 * </ul>
 */
public class CalendarActivity extends AppCompatActivity {

    private static final String STATE_SELECTED_DATE = "state_selected_date";

    private MemoDao memoDao;
    private Calendar selectedDate;
    private Calendar displayedMonth;

    private CalendarDayAdapter calendarAdapter;
    private MemoListAdapter memoAdapter;
    private GridView gridCalendar;
    private ListView listMemos;
    private Spinner spinnerFilter;
    private CheckBox checkImportant;
    private TextView tvMonthTitle;
    private TextView tvSelectedDay;
    private TextView tvSelectedDate;
    private TextView tvSelectedCount;
    private TextView tvFestivalBanner;
    private TextView tvEmptyTitle;
    private TextView tvEmptyHint;

    private ActivityResultLauncher<Intent> editorLauncher;
    private boolean filtersReady;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        View root = findViewById(R.id.rootCalendar);
        UiUtils.applyEdgeToEdge(this, root, false);

        memoDao = new MemoDao(this);
        restoreDate(savedInstanceState);
        bindViews();
        setupToolbar();
        setupCalendar();
        setupFilters();
        setupList();
        registerEditorLauncher();

        refreshAll();
    }

    private void restoreDate(Bundle savedInstanceState) {
        String dateKey;
        if (savedInstanceState != null) {
            dateKey = savedInstanceState.getString(STATE_SELECTED_DATE);
        } else {
            dateKey = getIntent().getStringExtra(Constants.EXTRA_SELECTED_DATE);
        }
        selectedDate = DateUtils.parseKey(dateKey);
        displayedMonth = DateUtils.firstDayOfMonth(selectedDate);
    }

    private void bindViews() {
        gridCalendar = findViewById(R.id.gridCalendar);
        listMemos = findViewById(R.id.listMemos);
        spinnerFilter = findViewById(R.id.spinnerFilter);
        checkImportant = findViewById(R.id.checkImportant);
        tvMonthTitle = findViewById(R.id.tvMonthTitle);
        tvSelectedDay = findViewById(R.id.tvSelectedDay);
        tvSelectedDate = findViewById(R.id.tvSelectedDate);
        tvSelectedCount = findViewById(R.id.tvSelectedCount);
        tvFestivalBanner = findViewById(R.id.tvFestivalBanner);
        tvEmptyTitle = findViewById(R.id.tvEmptyTitle);
        tvEmptyHint = findViewById(R.id.tvEmptyHint);
    }

    private void setupToolbar() {
        ImageButton back = findViewById(R.id.btnBack);
        back.setOnClickListener(v -> finishWithAnimation());

        findViewById(R.id.btnPrevMonth).setOnClickListener(v -> changeMonth(-1));
        findViewById(R.id.btnNextMonth).setOnClickListener(v -> changeMonth(1));
        findViewById(R.id.btnToday).setOnClickListener(v -> goToToday());
        findViewById(R.id.btnAddMemo).setOnClickListener(v -> openEditor(null));

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finishWithAnimation();
            }
        });
    }

    private void setupCalendar() {
        calendarAdapter = new CalendarDayAdapter(this);
        gridCalendar.setAdapter(calendarAdapter);
        gridCalendar.setOnItemClickListener((parent, view, position, id) -> {
            DateUtils.DayInfo info = calendarAdapter.getItem(position);
            selectedDate = (Calendar) info.date.clone();
            if (!DateUtils.isSameMonth(displayedMonth, selectedDate)) {
                displayedMonth = DateUtils.firstDayOfMonth(selectedDate);
            }
            refreshAll();
        });
    }

    private void setupFilters() {
        List<String> options = new ArrayList<>();
        options.add(getString(R.string.filter_all_categories));
        for (String category : Constants.CATEGORIES) {
            options.add(category);
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, R.layout.item_spinner_selected, options);
        adapter.setDropDownViewResource(R.layout.item_spinner_dropdown);
        spinnerFilter.setAdapter(adapter);
        filtersReady = true;

        spinnerFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                loadMemos();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                loadMemos();
            }
        });
        checkImportant.setOnCheckedChangeListener((buttonView, isChecked) -> loadMemos());
    }

    private void setupList() {
        memoAdapter = new MemoListAdapter(this);
        listMemos.setAdapter(memoAdapter);
        listMemos.setEmptyView(findViewById(R.id.emptyView));

        listMemos.setOnItemClickListener((parent, view, position, id) -> {
            Memo memo = memoAdapter.getItem(position);
            openEditor(memo);
        });
        listMemos.setOnItemLongClickListener((parent, view, position, id) -> {
            showDeleteDialog(memoAdapter.getItem(position));
            return true;
        });
    }

    private void registerEditorLauncher() {
        editorLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), result -> {
                    if (result.getResultCode() != RESULT_OK || result.getData() == null) {
                        return;
                    }
                    String dateKey = result.getData()
                            .getStringExtra(Constants.EXTRA_SELECTED_DATE);
                    if (dateKey == null) {
                        return;
                    }
                    // 保存后回到指定日期：清空筛选，保证新记录一定可见。
                    resetFilters();
                    selectedDate = DateUtils.parseKey(dateKey);
                    displayedMonth = DateUtils.firstDayOfMonth(selectedDate);
                    refreshAll();
                    flashCalendar();
                });
    }

    private void resetFilters() {
        if (spinnerFilter.getSelectedItemPosition() != 0) {
            spinnerFilter.setSelection(0);
        }
        if (checkImportant.isChecked()) {
            checkImportant.setChecked(false);
        }
    }

    private void changeMonth(int delta) {
        Calendar targetMonth = (Calendar) displayedMonth.clone();
        targetMonth.add(Calendar.MONTH, delta);
        selectedDate = DateUtils.clampToMonth(selectedDate, targetMonth);
        displayedMonth = DateUtils.firstDayOfMonth(selectedDate);
        refreshAll();
        flashCalendar();
    }

    private void goToToday() {
        selectedDate = DateUtils.today();
        displayedMonth = DateUtils.firstDayOfMonth(selectedDate);
        refreshAll();
        flashCalendar();
    }

    private void refreshAll() {
        String selectedKey = DateUtils.keyOf(selectedDate);
        tvMonthTitle.setText(DateUtils.monthTitle(this, displayedMonth));

        Map<String, Integer> dots = memoDao.dotCounts(DateUtils.monthPrefix(displayedMonth));
        calendarAdapter.setData(DateUtils.buildMonthGrid(displayedMonth), dots, selectedKey);

        tvSelectedDay.setText(String.valueOf(selectedDate.get(Calendar.DAY_OF_MONTH)));
        tvSelectedDate.setText(getString(R.string.month_day,
                selectedDate.get(Calendar.MONTH) + 1,
                selectedDate.get(Calendar.DAY_OF_MONTH))
                + " · " + DateUtils.weekday(this, selectedDate));

        updateFestivalBanner();
        loadMemos();
    }

    private void updateFestivalBanner() {
        List<Festival> festivals = FestivalUtils.findAll(selectedDate);
        if (festivals.isEmpty()) {
            tvFestivalBanner.setVisibility(View.GONE);
            tvFestivalBanner.setContentDescription(null);
            return;
        }
        StringBuilder text = new StringBuilder();
        StringBuilder description = new StringBuilder("节日：");
        for (Festival festival : festivals) {
            if (text.length() > 0) {
                text.append('\n');
                description.append('、');
            }
            text.append("✦ ").append(festival.name).append(" · ")
                    .append(festival.description);
            description.append(festival.name).append('，').append(festival.description);
        }
        tvFestivalBanner.setText(text);
        tvFestivalBanner.setContentDescription(description);
        tvFestivalBanner.setVisibility(View.VISIBLE);
    }

    private void loadMemos() {
        if (!filtersReady || memoAdapter == null) {
            return;
        }
        String selectedKey = DateUtils.keyOf(selectedDate);
        int position = spinnerFilter.getSelectedItemPosition();
        String category = position > 0 ? Constants.CATEGORIES[position - 1] : null;
        boolean onlyImportant = checkImportant.isChecked();
        boolean hasFilter = category != null || onlyImportant;

        List<Memo> memos = memoDao.findByDate(selectedKey, category, onlyImportant);
        memoAdapter.setItems(memos);

        if (memos.isEmpty()) {
            tvSelectedCount.setText(hasFilter
                    ? getString(R.string.memo_count, 0)
                    : getString(R.string.memo_count_zero));
            if (hasFilter) {
                tvEmptyTitle.setText(R.string.empty_filtered_title);
                tvEmptyHint.setText(R.string.empty_filtered_hint);
            } else {
                tvEmptyTitle.setText(R.string.empty_title);
                tvEmptyHint.setText(R.string.empty_hint);
            }
        } else {
            tvSelectedCount.setText(getString(R.string.memo_count, memos.size()));
        }
    }

    private void openEditor(Memo memo) {
        Intent intent = new Intent(this, MemoEditActivity.class);
        intent.putExtra(Constants.EXTRA_SELECTED_DATE, DateUtils.keyOf(selectedDate));
        if (memo != null) {
            intent.putExtra(Constants.EXTRA_MEMO_ID, memo.getId());
        }
        editorLauncher.launch(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    private void showDeleteDialog(Memo memo) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.delete_title)
                .setMessage(getString(R.string.delete_message) + "\n\n" + memo.getTitle())
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.delete, (dialog, which) -> {
                    memoDao.delete(memo.getId());
                    refreshAll();
                    Toast.makeText(this, R.string.toast_deleted, Toast.LENGTH_SHORT).show();
                })
                .show();
    }

    private void flashCalendar() {
        gridCalendar.setAlpha(0.25f);
        gridCalendar.animate().alpha(1f).setDuration(180L).start();
    }

    private void finishWithAnimation() {
        finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(STATE_SELECTED_DATE, DateUtils.keyOf(selectedDate));
    }

    @Override
    protected void onDestroy() {
        if (memoDao != null) {
            memoDao.close();
        }
        super.onDestroy();
    }
}
