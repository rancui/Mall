package com.mmall.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.mmall.common.Const;
import com.mmall.common.ServerResponse;
import com.mmall.dao.*;
import com.mmall.pojo.*;
import com.mmall.service.IOrderService;
import com.mmall.util.BigDecimalUtil;
import com.mmall.util.DateTimeUtil;
import com.mmall.util.PropertiesUtil;
import com.mmall.vo.*;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sun.nio.cs.US_ASCII;

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


    /**
     * 创建订单（从购物车中获取数据）
     * @param userId
     * @param shippingId 收货地址表的id
     * @return
     */
    public ServerResponse createOrder(Integer userId, Integer shippingId){

        //从购物车中获取数据
        List<Cart> cartList = cartMapper.selectCartByUserId(userId);

        //计算订单总价
        ServerResponse serverResponse = this.getOrderItemFromCart(userId,cartList);

        if(!serverResponse.isSuccess()){
            return serverResponse;

        }

        List<OrderItem> orderItemList =( List<OrderItem>) serverResponse.getData();

        BigDecimal totalPrice = this.getTotalPrice(orderItemList);


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
            Product product = productMapper.selectByPrimaryKey(cart.getProductId());
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


    /**
     * 取消订单
     * @param userId
     * @param orderNo
     * @return
     */
    public  ServerResponse cancelOrder(Integer userId,Long orderNo){

           Order order = orderMapper.selectOrderByUserIdAndOrderNo(userId,orderNo);

           if(order==null){

               return ServerResponse.createByErrorMessage("订单不存在");

           }

           if(Const.OrderStatusEnum.NO_PAY.getCode()< order.getStatus()){
               return ServerResponse.createByErrorMessage("该订单已付款，无法取消！");
           }

           if(Const.OrderStatusEnum.CANCELED.getCode()==order.getStatus()){
               return ServerResponse.createByErrorMessage("该订单已取消！");
           }


           Order orderNew = new Order();

           orderNew.setId(order.getId());
           orderNew.setStatus(Const.OrderStatusEnum.CANCELED.getCode());

           int rowCount = orderMapper.updateByPrimaryKeySelective(orderNew);
           if(rowCount>0){
               return ServerResponse.createBySuccessMessage("订单取消成功");
           }

           return ServerResponse.createByErrorMessage("订单取消失败");


}

    /**
     * 获取订单的商品信息
     * @param userId
     * @return
     */
    public ServerResponse getOrderCartProduct(Integer userId){

       List<Cart> cartList = cartMapper.selectCheckedSCartByUserId(userId);

       if(CollectionUtils.isEmpty(cartList)){
           return ServerResponse.createByErrorMessage("购物车为空");
       }

       ServerResponse serverResponse = this.getOrderItemFromCart(userId,cartList);

       List<OrderItem> orderItemList = (List<OrderItem>) serverResponse.getData();

       BigDecimal totalPrice = new BigDecimal("0");
       List<OrderItemVo> orderItemVoList = Lists.newArrayList();

       for(OrderItem orderItem:orderItemList){

           OrderItemVo orderItemVo = this.assmbleOrderItemVo(orderItem);
           totalPrice = BigDecimalUtil.add(totalPrice.doubleValue(),orderItem.getTotalPrice().doubleValue());
           orderItemVoList.add(orderItemVo);

       }

       OrderProductVo orderProductVo = new OrderProductVo();
       orderProductVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix"));
       orderProductVo.setProductTotalPrice(totalPrice);
       orderProductVo.setOrderItemVoList(orderItemVoList);

       return ServerResponse.createBySuccessData(orderProductVo);



}


    /**
     * 获取订单列表
     * @param userId
     * @param pageNum
     * @param pageSize
     * @return
     */
public ServerResponse getOrderList(Integer userId,int pageNum,int pageSize){

    PageHelper.startPage(pageNum,pageSize);

    List<Order> orderList = orderMapper.selectOrderListByUserId(userId);
    List<OrderVo> orderVoList = this.assmbleOrderVoList(userId,orderList);

    PageInfo pageInfo = new PageInfo(orderList);
    pageInfo.setList(orderVoList);

    return ServerResponse.createBySuccessData(pageInfo);

}


private List<OrderVo> assmbleOrderVoList(Integer userId,List<Order> orderList){

    List<OrderVo> orderVoList = Lists.newArrayList();

   for(Order order:orderList){

       List<OrderItem> orderItemList = Lists.newArrayList();

       if(userId==null){ //管理员,没有userId

           orderItemList = orderItemMapper.selectByOrderNo(order.getOrderNo());

       }else {
           orderItemList = orderItemMapper.selectByUserIdAndOrderNo(userId,order.getOrderNo());
       }

       OrderVo orderVo = assmbleOrderVo(order,orderItemList);
       orderVoList.add(orderVo);

   }

    return orderVoList;

}


    /**
     * 订单详情
     * @param userId
     * @param orderNo
     * @return
     */
    public  ServerResponse getOrderDetail(Integer userId,Long orderNo){

    Order order = orderMapper.selectOrderByUserIdAndOrderNo(userId,orderNo);
    if(order==null){
        return ServerResponse.createByErrorMessage("该订单为空");
    }

    List<OrderItem> orderItemList = orderItemMapper.selectByUserIdAndOrderNo(userId,orderNo);

    OrderVo orderVo = assmbleOrderVo(order,orderItemList);

    return ServerResponse.createBySuccessData(orderVo);


}





//后台管理系统

    /**
     * 订单list
     * @param pageNum
     * @param pageSize
     * @return
     */
    public ServerResponse<PageInfo> manageList(int pageNum,int pageSize){

        PageHelper.startPage(pageNum,pageSize);
        List<Order> orderList = orderMapper.selectAllOrder();
        List<OrderVo> orderVoList = this.assmbleOrderVoList(null,orderList);

        PageInfo pageInfo = new PageInfo(orderList);
        pageInfo.setList(orderVoList);

        return ServerResponse.createBySuccessData(pageInfo);


    }


    /**
     * 按订单号查询
     * @param userId
     * @param orderNo
     * @param pageNum
     * @param pageSize
     * @return
     */
    public ServerResponse<PageInfo> manageSearch(Integer userId,Long orderNo,int pageNum,int pageSize){

       PageHelper.startPage(pageNum,pageSize);
       Order order = orderMapper.selectOrderByOrderNo(orderNo);

       if(order!=null){

           List<OrderItem> orderItemList = orderItemMapper.selectByOrderNo(orderNo);

           OrderVo orderVo = assmbleOrderVo(order,orderItemList);

           PageInfo pageInfo = new PageInfo(Lists.newArrayList(order));

           pageInfo.setList(Lists.newArrayList(orderVo));

           return ServerResponse.createBySuccessData(pageInfo);

       }

       return ServerResponse.createByErrorMessage("该订单不存在");







    }















}
