package com.sky.controller.user;


import com.sky.constant.StatusConstant;
import com.sky.entity.Dish;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController("userDishController")
@RequestMapping("/user/dish")
@Slf4j
@Api("C端菜品浏览接口")
public class DishController {

    @Autowired
    private DishService dishService;
    @Autowired
    private RedisTemplate<String, List<DishVO>> redisTemplate;

    /**
     * 根据分类id查询菜品
     * @param categoryId
     * @return
     */
    @GetMapping("/list")
    @ApiOperation("根据分类id查询菜品")
    public Result<List<DishVO>> list(Long categoryId) {

        //构造redis中的key
        String key = "dish_" + categoryId;

        //查询redis中是否存在菜品
        List<DishVO> list = redisTemplate.opsForValue().get(key);
        if (list != null ) {
            //如果存在,直接返回
            return Result.success(list);
        }

        //如果不存在,查询数据库并存入redis
        log.info("根据分类id查询菜品: {}", categoryId);
        Dish dish = new Dish();
        dish.setCategoryId(categoryId);
        dish.setStatus(StatusConstant.ENABLE); // 只查询启用状态的菜品

        list = dishService.listByCategoryId(dish);

        if (list == null || list.isEmpty()) {
            list = new ArrayList<>();
        }
        //插入数据
        redisTemplate.opsForValue().set(key, list);

        return Result.success(list);
    }

}
