package com.pinyougou.sellergoods.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbBrandMapper;
import com.pinyougou.pojo.TbBrand;
import com.pinyougou.pojo.TbBrandExample;
import com.pinyougou.pojo.TbBrandExample.Criteria;
import com.pinyougou.sellergoods.service.BrandService;

import entity.PageResult;

@Service
@Transactional
public class BrandServiceImpl implements BrandService {
	//这里调用的是本地的，所以不用@Reference
	@Autowired
	private TbBrandMapper brandMapper;
	
	@Override
	public List<TbBrand> findAll() {

		return brandMapper.selectByExample(null);
	}

	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		
		PageHelper.startPage(pageNum, pageSize);//mybatis分页
		
		Page<TbBrand> pageData = (Page<TbBrand>) brandMapper.selectByExample(null);
		
		PageResult pResult = new PageResult(pageData.getTotal(), pageData.getResult());
		
		return pResult;
	}

	@Override
	public void add(TbBrand brand) {
		brandMapper.insert(brand);	
	}

	@Override
	public TbBrand findOne(Long id) {
		
		return brandMapper.selectByPrimaryKey(id);
	}

	@Override
	public void update(TbBrand brand) {
		brandMapper.updateByPrimaryKey(brand);
		
	}

	@Override
	public void delete(Long[] ids) {
		for(Long id : ids){
			brandMapper.deleteByPrimaryKey(id);
		}
	}

	@Override
	public PageResult findPage(TbBrand brand, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);//mybatis分页
		
		TbBrandExample example = new TbBrandExample();
		
		//mybatis条件拼接
		if(brand!=null){
			Criteria criteria = example.createCriteria();
			if(brand.getName()!=null && brand.getName()!="")
				criteria.andNameLike("%"+brand.getName()+"%");
			if(brand.getFirstChar()!=null && brand.getFirstChar()!="")
				criteria.andFirstCharLike("%"+brand.getFirstChar()+"%");
		}
		
		Page<TbBrand> pageData = (Page<TbBrand>) brandMapper.selectByExample(example);
		
		PageResult pResult = new PageResult(pageData.getTotal(), pageData.getResult());
		
		return pResult;
	}

	@Override
	public List<Map> selectOptionList() {
		// TODO Auto-generated method stub
		return brandMapper.selectOptionList();
	}

}
