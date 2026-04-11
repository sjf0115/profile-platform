package com.data.profile.web.vo;

import lombok.Data;

@Data
public class Item {

    public Item(String label, String key) {
        this.label = label;
        this.key = key;
    }

    private String label;

    private String key;

}
