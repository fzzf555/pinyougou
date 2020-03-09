package com.pinyougou.search.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.Criteria;
import org.springframework.data.solr.core.query.FilterQuery;
import org.springframework.data.solr.core.query.GroupOptions;
import org.springframework.data.solr.core.query.HighlightOptions;
import org.springframework.data.solr.core.query.HighlightQuery;
import org.springframework.data.solr.core.query.Query;
import org.springframework.data.solr.core.query.SimpleFilterQuery;
import org.springframework.data.solr.core.query.SimpleHighlightQuery;
import org.springframework.data.solr.core.query.SimpleQuery;
import org.springframework.data.solr.core.query.SolrDataQuery;
import org.springframework.data.solr.core.query.result.GroupEntry;
import org.springframework.data.solr.core.query.result.GroupPage;
import org.springframework.data.solr.core.query.result.GroupResult;
import org.springframework.data.solr.core.query.result.HighlightEntry;
import org.springframework.data.solr.core.query.result.HighlightEntry.Highlight;
import org.springframework.data.solr.core.query.result.HighlightPage;
import org.springframework.data.solr.core.query.result.ScoredPage;

import com.alibaba.dubbo.config.annotation.Service;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.search.service.ItemSearchService;

@Service(timeout = 5000) // 服务端调用solr的时候有可能会超过一秒，而我们的dubbo默认调用时间是一秒钟，不设置超时时间的话会报调用服务超时错误
// 如果服务端和消费端都配置了超时的话，那么以消费端为主，服务端的配置就会失效
public class ItemSearchServiceImpl implements ItemSearchService {

	@Autowired
	private SolrTemplate solrTemplate;

	@Override
	public Map search(Map searchMap) {
		Map map = new HashMap<>();
		
		//空格处理
		String keywords = (String)searchMap.get("keywords");
		searchMap.put("keywords", keywords.replace(" ", ""));//关键字去空格

		/*
		 * Query query = new SimpleQuery("*:*"); Criteria criteria = new
		 * Criteria("item_keywords").is(searchMap.get("keywords"));
		 * query.addCriteria(criteria );
		 * 
		 * ScoredPage<TbItem> page = solrTemplate.queryForPage(query ,
		 * TbItem.class); map.put("rows", page.getContent());
		 */

		//1.查询列表
		map.putAll(searchList(searchMap));
		//2.分组查询商品分类列表
		List<String> categorylist = searchCategoryList(searchMap);
		map.put("categoryList", categorylist);
		//3.根据商品分类名称获取规格和品牌列表	
		String category = (String) searchMap.get("category");
		if(!category.equals("")){
			map.putAll(searchBrandAndSpecList(category));//有多个分类名称默认取第一个来使用
		}else{
			if (categorylist.size() > 0) {
				map.putAll(searchBrandAndSpecList(categorylist.get(0)));//有多个分类名称默认取第一个来使用
			}
		}
			
		return map;
	}

	private Map searchList(Map searchMap) {
		Map map = new HashMap<>();
		// 高亮选项初始化
		HighlightQuery query = new SimpleHighlightQuery();
		HighlightOptions highlightOptions = new HighlightOptions().addField("item_title");// 高亮域
		highlightOptions.setSimplePrefix("<em style='color:red'>");// 前缀
		highlightOptions.setSimplePostfix("</em>");// 后缀
		query.setHighlightOptions(highlightOptions);// 未查询对象设置高亮选项
		
		//1.1关键字查询
		Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
		query.addCriteria(criteria);
		
		//1.2按照商品分类筛选
		if(!searchMap.get("category").equals("")){
			FilterQuery filterQuery = new SimpleFilterQuery();//构建过滤查询
			Criteria filter = new Criteria("item_category").is(searchMap.get("category"));
			filterQuery.addCriteria(filter);
			query.addFilterQuery(filterQuery);
		}
		
		//1.3按照品牌筛选
		if(!searchMap.get("brand").equals("")){
			FilterQuery filterQuery = new SimpleFilterQuery();//构建过滤查询
			Criteria filter = new Criteria("item_brand").is(searchMap.get("brand"));
			filterQuery.addCriteria(filter);
			query.addFilterQuery(filterQuery);
		}
		
		//1.4按照规格筛选
		if(searchMap.get("spec")!=null){
			Map<String,String> specMap= (Map) searchMap.get("spec");
			for(String key:specMap.keySet() ){
				FilterQuery filterQuery = new SimpleFilterQuery();//构建过滤查询
				Criteria filter = new Criteria("item_spec_"+key).is(specMap.get(key));
				filterQuery.addCriteria(filter);
				query.addFilterQuery(filterQuery);
			}
		}
		
		//1.5按价格过滤
		if(!"".equals(searchMap.get("price"))){
			String[] price = ((String) searchMap.get("price")).split("-");//500-1000
			if(!price[0].equals("0")){//如果最低价格不等于0
				FilterQuery filterQuery = new SimpleFilterQuery();//构建过滤查询
				Criteria filter = new Criteria("item_price").greaterThanEqual(price[0]);
				filterQuery.addCriteria(filter);
				query.addFilterQuery(filterQuery);
			}
			if(!price[1].equals("*")){//如果最高价格不等于*
				FilterQuery filterQuery = new SimpleFilterQuery();//构建过滤查询
				Criteria filter = new Criteria("item_price").lessThanEqual(price[1]);
				filterQuery.addCriteria(filter);
				query.addFilterQuery(filterQuery);
			}
		}
		
		//1.6分页
		Integer pageNo = (Integer)searchMap.get("pageNo");//获取页码
		if(pageNo == null){
			pageNo = 1;
		}
		
		Integer pageSize = (Integer)searchMap.get("pageSize");//获取页码
		if(pageSize == null){
			pageSize = 20;
		}
		
		query.setOffset((pageNo-1)*pageSize);//起始索引
		query.setRows(pageSize);
		
		
		//1.7排序
		String sortValue = (String)searchMap.get("sort");
		String sortField = (String)searchMap.get("sortField");
		if(sortValue!=null&& !sortValue.equals("")){
			if(sortValue.equals("ASC")){
				Sort sort = new Sort(Sort.Direction.ASC,"item_"+sortField);
				query.addSort(sort);
			}
			if(sortValue.equals("DESC")){
				Sort sort = new Sort(Sort.Direction.DESC,"item_"+sortField);
				query.addSort(sort);
			}
		}
		
		
		/************获取高亮结果集**************/
		// 高亮页对象
		HighlightPage<TbItem> page = solrTemplate.queryForHighlightPage(query, TbItem.class);
		// 高亮入口集合（每个元素就是每条记录的高亮入口）
		List<HighlightEntry<TbItem>> entryList = page.getHighlighted();
		for (HighlightEntry<TbItem> entry : entryList) {
			// 获取高亮列表（取决于高亮域的多少）
			List<Highlight> highList = entry.getHighlights();
			/*
			 * for(Highlight highlight : highList){ List<String> sns =
			 * highlight.getSnipplets();//每个域可能存储多个值 System.out.println(sns); }
			 */
			if (highList.size() > 0 && highList.get(0).getSnipplets().size() > 0) {
				TbItem entity = entry.getEntity();
				entity.setTitle(highList.get(0).getSnipplets().get(0));
			}
		}
		map.put("rows", page.getContent());
		map.put("totalPages", page.getTotalPages());//设置总页数
		map.put("total", page.getTotalElements());//设置总记录数
		return map;
	}
	
	/**
	 * 分组查询（查询商品分类列表）
	 * @return
	 */
	private List<String> searchCategoryList(Map searchMap){
		List<String> list = new ArrayList<>();
		Query query = new SimpleQuery("*:*");
		//关键字查询 相当于sql语句中的where
		Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
		query.addCriteria(criteria);
		//设置分组选项
		GroupOptions groupOptions = new GroupOptions().addGroupByField("item_category");//group by
		query.setGroupOptions(groupOptions );
		//获取分组页
		GroupPage<TbItem> page = solrTemplate.queryForGroupPage(query , TbItem.class);
		//获取分组结果对象
		GroupResult<TbItem> groupResult = page.getGroupResult("item_category");
		//获取分组入口对象
		Page<GroupEntry<TbItem>> groupEntries = groupResult.getGroupEntries();
		//获取分组入口集合
		List<GroupEntry<TbItem>> entryList = groupEntries.getContent();
		for(GroupEntry<TbItem> entry : entryList){
			list.add(entry.getGroupValue());//获取分组结果并添加到集合中
		}
		return list;
	}
	
	@Autowired
	private RedisTemplate redisTemplate;

	/**
	 * 根据商品分类名称去查询品牌和规格列表
	 * @param category 商品分类名称
	 * @return
	 */
	private Map searchBrandAndSpecList(String category){
		Map map = new HashMap<>();
		//1.根据商品分类名称来得到模板id
		Long templateId = (Long) redisTemplate.boundHashOps("itemCat").get(category);
		if (templateId != null) {
			//2.根据模板id获取品牌列表
			List brandList = (List) redisTemplate.boundHashOps("brandList").get(templateId);
			map.put("brandList", brandList);
			//3.根据模板id获取规格列表
			List specList = (List) redisTemplate.boundHashOps("specList").get(templateId);
			map.put("specList",specList);
		}		
		return map;
	}

	@Override
	public void importList(List list) {
		// TODO Auto-generated method stub
		solrTemplate.saveBeans(list);
		solrTemplate.commit();
	}

	@Override
	public void deleteByGoodsIds(List goodsIds) {
		// TODO Auto-generated method stub
		SolrDataQuery query = new SimpleQuery("*:*");
		Criteria criteria = new Criteria("item_goodsid").in(goodsIds);
		query.addCriteria(criteria);
		solrTemplate.delete(query);
		solrTemplate.commit();
	}

	
}
