package com.mmall.util;


import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.type.JavaType;
import org.codehaus.jackson.type.TypeReference;

import java.io.IOException;
import java.text.SimpleDateFormat;

@Slf4j
public class JsonUtil {


    private static ObjectMapper objectMapper = new ObjectMapper();


   static {


       //对象的所有字段全部列入
        objectMapper.setSerializationInclusion(JsonSerialize.Inclusion.ALWAYS);
       //所有的日期格式都统一为以下的样式，即yyyy-MM-dd HH:mm:ss
        objectMapper.setDateFormat(new SimpleDateFormat(DateTimeUtil.STANDARD_FORMAT));
       //忽略空Bean转json的错误
        objectMapper.configure(SerializationConfig.Feature.FAIL_ON_EMPTY_BEANS,false);
        //取消默认转换timestamps形式
        objectMapper.configure(SerializationConfig.Feature.WRITE_DATES_AS_TIMESTAMPS,false);
        //忽略 在json字符串中存在，但是在java对象中不存在对应属性的情况。防止错误
        objectMapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES,false);




   }


    public static<T> String obj2String(T obj){

      if(obj==null){
          return null;
      }

        try {
            return obj instanceof String?(String) obj:objectMapper.writeValueAsString(obj);
        } catch (IOException e) {
            log.warn("Parse Object  to String error",e);
            e.printStackTrace();
            return null;
        }

    }



    public static<T> String obj2StringPretty(T obj){

        if(obj==null){
            return null;
        }

        try {
            return obj instanceof String?(String) obj:objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
        } catch (IOException e) {
            log.warn("Parse Object  to String error",e);
            e.printStackTrace();
            return null;
        }

    }


    public static<T> T str2Obj(String str,Class<T> clazz){

       if(StringUtils.isNotEmpty(str)|| clazz==null){
           return null;
       }

        try {
            return str.equals(String.class)?(T)str:objectMapper.readValue(str,clazz);
        } catch (IOException e) {
            log.warn("Parse String to Object error",e);
            e.printStackTrace();
            return null;
        }


    }


    public static<T> T str2Obj(String str, TypeReference<T> typeReference){

       if(StringUtils.isEmpty(str)|| typeReference==null){
           return null;
       }

        try {
            return (T) (typeReference.getType().equals(String.class)?str:objectMapper.readValue(str,typeReference));
        } catch (IOException e) {
            log.warn("Parse String to Object error",e);
            e.printStackTrace();
            return null;
        }


    }


    public static<T> T str2Obj(String str,Class<?> collectionClass,Class<?>... elementClasses ){

        JavaType javaType = objectMapper.getTypeFactory().constructParametricType(collectionClass,elementClasses);

        try {
            return objectMapper.readValue(str,javaType);
        } catch (IOException e) {
            log.warn("Parse String to Object error",e);
            e.printStackTrace();
            return null;
        }
    }











}
