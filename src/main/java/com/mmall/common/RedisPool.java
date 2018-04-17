package com.mmall.common;

import com.mmall.util.PropertiesUtil;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisPool {


    private static JedisPool jedisPool;
    private static Integer maxTotal = Integer.parseInt(PropertiesUtil.getProperty("redis.max.total"));
    private static Integer maxIdle = Integer.parseInt(PropertiesUtil.getProperty("redis.max.idle"));
    private static Integer minIdle = Integer.parseInt(PropertiesUtil.getProperty("redis.min.idle"));
    private static Boolean testOnBorrow = Boolean.parseBoolean(PropertiesUtil.getProperty("redis.test.borrow"));
    private static Boolean testOnReturn = Boolean.parseBoolean(PropertiesUtil.getProperty("redis.on.return"));



    private static  String redisIp = PropertiesUtil.getProperty("redis.ip");
    private static  Integer redisPort = Integer.parseInt(PropertiesUtil.getProperty("redis.port"));


    private static void initPool(){

        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxTotal(maxTotal);
        jedisPoolConfig.setMaxIdle(maxIdle);
        jedisPoolConfig.setMinIdle(minIdle);
        jedisPoolConfig.setTestOnBorrow(testOnBorrow);
        jedisPoolConfig.setTestOnReturn(testOnReturn);
        jedisPoolConfig.setBlockWhenExhausted(true);

        jedisPool = new JedisPool(jedisPoolConfig,redisIp,redisPort,2000);//连接耗尽的时候，是否阻塞，false会抛出异常，true阻塞直到超时。默认为true。

    }

    //JVM启动时候，就加载进内存，实例化一次。
    static {
        initPool();
    }


    public static Jedis getJedis(){

        return jedisPool.getResource();
    }


    public static void returnBrokenResource(Jedis jedis){
       jedisPool.returnBrokenResource(jedis);
    }

    public static void returnResource(Jedis jedis){
        jedisPool.returnResource(jedis);
    }










}
