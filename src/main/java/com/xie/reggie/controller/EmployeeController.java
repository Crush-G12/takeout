package com.xie.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xie.reggie.comon.R;
import com.xie.reggie.entity.Employee;
import com.xie.reggie.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.tomcat.util.security.MD5Encoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;
import sun.security.krb5.internal.PAEncTSEnc;
import sun.security.provider.MD5;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.List;

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

    @PostMapping("/logout")
    public R<String> logout(HttpServletRequest request){
        HttpSession session = request.getSession();
        session.removeAttribute("employee");
        return R.success("退出成功");
    }

    @PostMapping
    public R<String> save(@RequestBody Employee employee,HttpServletRequest request){
        log.info("保存员工信息"+ employee.toString());
        //封装参数
        employee.setCreateTime(LocalDateTime.now());
        Long empId= (Long)request.getSession().getAttribute("employee");
        employee.setCreateUser(empId);
        employee.setUpdateTime(LocalDateTime.now());
        employee.setUpdateUser(empId);
        //设置初始密码吗
        String password = DigestUtils.md5DigestAsHex("123456".getBytes());
        employee.setPassword(password);

        //保存到数据库中
        employeeService.save(employee);

        return R.success("保存成功");
    }

    @GetMapping("/page")
    public R<Page> page(int page,int pageSize,String name){
        //log.info("page= {} ,pageSize= {},name = {}",page,pageSize,name);
        //分页构造器
        Page pageInfo = new Page(page,pageSize);
        //条件构造器
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        //查询条件
        queryWrapper.like(StringUtils.isNotEmpty(name),Employee::getName,name);
        queryWrapper.orderByDesc(Employee::getUpdateTime);
        //执行查询,page对象会被改造，不用返回值
        employeeService.page(pageInfo,queryWrapper);
        return R.success((pageInfo));
    }

    /**
     * 通用的更新方法
     * @param employee
     * @param request
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody Employee employee,HttpServletRequest request){
        log.info("员工信息：" + employee.toString());
        employee.setUpdateTime(LocalDateTime.now());
        Long emp = (Long) request.getSession().getAttribute("employee");
        employee.setUpdateUser(emp);
        employeeService.updateById(employee);
        return R.success("修改成功");
    }

    @GetMapping("{id}")
    public R<Employee> edit(@PathVariable Long id){
        //log.info("访问编辑功能");
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Employee::getId,id);
        Employee employee = employeeService.getOne(queryWrapper);

        return R.success(employee);
    }
}
