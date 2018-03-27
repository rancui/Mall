package com.mmall.controller.backend;

import com.mmall.common.Const;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;
import com.mmall.service.IOrderService;
import com.mmall.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("/manage/order/")
public class OrderManageController {

    @Autowired
    private IUserService iUserService;
    @Autowired
    private IOrderService orderService;


    /**
     * 订单list
     * @param session
     * @param pageNum
     * @param pageSize
     * @return
     */
    @RequestMapping(value = "list.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse list(HttpSession session, @RequestParam(value = "pageNum",defaultValue = "1") int pageNum,
                               @RequestParam(value = "pageSize",defaultValue = "10")int pageSize){


        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user==null){
            return ServerResponse.createByErrorCodeAndMessage(Const.ResponseCode.NEED_LOGIN.getCode(),"用户未登录,请登录管理员");
        }

        if(iUserService.checkAdminRole(user).isSuccess()){
            return orderService.manageList(pageNum,pageSize);
        }else {
            return ServerResponse.createByErrorMessage("无权限操作");
        }

    }


    /**
     * 按订单号查询
     * @param session
     * @param orderNo
     * @param pageNum
     * @param pageSize
     * @return
     */
    @RequestMapping(value = "search.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse search(HttpSession session, Long orderNo,@RequestParam(value = "pageNum",defaultValue = "1") int pageNum,
                                 @RequestParam(value = "pageSize",defaultValue = "10")int pageSize){


        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user==null){
            return ServerResponse.createByErrorCodeAndMessage(Const.ResponseCode.NEED_LOGIN.getCode(),"用户未登录,请登录管理员");
        }

        if(iUserService.checkAdminRole(user).isSuccess()){
            return orderService.manageSearch(user.getId(),orderNo,pageNum,pageSize);
        }else {
            return ServerResponse.createByErrorMessage("无权限操作");
        }

    }


    /**
     * 订单详情
     * @param session
     * @param orderNo
     * @return
     */
    @RequestMapping(value = "detail.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse detail(HttpSession session, Long orderNo){

        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user==null){
            return ServerResponse.createByErrorCodeAndMessage(Const.ResponseCode.NEED_LOGIN.getCode(),"用户未登录,请登录管理员");
        }

        if(iUserService.checkAdminRole(user).isSuccess()){
            return orderService.manageDetail(user.getId(),orderNo);
        }else {
            return ServerResponse.createByErrorMessage("无权限操作");
        }

    }


    /**
     * 订单发货
     * @param session
     * @param orderNo
     * @return
     */
    @RequestMapping(value = "send_goods.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse manageSendGoods(HttpSession session, Long orderNo){

        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user==null){
            return ServerResponse.createByErrorCodeAndMessage(Const.ResponseCode.NEED_LOGIN.getCode(),"用户未登录,请登录管理员");
        }

        if(iUserService.checkAdminRole(user).isSuccess()){
            return orderService.manageSendGoods(orderNo);
        }else {
            return ServerResponse.createByErrorMessage("无权限操作");
        }

    }




































}
