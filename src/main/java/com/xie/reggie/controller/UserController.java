package com.xie.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xie.reggie.comon.R;
import com.xie.reggie.entity.User;
import com.xie.reggie.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Map;

@RestController
@Slf4j
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/sendMsg")
    public R<String> sendMsg(@RequestBody User user, HttpSession session){
        String phone = user.getPhone();

        if(phone != null){
            String code = "123456";

            //将验证码保存到Session里
            session.setAttribute(phone,code);
            return R.success("默认验证码为：123456");
        }

        return R.error("验证码发送失败");
    }

    @PostMapping("/login")
    public R<User> login(@RequestBody Map map,HttpSession session){
        //获取用户输入的手机和验证码
        String phone = map.get("phone").toString();
        String code = map.get("code").toString();

        session.setAttribute(phone,code);
        //与Session的验证码进行比对
        String sessionCode = (String) session.getAttribute(phone);
        //验证码正确，登录成功，否则失败
        if("123456".equals(code)){
            //若登录成功，检查用户是否已存在
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(User::getPhone,phone);
            User user = userService.getOne(queryWrapper);
            //若不存在，则新增用户
            if(user == null){
                User newUser = new User();
                newUser.setStatus(1);
                newUser.setPhone(phone);
                userService.save(newUser);
                session.setAttribute("user",newUser.getId());
                return R.success(newUser);
            }
            session.setAttribute("user",user.getId());
            return R.success(user);
        }

        return R.error("登录失败");
    }
}
