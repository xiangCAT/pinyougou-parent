/** ======================================
 * Beijing Itcast Tech. Co.,Ltd
 * Date：2018年1月30日 上午11:23:19
 * Author：huyy
 * Version：1.0
 * =========Modification History==========
 * Date          Name        Description
 * 2018年1月30日       Administrator     创建CartController类
 */
package com.pinyougou.cart.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.pinyougou.cart.service.CartService;
import com.pinyougou.constant.CartConstant;
import com.pinyougou.pojo.group.Cart;

import entity.Result;
import util.CookieUtil;

/**
 * @Path com.pinyougou.cart.controller.CartController
 * @Description 购物车controller
 * @date 2018年1月30日上午11:23:19
 * @author huyy
 * @version：1.0
 */
@RequestMapping("/cart")
@RestController
public class CartController {
    
    @Autowired
    private HttpServletRequest request;
    
    @Autowired
    private HttpServletResponse response;
    
    @Reference
    private CartService cartService;

    @RequestMapping("/addGoodsToCartList")
    public Result addGoodsToCartList(Long itemId,Integer num){
        /**
         * 1、获取cookie购物车
         * 2、将商品添加到cookie购物车中
         * 3、将cookie购物车写会浏览器
         * 
         */
        
        /**
         * 解决js跨域调用
         */
        response.setHeader("Access-Control-Allow-Origin", "http://localhost:9105");//允许调用
        response.setHeader("Access-Control-Allow-Credentials", "true");//允许传递cookie信息
        
        
        try {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            System.out.println("登录用户："+username);
            
            List<Cart> cartList = findCartList();
            cartList = cartService.addGoodsToCartList(cartList, itemId, num);
            if("anonymousUser".equals(username)){
                //未登录
                CookieUtil.setCookie(request, response, CartConstant.COOKIE_NAME, JSON.toJSONString(cartList), 3600 * 24, "utf-8");
            }else{
                //登录状态
                cartService.saveCartListToRedis(cartList,username);
            }
            return new Result(true, "添加成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "添加失败");
        }
        
    }
    
    /**
     * 
     * 查询购物车列表
     * @return<br/>
     * ============History===========<br/>
     * 2018年1月30日   Administrator    新建
     */
    @RequestMapping("/findCartList")
    public List<Cart> findCartList(){
        
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        System.out.println("登录用户："+username);
        
        String cookieValue = CookieUtil.getCookieValue(request, CartConstant.COOKIE_NAME, "utf-8");
        if(StringUtils.isEmpty(cookieValue)){
            cookieValue = "[]";
        }
        List<Cart> cartList_cookie = JSON.parseArray(cookieValue, Cart.class);
        if("anonymousUser".equals(username)){
            //未登录
            return cartList_cookie;
        }else{
            //登录状态
            List<Cart> cartList_redis = cartService.findCartListFromRedis(username);
            if(cartList_cookie != null && cartList_cookie.size() > 0){//cookie购物车中有商品信息
                cartList_redis = cartService.mergeCartList(cartList_redis,cartList_cookie);
                CookieUtil.deleteCookie(request, response, CartConstant.COOKIE_NAME);
                cartService.saveCartListToRedis(cartList_redis, username);
            }
            return cartList_redis;
        }
    }
}
