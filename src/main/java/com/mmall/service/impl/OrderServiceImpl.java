package com.mmall.service.impl;

import com.alipay.api.AlipayResponse;
import com.alipay.api.response.AlipayTradePrecreateResponse;
import com.alipay.demo.trade.config.Configs;
import com.alipay.demo.trade.model.ExtendParams;
import com.alipay.demo.trade.model.GoodsDetail;
import com.alipay.demo.trade.model.builder.AlipayTradePrecreateRequestBuilder;
import com.alipay.demo.trade.model.result.AlipayF2FPrecreateResult;
import com.alipay.demo.trade.service.AlipayTradeService;
import com.alipay.demo.trade.service.impl.AlipayTradeServiceImpl;
import com.alipay.demo.trade.utils.ZxingUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mmall.common.Const;
import com.mmall.common.ServerResponse;
import com.mmall.dao.*;
import com.mmall.pojo.*;
import com.mmall.service.IOrderService;
import com.mmall.util.BigDecimalUtil;
import com.mmall.util.DateTimeUtil;
import com.mmall.util.FTPUtil;
import com.mmall.util.PropertiesUtil;
import com.mmall.vo.*;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sun.nio.cs.US_ASCII;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

@Service("iOrderService")
public class OrderServiceImpl implements IOrderService {

    private static  AlipayTradeService tradeService;
    static {

        /** 一定要在创建AlipayTradeService之前调用Configs.init()设置默认参数
         *  Configs会读取classpath下的zfbinfo.properties文件配置信息，如果找不到该文件则确认该文件是否在classpath目录
         */
        Configs.init("zfbinfo.properties");

        /** 使用Configs提供的默认参数
         *  AlipayTradeService可以使用单例或者为静态成员对象，不需要反复new
         */
        tradeService = new AlipayTradeServiceImpl.ClientBuilder().build();
    }

    private static final Logger logger = LoggerFactory.getLogger(OrderServiceImpl.class);
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

    @Autowired
    private PayInfoMapper payInfoMapper;

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


    /**
     * 订单详情
     * @param userId
     * @param orderNo
     * @return
     */
   public  ServerResponse manageDetail(Integer userId,Long orderNo){

        if(orderNo==null){
            return ServerResponse.createByErrorCodeAndMessage(Const.ResponseCode.ILLEGAL_ARGUMENT.getCode(),Const.ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }

        Order order = orderMapper.selectOrderByUserIdAndOrderNo(userId,orderNo);

        if(order!=null){

            List<OrderItem> orderItemList = orderItemMapper.selectByOrderNo(orderNo);

            OrderVo orderVo = this.assmbleOrderVo(order,orderItemList);

            return ServerResponse.createBySuccessData(orderVo);

        }

        return ServerResponse.createByErrorMessage("订单不存在");
   }


    /**
     * 订单发货
     * @param orderNo
     * @return
     */
   public  ServerResponse manageSendGoods(Long orderNo){

       Order order = orderMapper.selectOrderByOrderNo(orderNo);

       if(order!=null){
           if(order.getStatus()==Const.OrderStatusEnum.PAID.getCode()){

               order.setStatus(Const.OrderStatusEnum.SHIPPED.getCode());
               order.setSendTime(new Date());
               orderMapper.updateByPrimaryKeySelective(order);
               return ServerResponse.createBySuccessMessage("发货成功");

           }

       }

       return ServerResponse.createByErrorMessage("订单不存在");

   }











    public ServerResponse pay(Long orderNo,Integer userId,String path){
        Map<String ,String> resultMap = Maps.newHashMap();
        Order order = orderMapper.selectOrderByUserIdAndOrderNo(userId,orderNo);
        if(order == null){
            return ServerResponse.createByErrorMessage("用户没有该订单");
        }
        resultMap.put("orderNo",String.valueOf(order.getOrderNo()));



        // (必填) 商户网站订单系统中唯一订单号，64个字符以内，只能包含字母、数字、下划线，
        // 需保证商户系统端不能重复，建议通过数据库sequence生成，
        String outTradeNo = order.getOrderNo().toString();


        // (必填) 订单标题，粗略描述用户的支付目的。如“xxx品牌xxx门店当面付扫码消费”
        String subject = new StringBuilder().append("happymmall扫码支付,订单号:").append(outTradeNo).toString();


        // (必填) 订单总金额，单位为元，不能超过1亿元
        // 如果同时传入了【打折金额】,【不可打折金额】,【订单总金额】三者,则必须满足如下条件:【订单总金额】=【打折金额】+【不可打折金额】
        String totalAmount = order.getPayment().toString();


        // (可选) 订单不可打折金额，可以配合商家平台配置折扣活动，如果酒水不参与打折，则将对应金额填写至此字段
        // 如果该值未传入,但传入了【订单总金额】,【打折金额】,则该值默认为【订单总金额】-【打折金额】
        String undiscountableAmount = "0";



        // 卖家支付宝账号ID，用于支持一个签约账号下支持打款到不同的收款账号，(打款到sellerId对应的支付宝账号)
        // 如果该字段为空，则默认为与支付宝签约的商户的PID，也就是appid对应的PID
        String sellerId = "";

        // 订单描述，可以对交易或商品进行一个详细地描述，比如填写"购买商品2件共15.00元"
        String body = new StringBuilder().append("订单").append(outTradeNo).append("购买商品共").append(totalAmount).append("元").toString();


        // 商户操作员编号，添加此参数可以为商户操作员做销售统计
        String operatorId = "test_operator_id";

        // (必填) 商户门店编号，通过门店号和商家后台可以配置精准到门店的折扣信息，详询支付宝技术支持
        String storeId = "test_store_id";

        // 业务扩展参数，目前可添加由支付宝分配的系统商编号(通过setSysServiceProviderId方法)，详情请咨询支付宝技术支持
        ExtendParams extendParams = new ExtendParams();
        extendParams.setSysServiceProviderId("2088100200300400500");




        // 支付超时，定义为120分钟
        String timeoutExpress = "120m";

        // 商品明细列表，需填写购买商品详细信息，
        List<GoodsDetail> goodsDetailList = new ArrayList<GoodsDetail>();

        List<OrderItem> orderItemList = orderItemMapper.selectByUserIdAndOrderNo(userId,orderNo);
        for(OrderItem orderItem : orderItemList){
            GoodsDetail goods = GoodsDetail.newInstance(orderItem.getProductId().toString(), orderItem.getProductName(),
                    BigDecimalUtil.mul(orderItem.getCurrentUnitPrice().doubleValue(),new Double(100).doubleValue()).longValue(),
                    orderItem.getQuantity());
            goodsDetailList.add(goods);
        }

        // 创建扫码支付请求builder，设置请求参数
        AlipayTradePrecreateRequestBuilder builder = new AlipayTradePrecreateRequestBuilder()
                .setSubject(subject).setTotalAmount(totalAmount).setOutTradeNo(outTradeNo)
                .setUndiscountableAmount(undiscountableAmount).setSellerId(sellerId).setBody(body)
                .setOperatorId(operatorId).setStoreId(storeId).setExtendParams(extendParams)
                .setTimeoutExpress(timeoutExpress)
                .setNotifyUrl(PropertiesUtil.getProperty("alipay.callback.url"))//支付宝服务器主动通知商户服务器里指定的页面http路径,根据需要设置
                .setGoodsDetailList(goodsDetailList);


        AlipayF2FPrecreateResult result = tradeService.tradePrecreate(builder);
        switch (result.getTradeStatus()) {
            case SUCCESS:
                logger.info("支付宝预下单成功: )");

                AlipayTradePrecreateResponse response = result.getResponse();
                dumpResponse(response);

                File folder = new File(path);
                if(!folder.exists()){
                    folder.setWritable(true);
                    folder.mkdirs();
                }
                // 需要修改为运行机器上的路径
                //细节细节细节
                String qrPath = String.format(path+"/qr-%s.png",response.getOutTradeNo());
                String qrFileName = String.format("qr-%s.png",response.getOutTradeNo());
                ZxingUtils.getQRCodeImge(response.getQrCode(), 256, qrPath);

                File targetFile = new File(path,qrFileName);
                try {
                    FTPUtil.uploadFile(Lists.newArrayList(targetFile));
                } catch (IOException e) {
                    logger.error("上传二维码异常",e);
                }
                logger.info("qrPath:" + qrPath);
                String qrUrl = PropertiesUtil.getProperty("ftp.server.http.prefix")+targetFile.getName();
                resultMap.put("qrUrl",qrUrl);
                return ServerResponse.createBySuccessData(resultMap);
            case FAILED:
                logger.error("支付宝预下单失败!!!");
                return ServerResponse.createByErrorMessage("支付宝预下单失败!!!");

            case UNKNOWN:
                logger.error("系统异常，预下单状态未知!!!");
                return ServerResponse.createByErrorMessage("系统异常，预下单状态未知!!!");

            default:
                logger.error("不支持的交易状态，交易返回异常!!!");
                return ServerResponse.createByErrorMessage("不支持的交易状态，交易返回异常!!!");
        }

    }

    // 简单打印应答
    private void dumpResponse(AlipayResponse AlipayResponse) {
        if (AlipayResponse != null) {
            logger.info(String.format("code:%s, msg:%s", AlipayResponse.getCode(), AlipayResponse.getMsg()));
            if (StringUtils.isNotEmpty(AlipayResponse.getSubCode())) {
                logger.info(String.format("subCode:%s, subMsg:%s", AlipayResponse.getSubCode(),
                        AlipayResponse.getSubMsg()));
            }
            logger.info("body:" + AlipayResponse.getBody());
        }
    }




    public ServerResponse aliCallback(Map<String,String> params){
        Long orderNo = Long.parseLong(params.get("out_trade_no"));
        String tradeNo = params.get("trade_no");
        String tradeStatus = params.get("trade_status");
        Order order = orderMapper.selectOrderByOrderNo(orderNo);
        if(order == null){
            return ServerResponse.createByErrorMessage("非快乐慕商城的订单,回调忽略");
        }
        if(order.getStatus() >= Const.OrderStatusEnum.PAID.getCode()){
            return ServerResponse.createBySuccessMessage("支付宝重复调用");
        }
        if(Const.AlipayCallback.TRADE_STATUS_TRADE_SUCCESS.equals(tradeStatus)){
            order.setPaymentTime(DateTimeUtil.strToDate(params.get("gmt_payment")));
            order.setStatus(Const.OrderStatusEnum.PAID.getCode());
            orderMapper.updateByPrimaryKeySelective(order);
        }

        PayInfo payInfo = new PayInfo();
        payInfo.setUserId(order.getUserId());
        payInfo.setOrderNo(order.getOrderNo());
        payInfo.setPayPlatform(Const.PayPlatformEnum.ALIPAY.getCode());
        payInfo.setPlatformNumber(tradeNo);
        payInfo.setPlatformStatus(tradeStatus);

        payInfoMapper.insert(payInfo);

        return ServerResponse.createBySuccess();
    }





    public ServerResponse queryOrderPayStatus(Integer userId,Long orderNo){
        Order order = orderMapper.selectOrderByUserIdAndOrderNo(userId,orderNo);
        if(order == null){
            return ServerResponse.createByErrorMessage("用户没有该订单");
        }
        if(order.getStatus() >= Const.OrderStatusEnum.PAID.getCode()){
            return ServerResponse.createBySuccess();
        }
        return ServerResponse.createByError();
    }


















































}
