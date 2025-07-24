package com.sky.service;

import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.ShoppingCart;

public interface ShoppingCartService {

    /**
     * 添加购物车
     * @param shoppingCartDTO 购物车数据传输对象
     * @return ShoppingCart 实体对象
     */
    void addShoppingCart(ShoppingCartDTO shoppingCartDTO);

}
