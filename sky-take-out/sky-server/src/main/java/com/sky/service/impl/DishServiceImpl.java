package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.xiaoymin.knife4j.core.util.CollectionUtils;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.entity.Setmeal;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class DishServiceImpl implements DishService {

    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private DishFlavorMapper dishFlavorMapper;
    @Autowired
    private SetmealDishMapper setmealDishMapper;
    @Autowired
    private SetmealMapper setmealMapper;

    /**
     * 菜品分页查询
     * @param dishPageQueryDTO
     * @return
     */
    @Override
    public PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO) {
        PageHelper.startPage(dishPageQueryDTO.getPage(), dishPageQueryDTO.getPageSize());

        Page<Dish> page = dishMapper.pageQuery(dishPageQueryDTO);
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 新增菜品和对应的口味
     * @param dishDTO
     * @return
     */
    @Transactional
    @Override
    public void saveWithFlavor(DishDTO dishDTO) {
        Dish dish = new Dish();

        BeanUtils.copyProperties(dishDTO,dish);

        //插入菜品数据
        dishMapper.insert(dish);

        //获取insert语句生成的主键值
        long dishId = dish.getId();

        //插入口味数据
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if(flavors!=null&&flavors.size()>0){
            flavors.forEach(dishFlavor->{
                dishFlavor.setDishId(dishId);
            });
            dishFlavorMapper.insertBatch(flavors);
        }
    }

    /**
     * 批量删除菜品
     * @param ids
     * @return
     */
    @Override
    public void deleteByIds(List<Long> ids) {
        //判断当前菜品是否在起售
        for(Long id:ids){
            Dish dish = dishMapper.getById(id);
            if(dish.getStatus() == StatusConstant.ENABLE){
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }
        }
        //判断当前菜品是否在套餐内
        List<Long> setmealIds = setmealDishMapper.getSetmealIdsByDishIds(ids);
        if (setmealIds != null && setmealIds.size() > 0) {
            throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
        }

        //删除菜品数据
        for (Long id : ids) {
            dishMapper.deleteByIds(id);

            //删除菜品相关的口味数据
            dishFlavorMapper.deleteByDishId(id);
        }
    }

    /**
     * 根据id查询菜品和口味
     * @param id
     * @return
     */
    @Override
    public DishVO getByIdWithFlavor(Long id) {
        //根据id查询菜品
        Dish dish = dishMapper.getById(id);

        //根据菜品id查询口味
        List<DishFlavor> dishFlavors = dishFlavorMapper.getByDishId(dish.getId());
        //将查询的信息封装到dishVO
        DishVO dishVO = new DishVO();

        BeanUtils.copyProperties(dish,dishVO);
        dishVO.setFlavors(dishFlavors);

        return dishVO;
    }

    /**
     * 修改菜品信息
     * @param dishDTO
     * @return
     */
    @Override
    public void updateWithFlavor(DishDTO dishDTO) {
        Dish dish = new Dish();

        BeanUtils.copyProperties(dishDTO, dish);

        //修改菜品信息
        dishMapper.update(dish);
        //修改口味信息
        //删除原先的口味数据
        dishFlavorMapper.deleteByDishId(dish.getId());
        //再插入口味数据
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if(flavors!=null&&flavors.size()>0){
            flavors.forEach(dishFlavor->{
                dishFlavor.setDishId(dishDTO.getId());
            });
            dishFlavorMapper.insertBatch(flavors);
        }
    }

    /**
     * 起售,禁售菜品
     * @param status
     * @param id
     * @return
     */
    @Override
    public void startOrStop(Integer status, Long id) {

        if (status == StatusConstant.DISABLE){
            //如果停售菜品,将包含这个菜品的套餐也停售
            List<Long> dishIds = new ArrayList<>();
            dishIds.add(id);
            List<Long> setmealIds = setmealDishMapper.getSetmealIdsByDishIds(dishIds);
            if (setmealIds != null && setmealIds.size() > 0) {
                for (Long setmealId : setmealIds){
                    Setmeal setmeal = Setmeal.builder()
                            .id(setmealId)
                            .status(StatusConstant.DISABLE)
                            .build();

                    setmealMapper.update(setmeal);
                }
            }
        }

        Dish dish = Dish.builder()
                .id(id)
                .status(status)
                .build();

        dishMapper.update(dish);

    }

    /**
     * 根据分类id查询菜品
     * @param categoryId
     * @return
     */
    @Override
    public List<Dish> list(Long categoryId) {
        Dish dish = Dish.builder()
                .categoryId(categoryId)
                .status(StatusConstant.ENABLE)
                .build();

        return dishMapper.list(dish);
    }

    /**
     * 根据分类id查询菜品
     * @param dish
     * @return
     */
    @Override
    public List<DishVO> listByCategoryId(Dish dish) {
        List<Dish> dishes = dishMapper.list(dish);

        List<DishVO> dishVOList = new ArrayList<>();

        for (Dish d : dishes) {
            DishVO dishVO = new DishVO();
            BeanUtils.copyProperties(d, dishVO);

            List<DishFlavor> flavors = dishFlavorMapper.getByDishId(d.getId());

            dishVO.setFlavors(flavors);
            dishVOList.add(dishVO);
        }

        return dishVOList;
    }

    @Override
    public Dish getById(Long id) {
        if(id == null) {
            throw new IllegalArgumentException(MessageConstant.DISH_ID_CANNOT_BE_NULL);
        }

        return dishMapper.getById(id);
    }
}
