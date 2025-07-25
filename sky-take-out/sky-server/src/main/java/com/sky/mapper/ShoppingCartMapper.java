package com.sky.mapper;


import com.sky.annotation.AutoFill;
import com.sky.entity.ShoppingCart;
import com.sky.enumeration.OperationType;
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
    @AutoFill(OperationType.UPDATE)
    void updateNumberById(ShoppingCart existingCart);

    /**
     * 插入新的购物车记录
     * @param shoppingCart 购物车实体对象
     */
    @AutoFill(OperationType.INSERT)
    void insert(ShoppingCart shoppingCart);

    /**
     * 根据用户ID删除购物车数据
     * @param userId 用户ID
     */
    void deleteByUserId(Long userId);

    /**
     * 删除购物车中的某个商品
     * @param userId 用户ID
     */
    void deleteByUserIdAndDishIdOrSetmealId(Long userId, Long dishId, Long setmealId);

    /**
     * 根据用户ID查询购物车数据
     * @param userId
     * @return
     */
    List<ShoppingCart> getByUserId(Long userId);
}
