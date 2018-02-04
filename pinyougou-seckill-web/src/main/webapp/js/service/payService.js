app.service('payService',function($http){
	
	//生成二维码 
	this.createNative=function(){
		return $http.get('./pay/createNative.do');
	}
	//查询订单状态
	this.queryOrderStatus=function(order_no){
		return $http.get('./pay/queryOrderStatus.do?order_no='+order_no);
	}
});