//品牌服务
app.service("brandService",function($http){
	this.findAll=function(){
		return $http.get('../brand/findAll.do');
	}
	//分页
	this.findPage=function(page,size){
		return $http.get('../brand/findPage.do?page='+page+'&size='+size);
	}
	//查找实体
	this.findOne=function(id){
		return $http.get('../brand/findOne.do?id=' + id);
	}
	//增加
	this.add=function(entity){
		return $http.post('../brand/add.do',entity);
	}
	//修改
	this.update=function(entity){
		return $http.post('../brand/update.do',entity);
	}
	//删除
	this.del=function(selectIds){
		return $http.get('../brand/delete.do?ids='+selectIds);
	}
	//分页查询
	this.search=function(page,size,searchEntity){
		return $http.post('../brand/search.do?page='+page+'&size='+size,searchEntity);
	}
	//下拉列表
	this.selectOptionList=function(){
		return $http.get('../brand/selectOptionList.do');
	}
});