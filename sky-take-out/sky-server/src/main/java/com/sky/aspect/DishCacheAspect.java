package com.sky.aspect;


import com.sky.dto.DishDTO;
import com.sky.entity.Dish;
import com.sky.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.aspectj.lang.JoinPoint;

import java.util.List;

/**
 * 菜品缓存切面类,用于统一处理菜品相关的缓存逻辑
 */
@Aspect
@Component
@Slf4j
public class DishCacheAspect {

    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private DishService dishService;

    /**
     * 自定义切入点: 匹配菜品的增删改操作
     */
    @Pointcut("execution(* com.sky.controller.admin.DishController.save(..)) ||" +
              "execution(* com.sky.controller.admin.DishController.update(..)) ||" +
              "execution(* com.sky.controller.admin.DishController.deleteByIds(..)) ||" +
              "execution(* com.sky.controller.admin.DishController.startOrStop(..))")
    public void dishCachePointcut() {
        // 这里不需要具体实现,只是定义一个切入点
    }

    /**
     * 在执行对应方法时,清除相关的缓存
     */
    @AfterReturning("dishCachePointcut()")
    public void clearDishCache(JoinPoint joinPoint) {
        log.info("清除菜品相关缓存");

        // 获取目标方法名
        String methodName = joinPoint.getSignature().getName();
        //获取方法参数
        Object[] args  = joinPoint.getArgs();

        switch(methodName){
            case "save":
                handleSaveMethod(args);
                break;
            case "update":
                handleUpdateMethod(args);
                break;
            case "deleteByIds":
                handleDeleteMethod(args);
                break;
            case "startOrStop":
                handleStartOrStopMethod(args);
                break;
        }

        log.info("菜品相关缓存已清除");

    }

    /**
     * 处理新增菜品方法的缓存清除逻辑
     */
    private void handleSaveMethod(Object[] args) {
        DishDTO dishDTO = (DishDTO) args[0];
        String key = "dish_" + dishDTO.getCategoryId();
        redisTemplate.delete(key);
        log.info("清除菜品分类ID为 {} 的缓存", dishDTO.getCategoryId());
    }

    /**
     * 处理删除菜品方法的缓存清除逻辑
     */
    private void handleDeleteMethod(Object[] args) {
        List<Long> ids = (List<Long>) args[0];
        for (Long id :ids) {
            Dish dish = dishService.getById(id);
            if(dish != null) {
                String key = "dish_" + dish.getCategoryId();
                redisTemplate.delete(key);
                log.info("清除菜品分类ID为 {} 的缓存", dish.getCategoryId());
            }
        }
    }

    /**
     * 处理更新菜品方法的缓存清除逻辑
     * 需要同时删除旧分类和新分类的缓存
     */
    private void handleUpdateMethod(Object[] args) {
        DishDTO dishDTO = (DishDTO) args[0];
        Dish oldDish = dishService.getById(dishDTO.getId());

        //清除旧分类的缓存
        if (oldDish != null) {
            String oldKey = "dish_" + oldDish.getCategoryId();
            redisTemplate.delete(oldKey);
            log.info("清除旧菜品分类ID为 {} 的缓存", oldDish.getCategoryId());
        }

        //清除新分类的缓存
        String newKey = "dish_" + dishDTO.getCategoryId();
        redisTemplate.delete(newKey);
        log.info("清除新菜品分类ID为 {} 的缓存", dishDTO.getCategoryId());
    }

    /**
     * 处理启用/停用菜品方法的缓存清除逻辑
     * 需要删除对应分类的缓存
     */
    private void handleStartOrStopMethod(Object[] args) {
        //args[0]是status,args[1]是id列表
        Long dishId = (Long) args[1];
        Dish dish = dishService.getById(dishId);
        if(dish != null) {
            String key = "dish_" + dish.getCategoryId();
            redisTemplate.delete(key);
            log.info("清除菜品分类ID为 {} 的缓存", dish.getCategoryId());
        }
    }

}
