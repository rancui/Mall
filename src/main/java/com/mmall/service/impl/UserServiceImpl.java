package com.mmall.service.impl;

import com.mmall.common.Const;
import com.mmall.common.ServerResponse;
import com.mmall.common.TokenCache;
import com.mmall.dao.UserMapper;
import com.mmall.pojo.User;
import com.mmall.service.IUserService;
import com.mmall.util.MD5Util;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service("iUserService")
public class UserServiceImpl implements IUserService{
    @Autowired
    private UserMapper userMapper;

    /**
     *  登陆
     * @param username
     * @param password
     * @return
     */
    public ServerResponse<User> login(String username,String password){

        int usernameCount = userMapper.checkUsername(username);
        if(usernameCount==0){
            return ServerResponse.createByErrorMessage("用户名不存在");
        }

        String md5Password = MD5Util.MD5EncodeUtf8(password);
        User user = userMapper.checkLogin(username,md5Password);

        if(user==null){
           return ServerResponse.createByErrorMessage("密码错误");
        }

        user.setPassword(StringUtils.EMPTY);

        return ServerResponse.createBySuccessMsgAndData("登陆成功",user);

    }

    /**
     *  用户注册
     * @param user
     * @return
     */
    public ServerResponse<String> register(User user){

       ServerResponse usernameServerResponse = this.checkVaild(user.getUsername(),Const.USERNAME);
       if(!usernameServerResponse.isSuccess()){ // 已注册
            return usernameServerResponse;
       }

       ServerResponse emailServerResponse = this.checkVaild(user.getEmail(),Const.EMAIL);

        if(!usernameServerResponse.isSuccess()){// 已注册
            return emailServerResponse;
        }

        user.setRole(Const.Role.ROLE_CUSTOMER);
        user.setPassword(MD5Util.MD5EncodeUtf8(user.getPassword()));

        int userCount = userMapper.insert(user);
        if(userCount==0){
            return ServerResponse.createByErrorMessage("注册失败");
        }

        return ServerResponse.createBySuccessMessage("注册成功");


    }


    /***
     *  校验用户名和email的唯一性
     * @param name
     * @param type
     * @return
     */
    public ServerResponse checkVaild(String name,String type){

        if(StringUtils.isNotBlank(type)){

           if(Const.USERNAME.equals(type)){

               int count = userMapper.checkUsername(name);
               if(count>0){
                    return  ServerResponse.createByErrorMessage("用户名已存在");
               }

           }

           if(Const.EMAIL.equals(type)){
               int count = userMapper.checkEmail(name);
               if(count>0){
                   return  ServerResponse.createByErrorMessage("邮箱已存在");
               }

           }

        }else{

          return ServerResponse.createByErrorMessage("type这个参数有误");
        }

        return ServerResponse.createBySuccessMessage("校验成功");

    }


    /**
     *  获取用户信息
     * @param userId
     * @return
     */
   public ServerResponse<User> getUserInformation(Integer userId){

       User user = userMapper.selectByPrimaryKey(userId);

       if (user==null){
           return ServerResponse.createByErrorMessage("用户不存在");
       }
       user.setPassword(StringUtils.EMPTY);
       return ServerResponse.createBySuccessMsgAndData("已获取到用户信息",user);

   }

    /**
     *  忘记密码的情况，根据用户名获取问题
     * @param username
     * @return
     */
   public ServerResponse<String> forgetGetQuestion(String username){

       if(StringUtils.isBlank(username)){
           return ServerResponse.createByErrorMessage("用户名不能为空");
       }

       String question = userMapper.selectQuestionByUsername(username);
       if(StringUtils.isBlank(question)){

           return ServerResponse.createByErrorMessage("问题不存在");
       }

       return ServerResponse.createBySuccessData(question);


   }


    /***
     * 验证问题答案
     * @param username
     * @param question
     * @param answer
     * @return
     */
   public ServerResponse<String> checkAnswer(String username,String question,String answer){

       int count = userMapper.checkAnswer(username,question,answer);
       if(count==0){
           return ServerResponse.createByErrorMessage("答案错误");
       }
       return ServerResponse.createBySuccessData(answer);
   }

    /**
     * 忘记密码情况，根据用户名重置密码
     * @param username
     * @param passwordNew
     * @param forgetToken
     * @return
     */
    public  ServerResponse<String> forgetResetPassword(String username,String passwordNew,String forgetToken){

        if(StringUtils.isBlank(forgetToken)){
            return ServerResponse.createByErrorMessage("参数forgetToken不能为空");
        }

        ServerResponse serverResponse = this.checkVaild(username,Const.USERNAME);
        if(serverResponse.isSuccess()){
            return serverResponse.createBySuccessMessage("用户不存在");
        }

        String token = TokenCache.getKey(Const.PREFIX+username);
        if(StringUtils.isBlank(token)){
            return serverResponse.createByErrorMessage("token失效或过期");
        }

        if(StringUtils.equals(forgetToken,token)){
               String md5PasswordNew = MD5Util.MD5EncodeUtf8(passwordNew);
               int count = userMapper.resetPasswordByUsername(username,md5PasswordNew);
               if(count>0){
                   return serverResponse.createBySuccessMessage("密码重置成功");
               }

        }else {
            return serverResponse.createByErrorMessage("token有误，请重新获取");
        }

        return serverResponse.createByErrorMessage("密码重置失败");

    }


    /**
     * 登陆状态下，重置密码
     * @param passwordNew
     * @param passwordOld
     * @return
     */
    public  ServerResponse<String> resetPassword(User user,String passwordOld,String passwordNew){

        //根据用户Id,防止查询出其他也有相同密码的用户数
        int count = userMapper.checkUserPassword(MD5Util.MD5EncodeUtf8(passwordOld),user.getId());
        if(count==0){
            return ServerResponse.createByErrorMessage("用户旧密码不正确");
        }

        user.setPassword(MD5Util.MD5EncodeUtf8(passwordNew));

        int updateCount = userMapper.updateByPrimaryKeySelective(user);

        if(updateCount==0){
            return ServerResponse.createByErrorMessage("密码更新失败");
        }

        return ServerResponse.createBySuccessMessage("密码更新成功");

    }


    /***
     *  更新用户信息
     * @param user
     * @return
     */
    public ServerResponse<User> updatePersonalInformation(User user){

        //用户名username不能更改，email也要检查是否已存在
        int resultCount = userMapper.checkEmailByUserId(user.getEmail(),user.getId());
        if(resultCount>0){
            return  ServerResponse.createByErrorMessage("邮箱已存在，更更换其他邮箱");
        }

        User updateUser = new User();

        updateUser.setId(user.getId());
        updateUser.setEmail(user.getEmail());
        updateUser.setPhone(user.getPhone());
        updateUser.setQuestion(user.getQuestion());
        updateUser.setAnswer(user.getAnswer());

        int updateCount = userMapper.updateByPrimaryKeySelective(updateUser);
        if(updateCount==0){
            return ServerResponse.createByErrorMessage("用户信息更新失败");
        }
        return ServerResponse.createBySuccessMsgAndData("用户信息更新成功",updateUser);

    }


    /**
     *  检查是否具有管理员权限
     * @param user
     * @return
     */
    public ServerResponse checkAdminRole(User user){

        if(user!=null&&user.getRole().intValue()==Const.Role.ROLE_ADMIN){
            return ServerResponse.createBySuccess();
        }

        return ServerResponse.createByError();

    }









































































}
