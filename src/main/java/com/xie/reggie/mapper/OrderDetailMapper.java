package com.xie.reggie.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xie.reggie.entity.OrderDetail;
import com.xie.reggie.entity.Orders;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrderDetailMapper extends BaseMapper<OrderDetail> {
}
