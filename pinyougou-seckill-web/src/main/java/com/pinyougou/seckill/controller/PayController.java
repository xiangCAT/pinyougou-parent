/** ======================================
 * Beijing Itcast Tech. Co.,Ltd
 * Date：2018年2月2日 上午11:05:55
 * Author：huyy
 * Version：1.0
 * =========Modification History==========
 * Date          Name        Description
 * 2018年2月2日       Administrator     创建PayController类
 */
package com.pinyougou.seckill.controller;

import java.util.Map;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pay.service.WeixinPayService;
import com.pinyougou.pojo.TbPayLog;
import com.pinyougou.pojo.TbSeckillOrder;
import com.pinyougou.seckill.service.SeckillOrderService;

import entity.Result;
import util.IdWorker;

/**
 * @Path com.pinyougou.cart.controller.PayController
 * @Description 微信支付controller
 * @date 2018年2月2日上午11:05:55
 * @author huyy
 * @version：1.0
 */
@RequestMapping("/pay")
@RestController
public class PayController {

    @Reference
    private WeixinPayService payService;
    
    @Reference
    private SeckillOrderService orderService;
    
    /**
     * 
     * 调用统一下单API,生成二维码
     * @return<br/>
     * ============History===========<br/>
     * 2018年2月2日   Administrator    新建
     */
    @RequestMapping("/createNative")
    public Map createNative(){
        /**
         * 1、准备订单号和金额
         * 2、调用服务层
         * 3、返回结果
         */
        try {
//            IdWorker idWorker = new IdWorker();
            
            //查询订单支付日志(订单号、总金额)
            String userid = SecurityContextHolder.getContext().getAuthentication().getName();
            TbSeckillOrder seckillOrder= orderService.querySeckillOrderFromRedis(userid);
//            Map map = payService.createNative(idWorker.nextId()+"", "1");
            System.out.println(seckillOrder.getMoney().longValue());
            Map map = payService.createNative(seckillOrder.getId() + "", seckillOrder.getMoney().longValue()+"");
            return map;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        
    }
    
    /**
     * 
     * 查询订单支付状态
     * @param order_no ： 订单号
     * @return<br/>
     * ============History===========<br/>
     * 2018年2月2日   Administrator    新建
     */
    @RequestMapping("/queryOrderStatus")
    public Result queryOrderStatus(String order_no){
        
        Result result = new Result(false, "支付失败");
        int x=0;
        try {
            while(true){
                Map statusMap = payService.queryOrderStatus(order_no);
                if(statusMap == null){
                    //失败
                    
                    break;
                }
                //支付成功
                if(statusMap.get("trade_state").equals("SUCCESS")){
                    result = new Result(true, "支付成功");
                    
                    //支付成功，修改订单状态
                    Object id = statusMap.get("transaction_id");
                    String transaction_id = (String) id;
                    String userid = SecurityContextHolder.getContext().getAuthentication().getName();
                    orderService.updateSeckillOrderStatus(userid,order_no, transaction_id);
                    break;
                }
                if(x>=99){
                    //5分钟=100*3秒
                   /**
                    *   1、关闭微信支付订单
                    *   2、根据userid查询秒杀订单
                    *   3、删除redis中的秒杀订单
                    *   4、恢复库存操作
                    */
                    String userid = SecurityContextHolder.getContext().getAuthentication().getName();
                    Map closeOrderMap = payService.closeOrder(order_no);
                    if( !"SUCCESS".equals(closeOrderMap.get("result_code")) ){//如果返回结果是正常关闭
                        if("ORDERPAID".equals(closeOrderMap.get("err_code"))){
                            result=new Result(true, "支付成功");    
                            Object id = statusMap.get("transaction_id");
                            String transaction_id = (String) id;
                            orderService.updateSeckillOrderStatus(userid,order_no, transaction_id);
                        }
                    }
                    //支付超时
                    orderService.closeOrder(userid);
                    result = new Result(true, "TIME_OUT");
                    break;
                }
                Thread.sleep(3000);
                x++;
            }
            
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return result;
        }
    }
}
