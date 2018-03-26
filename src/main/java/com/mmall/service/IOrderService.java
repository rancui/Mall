package com.mmall.service;

import com.mmall.common.ServerResponse;

public interface IOrderService {
    ServerResponse createOrder(Integer userId, Integer shippingId);
}
