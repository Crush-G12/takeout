package com.xie.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xie.reggie.comon.CustomException;
import com.xie.reggie.entity.Category;
import com.xie.reggie.entity.Setmeal;
import com.xie.reggie.entity.SetmealDish;
import com.xie.reggie.entity.SetmealDto;
import com.xie.reggie.mapper.SetmealMapper;
import com.xie.reggie.service.SetmealDishService;
import com.xie.reggie.service.SetmealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.chrono.IsoChronology;
import java.util.List;

@Service
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {
    @Autowired
    private SetmealDishService setmealDishService;

    @Override
    @Transactional
    public void saveSetmealWithDish(SetmealDto setmealDto) {
        //保存到Setmeal表中
        this.save(setmealDto);
        //保存到SetmealDish表中
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        //给setmealId赋值
        for (SetmealDish setmealDish : setmealDishes) {
            setmealDish.setSetmealId(setmealDto.getId());
        }
        setmealDishService.saveBatch(setmealDishes);
    }

    @Override
    @Transactional
    public void deleteMealWithDish(List<Long> ids) {
        //先确定套餐状态
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        //多个id，SQL语句要使用in
        queryWrapper.in(Setmeal::getId,ids);
        //查询是否有在售卖中的菜品
        queryWrapper.eq(Setmeal::getStatus,1);
        //直接调用框架的方法
        int count = this.count(queryWrapper);
        //不能删除，抛出业务异常
        if(count > 0){
            throw new CustomException("套餐售卖中，不能删除");
        }
        //删除套餐表的数据
        this.removeByIds(ids);
        //删除关联菜品表
        LambdaQueryWrapper<SetmealDish> lambdaQueryWrapper = new LambdaQueryWrapper();
        //多个id，SQL语句要使用in
        lambdaQueryWrapper.in(SetmealDish::getSetmealId,ids);
        setmealDishService.remove(lambdaQueryWrapper);
    }
}
