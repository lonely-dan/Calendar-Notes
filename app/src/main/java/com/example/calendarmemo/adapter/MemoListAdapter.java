package com.example.calendarmemo.adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;

import com.example.calendarmemo.Constants;
import com.example.calendarmemo.R;
import com.example.calendarmemo.model.Memo;
import com.example.calendarmemo.util.CategoryUtils;
import com.example.calendarmemo.util.DateUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 自定义 BaseAdapter：当日备忘录列表。
 *
 * <p>展示标题、内容摘要、分类、优先级徽标与更新时间；分类颜色通过左侧色条
 * 与圆点统一体现，帮助用户快速扫描。</p>
 */
public class MemoListAdapter extends BaseAdapter {

    private final Context context;
    private final LayoutInflater inflater;
    private List<Memo> items = new ArrayList<>();

    public MemoListAdapter(Context context) {
        this.context = context;
        this.inflater = LayoutInflater.from(context);
    }

    public void setItems(List<Memo> items) {
        this.items = items == null ? new ArrayList<>() : items;
        notifyDataSetChanged();
    }

    public List<Memo> getItems() {
        return items;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Memo getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return items.get(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_memo, parent, false);
            holder = new ViewHolder();
            holder.strip = convertView.findViewById(R.id.viewCategoryStrip);
            holder.title = convertView.findViewById(R.id.tvMemoTitle);
            holder.content = convertView.findViewById(R.id.tvMemoContent);
            holder.badge = convertView.findViewById(R.id.tvPriorityBadge);
            holder.categoryDot = convertView.findViewById(R.id.viewCategoryDot);
            holder.category = convertView.findViewById(R.id.tvMemoCategory);
            holder.time = convertView.findViewById(R.id.tvMemoTime);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Memo memo = getItem(position);
        int categoryColor = CategoryUtils.color(context, memo.getCategory());

        String title = memo.getTitle() == null || memo.getTitle().trim().isEmpty()
                ? context.getString(R.string.no_title)
                : memo.getTitle();
        holder.title.setText(title);

        String content = memo.getContent() == null ? "" : memo.getContent().trim();
        if (content.isEmpty()) {
            holder.content.setVisibility(View.GONE);
        } else {
            holder.content.setVisibility(View.VISIBLE);
            holder.content.setText(content);
        }

        holder.strip.setBackgroundColor(categoryColor);
        ViewCompat.setBackgroundTintList(holder.categoryDot,
                ColorStateList.valueOf(categoryColor));
        holder.category.setText(memo.getCategory());
        holder.category.setTextColor(categoryColor);
        holder.time.setText(context.getString(R.string.record_time,
                DateUtils.timeOf(memo.getUpdatedAt())));

        int badgeText = CategoryUtils.priorityLabel(memo.getPriority());
        if (badgeText == 0) {
            holder.badge.setVisibility(View.GONE);
        } else {
            holder.badge.setVisibility(View.VISIBLE);
            holder.badge.setText(badgeText);
            if (memo.getPriority() == Constants.PRIORITY_URGENT) {
                holder.badge.setBackgroundResource(R.drawable.bg_badge_red);
                holder.badge.setTextColor(ContextCompat.getColor(context, R.color.priority_urgent));
            } else {
                holder.badge.setBackgroundResource(R.drawable.bg_badge_warm);
                holder.badge.setTextColor(ContextCompat.getColor(context, R.color.priority_important));
            }
        }

        convertView.setContentDescription(title + "，" + memo.getCategory()
                + (memo.isImportant() ? "，重要" : "")
                + (content.isEmpty() ? "" : "，" + content));
        return convertView;
    }

    private static class ViewHolder {
        View strip;
        TextView title;
        TextView content;
        TextView badge;
        View categoryDot;
        TextView category;
        TextView time;
    }
}
