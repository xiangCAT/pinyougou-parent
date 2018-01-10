package com.pinyougou.user.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 
 * @Path com.pinyougou.service.LoginController
 * @Description 登录用户相关的controller
 * @date 2018年1月9日下午6:07:49
 * @author huyy
 * @version：1.0
 */
@RestController
@RequestMapping("/login")
public class LoginController {

    
    /**
     * 
     * 获取当前登录用户信息
     * @return<br/>
     * ============History===========<br/>
     * 2018年1月9日   huyy    新建
     */
    @RequestMapping("/showName")
    public Map showName(){
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Map map = new HashMap<>();
        map.put("username", username);
        return map;
    }
}
