package com.mmall.controller.portal;

import com.mmall.common.Const;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;
import com.mmall.service.ICartService;
import com.mmall.util.CookieUtil;
import com.mmall.util.JsonUtil;
import com.mmall.util.RedisShardedPoolUtil;
import com.mmall.vo.CartVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/cart/")
public class CartController {

    @Autowired
    private ICartService iCartService;

    /**
     *  购物车中添加商品
     * @param request
     * @param productId
     * @param count
     * @return
     */
    @RequestMapping("add.do")
    @ResponseBody
    public ServerResponse add(HttpServletRequest request, Integer productId, Integer count){

        String loginToken = CookieUtil.readLoginToken(request);
        if(StringUtils.isEmpty(loginToken)){
            return ServerResponse.createByErrorMessage("用户未登录，无法获取当前用户信息");
        }

        String userStr = RedisShardedPoolUtil.get(loginToken);
        User user = JsonUtil.str2Obj(userStr,User.class);
        if(user==null){
            return ServerResponse.createByErrorCodeAndMessage(Const.ResponseCode.NEED_LOGIN.getCode(),Const.ResponseCode.NEED_LOGIN.getDesc());

        }

        return iCartService.add(user.getId(),productId,count);
    }


    /**
     *  购物车List列表
     * @param request
     * @return
     */
    @RequestMapping("list.do")
    @ResponseBody
    public ServerResponse list(HttpServletRequest request){

        String loginToken = CookieUtil.readLoginToken(request);
        if(StringUtils.isEmpty(loginToken)){
            return ServerResponse.createByErrorMessage("用户未登录，无法获取当前用户信息");
        }

        String userStr = RedisShardedPoolUtil.get(loginToken);
        User user = JsonUtil.str2Obj(userStr,User.class);
        if(user==null){
            return ServerResponse.createByErrorCodeAndMessage(Const.ResponseCode.NEED_LOGIN.getCode(),Const.ResponseCode.NEED_LOGIN.getDesc());

        }

        return iCartService.list(user.getId());
    }

    /**
     *  更新购物车中某个产品数量
     * @param request
     * @param productId
     * @param count
     * @return
     */
    @RequestMapping("update.do")
    @ResponseBody
    public ServerResponse update(HttpServletRequest request,Integer productId,Integer count){

        String loginToken = CookieUtil.readLoginToken(request);
        if(StringUtils.isEmpty(loginToken)){
            return ServerResponse.createByErrorMessage("用户未登录，无法获取当前用户信息");
        }

        String userStr = RedisShardedPoolUtil.get(loginToken);
        User user = JsonUtil.str2Obj(userStr,User.class);
        if(user==null){
            return ServerResponse.createByErrorCodeAndMessage(Const.ResponseCode.NEED_LOGIN.getCode(),Const.ResponseCode.NEED_LOGIN.getDesc());

        }

        return iCartService.update(user.getId(),productId,count);
    }


    /**
     * 移除购物车某个产品
     * @param request
     * @param productIds
     * @return
     */
    @RequestMapping("delete.do")
    @ResponseBody
    public ServerResponse delete( HttpServletRequest request,String productIds){

        String loginToken = CookieUtil.readLoginToken(request);
        if(StringUtils.isEmpty(loginToken)){
            return ServerResponse.createByErrorMessage("用户未登录，无法获取当前用户信息");
        }

        String userStr = RedisShardedPoolUtil.get(loginToken);
        User user = JsonUtil.str2Obj(userStr,User.class);
        if(user==null){
            return ServerResponse.createByErrorCodeAndMessage(Const.ResponseCode.NEED_LOGIN.getCode(),Const.ResponseCode.NEED_LOGIN.getDesc());

        }

        return iCartService.deleteProduct(user.getId(), productIds);
    }


    /**
     * 购物车选中某个商品
     * @param request
     * @param productId
     * @return
     */
    @RequestMapping("select.do")
    @ResponseBody
    public ServerResponse<CartVo> select(HttpServletRequest request, Integer productId){

        String loginToken = CookieUtil.readLoginToken(request);
        if(StringUtils.isEmpty(loginToken)){
            return ServerResponse.createByErrorMessage("用户未登录，无法获取当前用户信息");
        }

        String userStr = RedisShardedPoolUtil.get(loginToken);
        User user = JsonUtil.str2Obj(userStr,User.class);
        if(user==null){
            return ServerResponse.createByErrorCodeAndMessage(Const.ResponseCode.NEED_LOGIN.getCode(),Const.ResponseCode.NEED_LOGIN.getDesc());

        }

        return iCartService.selectProduct(user.getId(), productId,Const.Cart.CHECKED);
    }

    /**
     * 取消选中购物车某个商品
     * @param request
     * @param productId
     * @return
     */
    @RequestMapping("un_select.do")
    @ResponseBody
    public ServerResponse<CartVo> unselect(HttpServletRequest request,Integer productId){

        String loginToken = CookieUtil.readLoginToken(request);
        if(StringUtils.isEmpty(loginToken)){
            return ServerResponse.createByErrorMessage("用户未登录，无法获取当前用户信息");
        }

        String userStr = RedisShardedPoolUtil.get(loginToken);
        User user = JsonUtil.str2Obj(userStr,User.class);
        if(user==null){
            return ServerResponse.createByErrorCodeAndMessage(Const.ResponseCode.NEED_LOGIN.getCode(),Const.ResponseCode.NEED_LOGIN.getDesc());

        }

        return iCartService.unSelectProduct(user.getId(), productId,Const.Cart.UN_CHECKED);
    }




    /**
     *  购物车全选
     * @param request
     * @return
     */
    @RequestMapping("select_all.do")
    @ResponseBody
    public ServerResponse selectAll(HttpServletRequest request){

        String loginToken = CookieUtil.readLoginToken(request);
        if(StringUtils.isEmpty(loginToken)){
            return ServerResponse.createByErrorMessage("用户未登录，无法获取当前用户信息");
        }

        String userStr = RedisShardedPoolUtil.get(loginToken);
        User user = JsonUtil.str2Obj(userStr,User.class);
        if(user==null){
            return ServerResponse.createByErrorCodeAndMessage(Const.ResponseCode.NEED_LOGIN.getCode(),Const.ResponseCode.NEED_LOGIN.getDesc());

        }

        return iCartService.unSelectProduct(user.getId(), null,Const.Cart.CHECKED);
    }


    /**
     * 购物车取消全选
     * @param request
     * @return
     */
    @RequestMapping("un_select_all.do")
    @ResponseBody
    public ServerResponse unSelectAll(HttpServletRequest request){

        String loginToken = CookieUtil.readLoginToken(request);
        if(StringUtils.isEmpty(loginToken)){
            return ServerResponse.createByErrorMessage("用户未登录，无法获取当前用户信息");
        }

        String userStr = RedisShardedPoolUtil.get(loginToken);
        User user = JsonUtil.str2Obj(userStr,User.class);
        if(user==null){
            return ServerResponse.createByErrorCodeAndMessage(Const.ResponseCode.NEED_LOGIN.getCode(),Const.ResponseCode.NEED_LOGIN.getDesc());

        }

        return iCartService.unSelectProduct(user.getId(), null,Const.Cart.UN_CHECKED);
    }


    /**
     * 查询在购物车里的产品数量
     * @param request
     * @return
     */
    @RequestMapping("get_cart_product_count.do")
    @ResponseBody
    public ServerResponse getCartProductCount(HttpServletRequest request){

        String loginToken = CookieUtil.readLoginToken(request);
        if(StringUtils.isEmpty(loginToken)){
            return ServerResponse.createByErrorMessage("用户未登录，无法获取当前用户信息");
        }

        String userStr = RedisShardedPoolUtil.get(loginToken);
        User user = JsonUtil.str2Obj(userStr,User.class);
        if(user==null){
            return ServerResponse.createByErrorCodeAndMessage(Const.ResponseCode.NEED_LOGIN.getCode(),Const.ResponseCode.NEED_LOGIN.getDesc());

        }

        return iCartService.getCartProductCount(user.getId());
    }















































































}
