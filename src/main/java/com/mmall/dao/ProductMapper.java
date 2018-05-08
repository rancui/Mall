package com.mmall.dao;

import com.mmall.pojo.Product;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ProductMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(Product record);

    int insertSelective(Product record);

    Product selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Product record);

    int updateByPrimaryKey(Product record);

    List<Product> selectByKeywordCategoryIdList(@Param(value = "keyword") String keyword, @Param(value = "categoryIdList")List<Integer> categoryIdList);

    List<Product> selectProductList();

    List<Product> selectByProductNameAndProductId(@Param(value = "productName")String productName,@Param(value = "productId")Integer productId);

    Integer selectStockByProductId(Integer productId);








}