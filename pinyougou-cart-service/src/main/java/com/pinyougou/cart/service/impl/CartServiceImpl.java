package com.pinyougou.cart.service.impl;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.cart.service.CartService;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbOrderItem;
import com.pinyougou.pojogroup.Cart;

@Service
public class CartServiceImpl implements CartService {

	@Autowired
	private TbItemMapper tbItemMapper;
	@Override
	public List<Cart> addGoodsToCartList(List<Cart> cartList, Long itemId, Integer num) {
		//1.根据sku的id查询sku对象
		TbItem item = tbItemMapper.selectByPrimaryKey(itemId);
		if(item==null){
			throw new RuntimeException("商品不存在");
		}
		if(!item.getStatus().equals("1")){
			throw new RuntimeException("商品已下架");
		}
		
		//2.根据sku对象得到商家id
		String sellerId = item.getSellerId();
		
		//3.根据商家id查找购物车列表中是否存在
		Cart cart = searchCartBySellerId(cartList, sellerId);
		
		//4.购物车列表中不存在，
		if(cart == null){
			//4.1创建一个新的购物车对象
			cart = new Cart();
			cart.setSellerId(sellerId);
			cart.setSellerName(item.getSeller());
			List<TbOrderItem> orderItemList=new ArrayList<TbOrderItem>();
			
			TbOrderItem orderItem = createOrderItem(item, num);
			orderItemList.add(orderItem);
			cart.setOrderItemList(orderItemList);
			//4.2将新的购物车对象添加到购物车列表中
			cartList.add(cart);
		}
		//5.购物车列表存在。
		else {
			TbOrderItem orderItem = searchOrderItemByItemId(cart.getOrderItemList(),itemId);
			//判断该商品是否在该购物车明细对象中
			if(orderItem == null){
				//5.1不存在，创建新的购物车明细对象，并添加到购物车对象中
				orderItem = createOrderItem(item,num);
				cart.getOrderItemList().add(orderItem);
			}
			else {
				//5.2存在，在原有的数量上进行修改即可，同时要更新金额
				orderItem.setNum(orderItem.getNum() + num);
				System.out.println(orderItem.getNum());
				orderItem.setTotalFee(new BigDecimal(orderItem.getPrice().doubleValue()*orderItem.getNum()));
				//当商品数量小于等于0，移除
				if(orderItem.getNum() <= 0){
					cart.getOrderItemList().remove(orderItem);
				}
				//当购物车某一商家商品列表为空的时候，移除该商家
				if(cart.getOrderItemList().size()==0){
					cartList.remove(cart);
				}
			}	
		}
		return cartList;
	}
	
	/**
	 * 根据商家id查询购物车列表中是否存在
	 * @param cartList
	 * @param sellelId
	 * @return
	 */
	private Cart searchCartBySellerId(List<Cart> cartList,String sellerId){
		for(Cart cart : cartList){
			if(cart.getSellerId().equals(sellerId))
				return cart;
		}
		return null;
	}
	
	/**
	 * 根据skuid在购物车列表中查询购物车明细对象
	 * @param orderItemList
	 * @param itemId
	 * @return
	 */
	public TbOrderItem searchOrderItemByItemId(List<TbOrderItem> orderItemList,Long itemId){
		for(TbOrderItem orderItem : orderItemList){
			if(orderItem.getItemId().longValue() == itemId.longValue())
				return orderItem;
		}
		return null;
	}
	
	/**
	 * 创建购物车明细对象
	 * @param item
	 * @param num
	 * @return
	 */
	private TbOrderItem createOrderItem(TbItem item,Integer num){
		if(num <= 0){
			throw new RuntimeException("数量非法");
		}
		//创建新的购物车明细对象
		TbOrderItem orderItem = new TbOrderItem();
		orderItem.setGoodsId(item.getGoodsId());
		orderItem.setItemId(item.getId());
		orderItem.setNum(num);
		orderItem.setPicPath(item.getImage());
		orderItem.setPrice(item.getPrice());
		orderItem.setSellerId(item.getSellerId());
		orderItem.setTitle(item.getTitle());
		orderItem.setTotalFee(new BigDecimal(item.getPrice().doubleValue()*num));
		
		return orderItem;
	}

	@Autowired
	private RedisTemplate redisTemplate;
	
	@Override
	public List<Cart> findCartListFromRedis(String userName) {
		System.out.println("从redis读取购物车");
		List<Cart> cartList = (List<Cart>) redisTemplate.boundHashOps("cartList").get(userName);
		if(cartList == null){
			cartList = new ArrayList<>();
		}
		return cartList;
	}

	@Override
	public void saveCartListToRedis(String userName, List<Cart> cartList) {
		System.out.println("向redis存入购物车");
		redisTemplate.boundHashOps("cartList").put(userName, cartList);
	}

	@Override
	public List<Cart> mergeCartList(List<Cart> cartList1, List<Cart> cartList2) {
		//cartList1.addAll(cartList2);//不能简单合并，不然会有很多重复记录
		for( Cart cart : cartList2){
			for(TbOrderItem item : cart.getOrderItemList()){
				cartList1 = addGoodsToCartList(cartList1, item.getItemId(), item.getNum());
			}
		}
		
		return cartList1;
	}
	
	
}
