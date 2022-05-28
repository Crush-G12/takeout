package com.xie.reggie.comon;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * ThreadLocal工具类
 */
@Component
@Slf4j
public class BaseContext {
    //保存用户的id
    private static ThreadLocal<Long> threadLocal = new ThreadLocal<>();

    //设置id
    public static void setId(Long id){
        threadLocal.set(id);
    }

    //获取id
    public static Long getId(){
        return threadLocal.get();
    }

}
