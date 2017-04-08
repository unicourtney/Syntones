package com.syntones.model;

/**
 * Created by CourtneyLove on 12/5/2016.
 */

public class SampleItems {

    private String name;
    private Boolean isChecked = false;

    public SampleItems() {
    }

    public SampleItems(String name, Boolean isChecked) {
        this.name = name;
        this.isChecked = isChecked;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getChecked() {
        return isChecked;
    }

    public void setChecked(Boolean checked) {
        isChecked = checked;
    }
}
