package com.mmall.common;

import com.mmall.util.PropertiesUtil;
import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.redisson.config.Config;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
@Slf4j
public class RedissonManage {


    Config config = new Config();

    Redisson redisson = null;

    private final static String redis1Ip = PropertiesUtil.getProperty("redis1.ip");
    private final static Integer redis1Port = Integer.parseInt(PropertiesUtil.getProperty("redis1.port"));

    @PostConstruct
    private void init(){

        try {
            config.useSingleServer().setAddress(new StringBuilder().append(redis1Ip).append(":").append(redis1Port).toString());

            //单主模式
            config.useMasterSlaveServers().setMasterAddress(new StringBuilder().append(redis1Ip).append(":").append(redis1Port).toString());

            //主从模式
            config.useMasterSlaveServers().setMasterAddress("127.0.0.1:6379").addSlaveAddress("127.0.0.1:6380");

            redisson = (Redisson)Redisson.create(config);

            log.info("Redisson 初始化结束");
        } catch (Exception e) {
            log.error("Redisson 初始化错误",e);
        }

    }



    public Redisson getRedisson(){

        return redisson;
    }







}
