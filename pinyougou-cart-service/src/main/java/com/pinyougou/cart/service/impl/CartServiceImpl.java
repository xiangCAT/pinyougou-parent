/** ======================================
 * Beijing Itcast Tech. Co.,Ltd
 * Date：2018年1月30日 上午10:48:11
 * Author：huyy
 * Version：1.0
 * =========Modification History==========
 * Date          Name        Description
 * 2018年1月30日       Administrator     创建CartServiceImpl类
 */
package com.pinyougou.cart.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.cart.service.CartService;
import com.pinyougou.constant.CartConstant;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbOrderItem;
import com.pinyougou.pojo.group.Cart;

/**
 * @Path com.pinyougou.cart.service.impl.CartServiceImpl
 * @Description 购物车服务实现类
 * @date 2018年1月30日上午10:48:11
 * @author huyy
 * @version：1.0
 */
@Service
public class CartServiceImpl implements CartService {
    
    @Autowired
    private TbItemMapper tbItemMapper;
    
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 
     * 添加商品到购物车
     * @param cartList ： 购物车列表
     * @param itemId ： 商品skuid
     * @param num : 商品数量
     * @return 添加完商品后的购物车列表
     * @throws Exception<br/>
     * ============History===========<br/>
     * 2018年1月30日   Administrator    新建
     */
    @Override
    public List<Cart> addGoodsToCartList(List<Cart> cartList, Long itemId, Integer num) throws Exception {
        /**
         * 1、根据商家id查询购物车
         */
        TbItem tbItem = tbItemMapper.selectByPrimaryKey(itemId);
        String sellerId = tbItem.getSellerId();
        String sellerName = tbItem.getSeller();
        
        Cart cart = searchSellerCartByItemId(cartList,sellerId);
        
        if(cart == null){
            //不存在
            /**
             * 1、new Cart，补全属性
             * 2、将当前商品添加到new出来的cart
             * 3、将new 出来的cart,添加到购物车中
             */
            cart = new Cart();
            cart.setSellerId(sellerId);//商家id
            cart.setSellerName(sellerName);//商家名称
            List<TbOrderItem> orderItems = new ArrayList<>();
            TbOrderItem orderItem = createTbOrderItem(tbItem,num);
            orderItems.add(orderItem);
            cart.setOrderItemList(orderItems);
            cartList.add(cart);
        }else{
            //存在
            //在当前商家的购物车中查找当前商品
            TbOrderItem orderItem = searchOrderItemByItemId(cart.getOrderItemList(),itemId);
            if(orderItem == null){
                //不存在
                //1、将当前商品添加到当前商家的cart中
                TbOrderItem item = createTbOrderItem(tbItem, num);
                cart.getOrderItemList().add(item);
            }else{
                //存在
                orderItem.setNum(orderItem.getNum() + num);//数量累加
                orderItem.setTotalFee(BigDecimal.valueOf(orderItem.getPrice().doubleValue() * orderItem.getNum()));//总价格
                
                if(orderItem.getNum() <= 0){//商品的数量为0,移除商品
                    cart.getOrderItemList().remove(orderItem);
                }
                
                if(cart.getOrderItemList().size() <= 0){//商家的购物车中没有商品了，移除该商家的购物车
                    cartList.remove(cart);
                }
            }
        }
        return cartList;
    }

    /**
     * 
     * 根据skuid找购物车里面是否存在当前商品
     * @param orderItemList
     * @param itemId<br/>
     * ============History===========<br/>
     * 2018年1月30日   Administrator    新建
     */
    private TbOrderItem searchOrderItemByItemId(List<TbOrderItem> orderItemList, Long itemId) {
        for (TbOrderItem tbOrderItem : orderItemList) {
            if(itemId.longValue() == tbOrderItem.getItemId()){
                return tbOrderItem;
            }
        }
        return null;
    }

    /**
     * 
     * 根据商品sku创建TbOrderItem
     * @param tbItem
     * @return<br/>
     * ============History===========<br/>
     * 2018年1月30日   Administrator    新建
     */
    private TbOrderItem createTbOrderItem(TbItem tbItem,Integer num) {
        TbOrderItem tbOrderItem = new TbOrderItem();
        tbOrderItem.setGoodsId(tbItem.getGoodsId());
        tbOrderItem.setItemId(tbItem.getId());//商品skuid
        tbOrderItem.setNum(num);//数量
        tbOrderItem.setPicPath(tbItem.getImage());
        tbOrderItem.setPrice(tbItem.getPrice());
        double doubleValue = tbOrderItem.getPrice().doubleValue() * tbOrderItem.getNum();
        tbOrderItem.setTotalFee(BigDecimal.valueOf(doubleValue));//总价格
        tbOrderItem.setTitle(tbItem.getTitle());
        return tbOrderItem;
    }

    /**
     * 
     * 根据商家id查询购物车
     * @param cartList
     * @param sellerId
     * @return<br/>
     * ============History===========<br/>
     * 2018年1月30日   Administrator    新建
     */
    private Cart searchSellerCartByItemId(List<Cart> cartList, String sellerId) {
        for (Cart cart : cartList) {
            if(sellerId.equals(cart.getSellerId())){
                return cart;
            }
        }
        return null;
    }

    /**
     * 
     * 从redis中取购物车信息
     * @param username ： 用户信息
     * @return<br/>
     * ============History===========<br/>
     * 2018年1月30日   Administrator    新建
     */
    @Override
    public List<Cart> findCartListFromRedis(String username) {
        List<Cart> cartList = new ArrayList<>();
        Object object = redisTemplate.boundHashOps(CartConstant.REDIS_NAME).get(username);
        if(object != null){
            cartList = (List<Cart>) object;
        }
        return cartList;
    }

    /**
     * 
     * 把当前用户的购物车存放到redis中
     * @param cartList ： 购物车列表
     * @param username ： 用户名<br/>
     * ============History===========<br/>
     * 2018年1月30日   Administrator    新建
     */
    @Override
    public void saveCartListToRedis(List<Cart> cartList, String username) {
        redisTemplate.boundHashOps(CartConstant.REDIS_NAME).put(username, cartList);
    }

    /**
     * 
     * 合并两个购物车
     * @param cartList_redis ： redis购物车
     * @param cartList_cookie ： cookie购物车
     * @return 合并后的购物车<br/>
     * ============History===========<br/>
     * 2018年1月30日   Administrator    新建
     */
    @Override
    public List<Cart> mergeCartList(List<Cart> cartList_redis, List<Cart> cartList_cookie) {
        try {
            for (Cart cart : cartList_cookie) {
                for (TbOrderItem orderItem : cart.getOrderItemList()) {
                    cartList_redis = addGoodsToCartList(cartList_redis, orderItem.getItemId(), orderItem.getNum());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return cartList_redis;
    }

}
