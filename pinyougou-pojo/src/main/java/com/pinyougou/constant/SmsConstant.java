package com.pinyougou.constant;

/**
 * 
 * @Path com.alicom.dysms.api.SmsConstant
 * @Description 常量类
 * @date 2018年1月4日下午10:24:11
 * @author huyy
 * @version：1.0
 */
public class SmsConstant {
    public static final String AK_ID = "LTAIgvrElTNA5zbM";
    public static final String AK_SECRET = "hyWpXX4C1PkiittraXvL2xtnWxWJwG";
    
    /**
     * 下面是传递map类型的消息中存放消息的key键
     */
    public static final String PHONE = "telephone";
    public static final String SIGN_NAME = "signName";
    public static final String TEMPLATE_CODE = "templateCode";
    public static final String PARAM = "param";
    
    /**
     * 发送短信目的地
     */
    public static final String SMS_DESTINATION = "pinyougou_sms";
    
    /**
     * 存放在redis中的hash的大key
     */
    public static final String SMS_CODE = "smsCode";
    
    

}
