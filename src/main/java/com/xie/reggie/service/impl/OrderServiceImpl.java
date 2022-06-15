package com.xie.reggie.service.impl;

import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlShowOpenTablesStatement;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sun.org.apache.xpath.internal.operations.Or;
import com.xie.reggie.comon.BaseContext;
import com.xie.reggie.comon.CustomException;
import com.xie.reggie.entity.*;
import com.xie.reggie.mapper.OrderMapper;
import com.xie.reggie.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Orders> implements OrderService{

    @Autowired
    private ShoppingCartService shoppingCartService;

    @Autowired
    private UserService userService;

    @Autowired
    private AddressBookService addressBookService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderDetailService orderDetailService;

    @Override
    @Transactional
    public void submit(Orders orders) {
        //根据用户id获取购物车信息
        Long userId = BaseContext.getId();
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId,userId);
        List<ShoppingCart> list = shoppingCartService.list(queryWrapper);

        if(list == null || list.size() == 0){
            throw new CustomException("购物车为空，不能创建订单");
        }
        //先根据用户id查询用户数据和地址数据
        User user = userService.getById(userId);

        AddressBook addressBook = addressBookService.getById(orders.getAddressBookId());

        AtomicInteger amount = new AtomicInteger(0);

        Long orderId = IdWorker.getId();

        //遍历购物车，获取总金额、订单明细
        List<OrderDetail> orderDetails = new ArrayList<>();
        for (ShoppingCart shoppingCart : list) {
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setOrderId(orderId);
            orderDetail.setNumber(shoppingCart.getNumber());
            orderDetail.setDishFlavor(shoppingCart.getDishFlavor());
            orderDetail.setDishId(shoppingCart.getDishId());
            orderDetail.setImage(shoppingCart.getImage());
            orderDetail.setName(shoppingCart.getName());
            orderDetail.setSetmealId(shoppingCart.getSetmealId());
            orderDetail.setAmount(shoppingCart.getAmount().multiply(new BigDecimal(shoppingCart.getNumber())));

            orderDetails.add(orderDetail);
        }

        //向order表插入数据，创建订单
        Orders order = new Orders();
        order.setUserId(userId);
        order.setOrderTime(LocalDateTime.now());
        order.setCheckoutTime(LocalDateTime.now());
        order.setAddress(addressBook.getDetail());
        order.setUserName(user.getName());
        order.setConsignee(addressBook.getConsignee());
        order.setStatus(2);
        order.setPhone(user.getPhone());
        order.setAmount(new BigDecimal(amount.get()));
        order.setNumber(orderId.toString());
        order.setAddressBookId(orders.getAddressBookId());

        this.save(order);

        //向orderDetail表插入数据，创建订单明细
        orderDetailService.saveBatch(orderDetails);

        //清空购物车
        LambdaQueryWrapper<ShoppingCart> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(ShoppingCart::getUserId,userId);
        shoppingCartService.remove(lambdaQueryWrapper);

    }
}
