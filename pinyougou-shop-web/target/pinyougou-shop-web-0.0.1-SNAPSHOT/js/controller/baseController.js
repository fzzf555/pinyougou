app.controller('baseController',function($scope){
	//分页控件配置 currentPage:当前页码   totalItems:总记录数  itemsPerPage:每页记录数  perPageOptions:分页选项   onChange：当页码重新变更后自动触发的方法
	$scope.paginationConf = {
		currentPage: 1,
		totalItems: 10,
		itemsPerPage: 10,
		perPageOptions: [10, 20, 30, 40, 50],
		onChange: function(){
			$scope.reloadList();//重新加载
		}
	}; 
	
	//刷新列表
	$scope.reloadList=function(){
		//切换页码  
		$scope.search( $scope.paginationConf.currentPage, $scope.paginationConf.itemsPerPage);
	}
	
	//用户勾选的要删除的元素的id集合
	$scope.selectIds=[];//用户勾选的id集合
	$scope.updateSelection=function($event,id){
		if($event.target.checked){
			$scope.selectIds.push(id);//push方法向集合中添加元素
		}else{
			var index=$scope.selectIds.indexOf(id);//查找值得位置
			$scope.selectIds.splice(index,1);//参数一：移除的元素位置  参数二：移除的元素个数
		}	
	}
	
	 //JSON转换成String
	$scope.jsonToString=function(jsonString,key){
		var json=JSON.parse(jsonString);//将json字符串转换为json对象
		var value="";
		for(var i=0;i<json.length;i++){		
			if(i>0){
				value+=","
			}
			//var a={"id":1,"text":"联想"} ===>可以写成a[id]
			value+=json[i][key];//这句话就是提取出json对象中每一个text的值		
		}
		return value;
	}
	
	//在List集合中根据某key的值查询对象
	$scope.searchObjectByKey=function(list,key,keyValue){
		for(var i=0;i<list.length;i++){	
			if(list[i][key]==keyValue){
				return list[i];
			}
		}
		return null;
	}
});