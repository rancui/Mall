package com.mmall.service.impl;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.mmall.common.Const;
import com.mmall.common.ServerResponse;
import com.mmall.dao.CartMapper;
import com.mmall.dao.ProductMapper;
import com.mmall.pojo.Cart;
import com.mmall.pojo.Product;
import com.mmall.service.ICartService;
import com.mmall.util.BigDecimalUtil;
import com.mmall.util.PropertiesUtil;
import com.mmall.vo.CartProductVo;
import com.mmall.vo.CartVo;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;


@Service("iCartService")
public class CartServiceImpl implements ICartService{
    @Autowired
    private CartMapper cartMapper;

    @Autowired
    private ProductMapper productMapper;


    /**
     *  购物车中添加商品
     * @param userId
     * @param productId
     * @param count
     * @return
     */

    public ServerResponse<CartVo> add(Integer userId,Integer productId,Integer count){

        if(userId==null || productId==null ){
            return ServerResponse.createByErrorCodeAndMessage(Const.ResponseCode.ILLEGAL_ARGUMENT.getCode(),Const.ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }

        Cart cart = cartMapper.selectByUserIdProductId(userId,productId);
        if(cart==null){ //购物车中不存在该商品
             Cart cartItem = new Cart();
             cartItem.setId(productId);
             cartItem.setUserId(userId);
             cartItem.setQuantity(count);
             cartItem.setChecked(Const.Cart.CHECKED);
             cartMapper.insert(cartItem);

        }else{//购物车中已存在该商品

            count = cart.getQuantity()+count;
            cart.setQuantity(count);
            cartMapper.updateByPrimaryKeySelective(cart);

        }


        return this.list(userId);

    }




    private CartVo getCartVoLimit(Integer userId){

        CartVo cartVo = new CartVo();

        List<Cart> cartList = cartMapper.selectCartByUserId(userId);
        List<CartProductVo> cartProductVoList = Lists.newArrayList();

        //初始化购物车总价
        BigDecimal cartTotalPrice = new BigDecimal("0");

        if(CollectionUtils.isNotEmpty(cartList)){

            for(Cart cartItem:cartList){

                CartProductVo cartProductVo = new CartProductVo();

                cartProductVo.setId(cartItem.getId());
                cartProductVo.setProductId(cartItem.getProductId());

                Product product = productMapper.selectByPrimaryKey(cartItem.getProductId());

                if(product!=null){
                    cartProductVo.setProductName(product.getName());
                    cartProductVo.setProductMainImage(product.getMainImage());
                    cartProductVo.setProductSubtitle(product.getSubtitle());

                    cartProductVo.setProductPrice(product.getPrice());
                    cartProductVo.setProductStock(product.getStock());

                    int buyLimitCount = 0;

                    if(product.getStock()>cartItem.getQuantity()){//库存足够
                        buyLimitCount = cartItem.getQuantity();
                        cartProductVo.setLimitQuantity(Const.Cart.LIMIT_NUM_SUCCESS);
                    }else {
                        buyLimitCount = product.getStock();
                        cartProductVo.setLimitQuantity(Const.Cart.LIMIT_NUM_FAIL);
                        //购物车中跟新有效库存
                        Cart cartUpdate = new Cart();
                        cartUpdate.setId(cartItem.getId());
                        cartUpdate.setQuantity(buyLimitCount);
                        cartMapper.updateByPrimaryKeySelective(cartUpdate);

                    }

                    cartProductVo.setQuantity(buyLimitCount);

                    //计算总价
                    cartProductVo.setProductTotalPrice(BigDecimalUtil.mul(cartProductVo.getQuantity(),product.getPrice().doubleValue()));
                    cartProductVo.setProductChecked(cartItem.getChecked());
                }


                if(cartItem.getChecked()==Const.Cart.CHECKED){
                    //如果已经勾选,增加到整个的购物车总价中
                    cartTotalPrice = BigDecimalUtil.add(cartTotalPrice.doubleValue(),cartProductVo.getProductTotalPrice().doubleValue());
                }
                cartProductVoList.add(cartProductVo);
            }
        }

        cartVo.setCartProductVoList(cartProductVoList);
        cartVo.setCartTotalPrice(cartTotalPrice);
        cartVo.setAllChecked(this.getAllCheckedStatus(userId));
        cartVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix"));

        return cartVo;


    }


   private Boolean getAllCheckedStatus(Integer userId){

        if(userId!=null){
            return cartMapper.selectCartProductCheckedStatusByUserId(userId)==0;
        }

        return false;

   }

    /**
     *  购物车产品列表
     * @param userId
     * @return
     */

   public ServerResponse<CartVo> list(Integer userId){

       CartVo cartVo = this.getCartVoLimit(userId);

       return ServerResponse.createBySuccessData(cartVo);


   }

    /**
     * 更新购物车中某个产品的数量
     * @param userId
     * @param productId
     * @param count
     * @return
     */
    public ServerResponse update(Integer userId,Integer productId,Integer count){

     if(productId==null||count==null){

         return ServerResponse.createByErrorCodeAndMessage(Const.ResponseCode.ILLEGAL_ARGUMENT.getCode(),Const.ResponseCode.ILLEGAL_ARGUMENT.getDesc());

     }

     Cart cart = cartMapper.selectByUserIdProductId(userId,productId);

     if(cart!=null){

         cart.setQuantity(count);
     }

     cartMapper.updateByPrimaryKeySelective(cart);

     return this.list(userId);

    }


    /**
     * 移除购物车某个产品
     * @param userId
     * @param productIds
     * @return
     */
    public ServerResponse<CartVo> deleteProduct(Integer userId,String productIds){

       List<String> productIdList = Splitter.on(",").splitToList(productIds);

       if(CollectionUtils.isEmpty(productIdList)){
           return ServerResponse.createByErrorCodeAndMessage(Const.ResponseCode.ILLEGAL_ARGUMENT.getCode(),Const.ResponseCode.ILLEGAL_ARGUMENT.getDesc());

       }

       cartMapper.deleteUserIdPorductIds(userId,productIdList);

       return this.list(userId);

    }

    /**
     * 购物车选中某个商品
     * @param userId
     * @param productId
     * @param checked
     * @return
     */
    public ServerResponse<CartVo> selectProduct(Integer userId,Integer productId,Integer checked){

       cartMapper.checkedOrUncheckedProduct(userId,productId,checked);

       return this.list(userId);


    }

































































}
