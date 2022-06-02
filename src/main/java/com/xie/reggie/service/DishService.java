package com.xie.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xie.reggie.dto.DishDto;
import com.xie.reggie.entity.Dish;

public interface DishService extends IService<Dish> {
    //扩展方法，保存到菜品和口味两张数据库表中
    void saveDishWithFlavor(DishDto dishDto);

    //扩展方法，根据id查询菜品信息和口味信息
    DishDto getByIdWithFlavor(Long id);

    //扩展方法，修改菜品信息和口味信息
    void updateByIdWithFlavor(DishDto dishDto);
}
