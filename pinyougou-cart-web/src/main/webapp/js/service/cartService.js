app.service('cartService',function($http){
	
	//添加商品到购物车列表
	this.addGoodsToCartList=function(itemId,num){
		return $http.get('./cart/addGoodsToCartList.do?itemId='+itemId+'&num='+num);
	}
	
	//查询购物车列表
	this.findCartList=function(){
		return $http.get("./cart/findCartList.do");
	}
});