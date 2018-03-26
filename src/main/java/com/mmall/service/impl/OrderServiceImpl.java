package com.mmall.service.impl;

import com.google.common.collect.Lists;
import com.mmall.common.Const;
import com.mmall.common.ServerResponse;
import com.mmall.dao.*;
import com.mmall.pojo.*;
import com.mmall.service.IOrderService;
import com.mmall.util.BigDecimalUtil;
import com.mmall.util.DateTimeUtil;
import com.mmall.util.PropertiesUtil;
import com.mmall.vo.CartVo;
import com.mmall.vo.OrderItemVo;
import com.mmall.vo.OrderVo;
import com.mmall.vo.ShippingVo;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Random;

@Service("iOrderService")
public class OrderServiceImpl implements IOrderService {

    @Autowired
    private CartMapper cartMapper;
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private ShippingMapper shippingMapper;
    @Autowired
    private ProductMapper productMapper;
    @Autowired
    private OrderItemMapper orderItemMapper;




    public ServerResponse createOrder(Integer userId, Integer shippingId){

        //从购物车中获取数据
        List<Cart> cartList = cartMapper.selectCartByUserId(userId);

        //计算订单总价
        ServerResponse serverResponse = getOrderItemFromCart(userId,cartList);

        if(!serverResponse.isSuccess()){
            return serverResponse;

        }

        List<OrderItem> orderItemList =( List<OrderItem>) serverResponse.getData();

        BigDecimal totalPrice = getTotalPrice(orderItemList);


        //生成订单
        Order order = this.assmbleOrder(userId,shippingId,totalPrice);

        if(order==null){
            return serverResponse.createByErrorMessage("生成订单有误");
        }

        if(CollectionUtils.isEmpty(orderItemList)){
            return serverResponse.createByErrorMessage("购物车为空");
        }
        for (OrderItem orderItem:orderItemList){
            orderItem.setOrderNo(order.getOrderNo());
        }
        //批量插入数据库
        orderItemMapper.batchInsert(orderItemList);

        //生成成功，减少库存
        this.reduceProductStock(orderItemList);

        //清空购物车
        this.cleanCart(cartList);

        OrderVo orderVo = this.assmbleOrderVo(order,orderItemList);

        return serverResponse.createBySuccessData(orderVo);



    }



    private OrderItemVo assmbleOrderItemVo(OrderItem orderItem){

        OrderItemVo orderItemVo = new OrderItemVo();

        orderItemVo.setOrderNo(orderItem.getOrderNo());
        orderItemVo.setProductId(orderItem.getProductId());
        orderItemVo.setProductName(orderItem.getProductName());
        orderItemVo.setProductImage(orderItem.getProductImage());
        orderItemVo.setQuantity(orderItem.getQuantity());
        orderItemVo.setCurrentUnitPrice(orderItem.getCurrentUnitPrice());
        orderItemVo.setTotalPrice(orderItem.getTotalPrice());


        orderItemVo.setCreateTime(DateTimeUtil.dateToStr(orderItem.getCreateTime()));

        return orderItemVo;

    }




    private ShippingVo assmbleShippingVo(Shipping shipping){

        ShippingVo shippingVo = new ShippingVo();
        shippingVo.setReceiverAddress(shipping.getReceiverAddress());
        shippingVo.setReceiverCity(shipping.getReceiverCity());
        shippingVo.setReceiverDistrict(shipping.getReceiverDistrict());
        shippingVo.setReceiverMobile(shipping.getReceiverMobile());
        shippingVo.setReceiverName(shipping.getReceiverName());
        shippingVo.setReceiverPhone(shipping.getReceiverPhone());
        shippingVo.setReceiverProvince(shipping.getReceiverProvince());
        shippingVo.setReceiverZip(shipping.getReceiverZip());

       return shippingVo;


    }


    private OrderVo assmbleOrderVo(Order order,List<OrderItem> orderItemList){

        OrderVo orderVo = new OrderVo();

        orderVo.setOrderNo(order.getOrderNo());
        orderVo.setPayment(order.getPayment());
        orderVo.setShippingId(order.getShippingId());
        orderVo.setPostage(order.getPostage());
        orderVo.setPaymentType(order.getPaymentType());
        orderVo.setPaymentTypeDesc(Const.PaymentTypeEnum.codeOf(order.getPaymentType()).getValue());

        orderVo.setStatus(order.getStatus());
        orderVo.setStatusDesc(Const.OrderStatusEnum.codeOf(order.getStatus()).getValue());

        orderVo.setShippingId(order.getShippingId());

        Shipping shipping = shippingMapper.selectByPrimaryKey(order.getShippingId());
        if(shipping!=null){

            orderVo.setReceiverName(shipping.getReceiverName());
            orderVo.setShippingVo(assmbleShippingVo(shipping));

        }

        orderVo.setPaymentTime(DateTimeUtil.dateToStr(order.getPaymentTime()));
        orderVo.setCloseTime(DateTimeUtil.dateToStr(order.getCloseTime()));
        orderVo.setCreateTime(DateTimeUtil.dateToStr(order.getCreateTime()));
        orderVo.setSendTime(DateTimeUtil.dateToStr(order.getSendTime()));
        orderVo.setEndTime(DateTimeUtil.dateToStr(order.getEndTime()));

        orderVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix"));

        List<OrderItemVo> orderItemVoList = Lists.newArrayList();

        for(OrderItem orderItem : orderItemList){

            OrderItemVo orderItemVo = assmbleOrderItemVo(orderItem);
            orderItemVoList.add(orderItemVo);

        }

        orderVo.setOrderItemVoList(orderItemVoList);

        return orderVo;


    }




  private ServerResponse getOrderItemFromCart(Integer userId,List<Cart> cartList){

        List<OrderItem> orderItemList = Lists.newArrayList();

        if(CollectionUtils.isEmpty(cartList)){
            return ServerResponse.createByErrorMessage("购物车为空");
        }

        for(Cart cart: cartList){

            OrderItem orderItem = new OrderItem();
            Product product = productMapper.selectByPrimaryKey(cart.getId());
            if(Const.ProductStatusEnum.ON_SALE.getCode()!=product.getStatus()){

                return ServerResponse.createByErrorMessage("产品"+product.getName()+"已下线");

            }

            if(cart.getQuantity()>product.getStock()){
                return ServerResponse.createByErrorMessage(product.getName()+"产品库存不足");
            }

            orderItem.setUserId(userId);
            orderItem.setProductId(product.getId());
            orderItem.setProductName(product.getName());
            orderItem.setProductImage(product.getMainImage());
            orderItem.setCurrentUnitPrice(product.getPrice());
            orderItem.setQuantity(cart.getQuantity());
            orderItem.setTotalPrice(BigDecimalUtil.mul(product.getPrice().doubleValue(),cart.getQuantity().doubleValue()));

            orderItemList.add(orderItem);

        }


        return ServerResponse.createBySuccessData(orderItemList);

  }


  private BigDecimal getTotalPrice(List<OrderItem> orderItemList){


       BigDecimal totalPrice = new BigDecimal("0") ;

       for(OrderItem orderItem:orderItemList){
           totalPrice = BigDecimalUtil.add(totalPrice.doubleValue(),orderItem.getTotalPrice().doubleValue());
       }

       return totalPrice;

  }


  private Order  assmbleOrder(Integer userId,Integer shippingId,BigDecimal totalPrice){

        Order order = new Order();

        Long orderNo = this.generateOrderNo();

        order.setOrderNo(orderNo);
        order.setUserId(userId);
        order.setPayment(totalPrice);
        order.setPostage(0);
        order.setPaymentType(Const.PaymentTypeEnum.ONLINE_PAY.getCode());
        order.setStatus(Const.OrderStatusEnum.NO_PAY.getCode());
        order.setShippingId(shippingId);

        int rowCount = orderMapper.insert(order);

        if(rowCount>0){
            return order;
        }


        return null;


  }


//生成订单号
 private Long generateOrderNo(){

     long currentTime =System.currentTimeMillis();
     return currentTime+new Random().nextInt(100);


 }

 //减少库存
private void reduceProductStock(List<OrderItem> orderItemList){

        for(OrderItem orderItem:orderItemList){

            Product product = productMapper.selectByPrimaryKey(orderItem.getProductId());

            product.setStock(product.getStock()-orderItem.getQuantity());

            productMapper.updateByPrimaryKeySelective(product);

        }

}

//清空购物车
private void cleanCart(List<Cart> cartList){

        for (Cart cart:cartList){
            cartMapper.deleteByPrimaryKey(cart.getId());
        }
}









}
