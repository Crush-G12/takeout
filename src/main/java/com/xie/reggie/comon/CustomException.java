package com.xie.reggie.comon;

/**
 * 自定义业务异常
 */
public class CustomException extends RuntimeException{

    //将自定义的异常信息传进来即可
    public CustomException(String message){
        super(message);
    }
}
