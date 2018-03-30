package com.mmall.controller.portal;


import com.mmall.common.Const;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.Shipping;
import com.mmall.pojo.User;
import com.mmall.service.IShippingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("/shipping/")
public class ShippingController {

 @Autowired
 private IShippingService iShippingService;

    /**
     * 新建收货地址
     * @param session
     * @param shipping
     * @return
     */
    @RequestMapping(value = "add.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse add(HttpSession session, Shipping shipping){
      User user = (User) session.getAttribute(Const.CURRENT_USER);
      if(user==null){
          return ServerResponse.createByErrorCodeAndMessage(Const.ResponseCode.NEED_LOGIN.getCode(),Const.ResponseCode.NEED_LOGIN.getDesc());

      }

      return iShippingService.add(user.getId(),shipping);
    }

    /**
     * 删除收货地址
     * @param session
     * @param shippingId
     * @return
     */
    @RequestMapping(value = "delete.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse delete(HttpSession session, Integer shippingId){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user==null){
            return ServerResponse.createByErrorCodeAndMessage(Const.ResponseCode.NEED_LOGIN.getCode(),Const.ResponseCode.NEED_LOGIN.getDesc());

        }

        return iShippingService.delete(user.getId(),shippingId);
    }


    /**
     * 更新地址
     * @param session
     * @param shipping
     * @return
     */
    @RequestMapping(value = "update.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse update(HttpSession session, Shipping shipping){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user==null){
            return ServerResponse.createByErrorCodeAndMessage(Const.ResponseCode.NEED_LOGIN.getCode(),Const.ResponseCode.NEED_LOGIN.getDesc());

        }

        return iShippingService.update(user.getId(),shipping);
    }


    /**
     * 选中查看具体的地址
     * @param session
     * @param shippingId
     * @return
     */
    @RequestMapping(value = "select.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse select(HttpSession session, Integer shippingId){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user==null){
            return ServerResponse.createByErrorCodeAndMessage(Const.ResponseCode.NEED_LOGIN.getCode(),Const.ResponseCode.NEED_LOGIN.getDesc());

        }

        return iShippingService.select(user.getId(),shippingId);
    }


















































}
