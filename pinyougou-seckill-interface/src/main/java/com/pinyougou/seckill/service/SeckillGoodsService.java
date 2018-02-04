package com.pinyougou.seckill.service;
import java.util.List;
import com.pinyougou.pojo.TbSeckillGoods;

import entity.PageResult;
/**
 * 服务层接口
 * @author Administrator
 *
 */
public interface SeckillGoodsService {

	/**
	 * 返回全部列表
	 * @return
	 */
	public List<TbSeckillGoods> findAll();
	
	
	/**
	 * 返回分页列表
	 * @return
	 */
	public PageResult findPage(int pageNum,int pageSize);
	
	
	/**
	 * 增加
	*/
	public void add(TbSeckillGoods seckillGoods);
	
	
	/**
	 * 修改
	 */
	public void update(TbSeckillGoods seckillGoods);
	

	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	public TbSeckillGoods findOne(Long id);
	
	
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
	public PageResult findPage(TbSeckillGoods seckillGoods, int pageNum,int pageSize);

	
	/**
	 * 
	 * 查询秒杀商品列表
	 * @return
	 * @throws Exception<br/>
	 * ============History===========<br/>
	 * 2018年2月4日   Administrator    新建
	 */
	public List<TbSeckillGoods> findSeckillGoodsList() throws Exception;
	
	
	/**
	 * 
	 * 查询剩余库存量
	 * @param id 商品id
	 * @return
	 * @throws Exception<br/>
	 * ============History===========<br/>
	 * 2018年2月4日   Administrator    新建
	 */
	public Integer querySeckillStockCount(Long id) throws Exception;
}
