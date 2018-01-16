package com.mmall.controller.backend;

import com.mmall.common.Const;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.Category;
import com.mmall.pojo.User;
import com.mmall.service.ICategoryService;
import com.mmall.service.IUserService;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

@Controller
@RequestMapping(value = "/manage/category/")
public class CategoryManageController {

 @Autowired
 private ICategoryService iCategoryService;
 @Autowired
 private IUserService iUserService;

    /**
     * 添加新品类
     * @param session
     * @param categoryName
     * @param parentId
     * @return
     */
    @RequestMapping(value = "add_category.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse c(HttpSession session,String categoryName,@RequestParam(value = "parentId",defaultValue = "0") int parentId){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user==null){
            return ServerResponse.createByErrorCodeAndMessage(Const.ResponseCode.NEED_LOGIN.getCode(),Const.ResponseCode.NEED_LOGIN.getDesc());
        }
        if(iUserService.checkAdminRole(user).isSuccess()){ //是管理员
              return iCategoryService.addCategory(categoryName,parentId);
        }else {
            return ServerResponse.createByErrorMessage("非管理员权限");
        }

    }

    /**
     *  获取品类子节点(平级)
     * @param categoryId
     * @return
     */
    @RequestMapping(value = "get_category.do",method = RequestMethod.POST)
    @ResponseBody
    public  ServerResponse getCategory(HttpSession session,@RequestParam(value = "categoryId",defaultValue = "0") Integer categoryId){

         User user = (User) session.getAttribute(Const.CURRENT_USER);
         if(user==null){
             return ServerResponse.createByErrorMessage("用户未登录，请登录！");
         }
         if(iUserService.checkAdminRole(user).isSuccess()){
             return iCategoryService.getChildParallelCategory(categoryId);
         }else {
             return ServerResponse.createByErrorMessage("无操作权限，需要管理员权限");
         }

    }

    /**
     * 修改品类名字
     * @param categoryId
     * @param categoryName
     * @return
     */
    @RequestMapping(value = "set_category_name.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse setCategoryName(HttpSession session,Integer categoryId,String categoryName){

        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user==null){
            return ServerResponse.createByErrorCodeAndMessage(Const.ResponseCode.NEED_LOGIN.getCode(),Const.ResponseCode.NEED_LOGIN.getDesc());
        }

        if(iUserService.checkAdminRole(user).isSuccess()){

            return iCategoryService.updateCategoryName(categoryId,categoryName);
        }

        return ServerResponse.createByErrorMessage("用户无管理员权限");

    }













































































}
