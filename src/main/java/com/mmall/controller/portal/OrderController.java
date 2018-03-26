package com.mmall.controller.portal;


import com.mmall.common.Const;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;
import com.mmall.service.IOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("/order/")
public class OrderController {


    @Autowired
    private IOrderService iOrderService;

    /**
     * 创建订单
     * @param session
     * @param shippingId
     * @return
     */
    @RequestMapping(value = "create.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse createOrder(HttpSession session, Integer shippingId){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user==null){
            return ServerResponse.createByErrorCodeAndMessage(Const.ResponseCode.NEED_LOGIN.getCode(),Const.ResponseCode.NEED_LOGIN.getDesc());
        }
        return iOrderService.createOrder(user.getId(),shippingId);
    }


    /**
     * 取消订单
     * @param session
     * @param orderNo
     * @return
     */
    @RequestMapping(value = "cancel_order.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse cancelOrder(HttpSession session, Long orderNo){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user==null){
            return ServerResponse.createByErrorCodeAndMessage(Const.ResponseCode.NEED_LOGIN.getCode(),Const.ResponseCode.NEED_LOGIN.getDesc());
        }
        return iOrderService.cancelOrder(user.getId(),orderNo);
    }


    /**
     * 获取订单的商品信息
     * @param session
     * @return
     */
    @RequestMapping(value = "get_order_cart_product.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse getOrderCartProduct(HttpSession session){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user==null){
            return ServerResponse.createByErrorCodeAndMessage(Const.ResponseCode.NEED_LOGIN.getCode(),Const.ResponseCode.NEED_LOGIN.getDesc());
        }
        return iOrderService.getOrderCartProduct(user.getId());
    }





    @RequestMapping(value = "list.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse getOrderList(HttpSession session, @RequestParam(value = "pageNum",defaultValue = "1") int pageNum, @RequestParam(value = "pageSize",defaultValue = "10") int pageSize){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user==null){
            return ServerResponse.createByErrorCodeAndMessage(Const.ResponseCode.NEED_LOGIN.getCode(),Const.ResponseCode.NEED_LOGIN.getDesc());
        }
        return iOrderService.getOrderList(user.getId(),pageNum,pageSize);
    }



























}
