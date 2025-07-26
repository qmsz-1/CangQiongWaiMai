package com.sky.service;


import com.sky.constant.MessageConstant;
import com.sky.entity.ShoppingCart;
import com.sky.mapper.ShoppingCartMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class AsyncCartSyncService {

    @Autowired
    private ShoppingCartMapper shoppingCartMapper;

    /**
     * 异步将redis购物车数据同步到mysql数据库
     * @param shoppingCart 购物车实体对象
     */
    @Async
    public void syncToMysql(ShoppingCart shoppingCart) {
        try{
            //查询mysql中是否有该用户购物车数据
            ShoppingCart queryCart = new ShoppingCart();
            queryCart.setUserId(shoppingCart.getUserId());
            queryCart.setDishId(shoppingCart.getDishId());
            queryCart.setSetmealId(shoppingCart.getSetmealId());
            queryCart.setDishFlavor(shoppingCart.getDishFlavor());
            List<ShoppingCart> list = shoppingCartMapper.list(queryCart);

            if(!list.isEmpty()) {
                //如果存在，则更新数量和时间
                ShoppingCart existingCart = list.get(0);
                existingCart.setNumber(shoppingCart.getNumber());
                shoppingCartMapper.updateNumberById(existingCart);
            } else {
                //如果不存在，则新增一条记录
                shoppingCartMapper.insert(shoppingCart);
            }
        } catch (Exception e) {
            log.error(MessageConstant.ASYNCHRONOUS_ADDITION_TO_CART_FAILED, e);
            // TODO 添加消息队列重试机制
        }
    }

    /**
     * 异步清空mysql购物车数据
     */
    @Async
    public void clearMySqlCart(Long userId) {
        try {
          shoppingCartMapper.deleteByUserId(userId);
          log.info("清空用户ID为 {} 的购物车数据成功", userId);
        } catch (Exception e) {
            log.error(MessageConstant.CLEAN_CART_FAILED, e);
        }
    }

    /**
     * 异步删除购物车中的某个商品
     */
    public void deleteShoppingCartItem(Long userId, Long dishId, Long setmealId) {
        try {
            shoppingCartMapper.deleteByUserIdAndDishIdOrSetmealId(userId, dishId, setmealId);
            log.info("删除用户ID为 {} 的购物车中的商品成功", userId);
        } catch (Exception e) {
            log.error(MessageConstant.DELETE_MYSQL_CART_FAILED, e);
        }
    }
    
    /**
     * 异步删除购物车中的某个商品(支持口味区分)
     */
    public void deleteShoppingCartItem(Long userId, Long dishId, Long setmealId, String dishFlavor) {
        try {
            // 查询要操作的购物车项
            ShoppingCart queryCart = new ShoppingCart();
            queryCart.setUserId(userId);
            if (dishId != null) {
                queryCart.setDishId(dishId);
            }

            if (setmealId != null) {
                queryCart.setSetmealId(setmealId);
            }
            
            if (dishFlavor != null) {
                queryCart.setDishFlavor(dishFlavor);
            }

            List<ShoppingCart> list = shoppingCartMapper.list(queryCart);
            if (!list.isEmpty()) {
                ShoppingCart cart = list.get(0);
                // 如果数量大于1，则减少数量
                if (cart.getNumber() > 1) {
                    cart.setNumber(cart.getNumber() - 1);
                    shoppingCartMapper.updateNumberById(cart);
                    log.info("减少用户ID为 {} 的购物车中商品数量成功，当前数量: {}", userId, cart.getNumber());
                } else {
                    // 如果数量为1，则删除该项
                    shoppingCartMapper.deleteByUserIdAndDishIdOrSetmealIdAndFlavor(userId, dishId, setmealId, dishFlavor);
                    log.info("删除用户ID为 {} 的购物车中的商品成功", userId);
                }
            }
        } catch (Exception e) {
            log.error(MessageConstant.DELETE_MYSQL_CART_FAILED, e);
        }
    }

}