package com.shawnyu.springbootmall.constant;

public enum CartStatus {

    AVAILABLE,      // 商品可購買
    REACHED_LIMIT,  // 商品已達購買上限
    OUT_OF_STOCK,   // 商品目前缺貨
    DISCONTINUED    // 商品已下架
}
