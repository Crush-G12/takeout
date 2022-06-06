package com.xie.reggie.filter;

import com.alibaba.fastjson.JSON;
import com.sun.org.apache.xpath.internal.functions.FuncFalse;
import com.xie.reggie.comon.BaseContext;
import com.xie.reggie.comon.R;
import com.xie.reggie.entity.Employee;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.security.PublicKey;

@Slf4j
@WebFilter(filterName = "LoginCheckFilter",urlPatterns = "/*")
public class LoginCheckFilter implements Filter {
    //Spring的路径匹配器
    public static final AntPathMatcher ANT_PATH_MATCHER = new AntPathMatcher();

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        Filter.super.init(filterConfig);
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        //定义不需要过滤的请求
        String[] urls = new String[]{
          "/backend/**",
          "/front/**",
          "/common/**",
          "/employee/login",
          "/employee/logout",
          "/user/sendMsg",
          "/user/login"
        };
        //进行路径匹配
        String requestURI = request.getRequestURI();
        boolean check = check(urls, requestURI);
        if(check){
            //如果是不需要处理的请求，直接放行即可
            //log.info("不需要处理的请求："+requestURI);
            filterChain.doFilter(request,response);
            return;
        }

        //判断是否登录（后台系统）
        Long id = (Long) request.getSession().getAttribute("employee");
        if (id == null){
            //判断是否登录（用户系统）
            Long userId = (Long) request.getSession().getAttribute("user");
            if (userId == null){
                //如果未登录，则通过输出流向客户端响应数据
                response.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));
                return;
            }
            //将用户id保存到ThreadLocal中
            BaseContext.setId(userId);
        }
        //将用户id保存到ThreadLocal中
        if(id != null){
            BaseContext.setId(id);
        }


        //如果已经登录，直接放行
        filterChain.doFilter(request,response);
        return;
    }

    @Override
    public void destroy() {
        Filter.super.destroy();
    }

    /**
     * 路径匹配，匹配成功返回true
     * @param urls
     * @param URI
     * @return
     */
    public boolean check(String[] urls,String URI){
        for (String url : urls) {
            boolean match = ANT_PATH_MATCHER.match(url, URI);
            if(match){
                return true;
            }
        }
        return false;
    }

}
