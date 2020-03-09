app.controller('searchController',function($scope,$location,searchService){
	
	//定义搜索对象的结构
	$scope.searchMap={'keywords':'','category':'','brand':'','spec':{},'price':'','pageNo':1,'pageSize':40,'sort':'','sortField':''};
	
	$scope.search=function(){
		//将字符串转成int，后台我们接受到的pageNo转成Integer类型了。而跳转页从文本框获取的页码传到后台是String，会出现转换异常，所以在执行
		//搜索之前，把它转成int
		$scope.searchMap.pageNo=parseInt($scope.searchMap.pageNo);
		searchService.search( $scope.searchMap ).success(
				function(response){						
					$scope.resultMap=response;//搜索返回的结果						
					buildPageLable();////构建分页标签
					//alert($scope.resultMap.totalPages);
				}
		);	
	}
	
	//构建分页标签
	buildPageLable=function(){
		$scope.pageLable=[];
		var firstPage=1;
		var lastPage=$scope.resultMap.totalPages;
		$scope.firstDot=true;//前面有点
		$scope.lastDot=true;//后面有点
		if($scope.resultMap.totalPages > 5){//页码数量大于5
			if($scope.searchMap.pageNo <= 3){//显示前五页
				$scope.firstDot=false;//前面没点
				lastPage=5;
			}else if($scope.searchMap.pageNo >= $scope.resultMap.totalPages-2){//显示后五页
				$scope.lastDot=false;//后面没点
				firstPage=$scope.resultMap.totalPages-4;
			}else{//显示以当前页为中心的5页
				firstPage=$scope.searchMap.pageNo-2;
				lastPage=$scope.searchMap.pageNo+2;
			}
		}else{
			$scope.firstDot=false;//前面没点
			$scope.lastDot=false;//后面没点
		}
		
		for(var i = firstPage;i <= lastPage;i++){
			$scope.pageLable.push(i);
		}
		
	}
	
	//添加搜索项
	$scope.addSearchItem=function(key,value){
		 if(key=='category'||key=='brand'||key=='price'){//如果用户点击的是分类或者品牌
			 $scope.searchMap[key]=value;
		 }else{//点击的是规格
			 $scope.searchMap.spec[key]=value;
		 }
		 $scope.search();
	}
	
	//撤销搜索项
	$scope.removeSearchItem=function(key){
		 if(key=='category'||key=='brand'||key=='price'){//如果用户点击的是分类或者品牌
			 $scope.searchMap[key]="";
		 }else{//点击的是规格,这里定义的时候是一个对象，如果移除要把整个key移除，可以使用delete
			 delete $scope.searchMap.spec[key];
		 }
		 $scope.search();
	}
	
	//分页查询
	$scope.queryByPage=function(pageNo){
		if(pageNo<1 || pageNo>$scope.resultMap.totalPages){
			return;
		}
		$scope.searchMap.pageNo=pageNo;
		$scope.search();
	}
	
	//判断当前页为第一页
	$scope.isTopPage=function(){
		return $scope.searchMap.pageNo==1;	
	}
	
	//判断当前页是否未最后一页
	$scope.isEndPage=function(){
		return $scope.searchMap.pageNo==$scope.resultMap.totalPages;
	}
	
	//排序查询
	$scope.sortSearch=function(sortField,sort){
		$scope.searchMap.sortField=sortField;
		$scope.searchMap.sort=sort;
		$scope.search();
	}
	
	//判断关键字是否是品牌
	$scope.keywordsIsBrand=function(){
		for(var i = 0;i < $scope.resultMap.brandList.length;i++){
			if($scope.searchMap.keywords.indexOf($scope.resultMap.brandList[i].text)>=0){
				return true;
			}
		}
		return false;
	}
	
	//加载首页传递的关键字
	$scope.loadkeywords=function(){
		$scope.searchMap.keywords = $location.search()['keywords'];
		$scope.search();
	}
});