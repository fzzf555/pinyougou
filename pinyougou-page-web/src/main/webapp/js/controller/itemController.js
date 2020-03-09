app.controller("itemController",function($scope,$http){	
	$scope.specificationItems={};//记录用户选择的规格
	
	//商品数量的加减
	$scope.addNum=function(x){
		$scope.num+=x;
		if($scope.num < 1){
			$scope.num=1;
		}
	}
	
	//用户选择规则
	$scope.selectSpecification=function(key,value){
		$scope.specificationItems[key]=value;
		searchSku();//查询sku
	}
	
	//判断某规格选项是否被用户选中
	$scope.isSelected=function(name,value){
		if($scope.specificationItems[name]==value){
			return true;
		}else{
			return false;
		}		
	}
	
	$scope.sku={};//当前选择的sku
	
	$scope.loadSku=function(){
		$scope.sku=skuList[0];//skuList是在页面生成的，是全局的，不需要加$
		$scope.specificationItems=JSON.parse(JSON.stringify($scope.sku.spec));//深克隆
	}
	
	//匹配两个对象
	matchObject=function(map1,map2){		
		for(var k in map1){
			if(map1[k]!=map2[k]){
				return false;
			}			
		}
		for(var k in map2){
			if(map2[k]!=map1[k]){
				return false;
			}			
		}
		return true;		
	}
	
	//根据规格查找sku
	searchSku=function(){
		for(var i=0;i<skuList.length;i++ ){
			if( matchObject(skuList[i].spec ,$scope.specificationItems ) ){
				$scope.sku=skuList[i];
				return ;
			}			
		}	
		$scope.sku={id:0,title:'--------',price:0};//如果没有匹配的		
	}
	
	//添加商品到购物车
	$scope.addToCart=function(){
//		alert('skuid:'+$scope.sku.id);	
		//{'withCredentials':true}当调用的方法需要操作cookie的时候，我们需要在调用的地址后面加上这个，它的作用是用来携带凭证，供调用的
		//方法进行验证的。
		$http.get('http://localhost:9107/cart/addGoodsToCartList.do?itemId='
				+ $scope.sku.id + '&num=' + $scope.num,{'withCredentials':true}).success(
						function(response){
							if(response.success){
								location.href='http://localhost:9107/cart.html'
							}else{
								alert(response.message);
							}
						}
				);
	}
	
});