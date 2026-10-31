package com.example.calendarmemo.adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;

import com.example.calendarmemo.R;
import com.example.calendarmemo.model.Festival;
import com.example.calendarmemo.util.DateUtils;
import com.example.calendarmemo.util.FestivalUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 自定义 BaseAdapter：把 42 个日期数据绑定到月历 GridView。
 *
 * <p>同时处理“选中日期”“今天”“非本月日期”“有备忘录的小圆点”四种视觉状态。
 * 这是课程中 Adapter + GridView 知识点的核心实现。</p>
 */
public class CalendarDayAdapter extends BaseAdapter {

    private final Context context;
    private final LayoutInflater inflater;
    private List<DateUtils.DayInfo> days = new ArrayList<>();
    private Map<String, Integer> dotCounts = Collections.emptyMap();
    private String selectedKey = "";

    public CalendarDayAdapter(Context context) {
        this.context = context;
        this.inflater = LayoutInflater.from(context);
    }

    public void setData(List<DateUtils.DayInfo> days,
                        Map<String, Integer> dotCounts,
                        String selectedKey) {
        this.days = days;
        this.dotCounts = dotCounts;
        this.selectedKey = selectedKey;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return days.size();
    }

    @Override
    public DateUtils.DayInfo getItem(int position) {
        return days.get(position);
    }

    @Override
    public long getItemId(int position) {
        return days.get(position).date.getTimeInMillis();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_calendar_day, parent, false);
            holder = new ViewHolder();
            holder.day = convertView.findViewById(R.id.tvDay);
            holder.dot = convertView.findViewById(R.id.viewDot);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        DateUtils.DayInfo info = getItem(position);
        boolean selected = info.key.equals(selectedKey);
        Festival festival = FestivalUtils.find(info.date);
        holder.day.setText(String.valueOf(info.dayOfMonth));
        holder.day.setBackgroundResource(0);

        if (selected) {
            holder.day.setBackgroundResource(R.drawable.bg_day_selected);
            holder.day.setTextColor(ContextCompat.getColor(context, R.color.text_on_primary));
            holder.day.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        } else if (info.today) {
            holder.day.setBackgroundResource(R.drawable.bg_day_today);
            holder.day.setTextColor(ContextCompat.getColor(context, R.color.calendar_today_ring));
            holder.day.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        } else if (festival != null && info.inMonth) {
            holder.day.setTextColor(ContextCompat.getColor(context, R.color.festival_gold));
            holder.day.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        } else {
            int color = info.inMonth
                    ? ContextCompat.getColor(context, R.color.text_primary)
                    : ContextCompat.getColor(context, R.color.calendar_outside);
            holder.day.setTextColor(color);
            holder.day.setTypeface(Typeface.DEFAULT, Typeface.NORMAL);
        }

        int count = 0;
        Integer value = dotCounts.get(info.key);
        if (value != null) {
            count = value;
        }
        if (count > 0) {
            holder.dot.setVisibility(View.VISIBLE);
            int dotColor = selected
                    ? ContextCompat.getColor(context, R.color.accent)
                    : ContextCompat.getColor(context, R.color.calendar_dot);
            ViewCompat.setBackgroundTintList(holder.dot, ColorStateList.valueOf(dotColor));
        } else {
            holder.dot.setVisibility(View.INVISIBLE);
        }

        convertView.setAlpha(info.inMonth ? 1f : 0.55f);
        String description = info.dayOfMonth + "日"
                + (info.today ? "，今天" : "")
                + (festival != null ? "，" + festival.name : "")
                + (count > 0 ? "，" + count + "条记录" : "，暂无记录")
                + (selected ? "，已选中" : "");
        convertView.setContentDescription(description);
        return convertView;
    }

    private static class ViewHolder {
        TextView day;
        View dot;
    }
}
