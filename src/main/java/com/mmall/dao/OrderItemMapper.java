package com.mmall.dao;

import com.mmall.pojo.OrderItem;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface OrderItemMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(OrderItem record);

    int insertSelective(OrderItem record);

    OrderItem selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(OrderItem record);

    int updateByPrimaryKey(OrderItem record);

    void batchInsert(@Param("orderItemList") List<OrderItem> orderItemList);
    List<OrderItem>  selectByOrderNo(Long orderNo);
    List<OrderItem> selectByUserIdAndOrderNo(@Param("userId")Integer userId,@Param("orderNo")Long orderNo);


}