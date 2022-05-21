package com.xie.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xie.reggie.comon.R;
import com.xie.reggie.entity.Employee;
import com.xie.reggie.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.security.MD5Encoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sun.security.provider.MD5;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@Slf4j
@RestController
@RequestMapping(value = "/employee")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    @PostMapping("/login")
    public R<Employee> login(@RequestBody Employee employee, HttpServletRequest request){
        //获取前端数据
        String password = employee.getPassword();
        //MD5加密处理
        password = DigestUtils.md5DigestAsHex(password.getBytes());

        //查询用户名是否存在
        //使用MyBatis-Plus和Lambda表达式，SQL语句隐含在其中了（不用写SQL语句的原因）
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        //第一个参数：字段名，第二个参数：字段的值，eq表示查询等于这个字段的数据
        queryWrapper.eq(Employee::getUsername,employee.getUsername());
        //调用service在数据库进行查询
        Employee one = employeeService.getOne(queryWrapper);

        if(one == null){
            return R.error("用户不存在");
        }
        ///用户名存在,判断密码
        if(!password.equals(one.getPassword())){
            return R.error("密码错误");
        }
        //密码正确
        if(one.getStatus() == 0){
            return R.error("用户已被锁定");
        }
        //登录成功，保存到Session域中
        HttpSession session = request.getSession();
        session.setAttribute("employee",one.getId());

        return R.success(one);
    }

}
