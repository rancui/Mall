package com.mmall.task;

import com.mmall.service.IOrderService;
import com.mmall.util.PropertiesUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class timingCloseOrderTask {

    @Autowired
    private IOrderService iOrderService;


    @Scheduled(cron = "0 */1 * * * ?")
    private void closeOrderTaskV1(){

        int hour = Integer.parseInt(PropertiesUtil.getProperty("close.order.task.time.hou"));

        iOrderService.closeOrder(hour);


    }


}
