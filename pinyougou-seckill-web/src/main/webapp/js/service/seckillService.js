app.service('seckillService',function($http){
	
	//查询秒杀商品列表
	this.findSeckillList=function(){
		return $http.get("./seckillGoods/findSeckillList.do");
	}
	
	//获取秒杀商品
	this.findSeckillGoods=function(id){
		return $http.get("./seckillGoods/findOne.do?id=" + id);
	}
	
	
	//提交秒杀订单
	this.submitSeckillOrder=function(id){
		return $http.get("./seckillOrder/submitSeckillOrder.do?id=" + id);
	}
});