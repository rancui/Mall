package com.mmall.common;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.io.Serializable;

/**
 * Created by rancui on 2017/10/9.
 */
// 保证json序列化时候，当值为null时，key消失
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class ServerResponse<T> implements Serializable {

    private String msg;
    private int status;
    private T data;

    public String getMsg() {
        return msg;
    }

    public int getStatus() {
        return status;
    }

    public T getData() {
        return data;
    }


    private ServerResponse(int status){
        this.status = status;
    }

    private ServerResponse(int status,String msg){
        this.status = status;
        this.msg = msg;
    }

    private  ServerResponse(int status, T data){
          this.status = status;
          this.data = data;
    }

    private ServerResponse(int status, String msg, T data){
        this.status = status;
        this.msg = msg;
        this.data = data;
    }




    public static <T>  ServerResponse<T> createBySuccess(){

        return new ServerResponse(Const.ResponseCode.SUCCESS.getCode());

    }

    public static <T> ServerResponse<T> createBySuccessMessage(String msg){
        return new ServerResponse(Const.ResponseCode.SUCCESS.getCode(),msg);
    }
    public static <T> ServerResponse<T> createBySuccessData(T data){
        return new ServerResponse(Const.ResponseCode.SUCCESS.getCode(),data);
    }
    public static <T> ServerResponse<T> createBySuccessMsgAndData(String msg,T data){
        return new ServerResponse(Const.ResponseCode.SUCCESS.getCode(),msg,data);
    }


    public static <T> ServerResponse<T> createByError(){
        return new ServerResponse(Const.ResponseCode.ERROR.getCode());
    }


    public static <T> ServerResponse<T> createByErrorMessage(String msg){
        return new ServerResponse(Const.ResponseCode.ERROR.getCode(),msg);
    }


    public static <T> ServerResponse<T> createByErrorCodeAndMessage(int code,String msg){
        return new ServerResponse(code,msg);
    }



   @JsonIgnore
   public boolean isSuccess(){

        return  this.status == Const.ResponseCode.SUCCESS.getCode();
   }









}
