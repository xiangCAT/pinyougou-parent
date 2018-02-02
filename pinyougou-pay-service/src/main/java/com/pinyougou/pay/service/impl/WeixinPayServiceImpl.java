/** ======================================
 * Beijing Itcast Tech. Co.,Ltd
 * Date：2018年2月2日 上午10:48:09
 * Author：huyy
 * Version：1.0
 * =========Modification History==========
 * Date          Name        Description
 * 2018年2月2日       Administrator     创建WeixinPayServiceImpl类
 */
package com.pinyougou.pay.service.impl;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.wxpay.sdk.WXPayUtil;
import com.pinyougou.pay.service.WeixinPayService;

import util.HttpClient;

/**
 * @Path com.pinyougou.pay.service.impl.WeixinPayServiceImpl
 * @Description 微信支付服务
 * @date 2018年2月2日上午10:48:09
 * @author huyy
 * @version：1.0
 */
@Service
public class WeixinPayServiceImpl implements WeixinPayService {
    
    @Value("${appid}")
    private String appid;
    
    @Value("${partner}")
    private String partner;
    
    @Value("${partnerkey}")
    private String partnerkey;
    
    @Value("${notifyurl}")
    private String notifyurl;

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
    @Override
    public Map createNative(String order_no, String money) throws Exception {

        /**
         * 1、准备参数
         * 2、使用HttpClient工具POST方式向微信支付系统发送xml数据
         * 3、获取响应结果
         * 4、返回url
         */
        //准备参数
        Map paramMap = new HashMap<>();
        paramMap.put("appid", appid);
        paramMap.put("mch_id", partner);
        paramMap.put("nonce_str", WXPayUtil.generateNonceStr());
        paramMap.put("body", "品优购商城");
        paramMap.put("out_trade_no", order_no);
        paramMap.put("total_fee", money);
        paramMap.put("spbill_create_ip", "127.0.0.1");
        paramMap.put("notify_url", notifyurl);
        paramMap.put("trade_type", "NATIVE");
        
        //发送请求
        String xmlParam = WXPayUtil.generateSignedXml(paramMap, partnerkey);
        System.out.println("统一下单请求xml数据：" + xmlParam);
        HttpClient client = new HttpClient("https://api.mch.weixin.qq.com/pay/unifiedorder");
        
        client.setHttps(true);
        client.setXmlParam(xmlParam);//请求参数
        client.post();
        String content = client.getContent();
        System.out.println("统一下单结果：" + content);
        Map<String, String> xmlToMap = WXPayUtil.xmlToMap(content);
        
        //返回结果
        Map result = new HashMap<>();
        result.put("code_url", xmlToMap.get("code_url"));
        result.put("order_no", order_no);
        result.put("money", money);
        return result;
    }

    
    /**
     * 
     * 根据订单号查询订单支付状态
     * @param order_no ： 订单号
     * @return ：  返回支付是否成功
     * @throws Exception<br/>
     * ============History===========<br/>
     * 2018年2月2日   Administrator    新建
     */
    @Override
    public Map queryOrderStatus(String order_no) throws Exception {
        /**
         * 1、准备参数
         * 2、使用HttpClient工具POST方式向微信支付系统发送xml数据
         * 3、获取响应结果
         * 4、返回url
         */
        //准备参数
        Map paramMap = new HashMap<>();
        paramMap.put("appid", appid);
        paramMap.put("mch_id", partner);
        paramMap.put("nonce_str", WXPayUtil.generateNonceStr());
        paramMap.put("out_trade_no", order_no);
        
        //发送请求
        String xmlParam = WXPayUtil.generateSignedXml(paramMap, partnerkey);
        System.out.println("统一下单请求xml数据：" + xmlParam);
        HttpClient client = new HttpClient("https://api.mch.weixin.qq.com/pay/orderquery");
        
        client.setHttps(true);
        client.setXmlParam(xmlParam);//请求参数
        client.post();
        String content = client.getContent();
        System.out.println("统一下单结果：" + content);
        Map<String, String> xmlToMap = WXPayUtil.xmlToMap(content);
        
        return xmlToMap;
    }

}
