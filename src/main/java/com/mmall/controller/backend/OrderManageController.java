package com.mmall.controller.backend;

import com.mmall.common.Const;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;
import com.mmall.service.IOrderService;
import com.mmall.service.IUserService;
import com.mmall.util.CookieUtil;
import com.mmall.util.JsonUtil;
import com.mmall.util.RedisShardedPoolUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/manage/order/")
public class OrderManageController {

    @Autowired
    private IUserService iUserService;
    @Autowired
    private IOrderService orderService;


    /**
     * 订单list
     * @param request
     * @param pageNum
     * @param pageSize
     * @return
     */
    @RequestMapping(value = "list.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse list(HttpServletRequest request, @RequestParam(value = "pageNum",defaultValue = "1") int pageNum,
                               @RequestParam(value = "pageSize",defaultValue = "10")int pageSize){

//
//        String loginToken = CookieUtil.readLoginToken(request);
//        if(StringUtils.isEmpty(loginToken)){
//            return ServerResponse.createByErrorMessage("用户未登录，无法获取当前用户信息");
//        }
//
//        String userStr = RedisShardedPoolUtil.get(loginToken);
//
//        User user = JsonUtil.str2Obj(userStr,User.class);
//        if(user==null){
//            return ServerResponse.createByErrorCodeAndMessage(Const.ResponseCode.NEED_LOGIN.getCode(),"用户未登录,请登录管理员");
//        }
//
//        if(iUserService.checkAdminRole(user).isSuccess()){
//            return orderService.manageList(pageNum,pageSize);
//        }else {
//            return ServerResponse.createByErrorMessage("无权限操作");
//        }
        return orderService.manageList(pageNum,pageSize);
    }


    /**
     * 按订单号查询
     * @param request
     * @param orderNo
     * @param pageNum
     * @param pageSize
     * @return
     */
    @RequestMapping(value = "search.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse search(HttpServletRequest request, Long orderNo,@RequestParam(value = "pageNum",defaultValue = "1") int pageNum,
                                 @RequestParam(value = "pageSize",defaultValue = "10")int pageSize){

//
//        String loginToken = CookieUtil.readLoginToken(request);
//        if(StringUtils.isEmpty(loginToken)){
//            return ServerResponse.createByErrorMessage("用户未登录，无法获取当前用户信息");
//        }
//
//        String userStr = RedisShardedPoolUtil.get(loginToken);
//
//        User user = JsonUtil.str2Obj(userStr,User.class);
//        if(user==null){
//            return ServerResponse.createByErrorCodeAndMessage(Const.ResponseCode.NEED_LOGIN.getCode(),"用户未登录,请登录管理员");
//        }
//
//        if(iUserService.checkAdminRole(user).isSuccess()){
//            return orderService.manageSearch(user.getId(),orderNo,pageNum,pageSize);
//        }else {
//            return ServerResponse.createByErrorMessage("无权限操作");
//        }
        return orderService.manageSearch(orderNo,pageNum,pageSize);
    }


    /**
     * 订单详情
     * @param request
     * @param orderNo
     * @return
     */
    @RequestMapping(value = "detail.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse detail(HttpServletRequest request, Long orderNo){
//
//        String loginToken = CookieUtil.readLoginToken(request);
//        if(StringUtils.isEmpty(loginToken)){
//            return ServerResponse.createByErrorMessage("用户未登录，无法获取当前用户信息");
//        }
//
//        String userStr = RedisShardedPoolUtil.get(loginToken);
//
//        User user = JsonUtil.str2Obj(userStr,User.class);
//        if(user==null){
//            return ServerResponse.createByErrorCodeAndMessage(Const.ResponseCode.NEED_LOGIN.getCode(),"用户未登录,请登录管理员");
//        }
//
//        if(iUserService.checkAdminRole(user).isSuccess()){
//            return orderService.manageDetail(user.getId(),orderNo);
//        }else {
//            return ServerResponse.createByErrorMessage("无权限操作");
//        }
        return orderService.manageDetail(orderNo);
    }


    /**
     * 订单发货
     * @param request
     * @param orderNo
     * @return
     */
    @RequestMapping(value = "send_goods.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse manageSendGoods(HttpServletRequest request, Long orderNo){

//        String loginToken = CookieUtil.readLoginToken(request);
//        if(StringUtils.isEmpty(loginToken)){
//            return ServerResponse.createByErrorMessage("用户未登录，无法获取当前用户信息");
//        }
//
//        String userStr = RedisShardedPoolUtil.get(loginToken);
//
//        User user = JsonUtil.str2Obj(userStr,User.class);
//        if(user==null){
//            return ServerResponse.createByErrorCodeAndMessage(Const.ResponseCode.NEED_LOGIN.getCode(),"用户未登录,请登录管理员");
//        }
//
//        if(iUserService.checkAdminRole(user).isSuccess()){
//            return orderService.manageSendGoods(orderNo);
//        }else {
//            return ServerResponse.createByErrorMessage("无权限操作");
//        }
        return orderService.manageSendGoods(orderNo);
    }




































}
