package com.xie.reggie.comon;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import sun.net.idn.Punycode;

import java.sql.SQLIntegrityConstraintViolationException;

/**
 * 实现全局异常处理
 */
@ControllerAdvice(annotations = {RestController.class, Controller.class})
@ResponseBody
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 拦截SQLIntegrityConstraintViolationException异常
     * @return
     */
    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public R<String> exceptionHandler(SQLIntegrityConstraintViolationException exception){

        if(exception.getMessage().contains("Duplicate")){
            //截取错误信息的字符串
            //以空格为分界，截取字符串
            String[] split = exception.getMessage().split(" ");
            String message = split[2] + "已存在";
            return R.error(message);
        }

        return R.error("出现未知错误");
    }


}
