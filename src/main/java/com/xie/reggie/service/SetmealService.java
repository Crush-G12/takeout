package com.xie.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xie.reggie.entity.Setmeal;
import com.xie.reggie.entity.SetmealDto;

import java.util.List;

public interface SetmealService extends IService<Setmeal> {
    //扩展方法，保存套餐到两张数据库表中
    void saveSetmealWithDish(SetmealDto setmealDto);

    //扩展方法，删除套餐和关联的菜品
    void deleteMealWithDish(List<Long> ids);
}
