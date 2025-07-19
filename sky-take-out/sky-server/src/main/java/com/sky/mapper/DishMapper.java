package com.sky.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.pagehelper.Page;
import com.sky.annotation.AutoFill;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.enumeration.OperationType;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Mapper
public interface DishMapper {

    /**
     * 根据分类id查询菜品数量
     * @param id
     * @return
     */
    @Select("select count(id) from dish where category_id = #{category_id}")
    Integer countByCategoryId(long id);

    /**
     * 菜品分页查询
     * @param dishPageQueryDTO
     * @return
     */
    Page<Dish> pageQuery(DishPageQueryDTO dishPageQueryDTO);

    /**
     * 新增菜品
     * @param dish
     * @return
     */
    @AutoFill(value = OperationType.INSERT)
    void insert(Dish dish);

    /**
     * 批量删除菜品
     * @param id
     * @return
     */
    @Delete("delete from dish where id = #{id}")
    void deleteByIds(Long id);

    /**
     * 根据主键擦查询菜品
     * @param id
     * @return
     */
    @Select("select *from dish where id = #{id}")
    Dish getById(Long id);

    /**
     * 修改菜品信息
     * @param dish
     * @return
     */
    @AutoFill(value = OperationType.UPDATE)
    void update(Dish dish);

    /**
     * 根据分类id查询菜品
     * @param dish
     * @return
     */
    List<Dish> list(Dish dish);
}
