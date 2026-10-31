package com.example.calendarmemo.util;

import android.content.Context;

import androidx.core.content.ContextCompat;

import com.example.calendarmemo.Constants;
import com.example.calendarmemo.R;

/** 分类颜色与优先级文案的集中映射。 */
public final class CategoryUtils {

    private CategoryUtils() {
    }

    public static int color(Context context, String category) {
        int colorRes;
        if (Constants.CATEGORY_LIFE.equals(category)) {
            colorRes = R.color.category_life;
        } else if (Constants.CATEGORY_WORK.equals(category)) {
            colorRes = R.color.category_work;
        } else if (Constants.CATEGORY_OTHER.equals(category)) {
            colorRes = R.color.category_other;
        } else {
            colorRes = R.color.category_study;
        }
        return ContextCompat.getColor(context, colorRes);
    }

    public static int priorityLabel(int priority) {
        switch (priority) {
            case Constants.PRIORITY_URGENT:
                return R.string.priority_badge_urgent;
            case Constants.PRIORITY_IMPORTANT:
                return R.string.priority_badge_important;
            default:
                return 0;
        }
    }

    public static int indexOfCategory(String category) {
        for (int i = 0; i < Constants.CATEGORIES.length; i++) {
            if (Constants.CATEGORIES[i].equals(category)) {
                return i;
            }
        }
        return 0;
    }
}
