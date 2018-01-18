package com.mmall.service;

import com.mmall.common.ServerResponse;
import com.mmall.vo.ProductDetailVo;

public interface IProductService {

    ServerResponse<ProductDetailVo> getProductDetail(Integer productId);




}
