package com.example.calendarmemo;

/**
 * 全局常量：页面跳转参数、数据库字段取值、分类与优先级定义。
 */
public final class Constants {

    private Constants() {
    }

    /** 页面间传递“选中的日期”，格式固定为 yyyy-MM-dd。 */
    public static final String EXTRA_SELECTED_DATE = "com.example.calendarmemo.extra.SELECTED_DATE";
    /** 页面间传递备忘录主键；-1 表示新建。 */
    public static final String EXTRA_MEMO_ID = "com.example.calendarmemo.extra.MEMO_ID";
    public static final long NO_MEMO_ID = -1L;

    public static final int PRIORITY_NORMAL = 0;
    public static final int PRIORITY_IMPORTANT = 1;
    public static final int PRIORITY_URGENT = 2;

    public static final String CATEGORY_STUDY = "学习";
    public static final String CATEGORY_LIFE = "生活";
    public static final String CATEGORY_WORK = "工作";
    public static final String CATEGORY_OTHER = "其他";

    public static final String[] CATEGORIES = {
            CATEGORY_STUDY, CATEGORY_LIFE, CATEGORY_WORK, CATEGORY_OTHER
    };
}
