package com.sky.mapper;

import com.sky.entity.SetmealDish;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SetmealDishMapper {

    /**
     * 根据菜品id查询套餐id
     * @param id
     * @return
     */
    List<Long> getSetmealIdsByDishIds(@Param("dishIds") List<Long> id);

    /**
     * 保存套餐与菜品的关系
     * @param setmealDishes
     */
    void insertBatch(List<SetmealDish> setmealDishes);

    /**
     * 根据套餐id查询相关菜品信息
     * @param id
     * @return
     */
    List<SetmealDish> getBySetmealId(Long id);

    /**
     * 根据套餐id删除相关菜品
     * @param id
     */
    void deleteBySetmealId(Long id);
}
