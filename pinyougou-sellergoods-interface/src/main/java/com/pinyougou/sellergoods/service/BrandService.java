package com.pinyougou.sellergoods.service;
/**
 * 品牌接口
 * @author 丁梦伟
 *
 */

import java.util.List;
import java.util.Map;

import com.pinyougou.pojo.TbBrand;

import entity.PageResult;

public interface BrandService {
	public List<TbBrand> findAll();
	
	/**
	 * 分页显示
	 * @param pageNum：当前页码
	 * @param pageSize：页面大小
	 * @return
	 */
	public PageResult findPage(int pageNum,int pageSize);
	
	/**
	 * 增加品牌
	 * @param brand：品牌对象
	 */
	public void add(TbBrand brand);
	
	/**
	 * 根据ID查品牌
	 * @param id
	 * @return
	 */
	public TbBrand findOne(Long id);
	
	/**
	 * 修改品牌信息
	 * @param brand
	 */
	public void update(TbBrand brand);
	
	/**
	 * 批量删除品牌
	 * @param ids
	 */
	public void delete(Long[] ids);
	
	/**
	 * 条件分页显示
	 * @param pageNum：当前页码
	 * @param pageSize：页面大小
	 * @return
	 */
	public PageResult findPage(TbBrand brand,int pageNum,int pageSize);
	
	/**
	 * 返回下拉列表数据
	 * @return
	 */
	public List<Map> selectOptionList();
}
