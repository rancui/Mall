package com.mmall.common;

public class Const {

    public static final String CURRENT_USER = "CURRENT_USER";
    public static final String USERNAME = "username";
    public static final String EMAIL = "email";
    public static final String PREFIX = "token_";


    // 服务端返回的响应状态码
    public enum ResponseCode {

        ERROR(0,"ERROR"),
        SUCCESS(1,"SUCCESS"),
        ILLEGAL_ARGUMENT(2,"ILLEGAL_ARGUMENT"),
        NEED_LOGIN(3,"NEED_LOGIN");

        private final  int code;
        private  final  String desc;

        ResponseCode(int code,String desc){
            this.code = code;
            this.desc = desc;
        }

        public int getCode(){
            return code;
        }
        public String getDesc(){
            return desc;
        }

    }


    //角色权限
    public interface Role{
        int ROLE_ADMIN = 1; // 管理员
        int ROLE_CUSTOMER = 0;//普通用户
    }


}
