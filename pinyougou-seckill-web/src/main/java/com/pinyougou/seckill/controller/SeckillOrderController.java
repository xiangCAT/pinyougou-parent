package com.pinyougou.seckill.controller;
import java.util.List;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.TbSeckillOrder;
import com.pinyougou.seckill.service.SeckillGoodsService;
import com.pinyougou.seckill.service.SeckillOrderService;

import entity.PageResult;
import entity.Result;
/**
 * controller
 * @author Administrator
 *
 */
@RestController
@RequestMapping("/seckillOrder")
public class SeckillOrderController {

	@Reference
	private SeckillOrderService seckillOrderService;
	
	@Reference
	private SeckillGoodsService seckillGoodsService;
	
	/**
	 * 返回全部列表
	 * @return
	 */
	@RequestMapping("/findAll")
	public List<TbSeckillOrder> findAll(){			
		return seckillOrderService.findAll();
	}
	
	
	/**
	 * 返回全部列表
	 * @return
	 */
	@RequestMapping("/findPage")
	public PageResult  findPage(int page,int rows){			
		return seckillOrderService.findPage(page, rows);
	}
	
	/**
	 * 增加
	 * @param seckillOrder
	 * @return
	 */
	@RequestMapping("/add")
	public Result add(@RequestBody TbSeckillOrder seckillOrder){
		try {
			seckillOrderService.add(seckillOrder);
			return new Result(true, "增加成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "增加失败");
		}
	}
	
	/**
	 * 修改
	 * @param seckillOrder
	 * @return
	 */
	@RequestMapping("/update")
	public Result update(@RequestBody TbSeckillOrder seckillOrder){
		try {
			seckillOrderService.update(seckillOrder);
			return new Result(true, "修改成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "修改失败");
		}
	}	
	
	/**
	 * 获取实体
	 * @param id
	 * @return
	 */
	@RequestMapping("/findOne")
	public TbSeckillOrder findOne(Long id){
		return seckillOrderService.findOne(id);		
	}
	
	/**
	 * 批量删除
	 * @param ids
	 * @return
	 */
	@RequestMapping("/delete")
	public Result delete(Long [] ids){
		try {
			seckillOrderService.delete(ids);
			return new Result(true, "删除成功"); 
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "删除失败");
		}
	}
	
		/**
	 * 查询+分页
	 * @param brand
	 * @param page
	 * @param rows
	 * @return
	 */
	@RequestMapping("/search")
	public PageResult search(@RequestBody TbSeckillOrder seckillOrder, int page, int rows  ){
		return seckillOrderService.findPage(seckillOrder, page, rows);		
	}
	
	/**
	 * 
	 * 提交秒杀订单
	 * @param id ： 秒杀商品id
	 * @return<br/>
	 * ============History===========<br/>
	 * 2018年2月4日   Administrator    新建
	 */
	@RequestMapping("/submitSeckillOrder")
	public Result submitSeckillOrder(Long id){
	    /**
	     * 1、判断是否还有库存  , 是否在秒杀中
	     * 2、减库存 (减库存、生成秒杀订单、把秒杀订单扔到redis中)
	     */
	    Result result = new Result(false, "FAIL");
	    try {
            Integer stockCount = seckillGoodsService.querySeckillStockCount(id);
            if(stockCount <= 0){
                return result;//没有库存
            }
            String userid = SecurityContextHolder.getContext().getAuthentication().getName();
            Boolean success = seckillOrderService.submitSeckillOrder(id,userid);
            if(success){
                //如果成功
                result = new Result(true, "SUCCESS");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return result;
        }
	    return result;
	}
	
}
