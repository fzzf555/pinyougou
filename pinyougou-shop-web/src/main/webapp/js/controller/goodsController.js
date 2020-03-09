 //控制层 
app.controller('goodsController' ,function($scope,$controller,$location,goodsService,uploadService,itemCatService,typeTemplateService){	
	
	$controller('baseController',{$scope:$scope});//继承
	
    //读取列表数据绑定到表单中  
	$scope.findAll=function(){
		goodsService.findAll().success(
			function(response){
				$scope.list=response;
			}			
		);
	}    
	
	//分页
	$scope.findPage=function(page,rows){			
		goodsService.findPage(page,rows).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}
	
	//查询实体 
	$scope.findOne=function(){
		//angularjs在封装了$location用来实现页面跳转中参数传递问题
		var id = $location.search()['id'];//获取页面中传出来的多个参数并封装成一个数组，如果穿了多个参数在[]中添加上参数名就OK了,在url传参时？前面要多一个#，这是angularjs本身路由设置的
//		alert(id);
		if(id == null){
			return;
		}
		goodsService.findOne(id).success(
			function(response){
				$scope.entity= response;	
				//向富文本编辑器添加商品介绍
				editor.html($scope.entity.goodsDesc.introduction);
				//商品图片
				$scope.entity.goodsDesc.itemImages=JSON.parse($scope.entity.goodsDesc.itemImages);
				//扩展属性:这里与下面新建扩展属性中的代码重复了，导致数据读取不出来，我们需要对下面的代码进行修改
				$scope.entity.goodsDesc.customAttributeItems=JSON.parse($scope.entity.goodsDesc.customAttributeItems);
				//规格选择
				$scope.entity.goodsDesc.specificationItems=JSON.parse($scope.entity.goodsDesc.specificationItems);
				//转换SKU列表中的规格对象
				for(var i = 0;i < $scope.entity.itemList.length;i++){
					$scope.entity.itemList[i].spec=JSON.parse($scope.entity.itemList[i].spec);
				}
			}
		);			
	}
	
	//保存 
	$scope.save=function(){		
		//从富文本编辑器中提取文本数据
		$scope.entity.goodsDesc.introduction=editor.html();
		var serviceObject;//服务层对象  				
		if($scope.entity.goods.id!=null){//如果有ID
			serviceObject=goodsService.update( $scope.entity ); //修改  
		}else{
			serviceObject=goodsService.add( $scope.entity  );//增加 
		}				
		serviceObject.success(
			function(response){
				if(response.success){
					alert("新增成功");
					location.href='goods.html';
				}else{
					alert(response.message);
				}
			}		
		);				
	}
	
	 
	//批量删除 
	$scope.dele=function(){			
		//获取选中的复选框			
		goodsService.dele( $scope.selectIds ).success(
			function(response){
				if(response.success){
					$scope.reloadList();//刷新列表
					$scope.selectIds=[];
				}						
			}		
		);				
	}
	
	$scope.searchEntity={};//定义搜索对象 
	
	//搜索
	$scope.search=function(page,rows){			
		goodsService.search(page,rows,$scope.searchEntity).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}
	
	
	//上传图片
	$scope.uploadFile=function(){
		uploadService.uploadFile().success(
			function(response){
				if(response.success){
					$scope.image_entity.url= response.message;
				}else{
					alert(response.message);					
				}
			});			
	}
	
	$scope.entity={goods:{},goodsDesc:{itemImages:[],specificationItems:[]}}
	//将当前上传的图片实体存入图片列表
	$scope.add_image_entity=function(){
		$scope.entity.goodsDesc.itemImages.push($scope.image_entity);
	}
	
	//移除图片
	$scope.remove_image_entity=function(index){
		$scope.entity.goodsDesc.itemImages.splice(index,1);
	}
	
	//查询一级分类列表
	$scope.selectItemCat1List=function(){
	      itemCatService.findByParentId(0).success(
	    		 function(response){
	    			 $scope.itemCat1List=response; 
	    		 }
	      );
	}
    
	//查询二级分类，在angularjs中我们通过对绑定变量的值是否修改进行一个监控，也就是说当被监控的变量发生变化时我们会调用
	$scope.$watch('entity.goods.category1Id',function(newValue,oldValue){
		//newValue就是我们修改后的值
		itemCatService.findByParentId(newValue).success(
	    		 function(response){
	    			 $scope.itemCat2List=response; 
	    		 }
	      );
	});
	
	//查询三级分类
	$scope.$watch('entity.goods.category2Id',function(newValue,oldValue){
		//newValue就是我们修改后的值
		itemCatService.findByParentId(newValue).success(
	    		 function(response){
	    			 $scope.itemCat3List=response; 
	    		 }
	      );
	});
	
	//读取模板id
	$scope.$watch('entity.goods.category3Id',function(newValue,oldValue){
		//newValue就是我们修改后的值
		itemCatService.findOne(newValue).success(
	       		  function(response){
	       			    $scope.entity.goods.typeTemplateId=response.typeId; //更新模板ID    
	       		  }
	    );
	});
	
	//读取模板id后获取品牌列表，扩展属性，规格列表
	$scope.$watch('entity.goods.typeTemplateId',function(newValue,oldValue){
		//newValue就是我们修改后的值
		typeTemplateService.findOne(newValue).success(
	       		function(response){
	       			  $scope.typeTemplate=response;//获取类型模板
	       			  $scope.typeTemplate.brandIds= JSON.parse( $scope.typeTemplate.brandIds);//品牌列表
	       			  //扩展属性
	       			  if($location.search()['id'] == null){
	       				  $scope.entity.goodsDesc.customAttributeItems=JSON.parse( $scope.typeTemplate.customAttributeItems);
	       			  }
	       		}
	     );
		
		typeTemplateService.findSpecList(newValue).success(
	       		function(response){
	       			  $scope.specList=response;
	       		}
	     );
	});
	
	//规格列表
	$scope.updateSpecAttribute=function($event,name,value){
		var obj = $scope.searchObjectByKey($scope.entity.goodsDesc.specificationItems,'attributeName',name);
		if(obj!=null){
			if($event.target.checked){
				obj.attributeValue.push(value);
			}else{//取消勾选
				obj.attributeValue.splice(obj.attributeValue.indexOf(value),1);
				if(obj.attributeValue.length==0){
					//如果选项取消勾选了，将此条记录都移除
					$scope.entity.goodsDesc.specificationItems.splice(
							$scope.entity.goodsDesc.specificationItems.indexOf(obj),1);
				}
			}
		}else{
			$scope.entity.goodsDesc.specificationItems.push({"attributeName":name,"attributeValue":[value]})
		}
	}
	
	//创建SKU列表
	$scope.createItemList=function(){	
		
		$scope.entity.itemList=[{spec:{},price:'0',num:'999',status:'0',isDefault:'0'}];//列表初始化
		var items = $scope.entity.goodsDesc.specificationItems;
		for(var i = 0;i < items.length;i++){
			$scope.entity.itemList = addColumn($scope.entity.itemList,items[i].attributeName,items[i].attributeValue);
		}
		
	}
	
	addColumn=function(list,columnName,columnValues){
		var newList=[];
		for(var i = 0;i < list.length;i++){
			var oldRow = list[i];
			for(var j = 0;j < columnValues.length;j++){
				var newRow = JSON.parse(JSON.stringify(oldRow));//实现深克隆，stringify()将json转成字符串，parse()将字符串转成json
				newRow.spec[columnName] = columnValues[j];
				newList.push(newRow);
			}
		}
		return newList;
	}
	
	//我们查询到显示到页面的状态是数据库中存的id，我们声明一个数组，让每个对应值得下标与数据库中相关的id值相同，在页面上可以直接调用
	$scope.status=['未审核','已审核','已驳回','已关闭'];
	
	$scope.isMarketable=['下架商品','上架商品']
	
	$scope.itemCatList=[];//商品分类列表
	//加载商品分类列表
	$scope.findItemCatList=function(){		
		itemCatService.findAll().success(
				function(response){							
					for(var i=0;i<response.length;i++){
						$scope.itemCatList[response[i].id]=response[i].name;
					}
				}
		);
	}
	
	//判断规格与规格选项是否应该被勾选  specName:规格名    optionName：选项名
	$scope.checkAttributeValue=function(specName,optionName){
		var items = $scope.entity.goodsDesc.specificationItems;
		var obj = $scope.searchObjectByKey(items,'attributeName',specName);
		if(obj!=null){
			if(obj.attributeValue.indexOf(optionName) >= 0){//如果在数据库中能查到对应选项
				return true;
			}else{
				return false;
			}
		}else{
			return false;
		}
	}
	
	$scope.makeTable=function(status){
		goodsService.makeTable($scope.selectIds,status).success(
				function(response){
					if(response.success){//成功
						$scope.reloadList();//刷新列表
						$scope.selectIds=[];//清空ID集合
					}else{
						alert(response.message);
					}
				}
		);
			
		
	}
	
});	
