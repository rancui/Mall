package com.mmall.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.mmall.common.Const;
import com.mmall.common.ServerResponse;
import com.mmall.dao.ShippingMapper;
import com.mmall.pojo.Shipping;
import com.mmall.service.IShippingService;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.interfaces.RSAKey;
import java.util.List;


@Service("iShippingService")
public class ShippingServiceImpl implements IShippingService{

    @Autowired
    private ShippingMapper shippingMapper;

    /**
     * 新建收货地址
     * @param userId
     * @param shipping
     * @return
     */
    public ServerResponse add(Integer userId, Shipping shipping){

        shipping.setUserId(userId);

        int rowCount = shippingMapper.insert(shipping);
        if(rowCount>0){
            return ServerResponse.createBySuccessMessage("地址添加成功");
        }

        return ServerResponse.createByErrorMessage("地址添加失败");

    }

    /**
     * 删除收货地址
     * @param userId
     * @param shippingId
     * @return
     */
    public ServerResponse delete(Integer userId,Integer shippingId){


        int rowCount = shippingMapper.deleteByUserIdAndShippingId(userId,shippingId);
        if(rowCount>0){

            return ServerResponse.createByErrorMessage("删除地址成功");

        }

        return ServerResponse.createByErrorMessage("删除地址失败");


    }


    /**
     * 更新地址
     * @param userId
     * @param shipping
     * @return
     */
    public ServerResponse update(Integer userId,Shipping shipping){

        shipping.setUserId(userId);
        int count = shippingMapper.updateByPrimaryKeySelective(shipping);
        if(count>0){
            return ServerResponse.createBySuccessMessage("地址更新成功");
        }

        return ServerResponse.createByErrorMessage("更新地址失败");

    }


    /**
     * 选中查看具体的地址
     * @param userId
     * @param shippingId
     * @return
     */
    public ServerResponse select(Integer userId,Integer shippingId){

        Shipping shipping = shippingMapper.selectByUserIdAndShippingId(userId,shippingId);
        if(shipping==null){
            return ServerResponse.createByErrorMessage("改地址不存在");

        }

        return ServerResponse.createBySuccessMsgAndData("查询到改地址",shipping);

    }


    /**
     * 地址列表
     * @param userId
     * @param pageNum
     * @param pageSize
     * @return
     */
    public ServerResponse<PageInfo> list(Integer userId,Integer pageNum,Integer pageSize){

        PageHelper.startPage(pageNum,pageSize);

        List<Shipping> shippingList = shippingMapper.selectShippingListByUserId(userId);
        if(CollectionUtils.isEmpty(shippingList)){
            return ServerResponse.createByErrorMessage("地址列表为空");
        }

        PageInfo pageInfo = new PageInfo(shippingList);

        return ServerResponse.createBySuccessData(pageInfo);







    }



























































































}
