package com.mmall.util;

import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class CookieUtil {

    private static String DOMAN_NAME = ".rancui.com";
    private static String COOKIE_NAME = "login_cookie";

    public static void writeLoginToken(HttpServletResponse response,String token){

        Cookie cookie = new Cookie(COOKIE_NAME,token);

        cookie.setDomain(DOMAN_NAME);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(365*24*60*60);// 设置一年的缓存时间，单位秒。setMaxAge会把缓存存在硬盘，如果是-1，代表永久

        response.addCookie(cookie);

    }


    public static String readLoginToken(HttpServletRequest request){

        Cookie[] cookies = request.getCookies();
        if(cookies!=null){
            for(Cookie cookie:cookies){
                if(StringUtils.equals(cookie.getName(),COOKIE_NAME)){
                    return cookie.getValue();
                }
            }
        }
        return null;
    }



    public static void deleteLoginToken(HttpServletRequest request,HttpServletResponse response){

        Cookie[] cookies = request.getCookies();

        if(cookies!=null){
            for (Cookie cookie:cookies){
                if(StringUtils.equals(cookie.getName(),COOKIE_NAME)){
                    cookie.setDomain(DOMAN_NAME);
                    cookie.setPath("/");
                    cookie.setMaxAge(0);

                    response.addCookie(cookie);//0，代表删除cookie
                    return;

                }
            }

        }
    }















































}
