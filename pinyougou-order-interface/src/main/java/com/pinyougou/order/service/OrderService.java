package com.pinyougou.order.service;
import java.util.List;
import com.pinyougou.pojo.TbOrder;
import com.pinyougou.pojo.TbPayLog;

import entity.PageResult;
/**
 * 服务层接口
 * @author Administrator
 *
 */
public interface OrderService {

	/**
	 * 返回全部列表
	 * @return
	 */
	public List<TbOrder> findAll();
	
	
	/**
	 * 返回分页列表
	 * @return
	 */
	public PageResult findPage(int pageNum,int pageSize);
	
	
	/**
	 * 增加
	*/
	public void add(TbOrder order);
	
	
	/**
	 * 修改
	 */
	public void update(TbOrder order);
	

	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	public TbOrder findOne(Long id);
	
	
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
	public PageResult findPage(TbOrder order, int pageNum,int pageSize);


	/**
	 * 
	 * 查询订单支付日志(父订单)
	 * @param userid ： 当前用户
	 * @return<br/>
	 * ============History===========<br/>
	 * 2018年2月2日   Administrator    新建
	 */
    public TbPayLog queryPayLog(String userid);
    
    /**
     * 
     * 修改订单支付状态
     * @param paylogId : 订单支付日志id
     * @param transaction_id ： 微信交易流水号
     * @throws Exception<br/>
     * ============History===========<br/>
     * 2018年2月2日   Administrator    新建
     */
    public void updateOrderStatus(String paylogId,String transaction_id) throws Exception;
	
}
