app.controller('seckillController',function($scope,$location,$interval,seckillService){
	//查询秒杀商品列表
	$scope.findSeckillList=function(){
		seckillService.findSeckillList().success(
				function(response){
					$scope.seckillList=response;
				}
		);
	}
	
	//查询商品详情
	$scope.findOne=function(){
		var id = $location.search()['id'];
		seckillService.findSeckillGoods(id).success(
				function(response){
					$scope.seckillGoods = response;
					
					//读秒操作 
					allsecond =Math.floor( (  new Date($scope.seckillGoods.endTime).getTime()- (new Date().getTime())) /1000); //总秒数
					var time = $interval(function(){
						if(allsecond > 0){
							allsecond = allsecond -1;
							$scope.timeTitle = convertTimeString(allsecond);//转换时间字符串
						}else{
							alert("秒杀结束");
							$interval.cancel(time);//清除读秒操作
						}
					},1000);
				}
		);
	}
	
	//转换时间
	convertTimeString=function(allsecond){
		var days= Math.floor( allsecond/(60*60*24));//天数
		var hours= Math.floor( (allsecond-days*60*60*24)/(60*60) );//小时数
		var minutes= Math.floor(  (allsecond -days*60*60*24 - hours*60*60)/60    );//分钟数
		var seconds= allsecond -days*60*60*24 - hours*60*60 -minutes*60; //秒数
		var timeString="";
		if(days>0){
			timeString=days+"天 ";
		}
		return timeString+hours+":"+minutes+":"+seconds;

		
	}
	
	//提交秒杀订单
	$scope.submitSeckillOrder=function(id){
		seckillService.submitSeckillOrder(id).success(
				function(response){
					if(response.flag){
						location.href="pay.html";
					}else{
						alert("秒杀失败!");
					}
				}
		);
	}
	
	
	
});