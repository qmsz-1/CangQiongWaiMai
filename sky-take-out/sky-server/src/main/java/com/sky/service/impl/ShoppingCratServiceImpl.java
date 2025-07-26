package com.sky.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sky.constant.MessageConstant;
import com.sky.constant.RedisKeyConstant;
import com.sky.constant.StatusConstant;
import com.sky.context.BaseContext;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.ShoppingCart;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.service.AsyncCartSyncService;
import com.sky.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ShoppingCratServiceImpl implements ShoppingCartService {

    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private SetmealMapper setmealMapper;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    @Autowired
    private AsyncCartSyncService asyncCartSyncService;
    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 添加购物车
     * @param shoppingCartDTO 购物车数据传输对象
     * @return ShoppingCart 实体对象
     */
    @Override
    public void addShoppingCart(ShoppingCartDTO shoppingCartDTO) {

        Long userId = BaseContext.getCurrentId();
        String cartKey = RedisKeyConstant.SHOPPING_CART_KEY + userId;

        //校验商品Id
        Long dishId = shoppingCartDTO.getDishId();
        Long setmealId = shoppingCartDTO.getSetmealId();
        if(dishId == null && setmealId == null) {
            throw new IllegalArgumentException(MessageConstant.SETMEAL_ID_OR_DISH_ID_CANNOT_BE_NULL);
        }

        //生成redis Hash的Field(区分菜品和套餐，以及口味)
//        if (shoppingCartDTO.getDishFlavor() == null) {
//            throw new IllegalArgumentException(MessageConstant.DISH_FLAVOR_CANNOT_BE_NULL);
//        }
        String field = buildCartField(dishId, setmealId, shoppingCartDTO.getDishFlavor());
        BoundHashOperations<String, String, Object> cartHash = redisTemplate.boundHashOps(cartKey);

        try {
            ShoppingCart shoppingCart = (ShoppingCart) cartHash.get(field);
            if (shoppingCart != null) {
                //Redis中已存在,仅跟新数量
                shoppingCart.setNumber(shoppingCart.getNumber() + 1);
                cartHash.put(field, shoppingCart);
            } else {
                //Redis中不存在,构建购物车对象
                shoppingCart = buildShoppingCart(shoppingCartDTO, userId);

                //检查MySql历史数据
                ShoppingCart dbCart = queryDbCart(userId, dishId, setmealId, shoppingCartDTO.getDishFlavor());
                shoppingCart.setNumber(dbCart != null ? dbCart.getNumber() + 1 : 1);
                cartHash.put(field, shoppingCart);
            }
            //设置过期时间
            redisTemplate.expire(cartKey, RedisKeyConstant.CART_EXPIRE_DAYS, TimeUnit.DAYS);
            //异步同步到MySql
            asyncCartSyncService.syncToMysql(shoppingCart);
        } catch (Exception e) {
            log.error("Redis操作失败,降级到Mysql", e);
            //Redis操作异常时降级到Mysql
            directShoppingCart(shoppingCartDTO);
        }

//        //判断当前加入购物车的商品是否已经存在于购物车中
//        ShoppingCart shoppingCart = new ShoppingCart();
//        BeanUtils.copyProperties(shoppingCartDTO, shoppingCart);
//
//        //获取当前用户id
//        Long userId = BaseContext.getCurrentId();
//        shoppingCart.setUserId(userId);
//
//        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);
//        //如果已经存在，则更新数量
//        if(list != null && !list.isEmpty()) {
//            ShoppingCart existingCart = list.get(0);
//            existingCart.setNumber(existingCart.getNumber() + 1);
//            shoppingCartMapper.updateNumberById(existingCart);
//        }else{
//            //如果不存在，则新增一条记录
//            //判断当前添加的是菜品还是套餐
//            Long dishId = shoppingCartDTO.getDishId();
//            Long setmealId = shoppingCartDTO.getSetmealId();
//
//            if(dishId != null) {
//                //添加的是菜品
//                Dish dish = dishMapper.getById(dishId);
//                shoppingCart.setName(dish.getName());
//                shoppingCart.setImage(dish.getImage());
//                shoppingCart.setAmount(dish.getPrice());
//
//            } else if (setmealId != null) {
//                //添加的是套餐
//                Setmeal setmeal = setmealMapper.getById(setmealId);
//                shoppingCart.setName(setmeal.getName());
//                shoppingCart.setImage(setmeal.getImage());
//                shoppingCart.setAmount(setmeal.getPrice());
//
//            } else {
//                throw new IllegalArgumentException(MessageConstant.SETMEAL_ID_OR_DISH_ID_CANNOT_BE_NULL);
//            }
//            shoppingCart.setCreateTime(LocalDateTime.now());
//            shoppingCart.setNumber(StatusConstant.ENABLE);
//        }
//
//
//        shoppingCartMapper.insert(shoppingCart);
    }

    /**
     * 查看购物车
      * @return
     */
    @Override
    public List<ShoppingCart> showShoppingCart() {
        //获得当前用户id
        Long userId = BaseContext.getCurrentId();
        String cartKey = RedisKeyConstant.SHOPPING_CART_KEY + userId;

        try {
            //先从redis中查询
            BoundHashOperations<String, String, ShoppingCart> cartHash = redisTemplate.boundHashOps(cartKey);
            List<ShoppingCart> carts = cartHash.values();
            if (!carts.isEmpty()) {
                //存在,转换并返回
                carts = carts.stream()
                        .map(obj -> (ShoppingCart) obj)
                        .sorted(Comparator.comparing(ShoppingCart::getCreateTime))
                        .collect(Collectors.toList());
                return carts;
            }
        } catch (Exception e) {
            log.error(MessageConstant.SELECT_CART_FAILED);
        }

        //redis中无数据,查询MySQL,并从MySQL同步到redis
        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setUserId(userId);
        List<ShoppingCart> carts = shoppingCartMapper.getByUserId(userId);
        if (!carts.isEmpty()){
            syncMySQLToRedis(carts, userId);
        }
        return carts;
    }


    /**
     * 清空购物车
     */
    @Override
    public void cleanShoppingCartByUserId(Long userId) {
        String cartKey = RedisKeyConstant.SHOPPING_CART_KEY + userId;

        try{
            //清理redis缓存
            redisTemplate.delete(cartKey);
            log.info("用户{}购物车redis缓存以清空", userId);
        } catch (Exception e) {
            log.error(MessageConstant.CLEAN_CART_FAILED, e);
        }
        //异步清理MySQL数据
        asyncCartSyncService.clearMySqlCart(userId);

    }

    /**
     * 删除购物车中的某个商品
     * @param shoppingCartDTO
     */
    @Override
    public void deleteByDishIdOrSetmealId(ShoppingCartDTO shoppingCartDTO) {
        Long userId = BaseContext.getCurrentId();
        Long dishId = shoppingCartDTO.getDishId();
        Long setmealId = shoppingCartDTO.getSetmealId();
        String cartKey = RedisKeyConstant.SHOPPING_CART_KEY + userId;

        //生成redis Hash的Field(区分菜品和套餐，以及口味)
        String field = buildCartField(dishId, setmealId, shoppingCartDTO.getDishFlavor());

        try {
            //从redis中删除
            BoundHashOperations<String, String, ShoppingCart> cartHash = redisTemplate.boundHashOps(cartKey);
            ShoppingCart cartItem = cartHash.get(field);

            if (cartItem != null) {
                if (cartItem.getNumber() > 1) {
                    //商品数量大于1，仅减少数量
                    cartItem.setNumber(cartItem.getNumber() - 1);
                    cartHash.put(field, cartItem);
                    log.info("用户{}减少购物车商品{}数量，当前数量为{}", userId, field, cartItem.getNumber());
                } else {
                    //商品数量等于1，删除该记录
                    cartHash.delete(field);
                    log.info("用户{}删除购物车商品{}", userId,field);
                }
            }

            //异步删除MySQL中的购物车数据
            asyncCartSyncService.deleteShoppingCartItem(userId, dishId, setmealId, shoppingCartDTO.getDishFlavor());
        } catch (Exception e) {
            log.error(MessageConstant.CLEAN_REDIS_CART_FAILED);
            shoppingCartMapper.deleteByUserIdAndDishIdOrSetmealId(userId, dishId, setmealId);
        }

    }

    // -------------------------工具方法------------------------------------
    /**
     * 构建购物车对象
     */
    public ShoppingCart buildShoppingCart(ShoppingCartDTO shoppingCartDTO, Long userId) {
        ShoppingCart cart = new ShoppingCart();
        Long dishId = shoppingCartDTO.getDishId();
        Long setmealId = shoppingCartDTO.getSetmealId();

        BeanUtils.copyProperties(shoppingCartDTO, cart);
        cart.setUserId(userId);
        cart.setCreateTime(LocalDateTime.now());

        //查询数据库中是否有该商品数据
        ShoppingCart dbCart = queryDbCart(userId, dishId, setmealId, shoppingCartDTO.getDishFlavor());
        if (dbCart != null && dbCart.getId() != null) {
            //存在,复用数据库中的id
            cart.setId(dbCart.getId());
        } else {
            //不存在,设置一个临时的id
            cart.setId(System.currentTimeMillis()); // 使用时间戳作为临时ID
        }


        if (dishId != null) {
            Dish dish = dishMapper.getById(dishId);
            if (dish == null) {
                throw new IllegalArgumentException(MessageConstant.DISH_NOT_FOUND);
            }
            cart.setName(dish.getName());
            cart.setImage(dish.getImage());
            cart.setAmount(dish.getPrice());
        } else if (setmealId != null) {
            Setmeal setmeal = setmealMapper.getById(setmealId);
            if (setmeal == null) {
                throw new IllegalArgumentException(MessageConstant.SETMEAL_NOT_FOUND);
            }
            cart.setName(setmeal.getName());
            cart.setImage(setmeal.getImage());
            cart.setAmount(setmeal.getPrice());
        } else {
            throw new IllegalArgumentException(MessageConstant.SETMEAL_ID_OR_DISH_ID_CANNOT_BE_NULL);
        }
        return cart;
    }

    /**
     * 构建购物车Redis field
     * @param dishId 菜品ID
     * @param setmealId 套餐ID
     * @param dishFlavor 菜品口味
     * @return field字符串
     */
    private String buildCartField(Long dishId, Long setmealId, String dishFlavor) {
        StringBuilder fieldBuilder = new StringBuilder();
        if (dishId != null) {
            fieldBuilder.append("dish:").append(dishId);
        } else {
            fieldBuilder.append("setmeal:").append(setmealId);
        }

        //如果口味不同,做出区分
        if (dishFlavor != null && !dishFlavor.isEmpty()) {
            fieldBuilder.append(":flavor:").append(dishFlavor);
        }
        return fieldBuilder.toString();
    }

    /**
     * 从MySQL查询购物车数据
     * @param userId
     * @param dishId
     * @param setmealId
     * @return
     */
    private ShoppingCart queryDbCart(Long userId, Long dishId, Long setmealId, String dishFlavor) {
        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setUserId(userId);

        if (dishId != null) {
            shoppingCart.setDishId(dishId);
        }

        if (setmealId != null) {
            shoppingCart.setSetmealId(setmealId);
        }

        if (dishFlavor != null && !dishFlavor.isEmpty()) {
            shoppingCart.setDishFlavor(dishFlavor);
        }

        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);
        return list.isEmpty() ? null : list.get(0);
    }

    /**
     * 将MySQL中的数据同步到redis中
     * @param carts
     */
    private void syncMySQLToRedis(List<ShoppingCart> carts, Long userId) {
        String cartKey = RedisKeyConstant.SHOPPING_CART_KEY + userId;
        BoundHashOperations<String, String, Object> cartHash = redisTemplate.boundHashOps(cartKey);

        carts.forEach(cart -> {
            //生成redis Hash的Field(区分菜品、套餐和口味)
            String field = buildCartField(cart.getDishId(), cart.getSetmealId(), cart.getDishFlavor());
            cartHash.put(field, cart);
        });

        redisTemplate.expire(cartKey, RedisKeyConstant.CART_EXPIRE_DAYS, TimeUnit.DAYS);
        log.info("用户{}购物车数据同步到redis成功", userId);
    }

    /**
     * 购物车操作降级到MySQL
     * @param shoppingCartDTO
     */
    private void directShoppingCart(ShoppingCartDTO shoppingCartDTO) {
        Long userId = BaseContext.getCurrentId();
        try {
            ShoppingCart cart = buildShoppingCart(shoppingCartDTO, userId);
            List<ShoppingCart> carts = shoppingCartMapper.list(cart);

            if (!carts.isEmpty()) {
                ShoppingCart existingCart = carts.get(0);
                existingCart.setNumber(existingCart.getNumber() + 1);
                shoppingCartMapper.updateNumberById(existingCart);
            } else {
                cart.setNumber(1);
                cart.setCreateTime(LocalDateTime.now());
                shoppingCartMapper.insert(cart);
            }
            log.info("购物车操作降级到MySQL成功,用户Id: {}", userId);
        } catch (Exception e) {
            log.info("购物车操作降级失败,用户Id: {}", userId);
            throw new RuntimeException(MessageConstant.THE_CART_OPERATION_IS_NOT_AVAILABLE, e);
        }
    }

}
