package com.mmall.common;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class ExceptionReslover implements HandlerExceptionResolver {

    private Logger logger = LoggerFactory.getLogger(ExceptionReslover.class);


    @Override
    public ModelAndView resolveException(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, Exception e) {

        logger.info(httpServletRequest.getRequestURI()+"Exception:",e);
        ModelAndView modelAndView = new ModelAndView(new MappingJackson2JsonView());

        modelAndView.addObject("status",Const.ResponseCode.ERROR);
        modelAndView.addObject("msg","接口异常,详情请查看日志中的异常信息");
        modelAndView.addObject("data",e.toString());

        return modelAndView;
    }
}
