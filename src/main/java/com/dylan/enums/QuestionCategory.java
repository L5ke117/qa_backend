package com.dylan.enums;

public enum QuestionCategory {
    /**
     * 未分类
     */
    RAW("raw"),
    /**
     * 简单实体型
     */
    ENTITY("entity"),
    /**
     * 简单事实型
     */
    FACT("fact"),
    /**
     *  是非型
     */
    YES_NO("yes_no"),
    /**
     *  数量型
     */
    QUANTITY("quantity")

    ;

    QuestionCategory(String value) {
        this.value = value;
    }

    private String value;

    public String value() {
        return value;
    }
}
