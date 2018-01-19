package com.mmall.service;

import com.github.pagehelper.PageInfo;
import com.mmall.common.ServerResponse;
import com.mmall.vo.ProductDetailVo;

public interface IProductService {

    ServerResponse<ProductDetailVo> getProductDetail(Integer productId);
    ServerResponse<PageInfo> getProductListBykeyWordCategoryId(String keyword, Integer categoryId, int pageNum, int pageSize, String orderBy);
    ServerResponse<PageInfo> getProductList(int pageNum,int pageSize);
    ServerResponse<PageInfo> searchProduct(String productName, Integer productId, int pageNum, int pageSize);



}
