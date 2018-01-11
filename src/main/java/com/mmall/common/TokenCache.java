package com.mmall.common;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class TokenCache {

    private static Logger logger = LoggerFactory.getLogger(TokenCache.class);

    private static LoadingCache<String,String> cacheBuilder = CacheBuilder.newBuilder()
                                                             .initialCapacity(1000).maximumSize(1024)
                                                             .expireAfterAccess(12, TimeUnit.HOURS)
                                                             .build(new CacheLoader<String, String>() {
                                                                    @Override
                                                                    public String load(String s) throws Exception {
                                                                        return "null";
                                                                    }
                                                             });
    public static void setKey(String key,String value){
        cacheBuilder.put(key,value);

    }


    public static String getKey(String key){
        String value = null;
        try {
            value = cacheBuilder.get(key);
            if("null".equals(value)){
                return null;
            }
            return value;
        } catch (Exception e) {
           logger.error("本地缓存加载出错");
        }
        return null;
    }





































}
