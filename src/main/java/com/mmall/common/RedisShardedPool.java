package com.mmall.common;

import com.mmall.util.PropertiesUtil;
import redis.clients.jedis.*;
import redis.clients.util.Hashing;
import redis.clients.util.Sharded;

import java.util.ArrayList;
import java.util.List;

public class RedisShardedPool {


    private static ShardedJedisPool shardedJedisPool;
    private static Integer maxTotal = Integer.parseInt(PropertiesUtil.getProperty("redis.max.total"));
    private static Integer maxIdle = Integer.parseInt(PropertiesUtil.getProperty("redis.max.idle"));
    private static Integer minIdle = Integer.parseInt(PropertiesUtil.getProperty("redis.min.idle"));
    private static Boolean testOnBorrow = Boolean.parseBoolean(PropertiesUtil.getProperty("redis.test.borrow"));
    private static Boolean testOnReturn = Boolean.parseBoolean(PropertiesUtil.getProperty("redis.on.return"));



    private static  String redis1Ip = PropertiesUtil.getProperty("redis1.ip");
    private static  Integer redis1Port = Integer.parseInt(PropertiesUtil.getProperty("redis1.port"));
    private static  String redis2Ip = PropertiesUtil.getProperty("redis2.ip");
    private static  Integer redis2Port = Integer.parseInt(PropertiesUtil.getProperty("redis2.port"));





    private static void initPool(){

        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxTotal(maxTotal);
        jedisPoolConfig.setMaxIdle(maxIdle);
        jedisPoolConfig.setMinIdle(minIdle);
        jedisPoolConfig.setTestOnBorrow(testOnBorrow);
        jedisPoolConfig.setTestOnReturn(testOnReturn);
        jedisPoolConfig.setBlockWhenExhausted(true);//连接耗尽的时候，是否阻塞，false会抛出异常，true阻塞直到超时。默认为true。

        JedisShardInfo shardInfo1 = new JedisShardInfo(redis1Ip,redis1Port,2000);
        JedisShardInfo shardInfo2 = new JedisShardInfo(redis2Ip,redis2Port,2000);

        List<JedisShardInfo> jedisShardInfoList = new ArrayList<JedisShardInfo>(2);
        jedisShardInfoList.add(shardInfo1);
        jedisShardInfoList.add(shardInfo2);

        shardedJedisPool = new ShardedJedisPool(jedisPoolConfig,jedisShardInfoList, Hashing.MURMUR_HASH, Sharded.DEFAULT_KEY_TAG_PATTERN);


    }

    //JVM启动时候，就加载进内存，实例化一次。
    static {
        initPool();
    }


    public static ShardedJedis getJedis(){

        return shardedJedisPool.getResource();
    }


    public static void returnBrokenResource(ShardedJedis jedis){
        shardedJedisPool.returnBrokenResource(jedis);
    }

    public static void returnResource(ShardedJedis jedis){
        shardedJedisPool.returnResource(jedis);
    }










}
