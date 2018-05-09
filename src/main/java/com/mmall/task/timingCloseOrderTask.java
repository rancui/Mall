package com.mmall.task;

import com.mmall.common.Const;
import com.mmall.service.IOrderService;
import com.mmall.util.PropertiesUtil;
import com.mmall.util.RedisShardedPoolUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class timingCloseOrderTask {

    @Autowired
    private IOrderService iOrderService;


    //@Scheduled(cron = "0 */1 * * * ?")
    private void closeOrderTaskV1(){

        int hour = Integer.parseInt(PropertiesUtil.getProperty("close.order.task.time.hour"));

        iOrderService.closeOrder(hour);

    }

    @Scheduled(cron = "0 */1 * * * ?")
    private void closeOrderTaskV2(){

        long lockTimeout = Long.parseLong(PropertiesUtil.getProperty("lock.time"));

        Long setnxResult = RedisShardedPoolUtil.setnx(Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK,String.valueOf(System.currentTimeMillis()+lockTimeout));

        if(setnxResult!=null && setnxResult.intValue()==1){ //设置成功，获取锁

            closeOrder(Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK);
        }else{
            //没有获取到锁，判断是否已经过了设置的expire时间
            String lockValueStr = RedisShardedPoolUtil.get(Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK);
            if(lockValueStr!=null && System.currentTimeMillis()>Long.parseLong(lockValueStr)){// 如果lockValueStr不是空,并且当前时间大于锁的有效期,说明之前的lock的时间已超时,执行getset命令
                //获取之前的值
                String getBeforeValue = RedisShardedPoolUtil.getSet(Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK,String.valueOf(System.currentTimeMillis()+lockTimeout));
                if(getBeforeValue==null || (getBeforeValue!=null && StringUtils.equals(lockValueStr,getBeforeValue))){//之前的值为null，则锁已释放。或者不为null,锁仍存在，没有被其他线程拿到、设置并改变lockValueStr
                     closeOrder(Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK);
                }else{
                    log.info("没有获取到分布式锁:{}",Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK);
                }
            }else {
                log.info("没有获取到分布式锁：{}",Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK);
            }

        }
    }

    //关闭订单
    private void closeOrder(String lockName){

        //给分布式锁设定一个过期时间，防止死锁。
        RedisShardedPoolUtil.expire(lockName,5);//5秒

        log.info("获取分布式锁{},线程是：{}", Const.REDIS_LOCK.CLOSE_ORDER_TASK_LOCK,Thread.currentThread().getName());
        int hour = Integer.parseInt(PropertiesUtil.getProperty("close.order.task.time.hour"));

        iOrderService.closeOrder(hour);

        log.info("订单已关闭");

        RedisShardedPoolUtil.delete(lockName);//释放锁

        log.info("分布式锁已释放");



    }




























}
