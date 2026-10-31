package com.example.calendarmemo;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.calendarmemo.db.MemoDao;
import com.example.calendarmemo.model.Festival;
import com.example.calendarmemo.model.Memo;
import com.example.calendarmemo.util.CategoryUtils;
import com.example.calendarmemo.util.DateUtils;
import com.example.calendarmemo.util.FestivalUtils;
import com.example.calendarmemo.util.UiUtils;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * 记录本编辑页：新建或编辑一条备忘录。
 *
 * <p>通过 Intent 接收日期与备忘录 id，保存成功后把最终日期放入 result Intent，
 * 由日历页定位到该日期。页面使用 EditText、Spinner、RadioGroup、CheckBox
 * 与 Button 完成输入与保存。</p>
 */
public class MemoEditActivity extends AppCompatActivity {

    private static final String STATE_SELECTED_DATE = "state_edit_selected_date";

    private MemoDao memoDao;
    private Memo loadedMemo;
    private long memoId = Constants.NO_MEMO_ID;
    private Calendar selectedDate;

    private TextView tvEditTitle;
    private TextView tvEditDate;
    private TextView tvEditWeekday;
    private EditText etTitle;
    private EditText etContent;
    private Spinner spinnerCategory;
    private RadioGroup radioGroupPriority;
    private CheckBox checkShowDot;
    private ImageButton btnDelete;

    private String initialSnapshot = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memo_edit);

        View root = findViewById(R.id.rootEdit);
        UiUtils.applyEdgeToEdge(this, root, true);

        memoDao = new MemoDao(this);
        selectedDate = DateUtils.parseKey(getIntent().getStringExtra(Constants.EXTRA_SELECTED_DATE));
        memoId = getIntent().getLongExtra(Constants.EXTRA_MEMO_ID, Constants.NO_MEMO_ID);
        if (savedInstanceState != null) {
            String savedDate = savedInstanceState.getString(STATE_SELECTED_DATE);
            if (savedDate != null) {
                selectedDate = DateUtils.parseKey(savedDate);
            }
        }

        bindViews();
        setupCategorySpinner();
        setupListeners();

        if (memoId != Constants.NO_MEMO_ID) {
            if (!loadExistingMemo(savedInstanceState != null)) {
                return;
            }
        } else {
            tvEditTitle.setText(R.string.edit_title_new);
            btnDelete.setVisibility(View.GONE);
            updateDateText();
        }

        initialSnapshot = snapshot();
    }

    private void bindViews() {
        tvEditTitle = findViewById(R.id.tvEditTitle);
        tvEditDate = findViewById(R.id.tvEditDate);
        tvEditWeekday = findViewById(R.id.tvEditWeekday);
        etTitle = findViewById(R.id.etTitle);
        etContent = findViewById(R.id.etContent);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        radioGroupPriority = findViewById(R.id.radioGroupPriority);
        checkShowDot = findViewById(R.id.checkShowDot);
        btnDelete = findViewById(R.id.btnDelete);
    }

    private void setupCategorySpinner() {
        List<String> categories = Arrays.asList(Constants.CATEGORIES);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, R.layout.item_spinner_editor_selected, categories);
        adapter.setDropDownViewResource(R.layout.item_spinner_dropdown);
        spinnerCategory.setAdapter(adapter);
        spinnerCategory.setSelection(0);
    }

    private void setupListeners() {
        findViewById(R.id.btnBackEdit).setOnClickListener(v -> confirmDiscard());
        findViewById(R.id.dateRow).setOnClickListener(v -> showDatePicker());
        Button save = findViewById(R.id.btnSave);
        save.setOnClickListener(v -> saveMemo());
        btnDelete.setOnClickListener(v -> confirmDelete());

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                confirmDiscard();
            }
        });
    }

    private boolean loadExistingMemo(boolean restoring) {
        loadedMemo = memoDao.findById(memoId);
        if (loadedMemo == null) {
            Toast.makeText(this, R.string.toast_load_failed, Toast.LENGTH_SHORT).show();
            finishWithAnimation();
            return false;
        }
        tvEditTitle.setText(R.string.edit_title_edit);
        btnDelete.setVisibility(View.VISIBLE);
        if (!restoring) {
            selectedDate = DateUtils.parseKey(loadedMemo.getDate());
        }
        etTitle.setText(loadedMemo.getTitle());
        etContent.setText(loadedMemo.getContent());
        spinnerCategory.setSelection(CategoryUtils.indexOfCategory(loadedMemo.getCategory()));
        setPriority(loadedMemo.getPriority());
        checkShowDot.setChecked(loadedMemo.isShowDot());
        updateDateText();
        return true;
    }

    private void showDatePicker() {
        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    Calendar picked = Calendar.getInstance(Locale.CHINA);
                    picked.set(year, month, dayOfMonth);
                    DateUtils.clearTime(picked);
                    selectedDate = picked;
                    updateDateText();
                },
                selectedDate.get(Calendar.YEAR),
                selectedDate.get(Calendar.MONTH),
                selectedDate.get(Calendar.DAY_OF_MONTH));
        dialog.setTitle(getString(R.string.pick_date));
        dialog.show();
    }

    private void updateDateText() {
        tvEditDate.setText(getString(R.string.date_with_year,
                selectedDate.get(Calendar.YEAR),
                selectedDate.get(Calendar.MONTH) + 1,
                selectedDate.get(Calendar.DAY_OF_MONTH)));
        Festival festival = FestivalUtils.find(selectedDate);
        String weekday = DateUtils.weekday(this, selectedDate);
        if (festival != null) {
            weekday = weekday + " · " + festival.name;
        }
        tvEditWeekday.setText(weekday + " · " + getString(R.string.tap_to_change_date));
    }

    private void saveMemo() {
        String title = etTitle.getText().toString().trim();
        if (TextUtils.isEmpty(title)) {
            etTitle.setError(getString(R.string.toast_title_required));
            etTitle.requestFocus();
            return;
        }
        String content = etContent.getText().toString().trim();
        int categoryPosition = spinnerCategory.getSelectedItemPosition();
        String category = categoryPosition >= 0
                ? Constants.CATEGORIES[categoryPosition]
                : Constants.CATEGORY_STUDY;

        Memo memo = loadedMemo != null ? loadedMemo : new Memo();
        memo.setTitle(title);
        memo.setContent(content);
        memo.setDate(DateUtils.keyOf(selectedDate));
        memo.setCategory(category);
        memo.setPriority(readPriority());
        memo.setShowDot(checkShowDot.isChecked());

        boolean success;
        try {
            if (memo.getId() == Constants.NO_MEMO_ID) {
                success = memoDao.insert(memo) > 0;
            } else {
                success = memoDao.update(memo) > 0;
            }
        } catch (RuntimeException e) {
            success = false;
        }

        if (!success) {
            Toast.makeText(this, R.string.toast_save_failed, Toast.LENGTH_SHORT).show();
            return;
        }

        UiUtils.hideKeyboard(this);
        Toast.makeText(this,
                getString(R.string.toast_saved, DateUtils.monthDay(this, selectedDate)),
                Toast.LENGTH_SHORT).show();

        Intent result = new Intent();
        result.putExtra(Constants.EXTRA_SELECTED_DATE, DateUtils.keyOf(selectedDate));
        setResult(RESULT_OK, result);
        finishWithAnimation();
    }

    private int readPriority() {
        int checkedId = radioGroupPriority.getCheckedRadioButtonId();
        if (checkedId == R.id.radioUrgent) {
            return Constants.PRIORITY_URGENT;
        }
        if (checkedId == R.id.radioImportant) {
            return Constants.PRIORITY_IMPORTANT;
        }
        return Constants.PRIORITY_NORMAL;
    }

    private void setPriority(int priority) {
        if (priority == Constants.PRIORITY_URGENT) {
            radioGroupPriority.check(R.id.radioUrgent);
        } else if (priority == Constants.PRIORITY_IMPORTANT) {
            radioGroupPriority.check(R.id.radioImportant);
        } else {
            radioGroupPriority.check(R.id.radioNormal);
        }
    }

    private void confirmDelete() {
        if (loadedMemo == null) {
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle(R.string.delete_title)
                .setMessage(R.string.delete_message)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.delete, (dialog, which) -> deleteMemo())
                .show();
    }

    private void deleteMemo() {
        memoDao.delete(loadedMemo.getId());
        UiUtils.hideKeyboard(this);
        Toast.makeText(this, R.string.toast_deleted, Toast.LENGTH_SHORT).show();
        Intent result = new Intent();
        result.putExtra(Constants.EXTRA_SELECTED_DATE, DateUtils.keyOf(selectedDate));
        setResult(RESULT_OK, result);
        finishWithAnimation();
    }

    private void confirmDiscard() {
        if (!isDirty()) {
            finishWithAnimation();
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle(R.string.discard_title)
                .setMessage(R.string.discard_message)
                .setNegativeButton(R.string.keep_editing, null)
                .setPositiveButton(R.string.discard, (dialog, which) -> finishWithAnimation())
                .show();
    }

    private boolean isDirty() {
        return !snapshot().equals(initialSnapshot);
    }

    private String snapshot() {
        return DateUtils.keyOf(selectedDate)
                + "|" + etTitle.getText().toString()
                + "|" + etContent.getText().toString()
                + "|" + spinnerCategory.getSelectedItemPosition()
                + "|" + readPriority()
                + "|" + checkShowDot.isChecked();
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
