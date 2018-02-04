package com.pinyougou.seckill.service;
import java.util.List;
import com.pinyougou.pojo.TbSeckillOrder;

import entity.PageResult;
/**
 * 服务层接口
 * @author Administrator
 *
 */
public interface SeckillOrderService {

	/**
	 * 返回全部列表
	 * @return
	 */
	public List<TbSeckillOrder> findAll();
	
	
	/**
	 * 返回分页列表
	 * @return
	 */
	public PageResult findPage(int pageNum,int pageSize);
	
	
	/**
	 * 增加
	*/
	public void add(TbSeckillOrder seckillOrder);
	
	
	/**
	 * 修改
	 */
	public void update(TbSeckillOrder seckillOrder);
	

	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	public TbSeckillOrder findOne(Long id);
	
	
	/**
	 * 批量删除
	 * @param ids
	 */
	public void delete(Long [] ids);

	/**
	 * 分页
	 * @param pageNum 当前页 码
	 * @param pageSize 每页记录数
	 * @return
	 */
	public PageResult findPage(TbSeckillOrder seckillOrder, int pageNum,int pageSize);


	/**
	 * 
	 * 秒杀下单接口
	 * @param id ： 商品id
	 * @param userid ： 用户id
	 * @return<br/>
	 * ============History===========<br/>
	 * 2018年2月4日   Administrator    新建
	 */
    public Boolean submitSeckillOrder(Long id, String userid);


   /**
    * 
    * 查询redis中的秒杀订单
    * @param userid ： 用户id
    * @return<br/>
    * ============History===========<br/>
    * 2018年2月4日   Administrator    新建
    */
    public TbSeckillOrder querySeckillOrderFromRedis(String userid);

    
    /**
     * 
     * 更新秒杀订单的支付状态(保存到数据库，清除redis中的秒杀订单)
     * @param order_no
     * @param transaction_id<br/>
     * ============History===========<br/>
     * 2018年2月4日   Administrator    新建
     */
    public void updateSeckillOrderStatus(String userid,String order_no, String transaction_id);


    /**
     * 
     * 订单支付超时、或者失败
     * @param userid<br/>
     * ============History===========<br/>
     * 2018年2月4日   Administrator    新建
     */
    public void closeOrder(String userid);
        
    }
