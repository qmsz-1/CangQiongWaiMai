package com.sky.service;

import com.sky.annotation.AutoFill;
import com.sky.dto.DishDTO;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.enumeration.OperationType;
import com.sky.result.PageResult;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealVO;
import org.springframework.stereotype.Service;

import java.util.List;

public interface SetmealService {

    /**
     * 套餐分页查询
     * @param setmealPageQueryDTO
     * @return
     */
    PageResult pageQuery(SetmealPageQueryDTO setmealPageQueryDTO);

    /**
     * 新增套餐
     * @param setmealDTO
     * @return
     */
    void saveWithDish(SetmealDTO setmealDTO);

    /**
     * 根据id查询套餐
     * @param id
     * @return
     */
    SetmealVO getByIdWithDish(Long id);

    /**
     * 修改套餐
     * @param setmealDTO
     * @return
     */
    void update(SetmealDTO setmealDTO);

    /**
     * 批量删除套餐
     * @param ids
     * @return
     */
    void delete(List<Long> ids);

    /**
     * 起售,停售套餐
     * @param status
     * @param id
     * @return
     */
    void startOrStop(Integer status, Long id);

    /**
     * 根据条件查询套餐列表
     * @param setmeal
     * @return
     */
    List<Setmeal> list(Setmeal setmeal);

    /**
     * 根据套餐id查询套餐菜品
     * @param id
     * @return
     */
    List<DishItemVO> getDishBySetmealId(Long id);

    /**
     * 根据套餐id查询套餐信息
     * @param setmealId
     * @return
     */
    Setmeal getById(Long setmealId);
}
