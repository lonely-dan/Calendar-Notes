package com.example.calendarmemo.model;

import com.example.calendarmemo.Constants;

/**
 * 一条日历备忘录。
 */
public class Memo {

    private long id = Constants.NO_MEMO_ID;
    private String title = "";
    private String content = "";
    private String date = "";
    private String category = Constants.CATEGORY_STUDY;
    private int priority = Constants.PRIORITY_NORMAL;
    private boolean showDot = true;
    private long createdAt;
    private long updatedAt;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public boolean isShowDot() {
        return showDot;
    }

    public void setShowDot(boolean showDot) {
        this.showDot = showDot;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }

    public boolean isImportant() {
        return priority >= Constants.PRIORITY_IMPORTANT;
    }
}
