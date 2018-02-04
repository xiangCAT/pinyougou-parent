package com.pinyougou.seckill.service.impl;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.support.atomic.RedisAtomicInteger;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.constant.OrderConstant;
import com.pinyougou.constant.SeckillConstant;
import com.pinyougou.mapper.TbSeckillOrderMapper;
import com.pinyougou.pojo.TbSeckillGoods;
import com.pinyougou.pojo.TbSeckillOrder;
import com.pinyougou.pojo.TbSeckillOrderExample;
import com.pinyougou.pojo.TbSeckillOrderExample.Criteria;
import com.pinyougou.seckill.service.SeckillOrderService;

import entity.PageResult;
import util.IdWorker;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
public class SeckillOrderServiceImpl implements SeckillOrderService {

	@Autowired
	private TbSeckillOrderMapper seckillOrderMapper;
	
	@Autowired
	private RedisTemplate redisTemplate;
	
	@Autowired
	private IdWorker idWorker;
	
	/**
	 * 查询全部
	 */
	@Override
	public List<TbSeckillOrder> findAll() {
		return seckillOrderMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbSeckillOrder> page=   (Page<TbSeckillOrder>) seckillOrderMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(TbSeckillOrder seckillOrder) {
		seckillOrderMapper.insert(seckillOrder);		
	}

	
	/**
	 * 修改
	 */
	@Override
	public void update(TbSeckillOrder seckillOrder){
		seckillOrderMapper.updateByPrimaryKey(seckillOrder);
	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public TbSeckillOrder findOne(Long id){
		return seckillOrderMapper.selectByPrimaryKey(id);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			seckillOrderMapper.deleteByPrimaryKey(id);
		}		
	}
	
	
		@Override
	public PageResult findPage(TbSeckillOrder seckillOrder, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbSeckillOrderExample example=new TbSeckillOrderExample();
		Criteria criteria = example.createCriteria();
		
		if(seckillOrder!=null){			
						if(seckillOrder.getUserId()!=null && seckillOrder.getUserId().length()>0){
				criteria.andUserIdLike("%"+seckillOrder.getUserId()+"%");
			}
			if(seckillOrder.getSellerId()!=null && seckillOrder.getSellerId().length()>0){
				criteria.andSellerIdLike("%"+seckillOrder.getSellerId()+"%");
			}
			if(seckillOrder.getStatus()!=null && seckillOrder.getStatus().length()>0){
				criteria.andStatusLike("%"+seckillOrder.getStatus()+"%");
			}
			if(seckillOrder.getReceiverAddress()!=null && seckillOrder.getReceiverAddress().length()>0){
				criteria.andReceiverAddressLike("%"+seckillOrder.getReceiverAddress()+"%");
			}
			if(seckillOrder.getReceiverMobile()!=null && seckillOrder.getReceiverMobile().length()>0){
				criteria.andReceiverMobileLike("%"+seckillOrder.getReceiverMobile()+"%");
			}
			if(seckillOrder.getReceiver()!=null && seckillOrder.getReceiver().length()>0){
				criteria.andReceiverLike("%"+seckillOrder.getReceiver()+"%");
			}
			if(seckillOrder.getTransactionId()!=null && seckillOrder.getTransactionId().length()>0){
				criteria.andTransactionIdLike("%"+seckillOrder.getTransactionId()+"%");
			}
	
		}
		
		Page<TbSeckillOrder> page= (Page<TbSeckillOrder>)seckillOrderMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}

		
		/**
	     * 
	     * 秒杀下单接口
	     * @param id ： 商品id
	     * @param userid ： 用户id
	     * @return<br/>
	     * ============History===========<br/>
	     * 2018年2月4日   Administrator    新建
	     */
        @Override
        public Boolean submitSeckillOrder(Long id, String userid) {
            /**
             * 1、减库存
             * 2、生成秒杀订单
             * 3、把秒杀订单存到redis中
             */
            try {
                RedisAtomicInteger atomicInteger = new RedisAtomicInteger(id + "", redisTemplate.getConnectionFactory());
                int stockCount = atomicInteger.decrementAndGet();
                
                System.out.println("商品" + id + "进行减库存操作，剩余库存  " + stockCount);
                
                TbSeckillGoods seckillGoods = (TbSeckillGoods) redisTemplate.boundHashOps(SeckillConstant.SECKILL_GOODS_LIST).get(id +"");
                TbSeckillOrder seckillOrder = new TbSeckillOrder();
                seckillOrder.setId(idWorker.nextId());
                seckillOrder.setCreateTime(new Date());
                seckillOrder.setSeckillId(id);
                seckillOrder.setUserId(userid);
                seckillOrder.setStatus(OrderConstant.NO_PAY);
                seckillOrder.setMoney(BigDecimal.valueOf(seckillGoods.getCostPrice().doubleValue() * 100));
                
                redisTemplate.boundHashOps(SeckillConstant.SECKILL_ORDER_LIST).put(userid, seckillOrder);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        /**
         * 
         * 查询redis中的秒杀订单
         * @param userid ： 用户id
         * @return<br/>
         * ============History===========<br/>
         * 2018年2月4日   Administrator    新建
         */
        @Override
        public TbSeckillOrder querySeckillOrderFromRedis(String userid) {
            return (TbSeckillOrder) redisTemplate.boundHashOps(SeckillConstant.SECKILL_ORDER_LIST).get(userid);
        }

        
        /**
         * 
         * 更新秒杀订单的支付状态(保存到数据库，清除redis中的秒杀订单)
         * @param order_no
         * @param transaction_id<br/>
         * ============History===========<br/>
         * 2018年2月4日   Administrator    新建
         */
        @Override
        public void updateSeckillOrderStatus(String userid ,String order_no, String transaction_id) {
            
            /**
             * 1、查询当前用户的秒杀订单
             *   2、补全秒杀订单(微信流水号，秒杀订单状态)
             *  3、保存秒杀订单到DB中
             *   4、删除redis中的秒杀订单
             */
            
            TbSeckillOrder seckillOrder = querySeckillOrderFromRedis(userid);
            seckillOrder.setPayTime(new Date());//支付时间
            seckillOrder.setStatus(OrderConstant.PAY);//已经支付
            seckillOrder.setTransactionId(transaction_id);//微信支付流水号
            seckillOrderMapper.insert(seckillOrder);//保存到数据库中
            redisTemplate.boundHashOps(SeckillConstant.SECKILL_ORDER_LIST).delete(userid);
            
        }

        @Override
        public void closeOrder(String userid) {
            /**
             * 2、根据userid查询秒杀订单
             *   3、删除redis中的秒杀订单
             *   4、恢复库存操作
             */
            TbSeckillOrder seckillOrder = querySeckillOrderFromRedis(userid);
            RedisAtomicInteger atomicInteger = new RedisAtomicInteger(seckillOrder.getSeckillId()+"", redisTemplate.getConnectionFactory());
            int incrementAndGet = atomicInteger.incrementAndGet();
            System.out.println("商品" + seckillOrder.getSeckillId() + "库存量恢复为 "+ incrementAndGet);
            redisTemplate.boundHashOps(SeckillConstant.SECKILL_ORDER_LIST).delete(userid);
        }
	
}
