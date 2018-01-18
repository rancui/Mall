package com.mmall.service.impl;

import com.mmall.common.Const;
import com.mmall.common.ServerResponse;
import com.mmall.dao.CategoryMapper;
import com.mmall.dao.ProductMapper;
import com.mmall.pojo.Category;
import com.mmall.pojo.Product;
import com.mmall.service.IProductService;
import com.mmall.util.DateTimeUtil;
import com.mmall.util.PropertiesUtil;
import com.mmall.vo.ProductDetailVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.stereotype.Service;

@Service("iProductService")
public class ProductServiceImpl implements IProductService{

    @Autowired
    private ProductMapper productMapper;
    @Autowired
    private CategoryMapper categoryMapper;


    /**
     * 获取产品详情
     * @param productId
     * @return
     */
    public ServerResponse<ProductDetailVo> getProductDetail(Integer productId){

        if(productId==null){
            return ServerResponse.createByErrorCodeAndMessage(Const.ResponseCode.ILLEGAL_ARGUMENT.getCode(),Const.ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }

        Product product = productMapper.selectByPrimaryKey(productId);
        if(product==null){
            return ServerResponse.createByErrorMessage("产品不存在或已下架");
        }
        if(product.getStatus()!=Const.ProductStatusEnum.ON_SALE.getCode()){
            return ServerResponse.createByErrorMessage("产品不存在或已下架");
        }

        ProductDetailVo productDetailVo = assmbleProductDetailVo(product);

        return ServerResponse.createBySuccessData(productDetailVo);

    }


    /**
     *  组装ProductDetailVo
     * @param product
     * @return
     */
    private ProductDetailVo assmbleProductDetailVo(Product product){

        ProductDetailVo productDetailVo = new ProductDetailVo();
        productDetailVo.setId(product.getId());
        productDetailVo.setName(product.getName());
        productDetailVo.setCategoryId(product.getCategoryId());
        productDetailVo.setSubtitle(product.getSubtitle());
        productDetailVo.setMainImage(product.getMainImage());
        productDetailVo.setSubImages(product.getSubImages());
        productDetailVo.setDetail(product.getDetail());
        productDetailVo.setStatus(product.getStatus());
        productDetailVo.setStock(product.getStock());


        productDetailVo.setCreateTime(DateTimeUtil.dateToStr(product.getCreateTime()));
        productDetailVo.setUpdateTime(DateTimeUtil.dateToStr(product.getUpdateTime()));

        productDetailVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix"));

        Category category = categoryMapper.selectByPrimaryKey(product.getCategoryId());
        if(category==null){
            productDetailVo.setParentCategoryId(0);
        }else {

            productDetailVo.setParentCategoryId(category.getParentId());
        }

        return productDetailVo;


    }


























































}
