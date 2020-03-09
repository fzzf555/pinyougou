app.controller('brandController',function($scope,$controller,brandService){
	
	$controller('baseController',{$scope:$scope});//伪继承，并不是真正意义上的继承，加上这句话是让两个controller的scope共通
	
	//查询数据
	$scope.findAll = function(){
		brandService.findAll().success(
				//参数可以随意起
			function(response){
				$scope.list = response;
			}		
		);
	}
	
	//分页
	$scope.findPage=function(page,size){	
		brandService.findPage(page,size).success(
			function(response){
				$scope.list=response.rows;	//显示当前页数据
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}
	
	//查询实体
	$scope.findOne=function(id){
		brandService.findOne(id).success(
			function(response){
				$scope.entity=response;
			}		
		);
	}
	
	//添加品牌
	$scope.save=function(){
		var obj = null;
		if($scope.entity.id!=null){
			obj = brandService.update($scope.entity);
		}else{
			obj = brandService.add($scope.entity);
		}
		obj.success(
			function(response){
				if(response.success){
					$scope.reloadList();//刷新
				}else{
					alert(response.message);
				}
			}		
		);
	}
	
	//批量删除
	$scope.del=function(){
		if(confirm('确定要删除吗？')){
			brandService.del($scope.selectIds).success(
				function(response){
					if(response.success){
						$scope.reloadList();
					}else{
						alert(response.message);
					}
				}		
			);
		}
	}
	
	$scope.searchEntity={};
	
	//分页条件查询
	$scope.search=function(page,size){
		brandService.search(page,size,$scope.searchEntity).success(
			function(response){
				$scope.list=response.rows;	//显示当前页数据
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}
});