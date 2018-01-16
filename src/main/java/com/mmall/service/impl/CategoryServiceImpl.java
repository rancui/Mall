package com.mmall.service.impl;

import com.mmall.common.ServerResponse;
import com.mmall.dao.CategoryMapper;
import com.mmall.pojo.Category;
import com.mmall.service.ICategoryService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;


@Service("iCategoryService")
public class CategoryServiceImpl implements ICategoryService {
    @Autowired
    private CategoryMapper categoryMapper;

    /**
     *  添加新品类
     * @param categoryName
     * @param parentId
     * @return
     */
    public ServerResponse<Category> addCategory(String categoryName,Integer parentId){
        if(StringUtils.isBlank(categoryName)){
            return ServerResponse.createByErrorMessage("品类的名称不能为空");
        }
        Category category = new Category();

        category.setParentId(parentId);
        category.setName(categoryName);
        category.setStatus(true);
        int count = categoryMapper.insert(category);
        if(count==0){
            return ServerResponse.createByErrorMessage("添加失败");
        }
        return ServerResponse.createBySuccessMsgAndData("添加成功",category);

    }


    /***
     *  获取品类子节点(平级)
     * @param categoryId
     * @return
     */
    public ServerResponse getChildParallelCategory(Integer categoryId){

        List<Category> categoryList = categoryMapper.selectCategoryChildrenParallelByParentId(categoryId);
        if(CollectionUtils.isEmpty(categoryList)){
            return ServerResponse.createByErrorMessage("子分类为空");
        }

        return ServerResponse.createBySuccessData(categoryList);


    }























}
