app.controller('payController',function($scope,$location,payService){
	
	//生成二维码
	$scope.createNative=function(){
		payService.createNative().success(
				function(response){
					//生成二维码
					var qr  = new QRious({
						element:document.getElementById('ewm'),
					    size: 250,    
					    value: response.code_url
					  });
					
					$scope.order_no = response.order_no;
					$scope.money = response.money;
					
					//触发查询订单状态的函数
					payService.queryOrderStatus($scope.order_no).success(
							function(response){
								if(response.flag){
									if(response.message == "TIME_OUT"){//超时
										$scope.createNative();//触发重新生成二维码
									}else{
										location.href="paysuccess.html#?money="+$scope.money;
									}
								}else{
									location.href="payfail.html";
								}
							}
					);
				}
		);
	}
	
	//获取支付金额
	$scope.getMoney=function(){
		$scope.money = $location.search()['money'];
	}
	
});