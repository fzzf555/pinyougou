package com.pinyougou.page.service.impl;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.jms.TextMessage;

import org.apache.activemq.filter.function.inListFunction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.pinyougou.page.service.ItemPageService;
/**
 * 监听类：用于生成静态页面
 * @author 丁梦伟
 *
 */
@Component
public class PageDeleteListener implements MessageListener {
	
	@Autowired
	private ItemPageService itemPageService;

	@Override
	public void onMessage(Message message) {
		ObjectMessage objectMessage = (ObjectMessage) message;
		try {
			Long[] ids = (Long[]) objectMessage.getObject();
			System.out.println("接收到消息:" + ids);
			boolean b = itemPageService.deleteItemHtml(ids);
			System.out.println("网页生成结果：" + b);
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
