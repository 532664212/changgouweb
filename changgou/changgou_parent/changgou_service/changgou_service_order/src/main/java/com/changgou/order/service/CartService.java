package com.changgou.order.service;

import java.util.Map;

public interface CartService {
    /**
     * 添加购物车
     * @param skuId
     * @param num
     */
    void add(String skuId, Integer num, String username);

    Map list(String username);
}
