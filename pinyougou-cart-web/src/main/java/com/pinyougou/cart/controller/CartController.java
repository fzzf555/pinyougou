package com.pinyougou.cart.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.pinyougou.cart.service.CartService;
import com.pinyougou.pojogroup.Cart;

import entity.Result;

@RestController
@RequestMapping("/cart")
public class CartController {
	@Autowired
	private HttpServletRequest request;
	
	@Autowired
	private HttpServletResponse response;
	
	@Reference(timeout=6000)
	private CartService cartService;
	
	@RequestMapping("/findCartList")
	public List<Cart> findCartList(){
		//当前登录的账号
		String userName = SecurityContextHolder.getContext().getAuthentication().getName();
		System.out.println("登录用户名：" + userName);
		String cartListString = util.CookieUtil.getCookieValue(request, "cartList", "UTF-8");
		if(cartListString==null || cartListString.equals("")){
			cartListString="[]";
		}
		List<Cart> cartList_cookie = JSON.parseArray(cartListString,Cart.class);
		if(userName.equals("anonymousUser")){//未登录的话会默认获取的用户名为anonymousUser
			//从Cookie中提取购物车
			System.out.println("从cookie中提取购物车");
			
			return cartList_cookie;
		}else {//已登录
			List<Cart> cartList_redis = cartService.findCartListFromRedis(userName);
			if(cartList_cookie.size() > 0){
				//合并购物车
				List<Cart> cartList = cartService.mergeCartList(cartList_cookie, cartList_redis);
				//合并后的购物车存入redis
				cartService.saveCartListToRedis(userName, cartList);
				//清除掉cookie中的购物车，避免重复添加
				util.CookieUtil.deleteCookie(request, response, "cartList");
				return cartList;
			}
			
			return cartList_redis;
		}
		
		
	}
	
	@RequestMapping("/addGoodsToCartList")
	//注解版本的跨域请求，allowCredentials="true"可以缺省，注意，只能够在springMVC4.2版本以后使用
	@CrossOrigin(origins="http://localhost:9105",allowCredentials="true")
	public Result addGoodsToCartList(Long itemId,Integer num){
//		//设置可以访问的域（当此方法不需要操作cookie的时候，只需要这一句话就够了）
//		response.addHeader("Access-Control-Allow-Origin", "http://localhost:9105");
//		//允许使用cookie，如果要操作cookie的话，必须加上这个凭证，同时如果方法需要操作cookie的时候，那么上面的这句话允许访问的域必须是具体
//		//的，而不能是*
//		response.setHeader("Access-Control-Allow-Credentials", "true");
		
		String name = SecurityContextHolder.getContext().getAuthentication().getName();
		System.out.println("登录用户名：" + name);
		try {
			//当前登录的账号
			//提取购物车
			List<Cart> cartList = findCartList();
			
			//调用服务方法操作购物车
			cartList = cartService.addGoodsToCartList(cartList, itemId, num);
			if(name.equals("anonymousUser")){//未登录的话会默认获取的用户名为anonymousUser
				//将新的购物车存入Cookie
				System.out.println(JSON.toJSONString(cartList));
				util.CookieUtil.setCookie(request, response, "cartList", JSON.toJSONString(cartList), 3600*24, "UTF-8");		
				System.out.println("向cookie中存入购物车");
			}else {
				cartService.saveCartListToRedis(name, cartList);
			}
			return new Result(true, "存入成功");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return new Result(false, "存入失败");
		}
	}
}
