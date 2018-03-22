package com.mmall.service;

import com.mmall.common.ServerResponse;
import com.mmall.vo.CartVo;

public interface ICartService {
    ServerResponse<CartVo> add(Integer userId, Integer productId, Integer count);
    ServerResponse<CartVo> list(Integer userId);
    ServerResponse update(Integer userId,Integer productId,Integer count);
    ServerResponse<CartVo> deleteProduct(Integer userId,String prodductIds);
    ServerResponse<CartVo> selectProduct(Integer userId,Integer productId,Integer checked);
    ServerResponse<CartVo> unSelectProduct(Integer userId,Integer productId,Integer checked);
}
