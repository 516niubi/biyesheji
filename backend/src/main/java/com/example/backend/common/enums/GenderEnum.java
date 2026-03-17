package com.example.backend.common.enums;

import java.util.Arrays;
import java.util.Objects;

/**
 * @Author caiya
 * @Description 性别枚举类
 * @Version 1.0
 */
public enum GenderEnum {
    MALE(0, "男"),
    GIRL(1, "女"),
    SECRECY(2, "保密");

    private Integer key;
    private String value;

    GenderEnum(Integer key, String value) {
        this.key = key;
        this.value = value;
    }

    public static GenderEnum getGender(Integer key) {
        if (key == null) return null;
        GenderEnum[] values = GenderEnum.values();
        for (GenderEnum genderEnum : values) {
            if (Objects.equals(genderEnum.getKey(), key))
                return genderEnum;
        }
        return null;
    }

    public Integer getKey() {
        return key;
    }

    public void setKey(Integer key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
