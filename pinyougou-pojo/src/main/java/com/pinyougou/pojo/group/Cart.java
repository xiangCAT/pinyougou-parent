/** ======================================
 * Beijing Itcast Tech. Co.,Ltd
 * Date：2018年1月30日 上午10:13:20
 * Author：Administrator
 * Version：1.0
 * =========Modification History==========
 * Date          Name        Description
 * 2018年1月30日       Administrator     创建Cart类
 */
package com.pinyougou.pojo.group;

import java.io.Serializable;
import java.util.List;

import com.pinyougou.pojo.TbOrderItem;

/**
 * 
 * @Path com.pinyougou.pojo.group.Cart
 * @Description 购物车实体类
 * @date 2018年1月30日上午10:14:28
 * @author huyy
 * @version：1.0
 */
public class Cart implements Serializable{
    
    private String sellerName;
    
    private String sellerId;
    
    private List<TbOrderItem> orderItemList;

    public String getSellerName() {
        return sellerName;
    }

    public void setSellerName(String sellerName) {
        this.sellerName = sellerName;
    }

    public String getSellerId() {
        return sellerId;
    }

    public void setSellerId(String sellerId) {
        this.sellerId = sellerId;
    }

    public List<TbOrderItem> getOrderItemList() {
        return orderItemList;
    }

    public void setOrderItemList(List<TbOrderItem> orderItemList) {
        this.orderItemList = orderItemList;
    }
    
    

}
