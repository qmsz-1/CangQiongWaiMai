package com.sky.controller.user;


import com.sky.constant.StatusConstant;
import com.sky.entity.Setmeal;
import com.sky.result.Result;
import com.sky.service.CategoryService;
import com.sky.service.SetmealService;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController("userSetmealController")
@RequestMapping("/user/setmeal")
@Slf4j
@Api("C端套餐浏览接口")
public class SetmealController {

    @Autowired
    private SetmealService setService;
    @Autowired
    private CategoryService categoryService;

    /**
     * 根据分类id查询套餐
     * @param categoryId
     * @return
     */
    @GetMapping("/list")
    @ApiOperation("根据分类id查询套餐")
    public Result<List<Setmeal>> list(Long categoryId) {
        log.info("根据分类id查询套餐: {}", categoryId);
        Setmeal setmeal = new Setmeal();
        setmeal.setCategoryId(categoryId);
        setmeal.setStatus(StatusConstant.ENABLE); // 只查询启用状态的套餐

        List<Setmeal> setmealList = setService.list(setmeal);

        return Result.success(setmealList);
    }

    /**
     * 根据套餐id查询套餐详情
     * @param id
     * @return
     */
    @GetMapping("dish/{id}")
    @ApiOperation("根据套餐id查询套餐菜品")
    public Result<List<DishItemVO>> getDishBySetmealId(@PathVariable("id") Long id) {
        log.info("根据套餐id查询套餐菜品: {}", id);
        List<DishItemVO> dishItemVOList = setService.getDishBySetmealId(id);
        return Result.success(dishItemVOList);
    }

}
