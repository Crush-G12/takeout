package com.xie.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xie.reggie.dto.DishDto;
import com.xie.reggie.entity.Dish;
import com.xie.reggie.entity.DishFlavor;
import com.xie.reggie.mapper.DishMapper;
import com.xie.reggie.service.DishFlavorService;
import com.xie.reggie.service.DishService;
import org.springframework.beans.BeanUtils;
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

    @Override
    public DishDto getByIdWithFlavor(Long id) {
        DishDto dishDto = new DishDto();
        //先查菜品表
        Dish dish = this.getById(id);
        //拷贝到DishDto
        BeanUtils.copyProperties(dish,dishDto);

        //再查口味表
        LambdaQueryWrapper<DishFlavor> flavorQueryWrapper = new LambdaQueryWrapper<>();
        flavorQueryWrapper.eq(DishFlavor::getDishId,id);
        List<DishFlavor> flavors = dishFlavorService.list(flavorQueryWrapper);
        //拷贝到DishDto
        dishDto.setFlavors(flavors);

        //将对象返回
        return dishDto;
    }

    @Override
    public void updateByIdWithFlavor(DishDto dishDto) {
        //先更新Dish表
        this.updateById(dishDto);
        //再更新DishFlavor表

        //先清理之前的口味数据
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        Long id = dishDto.getId();
        queryWrapper.eq(DishFlavor::getDishId,id);
        dishFlavorService.remove(queryWrapper);
        //再插入新的口味数据
        List<DishFlavor> flavors = dishDto.getFlavors();
        for (DishFlavor flavor : flavors) {
            flavor.setDishId(id);
        }
        dishFlavorService.saveBatch(flavors);

    }
}
