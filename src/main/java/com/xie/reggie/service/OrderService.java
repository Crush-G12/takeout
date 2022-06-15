package com.xie.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xie.reggie.entity.Orders;

public interface OrderService extends IService<Orders> {
    //扩展submit方法，获取用户购物车信息
    void submit(Orders orders);

}
