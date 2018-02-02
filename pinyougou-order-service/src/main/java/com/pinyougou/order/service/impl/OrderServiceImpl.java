package com.pinyougou.order.service.impl;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.constant.CartConstant;
import com.pinyougou.constant.OrderConstant;
import com.pinyougou.mapper.TbOrderItemMapper;
import com.pinyougou.mapper.TbOrderMapper;
import com.pinyougou.mapper.TbPayLogMapper;
import com.pinyougou.pojo.TbOrder;
import com.pinyougou.pojo.TbOrderExample;
import com.pinyougou.pojo.TbOrderExample.Criteria;
import com.pinyougou.pojo.TbOrderItem;
import com.pinyougou.pojo.TbPayLog;
import com.pinyougou.pojo.group.Cart;
import com.pinyougou.order.service.OrderService;

import entity.PageResult;
import util.IdWorker;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
public class OrderServiceImpl implements OrderService {

	@Autowired
	private TbOrderMapper orderMapper;
	
	@Autowired
	private TbOrderItemMapper tbOrderItemMapper;
	
	@Autowired
	private RedisTemplate redisTemplate;
	
	@Autowired
	private IdWorker idWorker;
	
	@Autowired
	private TbPayLogMapper tbPayLogMapper;
	
	/**
	 * 查询全部
	 */
	@Override
	public List<TbOrder> findAll() {
		return orderMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbOrder> page=   (Page<TbOrder>) orderMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(TbOrder order) {
	    
	    /**
	     * 1、获取到购物车列表(redis)
	     * 2、进行订单的拆解
	     *     根据商家拆单
	     * 3、保存订单和订单项到数据库中
	     * 4、清空redis中的购物车
	     */
	    String username = order.getUserId();
	    Object object = redisTemplate.boundHashOps(CartConstant.REDIS_NAME).get(username);
	    if(object == null){
	        throw new RuntimeException("您的购物车中没有商品");
	    }
	    List<Cart> cartList = (List<Cart>) object;
	    
	    //子订单编号
	    List<Long> orderIdList = new ArrayList<>();
	    double total_money = 0.0;//订单的总金额
	    for (Cart cart : cartList) {
            TbOrder order_seller = new TbOrder();//商家的订单
            order_seller.setOrderId(idWorker.nextId());//订单id
            order_seller.setSourceType(order.getSourceType());
            order_seller.setCreateTime(new Date());
            order_seller.setPaymentType(order.getPaymentType());
            order_seller.setUserId(username);
            order_seller.setUpdateTime(new Date());
            order_seller.setStatus(OrderConstant.NO_PAY);//支付状态
            order_seller.setSellerId(cart.getSellerId());
            order_seller.setReceiver(order.getReceiver());//收货人信息
            order_seller.setReceiverMobile(order.getReceiverMobile());
            order_seller.setReceiverAreaName(order.getReceiverAreaName());
            
            double payment = 0.0;
            for (TbOrderItem orderItem : cart.getOrderItemList()) {
                //保存订单明细
                payment += orderItem.getTotalFee().doubleValue();
                orderItem.setOrderId(order_seller.getOrderId());
                orderItem.setId(idWorker.nextId());
                tbOrderItemMapper.insert(orderItem);
                
            }
            total_money += payment;
            
            order_seller.setPayment(BigDecimal.valueOf(payment));//订单总金额
            //保存订单
            orderMapper.insert(order_seller);
            
            
            orderIdList.add(order_seller.getOrderId());//订单编号
            
        }
	    
	    //生成订单日志
	    /**
	     * 1、组装订单日志
	     * 2、保存到数据库
	     * 3、存放到redis中
	     */
	    TbPayLog payLog = new TbPayLog();
	    payLog.setCreateTime(new Date());
	    //订单号列表，逗号分隔
        String ids=orderIdList.toString().replace("[", "").replace("]", "").replace(" ", "");//逗号分隔的id
	    payLog.setOrderList(ids);//订单id列表
	    payLog.setOutTradeNo(idWorker.nextId()+"");//订单支付日志的id
	    payLog.setPayType(order.getPaymentType());//支付类型
	    payLog.setUserId(order.getUserId());
	    payLog.setTradeState(OrderConstant.NO_PAY);//未支付
	    payLog.setTotalFee((long) (total_money*100));//分为单位，总金额
	    //保存到数据库
	    tbPayLogMapper.insert(payLog);
	    //存放到redis中
	    redisTemplate.boundHashOps("paylog").put(payLog.getUserId(),payLog);
	    
	    //清空当前用户的购物车
	    redisTemplate.boundHashOps(CartConstant.REDIS_NAME).delete(username);
	}

	
	/**
	 * 修改
	 */
	@Override
	public void update(TbOrder order){
		orderMapper.updateByPrimaryKey(order);
	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public TbOrder findOne(Long id){
		return orderMapper.selectByPrimaryKey(id);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			orderMapper.deleteByPrimaryKey(id);
		}		
	}
	
	
		@Override
	public PageResult findPage(TbOrder order, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbOrderExample example=new TbOrderExample();
		Criteria criteria = example.createCriteria();
		
		if(order!=null){			
						if(order.getPaymentType()!=null && order.getPaymentType().length()>0){
				criteria.andPaymentTypeLike("%"+order.getPaymentType()+"%");
			}
			if(order.getPostFee()!=null && order.getPostFee().length()>0){
				criteria.andPostFeeLike("%"+order.getPostFee()+"%");
			}
			if(order.getStatus()!=null && order.getStatus().length()>0){
				criteria.andStatusLike("%"+order.getStatus()+"%");
			}
			if(order.getShippingName()!=null && order.getShippingName().length()>0){
				criteria.andShippingNameLike("%"+order.getShippingName()+"%");
			}
			if(order.getShippingCode()!=null && order.getShippingCode().length()>0){
				criteria.andShippingCodeLike("%"+order.getShippingCode()+"%");
			}
			if(order.getUserId()!=null && order.getUserId().length()>0){
				criteria.andUserIdLike("%"+order.getUserId()+"%");
			}
			if(order.getBuyerMessage()!=null && order.getBuyerMessage().length()>0){
				criteria.andBuyerMessageLike("%"+order.getBuyerMessage()+"%");
			}
			if(order.getBuyerNick()!=null && order.getBuyerNick().length()>0){
				criteria.andBuyerNickLike("%"+order.getBuyerNick()+"%");
			}
			if(order.getBuyerRate()!=null && order.getBuyerRate().length()>0){
				criteria.andBuyerRateLike("%"+order.getBuyerRate()+"%");
			}
			if(order.getReceiverAreaName()!=null && order.getReceiverAreaName().length()>0){
				criteria.andReceiverAreaNameLike("%"+order.getReceiverAreaName()+"%");
			}
			if(order.getReceiverMobile()!=null && order.getReceiverMobile().length()>0){
				criteria.andReceiverMobileLike("%"+order.getReceiverMobile()+"%");
			}
			if(order.getReceiverZipCode()!=null && order.getReceiverZipCode().length()>0){
				criteria.andReceiverZipCodeLike("%"+order.getReceiverZipCode()+"%");
			}
			if(order.getReceiver()!=null && order.getReceiver().length()>0){
				criteria.andReceiverLike("%"+order.getReceiver()+"%");
			}
			if(order.getInvoiceType()!=null && order.getInvoiceType().length()>0){
				criteria.andInvoiceTypeLike("%"+order.getInvoiceType()+"%");
			}
			if(order.getSourceType()!=null && order.getSourceType().length()>0){
				criteria.andSourceTypeLike("%"+order.getSourceType()+"%");
			}
			if(order.getSellerId()!=null && order.getSellerId().length()>0){
				criteria.andSellerIdLike("%"+order.getSellerId()+"%");
			}
	
		}
		
		Page<TbOrder> page= (Page<TbOrder>)orderMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}

		
		/**
	     * 
	     * 查询订单支付日志(父订单)
	     * @param userid ： 当前用户
	     * @return<br/>
	     * ============History===========<br/>
	     * 2018年2月2日   Administrator    新建
	     */
        @Override
        public TbPayLog queryPayLog(String userid) {
            return (TbPayLog) redisTemplate.boundHashOps("paylog").get(userid);
        }

        
        /**
         * 
         * 修改订单支付状态
         * @param paylogId : 订单支付日志id
         * @param transaction_id ： 微信交易流水号
         * @throws Exception<br/>
         * ============History===========<br/>
         * 2018年2月2日   Administrator    新建
         */
        @Override
        public void updateOrderStatus(String paylogId, String transaction_id) throws Exception {
            
            /**
             * 1、查询订单日志信息
             * 2、处理子订单的支付状态
             * 3、清除redis中的paylog（支付日志）
             */
            
            TbPayLog payLog = tbPayLogMapper.selectByPrimaryKey(paylogId);
            payLog.setTradeState(OrderConstant.PAY);//订单支付状态
            payLog.setPayTime(new Date());
            payLog.setTransactionId(transaction_id);
            tbPayLogMapper.updateByPrimaryKey(payLog);
            
            String orderList = payLog.getOrderList();
            String[] orderIds = orderList.split(",");
            for (String orderId : orderIds) {
                //修改状态
                TbOrder tbOrder = orderMapper.selectByPrimaryKey(Long.parseLong(orderId));
                tbOrder.setStatus(OrderConstant.PAY);//已经支付
                
                orderMapper.updateByPrimaryKey(tbOrder);
            }
            
            //清空redis中的支付日志
            redisTemplate.boundHashOps("paylog").delete(payLog.getUserId());
            
        }
	
}
