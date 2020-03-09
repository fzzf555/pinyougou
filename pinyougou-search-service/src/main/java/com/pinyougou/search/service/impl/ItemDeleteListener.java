package com.pinyougou.search.service.impl;

import java.util.Arrays;
import java.util.List;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.jms.TextMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.search.service.ItemSearchService;

@Component
public class ItemDeleteListener implements MessageListener {

	@Autowired
	private ItemSearchService ItemSearchService;
	
	@Override
	public void onMessage(Message message) {
		ObjectMessage objectMessage = (ObjectMessage)message;
		try {
			Long[] goodsIds = (Long[]) objectMessage.getObject();//json字符串
			System.out.println("收到到消息:" + goodsIds);
			ItemSearchService.deleteByGoodsIds(Arrays.asList(goodsIds));
			System.out.println("移除索引库中的内容");
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
