package com.mmall.service.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
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
import java.util.Set;


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


    /**
     * 更新设置品类名称
     * @param categoryId
     * @param categoryName
     * @return
     */
    public  ServerResponse updateCategoryName(Integer categoryId, String categoryName){

        if(categoryId==null || StringUtils.isBlank(categoryName)){
            return ServerResponse.createByErrorMessage("更新品类名的传入的参数有数");
        }

        Category category = new Category();

        category.setId(categoryId);
        category.setName(categoryName);

        int count = categoryMapper.updateByPrimaryKeySelective(category);

        if(count==0){
            return ServerResponse.createByErrorMessage("品类名更新失败");
        }

        return ServerResponse.createBySuccessMessage("品类名更新成功");

    }

    /**
     * 获取当前分类id及递归子节点categoryId
     * @param categoryId
     * @return
     */
    public ServerResponse<List<Integer>> selectCategoryAndChildrenById(Integer categoryId){

       List<Integer> categoryIdList = Lists.newArrayList();
       Set<Category> categorySet = Sets.newHashSet();

       findChildCategory(categorySet,categoryId);


       if(categoryId!=null){
           for(Category category:categorySet){
               categoryIdList.add(category.getId());
           }
       }

       return ServerResponse.createBySuccessData(categoryIdList);

    }

    /**
     * 递归查找子类
     * @param categorySet
     * @param categoryId
     * @return
     */
    public Set<Category> findChildCategory(Set<Category> categorySet,Integer categoryId){

        Category category = categoryMapper.selectByPrimaryKey(categoryId);
        if(category!=null){

             categorySet.add(category);
        }

        List<Category> categoryList = categoryMapper.selectCategoryChildrenParallelByParentId(categoryId);

        for (Category categoryItem :categoryList){
            findChildCategory(categorySet,categoryItem.getId());
        }

        return categorySet;


    }




















}
