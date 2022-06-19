package com.xie.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xie.reggie.comon.R;
import com.xie.reggie.entity.Category;
import com.xie.reggie.entity.Setmeal;
import com.xie.reggie.entity.SetmealDto;
import com.xie.reggie.service.CategoryService;
import com.xie.reggie.service.SetmealDishService;
import com.xie.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 套餐管理
 */
@RestController
@RequestMapping("/setmeal")
@Slf4j
public class SetmealController {

    @Autowired
    SetmealService setmealService;

    @Autowired
    CategoryService categoryService;

    @PostMapping
    @CacheEvict(value = "setMealCache",allEntries = true)
    public R<String> saveMeal(@RequestBody SetmealDto setmealDto){
        setmealService.saveSetmealWithDish(setmealDto);

        return R.success("新建套餐成功");
    }

    @GetMapping("/page")
    public R<Page> page(int page,int pageSize,String name){
        //分页构造器
        Page<Setmeal> pageInfo = new Page<>(page,pageSize);
        Page<SetmealDto> dtoPage = new Page<>();
        //分页条件
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(StringUtils.isNotEmpty(name),Setmeal::getName,name);
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);
        //查询数据
        Page<Setmeal> setmealPage = setmealService.page(pageInfo, queryWrapper);
        //对象拷贝
        BeanUtils.copyProperties(pageInfo,dtoPage,"records");
        //获得套餐的分类id,并查询套餐的名称
        List<Setmeal> records = pageInfo.getRecords();
        List<SetmealDto> dtoList = new ArrayList<>();
        for (Setmeal record : records) {
            SetmealDto setmealDto = new SetmealDto();
            //先将其它属性拷贝到setmealDto里面
            BeanUtils.copyProperties(record,setmealDto);
            //再设置categoryName属性
            Category category = categoryService.getById(record.getCategoryId());
            if (category != null){
                String categoryName = category.getName();
                setmealDto.setCategoryName(categoryName);
                dtoList.add(setmealDto);
            }
        }
        dtoPage.setRecords(dtoList);

        return R.success(dtoPage);
    }

    @DeleteMapping
    @CacheEvict(value = "setMealCache",allEntries = true)
    public R<String> delete(@RequestParam List<Long> ids){
        setmealService.deleteMealWithDish(ids);
        return  R.success("删除成功");
    }

    @GetMapping("/list")
    @Cacheable(value = "setMealCache", key = "#setmeal.categoryId")
    public R<List<Setmeal>> list(Setmeal setmeal){
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(setmeal.getId() != null,Setmeal::getId,setmeal.getId());
        queryWrapper.eq(setmeal.getStatus() != null,Setmeal::getStatus,setmeal.getStatus());
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);
        List<Setmeal> setmealList = setmealService.list(queryWrapper);

        return R.success(setmealList);
    }
}
