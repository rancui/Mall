package com.mmall.controller.common.interceptor;

import com.google.common.collect.Maps;
import com.mmall.common.Const;
import com.mmall.pojo.User;
import com.mmall.util.CookieUtil;
import com.mmall.util.JsonUtil;
import com.mmall.util.RedisShardedPoolUtil;
import com.sun.org.apache.regexp.internal.RE;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;

@Slf4j
public class AuthorityInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object handler) throws Exception {


        HandlerMethod handlerMethod = (HandlerMethod) handler ;

        String methodName = handlerMethod.getMethod().getName();
        String className = handlerMethod.getBean().getClass().getSimpleName();

        StringBuffer stringBuffer = new StringBuffer();

        Map paramMap = httpServletRequest.getParameterMap();

        Iterator iterator = paramMap.entrySet().iterator();

        while (iterator.hasNext()){

            Map.Entry entry = (Map.Entry)iterator.next();

            String mapKey = (String) entry.getKey();

            String mapValue = StringUtils.EMPTY;

            Object obj = entry.getValue();

            if(obj instanceof String[]){
                String[] strs = (String[]) obj;
                mapValue = Arrays.toString(strs);
            }

            stringBuffer.append(mapKey).append("=").append(mapValue);
        }

        User user=null;
        String loginToken  = CookieUtil.readLoginToken(httpServletRequest);
        if(StringUtils.isNotEmpty(loginToken)){
            String userJsonStr = RedisShardedPoolUtil.get(loginToken);
            user = JsonUtil.str2Obj(userJsonStr,User.class);
        }

        if(user==null|| user.getRole().intValue()!=Const.Role.ROLE_ADMIN){ //用户没登录或者不具备管理员权限

             httpServletResponse.reset();
             httpServletResponse.setCharacterEncoding("UTF-8");
             httpServletResponse.setContentType("application/json;character=utf-8");

            PrintWriter printWriter = httpServletResponse.getWriter();


            if(user==null){ //用户未登录
                if(StringUtils.equals(className,"ProductManageController")&&StringUtils.equals(methodName,"richtext_img_upload")){
                    Map resultMap = Maps.newHashMap();
                    resultMap.put("success",false);
                    resultMap.put("msg","用户未登录");
                    printWriter.print(JsonUtil.obj2String(resultMap));
                }else{
                    printWriter.print("拦截器拦截，用户未登录");
                }

            }else { //用户无权限
                if(StringUtils.equals(className,"ProductManageController")&&StringUtils.equals(methodName,"richtext_img_upload")){
                    Map resultMap = Maps.newHashMap();
                    resultMap.put("success",false);
                    resultMap.put("msg","用户无权限");
                    printWriter.print(resultMap);
                }else{
                    printWriter.print("拦截器拦截，用户无权限");
                }


            }

            printWriter.flush();
            printWriter.close();

            return false;
        }



        return true;
    }

    @Override
    public void postHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, ModelAndView modelAndView) throws Exception {

    }

    @Override
    public void afterCompletion(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, Exception e) throws Exception {

    }
}
