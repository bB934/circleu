package com.secondhand.enums;

public enum GoodsStatusEnum {
    下架(0),
    上架(1),
    已售(2);

    private final int code;

    GoodsStatusEnum(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}