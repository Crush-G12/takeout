package com.xie.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xie.reggie.dto.DishDto;
import com.xie.reggie.entity.Dish;

public interface DishService extends IService<Dish> {
    //扩展方法，保存到菜品和口味两张数据库表中
    void saveDishWithFlavor(DishDto dishDto);
}
