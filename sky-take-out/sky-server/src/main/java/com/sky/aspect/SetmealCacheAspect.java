package com.sky.aspect;


import com.sky.dto.DishDTO;
import com.sky.dto.SetmealDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.service.DishService;
import com.sky.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 菜品缓存切面类,用于统一处理菜品相关的缓存逻辑
 */
//@Aspect
//@Component
@Slf4j
public class SetmealCacheAspect {

    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private SetmealService setmealService;

    /**
     * 自定义切入点: 匹配菜品的增删改操作
     */
    @Pointcut("execution(* com.sky.controller.admin.SetmealController.save(..)) ||" +
              "execution(* com.sky.controller.admin.SetmealController.update(..)) ||" +
              "execution(* com.sky.controller.admin.SetmealController.delete(..)) ||" +
              "execution(* com.sky.controller.admin.SetmealController.startOrStop(..))")
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
            case "delete":
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
        SetmealDTO setmealDTO = (SetmealDTO) args[0];
        String key = "setmeal_" + setmealDTO.getCategoryId();
        redisTemplate.delete(key);
        log.info("清除菜品分类ID为 {} 的缓存", setmealDTO.getCategoryId());
    }

    /**
     * 处理删除菜品方法的缓存清除逻辑
     */
    private void handleDeleteMethod(Object[] args) {
        List<Long> ids = (List<Long>) args[0];
        for (Long id :ids) {
            Setmeal setmeal = setmealService.getById(id);
            if(setmeal != null) {
                String key = "setmeal_" + setmeal.getCategoryId();
                redisTemplate.delete(key);
                log.info("清除菜品分类ID为 {} 的缓存", setmeal.getCategoryId());
            }
        }
    }

    /**
     * 处理更新菜品方法的缓存清除逻辑
     * 需要同时删除旧分类和新分类的缓存
     */
    private void handleUpdateMethod(Object[] args) {
        SetmealDTO setmealDTO = (SetmealDTO) args[0];
        Setmeal oldSetmeal = setmealService.getById(setmealDTO.getId());

        //清除旧分类的缓存
        if (oldSetmeal != null) {
            String oldKey = "setmeal_" + oldSetmeal.getCategoryId();
            redisTemplate.delete(oldKey);
            log.info("清除旧菜品分类ID为 {} 的缓存", oldSetmeal.getCategoryId());
        }

        //清除新分类的缓存
        String newKey = "setmeal_" + setmealDTO.getCategoryId();
        redisTemplate.delete(newKey);
        log.info("清除新菜品分类ID为 {} 的缓存", setmealDTO.getCategoryId());
    }

    /**
     * 处理启用/停用菜品方法的缓存清除逻辑
     * 需要删除对应分类的缓存
     */
    private void handleStartOrStopMethod(Object[] args) {
        //args[0]是status,args[1]是id列表
        Long setmealId = (Long) args[1];
        Setmeal setmeal = setmealService.getById(setmealId);
        if(setmeal != null) {
            String key = "srtmeal_" + setmeal.getCategoryId();
            redisTemplate.delete(key);
            log.info("清除菜品分类ID为 {} 的缓存", setmeal.getCategoryId());
        }
    }

}
