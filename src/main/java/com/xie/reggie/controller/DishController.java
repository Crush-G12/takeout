package com.xie.reggie.controller;

import com.xie.reggie.comon.R;
import com.xie.reggie.dto.DishDto;
import com.xie.reggie.service.DishFlavorService;
import com.xie.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dish")
@Slf4j
public class DishController {

    @Autowired
    private DishService dishService;

    @Autowired
    private DishFlavorService dishFlavorService;

    @PostMapping
    public R<String> saveDish(@RequestBody DishDto dishDto){
        //log.info("Dish  dto");
        dishService.saveDishWithFlavor(dishDto);
        return R.success("新增菜品成功");
    }
}
