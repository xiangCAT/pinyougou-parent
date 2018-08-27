/** ======================================
 * Beijing Itcast Tech. Co.,Ltd
 * Date：2018年2月5日 上午9:34:03
 * Author：huyy
 * Version：1.0
 * =========Modification History==========
 * Date          Name        Description
 * 2018年2月5日       Administrator     创建SeckillTask类
 */
package com.pinyougou.task;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.support.atomic.RedisAtomicInteger;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.pinyougou.constant.SeckillConstant;
import com.pinyougou.mapper.TbSeckillGoodsMapper;
import com.pinyougou.pojo.TbSeckillGoods;
import com.pinyougou.pojo.TbSeckillGoodsExample;
import com.pinyougou.pojo.TbSeckillGoodsExample.Criteria;

/**
 * @Path com.pinyougou.task.SeckillTask
 * @Description 定时任务
 * @date 2018年2月5日上午9:34:03
 * @author huyy
 * @version：1.0
 */
@Component
public class SeckillTask {
    
    @Autowired
    private RedisTemplate redisTemplate;
    
    @Autowired
    private TbSeckillGoodsMapper seckillGoodsMapper;

    /**
     * 
     * 定时执行该方法<br/>
     * ============History===========<br/>
     * 2018年2月5日   Administrator    新建
     */
    @Scheduled(cron="0-30 * * * * ?")
    public void refreshSeckillGoodsDemo(){
        /*SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        System.out.println("执行了任务调度"+sdf.format(new Date()));*/       
    }   
    
    
    /**
     * 刷新秒杀商品
     */
    @Scheduled(cron="0 * * * * ?")
    public void refreshSeckillGoods(){
        try {
            System.out.println("执行了任务调度"+new Date());           
            //查询所有的秒杀商品键集合
            List ids = new ArrayList( redisTemplate.boundHashOps(SeckillConstant.SECKILL_GOODS_LIST).keys());
            //查询正在秒杀的商品列表       
            TbSeckillGoodsExample example=new TbSeckillGoodsExample();
            Criteria criteria = example.createCriteria();
            criteria.andStatusEqualTo("1");//审核通过
            criteria.andStockCountGreaterThan(0);//剩余库存大于0
            criteria.andStartTimeLessThanOrEqualTo(new Date());//开始时间小于等于当前时间
            criteria.andEndTimeGreaterThan(new Date());//结束时间大于当前时间     
            if(ids !=null && ids.size() > 0){
                criteria.andIdNotIn(ids);//排除缓存中已经有的商品      
            }
            List<TbSeckillGoods> seckillGoodsList= seckillGoodsMapper.selectByExample(example);     
            //装入缓存 
            for( TbSeckillGoods seckill:seckillGoodsList ){
                redisTemplate.boundHashOps(SeckillConstant.SECKILL_GOODS_LIST).put(seckill.getId() + "", seckill);
                RedisAtomicInteger redisAtomicInteger = new RedisAtomicInteger(seckill.getId() + "", redisTemplate.getConnectionFactory());
                redisAtomicInteger.set(seckill.getStockCount());
                System.out.println("将商品" + seckill.getId() + "放入redis缓存,剩余库存为" + seckill.getStockCount());
            }
            System.out.println("将"+seckillGoodsList.size()+"条商品装入缓存");
        } catch (Exception e) {
            System.out.println("报错了......");
            e.printStackTrace();
        }
    }
    
    
    /**
     * 移除秒杀商品（秒杀商品过期）
     */
    @Scheduled(cron="5 * * * * ?")
    public void removeSeckillGoods(){
        try {
            System.out.println("移除秒杀商品任务在执行");
            //扫描缓存中秒杀商品列表，发现过期的移除
            List<TbSeckillGoods> seckillGoodsList = redisTemplate.boundHashOps(SeckillConstant.SECKILL_GOODS_LIST).values();
            for( TbSeckillGoods seckill:seckillGoodsList ){
                if(seckill.getEndTime().getTime()<new Date().getTime()  ){//如果结束日期小于当前日期，则表示过期
                    redisTemplate.boundHashOps(SeckillConstant.SECKILL_GOODS_LIST).delete(seckill.getId());//移除缓存数据
                    System.out.println("移除秒杀商品"+seckill.getId());
                }           
            }
            System.out.println("移除秒杀商品任务结束");
        } catch (Exception e) {
            System.out.println("报错了");
            e.printStackTrace();
        }       
    }



}
