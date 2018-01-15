package com.mmall.controller.portal;

import com.mmall.common.Const;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;
import com.mmall.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

@Controller
@RequestMapping(value = "/user/")
public class UserController {

    @Autowired
    private IUserService iUserService;

    /***
     *   登陆
     * @param username 用户名
     * @param password 密码
     * @param session
     * @return
     */
    @RequestMapping(value = "login.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<User> login(String username, String password, HttpSession session){

        ServerResponse<User> userServerResponse = iUserService.login(username,password);
        if(userServerResponse.isSuccess()){
            session.setAttribute(Const.CURRENT_USER,userServerResponse.getData());
        }

        return userServerResponse;

    }


    /***
     *  登出
     * @param session
     * @return
     */
    @RequestMapping(value = "logout.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> logout(HttpSession session){

        session.removeAttribute(Const.CURRENT_USER);
        return ServerResponse.createBySuccessMessage(" 登出成功");

    }

    /**
     *  注册
     * @param user
     * @return
     */
    @RequestMapping(value = "register.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> register(User user){

      return iUserService.register(user);

    }


    /**
     *  获取用户信息
     * @param session
     * @return
     */
    @RequestMapping(value = "get_user_info.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse getUserInfo(HttpSession session){

        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if(user==null){

            return ServerResponse.createByErrorMessage("用户尚未登录");
        }

        return ServerResponse.createBySuccessData(user);
    }

    /**
     *  忘记密码的情况，根据用户名获取问题
     * @param username
     * @return
     */
    @RequestMapping(value = "forget_get_question.do",method = RequestMethod.POST)
    @ResponseBody
    public  ServerResponse<String> forgetGetQuestion(String username){

          return iUserService.forgetGetQuestion(username);

    }

    /**
     *  验证问题答案
     * @param username
     * @param question
     * @param answer
     * @return
     */

    public ServerResponse<String> forgetGetAnswer(String username,String question,String answer){

             return iUserService.checkAnswer(username,question,answer);

    }

    /**
     * 忘记密码情况下，重置密码
     * @param username
     * @param passwordNew
     * @param forgetToken
     * @return
     */
    @RequestMapping(value = "forget_reset_password.do",method = RequestMethod.POST)
    @ResponseBody
    public  ServerResponse<String> forgetResetPassword(String username,String passwordNew,String forgetToken){

        return iUserService.forgetResetPassword(username,passwordNew,forgetToken);

    }


    /**
     *  已登录状态下，更新密码
     * @param session
     * @param passwordOld 旧密码
     * @param passwordNew 新密码
     * @return
     */
    @RequestMapping(value = "reset_password.do",method = RequestMethod.POST)
    @ResponseBody
    public  ServerResponse<String> resetPassword(HttpSession session,String passwordOld,String passwordNew){

        User user = (User) session.getAttribute(Const.CURRENT_USER);

        if(user==null){
           return ServerResponse.createByErrorMessage("用户未登陆");
        }

        return iUserService.resetPassword(user,passwordOld,passwordNew);

    }


























}

