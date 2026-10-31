package com.example.calendarmemo.model;

/**
 * 某个公历日期对应的节日 / 纪念日。
 */
public class Festival {

    public final String name;
    public final String description;
    public final String type;

    public Festival(String name, String description, String type) {
        this.name = name;
        this.description = description;
        this.type = type;
    }
}
