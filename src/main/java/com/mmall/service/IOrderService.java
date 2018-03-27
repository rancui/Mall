package com.mmall.service;

import com.github.pagehelper.PageInfo;
import com.mmall.common.ServerResponse;

public interface IOrderService {
    ServerResponse createOrder(Integer userId, Integer shippingId);
    ServerResponse cancelOrder(Integer userId,Long orderNo);
    ServerResponse getOrderCartProduct(Integer userId);
    ServerResponse getOrderList(Integer userId,int pageNum,int pageSize);
    ServerResponse getOrderDetail(Integer userId,Long orderNo);

    //后台

    ServerResponse<PageInfo> manageList(int pageNum, int pageSize);



}
