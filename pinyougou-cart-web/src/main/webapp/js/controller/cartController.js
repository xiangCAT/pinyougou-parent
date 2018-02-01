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
					$scope.cartList=response;//List<Cart>
					//计算总价格
					$scope.totalValue = sum($scope.cartList);
				}
		);
	}
	
	//计算总价格、总数量
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
	
	//查询地址列表
	$scope.findAddressList=function(){
		cartService.findAddressListByUserId().success(
				function(response){
					$scope.addressList = response;
					
					//默认选中默认地址
					for(var i=0;i<$scope.addressList.length;i++){
						if($scope.addressList[i].isDefault == '1'){
							$scope.selectedAddress =$scope.addressList[i];
						}
					}
					
				}
		);
	}
	
	//选择地址
	$scope.selectAddress=function(address){
		$scope.selectedAddress =address;
	}
	
	//判读地址是否被选中
	$scope.isSelectedAddress=function(address){
		if(address == $scope.selectedAddress){
			return true;
		}
		return false;
	}
	
	//支付方式的选择
	$scope.order = {paymentType:'1'};
	$scope.selectPayment=function(paymentType){
		$scope.order.paymentType=paymentType;
	}
	
	
	//提交订单
	$scope.submitOrder=function(){
		//提交订单，进行表单验证
		
		//补全收货人地址信息
		$scope.order.receiverAreaName=$scope.selectedAddress.address;//地址
		$scope.order.receiverMobile=$scope.selectedAddress.mobile;//手机
		$scope.order.receiver=$scope.selectedAddress.contact;//联系人
		cartService.submitOrder($scope.order).success(
				function(response){
					if(response.flag){
						//成功，跳转到支付页面
						location.href="pay.html";
					}else{
						alert(response.message);
					}
					
				}
		);
	}
});