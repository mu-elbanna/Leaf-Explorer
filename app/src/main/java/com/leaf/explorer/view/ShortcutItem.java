package com.leaf.explorer.view;

public class ShortcutItem {
    String name, desc;
    int res;

    public ShortcutItem(String name, String desc, int res) {
        this.name = name;
        this.desc = desc;
        this.res = res;
    }

    public String getName() {
        return name;
    }

    public String getDesc() {
        return desc;
    }

    public int getResources() {
        return res;
    }
}
