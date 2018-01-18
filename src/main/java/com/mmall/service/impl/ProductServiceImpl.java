package com.mmall.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.mmall.common.Const;
import com.mmall.common.ServerResponse;
import com.mmall.dao.CategoryMapper;
import com.mmall.dao.ProductMapper;
import com.mmall.pojo.Category;
import com.mmall.pojo.Product;
import com.mmall.service.ICategoryService;
import com.mmall.service.IProductService;
import com.mmall.util.DateTimeUtil;
import com.mmall.util.PropertiesUtil;
import com.mmall.vo.ProductDetailVo;
import com.mmall.vo.ProductListVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service("iProductService")
public class ProductServiceImpl implements IProductService{

    @Autowired
    private ProductMapper productMapper;
    @Autowired
    private CategoryMapper categoryMapper;
    @Autowired
    private ICategoryService iCategoryService;


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

    /**
     * 产品搜索及动态排序List
     * @param keyword
     * @param categoryId
     * @param pageNum
     * @param pageSize
     * @param orderBy
     * @return
     */
    public ServerResponse<PageInfo> getProductListBykeyWordCategoryId(String keyword,Integer categoryId,int pageNum,int pageSize,String orderBy){

            if(StringUtils.isBlank(keyword)&&categoryId==null){
                return ServerResponse.createByErrorCodeAndMessage(Const.ResponseCode.ILLEGAL_ARGUMENT.getCode(),Const.ResponseCode.ILLEGAL_ARGUMENT.getDesc());
            }

            List<Integer> categoryIdList = Lists.newArrayList();
            String a;

            if(categoryId!=null){
                Category category = categoryMapper.selectByPrimaryKey(categoryId);
                if(category==null && StringUtils.isBlank(keyword)){ // 该品类为空在且关键字不存在，不报错，返回空结果集

                    PageHelper.startPage(pageNum,pageSize);
                    List<ProductListVo> productListVoList = Lists.newArrayList();
                    PageInfo pageInfo = new PageInfo(productListVoList);
                    return ServerResponse.createBySuccessData(pageInfo);
                }
                categoryIdList = iCategoryService.selectCategoryAndChildrenById(category.getId()).getData();
            }

            if(StringUtils.isNotBlank(keyword)){

               keyword = new StringBuilder().append("%").append(keyword).append("%").toString();
            }

            PageHelper.startPage(pageNum,pageSize);

            // 对列表排序
            if(StringUtils.isNotBlank(orderBy)){
                if(Const.ProductListOrderBy.PRICE_ASC_DESC.contains(orderBy)){
                    String[] orderByArray = orderBy.split("_");
                    PageHelper.orderBy(orderByArray[0]+" "+orderByArray[1]);
                }
            }

            List<Product> productList = productMapper.selectByKewordCategoryIdList(StringUtils.isBlank(keyword)?null:keyword,categoryIdList==null?null:categoryIdList);
            List<ProductListVo> productListVos = Lists.newArrayList();
            for(Product product:productList){
                ProductListVo productListVo = assmbleProductListVo(product);
                productListVos.add(productListVo);
            }

            PageInfo pageInfo = new PageInfo(productList);
            pageInfo.setList(productListVos);

            return ServerResponse.createBySuccessData(pageInfo);


    }

    /**
     * 组装ProductListVo
     * @param product
     * @return
     */
    private ProductListVo assmbleProductListVo(Product product){

        ProductListVo productListVo = new ProductListVo();
        productListVo.setId(product.getId());
        productListVo.setName(product.getName());
        productListVo.setCategoryId(product.getCategoryId());
        productListVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix","http://image.rancui.com/"));
        productListVo.setMainImage(product.getMainImage());
        productListVo.setPrice(product.getPrice());
        productListVo.setSubtitle(product.getSubtitle());
        productListVo.setStatus(product.getStatus());
        return productListVo;

    }























































}
