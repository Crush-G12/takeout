package com.xie.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xie.reggie.comon.R;
import com.xie.reggie.dto.DishDto;
import com.xie.reggie.entity.Category;
import com.xie.reggie.entity.Dish;
import com.xie.reggie.entity.DishFlavor;
import com.xie.reggie.service.CategoryService;
import com.xie.reggie.service.DishFlavorService;
import com.xie.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;
import sun.security.krb5.internal.PAEncTSEnc;

import javax.security.auth.callback.LanguageCallback;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/dish")
@Slf4j
public class DishController {

    @Autowired
    private DishService dishService;

    @Autowired
    private DishFlavorService dishFlavorService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private RedisTemplate redisTemplate;


    @PostMapping
    public R<String> saveDish(@RequestBody DishDto dishDto){
        //log.info("Dish  dto");
        dishService.saveDishWithFlavor(dishDto);

        //根据菜品分类id，清理Redis缓存，避免出现脏数据
        String id = "dishId_" + dishDto.getCategoryId();
        redisTemplate.delete(id);


        return R.success("新增菜品成功");
    }

    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name){
        //分页构造器
        Page<Dish> pageInfo = new Page(page,pageSize);
        Page<DishDto> dishDtoPage = new Page<>();
        //条件构造器
        LambdaQueryWrapper<Dish> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        //查询条件
        lambdaQueryWrapper.like(StringUtils.isNotEmpty(name),Dish::getName,name);
        lambdaQueryWrapper.orderByDesc(Dish::getSort).orderByDesc(Dish::getUpdateTime);
        //查询数据
        dishService.page(pageInfo,lambdaQueryWrapper);

        //对象拷贝
        BeanUtils.copyProperties(pageInfo,dishDtoPage,"records");

        //重新设置categoryName属性
        List<Dish> records = pageInfo.getRecords();
        List<DishDto> dishDtoList = new ArrayList<>();

        for (Dish record : records) {
            //使用DishDto对象替换records里的Dish对象
            DishDto dishDto = new DishDto();
            //将Dish的数据拷贝到DishDto里
            BeanUtils.copyProperties(record,dishDto);
            //先通过重新设置categoryId查询出categoryName
            Category category = categoryService.getById(record.getCategoryId());
            String categoryName = null;
            if (category != null){
                categoryName = category.getName();
            }else {
                categoryName = "未分类";
            }
            //将categoryName保存到DishDto
            dishDto.setCategoryName(categoryName);
            //替换records里的Dish对象
            dishDtoList.add(dishDto);
        }
        //替换records里的Dish对象
        dishDtoPage.setRecords(dishDtoList);

        return R.success(dishDtoPage);
    }

    @GetMapping("/{id}")
    public R<DishDto> queryById(@PathVariable Long id){

        //根据id查询DishDto对象
        DishDto dishDto = dishService.getByIdWithFlavor(id);

        return R.success(dishDto);
    }

    @PutMapping
    public R<String> updateDish(@RequestBody DishDto dishDto){
        //修改涉及两张表的操作
        dishService.updateByIdWithFlavor(dishDto);

        //根据菜品分类id，清理Redis缓存，避免出现脏数据
        String id = "dishId_" + dishDto.getCategoryId();
        redisTemplate.delete(id);

        return R.success("修改菜品成功");
    }

    /*@GetMapping("/list")
    public R<List<Dish>> list(Dish dish){
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        List<Dish> dishList = null;
        if(dish.getCategoryId() != null){
            queryWrapper.eq(Dish::getCategoryId,dish.getCategoryId());
            queryWrapper.eq(Dish::getStatus,1);
            queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);
            dishList = dishService.list(queryWrapper);
        }

        return R.success(dishList);
    }*/

    @GetMapping("/list")
    public R<List<DishDto>> list(Dish dish){
        List<DishDto> dishDtoList = new ArrayList<>();
        //先看Redis缓存中是否存在数据

        //Redis中，根据菜品的id分类存储——动态构造id
        String id = "dishId_" + dish.getCategoryId();
        dishDtoList = (List<DishDto>) redisTemplate.opsForValue().get(id);


        //若存在，直接返回
        if(dishDtoList != null){
            return R.success(dishDtoList);
        }

        //若不存在，则继续执行数据库查询操作

        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        List<Dish> dishList = null;
        //根据菜品的分类id查询
        if(dish.getCategoryId() != null){
            queryWrapper.eq(Dish::getCategoryId,dish.getCategoryId());
            queryWrapper.eq(Dish::getStatus,1);
            queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);
            dishList = dishService.list(queryWrapper);
        }

        dishDtoList = new ArrayList<>();
        for (Dish d : dishList) {
            //获取口味
            LambdaQueryWrapper<DishFlavor> lambdaQueryWrapper = new LambdaQueryWrapper<>();
            lambdaQueryWrapper.eq(DishFlavor::getDishId,d.getId());
            List<DishFlavor> flavors = dishFlavorService.list(lambdaQueryWrapper);
            //获取分类名称
            Category category = categoryService.getById(d.getCategoryId());
            String categoryName = category.getName();
            //拷贝
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(d,dishDto);
            dishDto.setFlavors(flavors);
            dishDto.setCategoryName(categoryName);
            dishDtoList.add(dishDto);
        }

        //将数据保存到缓存中,有效期60分钟
        redisTemplate.opsForValue().set(id,dishDtoList,60, TimeUnit.MINUTES);

        return R.success(dishDtoList);
    }


}
