package com.sky.constant;

/**
 * Redis键常量定义
 */
public class RedisKeyConstant {

    // 购物车Redis键前缀（userId作为唯一标识）
    public static final String SHOPPING_CART_KEY = "ShoppingCart"; // %s替换为userId
    // 购物车数据过期时间（7天）
    public static final long CART_EXPIRE_DAYS = 7;

}
