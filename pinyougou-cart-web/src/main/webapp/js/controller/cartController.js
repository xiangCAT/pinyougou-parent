app.controller('cartController',function($scope,cartService){
	
	//添加商品到购物车
	$scope.addGoodsToCartList=function(itemId,num){
		cartService.addGoodsToCartList(itemId,num).success(
				function(response){
					if(response.flag){
						$scope.findCartList();
					}else{
						alert(response.message);
					}
				}
		);
	}
	//查询购物车列表
	$scope.findCartList=function(){
		cartService.findCartList().success(
				function(response){
					$scope.cartList=response;
					//计算总价格
					$scope.totalValue = sum($scope.cartList);
				}
		);
	}
	
	sum=function(cartList){
		
		var totalValue={totalNum:0,totalMoney:0.0};
		for(var i=0;i<cartList.length;i++){
			var cart = cartList[i];
			var itemList = cart.orderItemList;
			for(var j=0;j<itemList.length;j++){
				var orderItem = itemList[j];
				totalValue.totalNum += orderItem.num;
				totalValue.totalMoney += orderItem.totalFee;
			}
			
		}
		return totalValue;
	}
	
	
});