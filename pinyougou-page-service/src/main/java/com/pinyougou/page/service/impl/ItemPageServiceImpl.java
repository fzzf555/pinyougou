package com.pinyougou.page.service.impl;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfig;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import com.pinyougou.mapper.TbGoodsDescMapper;
import com.pinyougou.mapper.TbGoodsMapper;
import com.pinyougou.mapper.TbItemCatMapper;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.page.service.ItemPageService;
import com.pinyougou.pojo.TbGoods;
import com.pinyougou.pojo.TbGoodsDesc;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbItemCat;
import com.pinyougou.pojo.TbItemCatExample;
import com.pinyougou.pojo.TbItemCatExample.Criteria;
import com.pinyougou.pojo.TbItemExample;

import freemarker.core.ParseException;
import freemarker.template.Configuration;
import freemarker.template.MalformedTemplateNameException;
import freemarker.template.Template;
import freemarker.template.TemplateNotFoundException;


@Service
public class ItemPageServiceImpl implements ItemPageService {

	@Autowired
	private FreeMarkerConfigurer freeMarkerConfigurer;
	
	@Value("${pagedir}")
	private String pagedir;
	
	@Autowired
	private TbGoodsMapper tbGoodsMapper;
	
	@Autowired
	private TbGoodsDescMapper tbGoodsDescMapper;
	
	@Autowired
	private TbItemCatMapper tbItemCatMapper;
	
	@Autowired
	private TbItemMapper tbItemMapper;
	
	@Override
	public boolean genItemHtml(Long goodsId) {
		Configuration configuration = freeMarkerConfigurer.getConfiguration();
		try {
			Template template = configuration.getTemplate("item.ftl");
			//创建数据模型
			Map dataModel = new HashMap<>();
			TbGoods goods = tbGoodsMapper.selectByPrimaryKey(goodsId);//商品主表的数据
			dataModel.put("goods", goods);
			TbGoodsDesc goodsDesc = tbGoodsDescMapper.selectByPrimaryKey(goodsId);//商品扩展表数据
			dataModel.put("goodsDesc", goodsDesc);
			//读取商品分类
			String itemCa1 = tbItemCatMapper.selectByPrimaryKey(goods.getCategory1Id()).getName();
			String itemCa2 = tbItemCatMapper.selectByPrimaryKey(goods.getCategory2Id()).getName();
			String itemCa3 = tbItemCatMapper.selectByPrimaryKey(goods.getCategory3Id()).getName();
			
			dataModel.put("itemCa1", itemCa1);
			dataModel.put("itemCa2", itemCa2);
			dataModel.put("itemCa3", itemCa3);
			
			//4.读取SKU列表
			TbItemExample example = new TbItemExample();
			com.pinyougou.pojo.TbItemExample.Criteria criteria = example.createCriteria();
			criteria.andGoodsIdEqualTo(goodsId);//SPUid
			criteria.andStatusEqualTo("1");//状态有效
			example.setOrderByClause("is_default desc");//按是否默认字段排序进行降序，目的是返回的结果第一条是默认的SKU
			List<TbItem> itemList = tbItemMapper.selectByExample(example);
			dataModel.put("itemList", itemList);
			
			
			Writer out = new FileWriter(pagedir + goodsId + ".html");
			template.process(dataModel, out);//输出
			out.close();
			return true;
			
		} catch (Exception e) {		
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public boolean deleteItemHtml(Long [] goodsIds) {
		try {
			for(Long goodsId:goodsIds){
				new File(pagedir+goodsId+".html").delete();
			}
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
}
