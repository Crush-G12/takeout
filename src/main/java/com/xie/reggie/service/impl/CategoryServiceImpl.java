package com.xie.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xie.reggie.comon.CustomException;
import com.xie.reggie.entity.Category;
import com.xie.reggie.entity.Dish;
import com.xie.reggie.entity.Setmeal;
import com.xie.reggie.mapper.CategoryMapper;
import com.xie.reggie.mapper.SetmealMapper;
import com.xie.reggie.service.CategoryService;
import com.xie.reggie.service.DishService;
import com.xie.reggie.service.SetmealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    @Autowired
    private DishService dishService;

    @Autowired
    private SetmealService setmealService;


    /**
     * 删除分类前，要先判断是否关联了菜品
     * @param id
     */
    @Override
    public void remove(Long id) {
        //分类是否关联了菜品
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Dish::getCategoryId,id);
        int count = dishService.count(queryWrapper);
        if(count > 0){
            //抛出自定义的异常
            throw new CustomException("当前分类关联了菜品，不能删除");
        }

        //分类是否关联了套餐
        LambdaQueryWrapper<Setmeal> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(Setmeal::getCategoryId,id);
        count = setmealService.count(lambdaQueryWrapper);
        if(count > 0){
            //抛出自定义的异常
            throw new CustomException("当前分类关联了套餐，不能删除");
        }

        //若没有关联，则执行删除操作
        //调用父类的方法进行删除
        super.removeById(id);
    }
}
