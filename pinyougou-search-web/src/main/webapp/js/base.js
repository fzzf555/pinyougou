//定义模块
var app = angular.module('pinyougou',[]);//定义品优购模块
//定义过滤器
app.filter('trustHtml',['$sce',function($sce){
	return function(data){//传入需要被过滤的内容
		return $sce.trustAsHtml(data);//返回过滤后的内容（信任html的转换，anglarjs默认不信任，防止html攻击）
	}
}]);