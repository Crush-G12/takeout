package com.xie.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xie.reggie.entity.Category;

public interface CategoryService extends IService<Category> {
    //Service层还可以自己扩展方法
    void remove(Long id);
}
