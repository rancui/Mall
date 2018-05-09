package com.mmall.util;

import com.mmall.common.RedisPool;
import com.mmall.common.RedisShardedPool;
import com.sun.javafx.binding.StringFormatter;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.ShardedJedis;

@Slf4j
public class RedisShardedPoolUtil {


    public static String setEx(String key,String value,int extTime){

        ShardedJedis jedis = null;
        String result = null;

        try {
            jedis = RedisShardedPool.getJedis();
            result = jedis.setex(key,extTime,value);
        } catch (Exception e) {
            log.error("setex key:{} value:{} error",key,value,e);
            RedisShardedPool.returnBrokenResource(jedis);
            return result;

        }

        RedisShardedPool.returnResource(jedis);
        return result;

    }



    public static String set(String key,String value){

        ShardedJedis jedis = null;
        String result = null;

        try {
            jedis = RedisShardedPool.getJedis();
            result = jedis.set(key,value);
        } catch (Exception e) {
            log.error("set key:{} value:{} error",key,value,e);
            RedisShardedPool.returnBrokenResource(jedis);
            e.printStackTrace();
        }


        RedisShardedPool.returnResource(jedis);

        return result;

    }
    public static Long setnx(String key,String value){

        ShardedJedis jedis = null;
        Long result = null;

        try {
            jedis = RedisShardedPool.getJedis();
            result = jedis.setnx(key,value);
        } catch (Exception e) {
            log.error("setnx key:{} value:{} error",key,value,e);
            RedisShardedPool.returnBrokenResource(jedis);
            e.printStackTrace();
        }


        RedisShardedPool.returnResource(jedis);

        return result;

    }


    public static String get(String key){

        ShardedJedis jedis = null;
        String result = null;

        try {
            jedis = RedisShardedPool.getJedis();
            result = jedis.get(key);
        } catch (Exception e) {
            log.error("get key:{}  error",key,e);
            RedisShardedPool.returnBrokenResource(jedis);
            e.printStackTrace();
        }


        RedisShardedPool.returnResource(jedis);

        return result;

    }
    public static String getSet(String key, String value){

        ShardedJedis jedis = null;
        String result = null;

        try {
            jedis = RedisShardedPool.getJedis();
            result = jedis.getSet(key,value);
        } catch (Exception e) {
            log.error("getSet key:{}  error",key,e);
            RedisShardedPool.returnBrokenResource(jedis);
            e.printStackTrace();
        }


        RedisShardedPool.returnResource(jedis);

        return result;

    }



    public static Long delete(String key){

        ShardedJedis jedis = null;
        Long result = null;

        try {
            jedis = RedisShardedPool.getJedis();
            result = jedis.del(key);
        } catch (Exception e) {
            log.error("delete key:{}  error",key,e);
            RedisShardedPool.returnBrokenResource(jedis);
            e.printStackTrace();
        }


        RedisShardedPool.returnResource(jedis);

        return result;

    }

    /**
     * set the key's period of validity
     * @param key
     * @param exTime
     * @return
     */
    public static Long expire(String key,int exTime){

        ShardedJedis jedis = null;
        Long result = null;

        try {
            jedis = RedisShardedPool.getJedis();
            result = jedis.expire(key,exTime);
        } catch (Exception e) {
            log.error("delete key:{}  error",key,e);
            RedisShardedPool.returnBrokenResource(jedis);
            e.printStackTrace();
        }


        RedisShardedPool.returnResource(jedis);

        return result;

    }












}
