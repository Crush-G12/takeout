package com.xie.reggie.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xie.reggie.entity.ShoppingCart;
import com.xie.reggie.mapper.ShoppingCartMapper;
import com.xie.reggie.service.ShoppingCartService;
import org.springframework.stereotype.Service;

@Service
public class ShoppingCartServiceImpl extends ServiceImpl<ShoppingCartMapper, ShoppingCart> implements ShoppingCartService {
}
