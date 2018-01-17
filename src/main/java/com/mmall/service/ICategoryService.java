package com.mmall.service;

import com.mmall.common.ServerResponse;
import com.mmall.pojo.Category;

import java.util.List;

public interface ICategoryService {
    ServerResponse<Category> addCategory(String categoryName, Integer parentId);
    ServerResponse getChildParallelCategory(Integer categoryId);
    ServerResponse updateCategoryName(Integer categoryId, String categoryName);
    ServerResponse<List<Integer>> selectCategoryAndChildrenById(Integer categoryId);
}
