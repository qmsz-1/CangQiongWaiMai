package com.sky.mapper;


import com.sky.entity.ShoppingCart;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ShoppingCartMapper {

    /**
     * 添加购物车
     * @param shoppingCart 购物车实体对象
     * @return 受影响的行数
     */
    List<ShoppingCart> list(ShoppingCart shoppingCart);

    /**
     * 更新购物车
     * @param existingCart 已存在的购物车实体对象
     */
    void updateNumberById(ShoppingCart existingCart);

    /**
     * 插入新的购物车记录
     * @param shoppingCart 购物车实体对象
     */
    void insert(ShoppingCart shoppingCart);
}
