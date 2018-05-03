package com.mmall.service;

import com.alipay.api.AlipayResponse;
import com.github.pagehelper.PageInfo;
import com.mmall.common.ServerResponse;

import javax.servlet.http.HttpSession;
import java.util.Map;

public interface IOrderService {
    ServerResponse createOrder(Integer userId, Integer shippingId);
    ServerResponse cancelOrder(Integer userId,Long orderNo);
    ServerResponse getOrderCartProduct(Integer userId);
    ServerResponse getOrderList(Integer userId,int pageNum,int pageSize);
    ServerResponse getOrderDetail(Integer userId,Long orderNo);

    //后台

    ServerResponse<PageInfo> manageList(int pageNum, int pageSize);
    ServerResponse<PageInfo> manageSearch(Long orderNo,int pageNum,int pageSize);
    ServerResponse manageDetail(Long orderNo);
    ServerResponse manageSendGoods(Long orderNo);



    ServerResponse pay(Long orderNo,Integer userId,String path);
    ServerResponse aliCallback(Map<String,String> params);
    ServerResponse queryOrderPayStatus(Integer userId,Long orderNo);



}
