package com.xie.reggie.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xie.reggie.dto.DishDto;
import com.xie.reggie.entity.Dish;
import com.xie.reggie.entity.DishFlavor;
import com.xie.reggie.mapper.DishMapper;
import com.xie.reggie.service.DishFlavorService;
import com.xie.reggie.service.DishService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {

    @Autowired
    DishFlavorService dishFlavorService;

    /**
     * 扩展方法，保存到菜品和口味两张数据库表中
     * @param dishDto
     */
    @Override
    @Transactional
    public void saveDishWithFlavor(DishDto dishDto) {
        //先将部分数据保存到菜品表中
        this.save(dishDto);

        //获取id
        Long id = dishDto.getId();
        //给DishFlavor实体类绑定菜品的id
        List<DishFlavor> flavors = dishDto.getFlavors();
        for (DishFlavor flavor : flavors) {
            flavor.setDishId(id);
        }
        //将数据保存到口味表中
        dishFlavorService.saveBatch(flavors);

    }
}
