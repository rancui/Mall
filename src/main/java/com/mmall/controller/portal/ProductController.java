package com.mmall.controller.portal;

import com.mmall.common.Const;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;
import com.mmall.service.IProductService;
import com.mmall.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("/product/")
public class ProductController {
    @Autowired
    private IProductService iProductService;
    @Autowired
    private IUserService iUserService;

    /**
     *  （前台）获取产品详情
     * @param session
     * @param productId
     * @return
     */
    @RequestMapping(value = "product_detail.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse productDetail(HttpSession session,Integer productId){

        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user==null){
            return ServerResponse.createByErrorCodeAndMessage(Const.ResponseCode.NEED_LOGIN.getCode(),Const.ResponseCode.NEED_LOGIN.getDesc());
        }

        return iProductService.getProductDetail(productId);

    }


    /**
     * (前台)产品搜索及动态排序List
     * @param session
     * @param keyword
     * @param categoryId
     * @param pageNum
     * @param pageSize
     * @param orderBy
     * @return
     */
    @RequestMapping(value = "product_list.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse productList(HttpSession session, @RequestParam(value = "keyword",required = false)String keyword, @RequestParam(value = "categoryId",required = false) Integer categoryId, @RequestParam(value = "pageNum",defaultValue = "1") int pageNum,@RequestParam(value = "pageSize",defaultValue = "10") int pageSize, @RequestParam(value = "orderBy",defaultValue = "") String orderBy){

        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user==null){
            return ServerResponse.createByErrorCodeAndMessage(Const.ResponseCode.NEED_LOGIN.getCode(),Const.ResponseCode.NEED_LOGIN.getDesc());
        }


        return iProductService.getProductListBykeyWordCategoryId(keyword,categoryId, pageNum,pageSize,orderBy);

    }











































































}
