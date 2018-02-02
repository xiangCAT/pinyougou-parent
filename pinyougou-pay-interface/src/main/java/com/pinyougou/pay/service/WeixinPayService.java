/** ======================================
 * Beijing Itcast Tech. Co.,Ltd
 * Date：2018年2月2日 上午10:45:27
 * Author：huyy
 * Version：1.0
 * =========Modification History==========
 * Date          Name        Description
 * 2018年2月2日       Administrator     创建WeixinPayService类
 */
package com.pinyougou.pay.service;

import java.util.Map;

/**
 * @Path com.pinyougou.pay.service.WeixinPayService
 * @Description 微信支付交互接口
 * @date 2018年2月2日上午10:45:27
 * @author huyy
 * @version：1.0
 */
public interface WeixinPayService {

    
    /**
     * 
     * 调用微信统一下单API
     * @param order_no  : 订单号
     * @param money ： 订单金额
     * @return ： 预支付url
     * @throws Exception<br/>
     * ============History===========<br/>
     * 2018年2月2日   Administrator    新建
     */
    public Map createNative(String order_no,String money) throws Exception;
    
    
    /**
     * 
     * 根据订单号查询订单支付状态
     * @param order_no ： 订单号
     * @return ：  返回支付是否成功
     * @throws Exception<br/>
     * ============History===========<br/>
     * 2018年2月2日   Administrator    新建
     */
    public Map queryOrderStatus(String order_no) throws Exception;
}
