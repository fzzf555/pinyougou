package com.pinyougou.manager.controller;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.TbBrand;
import com.pinyougou.sellergoods.service.BrandService;

import entity.Result;
import entity.PageResult;

@RestController//@RestController = @ResponseBody + @Controller
@RequestMapping("/brand")
public class BrandController {
	@Reference
	private BrandService brandService;
	
	@RequestMapping("/findAll")
	public List<TbBrand> findAll(){
		return brandService.findAll();
	}
	
	@RequestMapping("/findPage")
	public PageResult findPage(int page,int size){
		return brandService.findPage(page, size);
	}
	
	@RequestMapping("/add")
	//springMVC中传递对象的话需要加上@RequestBody
	public Result add(@RequestBody TbBrand brand){
		try {
			brandService.add(brand);
			return new Result(true, "增加成功-v-");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "增加失败QAQ");
		}
	}
	
	@RequestMapping("/findOne")
	public TbBrand findOne(Long id){
		return brandService.findOne(id);
	}
	
	@RequestMapping("/update")
	public Result update(@RequestBody TbBrand brand){
		try {
			brandService.update(brand);
			return new Result(true, "修改成功-v-");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "修改失败QAQ");
		}
	}
	
	@RequestMapping("/delete")
	public Result delete(Long[] ids){
		try {
			brandService.delete(ids);
			return new Result(true, "删除成功-v-");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "删除失败QAQ");
		}
	}
	
	@RequestMapping("/search")
	public PageResult search(@RequestBody TbBrand brand,int page,int size){
		return brandService.findPage(brand, page, size);
	}
	
	@RequestMapping("/selectOptionList")
	public List<Map> selectOptionList(){
		return brandService.selectOptionList();
	}
}
