package com.example.calendarmemo.util;

import android.content.Context;

import com.example.calendarmemo.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * 日期工具类。
 *
 * <p>课程设计中使用 {@link Calendar} 而不是 java.time，保证 minSdk 24 也能直接运行。
 * 数据库统一保存 yyyy-MM-dd 字符串，避免时区与格式歧义。</p>
 */
public final class DateUtils {

    public static final String KEY_PATTERN = "yyyy-MM-dd";

    private DateUtils() {
    }

    public static Calendar today() {
        Calendar calendar = Calendar.getInstance(Locale.CHINA);
        clearTime(calendar);
        return calendar;
    }

    public static void clearTime(Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
    }

    public static String todayKey() {
        return keyOf(today());
    }

    public static String keyOf(Calendar calendar) {
        return String.format(Locale.CHINA, "%04d-%02d-%02d",
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH) + 1,
                calendar.get(Calendar.DAY_OF_MONTH));
    }

    public static Calendar parseKey(String key) {
        if (key != null) {
            String[] parts = key.split("-");
            if (parts.length == 3) {
                try {
                    Calendar calendar = Calendar.getInstance(Locale.CHINA);
                    calendar.set(Integer.parseInt(parts[0]),
                            Integer.parseInt(parts[1]) - 1,
                            Integer.parseInt(parts[2]));
                    clearTime(calendar);
                    return calendar;
                } catch (NumberFormatException ignored) {
                    // 参数异常时回退到今天，保证页面不崩溃。
                }
            }
        }
        return today();
    }

    public static Calendar firstDayOfMonth(Calendar source) {
        Calendar calendar = (Calendar) source.clone();
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        clearTime(calendar);
        return calendar;
    }

    public static int lastDayOfMonth(Calendar source) {
        return source.getActualMaximum(Calendar.DAY_OF_MONTH);
    }

    /** 把日期裁剪到目标月份内，例如从 1 月 31 日切换到 2 月时得到 2 月 28/29 日。 */
    public static Calendar clampToMonth(Calendar source, Calendar targetMonth) {
        Calendar calendar = firstDayOfMonth(targetMonth);
        int day = Math.min(source.get(Calendar.DAY_OF_MONTH), lastDayOfMonth(calendar));
        calendar.set(Calendar.DAY_OF_MONTH, day);
        return calendar;
    }

    public static boolean isSameDay(Calendar first, Calendar second) {
        return first.get(Calendar.ERA) == second.get(Calendar.ERA)
                && first.get(Calendar.YEAR) == second.get(Calendar.YEAR)
                && first.get(Calendar.DAY_OF_YEAR) == second.get(Calendar.DAY_OF_YEAR);
    }

    public static boolean isSameMonth(Calendar first, Calendar second) {
        return first.get(Calendar.YEAR) == second.get(Calendar.YEAR)
                && first.get(Calendar.MONTH) == second.get(Calendar.MONTH);
    }

    public static String monthPrefix(Calendar month) {
        return String.format(Locale.CHINA, "%04d-%02d-",
                month.get(Calendar.YEAR), month.get(Calendar.MONTH) + 1);
    }

    public static String monthTitle(Context context, Calendar calendar) {
        return context.getString(R.string.month_title,
                calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1);
    }

    public static String fullDate(Context context, Calendar calendar) {
        return context.getString(R.string.full_date,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH) + 1,
                calendar.get(Calendar.DAY_OF_MONTH),
                weekday(context, calendar));
    }

    public static String monthDay(Context context, Calendar calendar) {
        return context.getString(R.string.month_day,
                calendar.get(Calendar.MONTH) + 1,
                calendar.get(Calendar.DAY_OF_MONTH));
    }

    public static String weekday(Context context, Calendar calendar) {
        int resource;
        switch (calendar.get(Calendar.DAY_OF_WEEK)) {
            case Calendar.MONDAY:
                resource = R.string.weekday_mon;
                break;
            case Calendar.TUESDAY:
                resource = R.string.weekday_tue;
                break;
            case Calendar.WEDNESDAY:
                resource = R.string.weekday_wed;
                break;
            case Calendar.THURSDAY:
                resource = R.string.weekday_thu;
                break;
            case Calendar.FRIDAY:
                resource = R.string.weekday_fri;
                break;
            case Calendar.SATURDAY:
                resource = R.string.weekday_sat;
                break;
            case Calendar.SUNDAY:
            default:
                resource = R.string.weekday_sun;
                break;
        }
        return context.getString(resource);
    }

    public static String timeOf(long timestamp) {
        Calendar calendar = Calendar.getInstance(Locale.CHINA);
        calendar.setTimeInMillis(timestamp);
        return String.format(Locale.CHINA, "%02d:%02d",
                calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE));
    }

    /**
     * 生成 6 × 7 = 42 个日期格子，周一为每周第一天。
     * 包含上月末尾与下月开头的日期，使月历视觉完整。
     */
    public static List<DayInfo> buildMonthGrid(Calendar month) {
        Calendar first = firstDayOfMonth(month);
        int dayOfWeek = first.get(Calendar.DAY_OF_WEEK);
        int offset = (dayOfWeek - Calendar.MONDAY + 7) % 7;

        Calendar cursor = (Calendar) first.clone();
        cursor.add(Calendar.DAY_OF_MONTH, -offset);
        Calendar today = today();

        List<DayInfo> days = new ArrayList<>(42);
        for (int i = 0; i < 42; i++) {
            Calendar date = (Calendar) cursor.clone();
            boolean inMonth = date.get(Calendar.MONTH) == first.get(Calendar.MONTH)
                    && date.get(Calendar.YEAR) == first.get(Calendar.YEAR);
            days.add(new DayInfo(date, keyOf(date), date.get(Calendar.DAY_OF_MONTH),
                    inMonth, isSameDay(date, today)));
            cursor.add(Calendar.DAY_OF_MONTH, 1);
        }
        return days;
    }

    /** 月历中的一个日期格子。 */
    public static final class DayInfo {
        public final Calendar date;
        public final String key;
        public final int dayOfMonth;
        public final boolean inMonth;
        public final boolean today;

        public DayInfo(Calendar date, String key, int dayOfMonth, boolean inMonth, boolean today) {
            this.date = date;
            this.key = key;
            this.dayOfMonth = dayOfMonth;
            this.inMonth = inMonth;
            this.today = today;
        }
    }
}
