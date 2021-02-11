package com.changgou.service.impl;

import com.alibaba.fastjson.JSON;
import com.changgou.dao.ESManagerMapper;
import com.changgou.goods.feign.SkuFeign;
import com.changgou.goods.pojo.Sku;
import com.changgou.search.SkuInfo;
import com.changgou.service.ESManagerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class ESManagerServiceImpl implements ESManagerService {
    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;
    @Autowired
    public ESManagerMapper esManagerMapper;
    @Autowired
    private SkuFeign skuFeign;
    //创建索引库结构
    @Override
    public void createMappingAndIndex() {
        //创建索引
        elasticsearchTemplate.createIndex(SkuInfo.class);
        //创建映射
        elasticsearchTemplate.putMapping(SkuInfo.class);
    }
    //导入全部sku集合进入到索引库
    @Override
    public void importAll() {
        //查询sku集合
        List<Sku> skuList = skuFeign.findSkuListBySpuId("all");
        if (skuList==null||skuList.size()<=0){
            throw  new RuntimeException("当前没有数据被查询到,无法导入索引库");
        }
        //skulist转换为json
        String  jsonSkuList = JSON.toJSONString(skuList);

        //将json转换为skuinfo
        List<SkuInfo> skuInfoList  = JSON.parseArray(jsonSkuList, SkuInfo.class);

        for (SkuInfo skuInfo : skuInfoList) {
            //将规格信息转换为map
            Map specMap  = JSON.parseObject(skuInfo.getSpec(), Map.class);
            skuInfo.setSpecMap(specMap);
        }
        //导入索引库
        esManagerMapper.saveAll(skuInfoList);
    }
    //根据spuid查询skuList,添加到索引库
    @Override
    public void importDataBySpuId(String spuId){
        List<Sku> skuListBySpuId = skuFeign.findSkuListBySpuId(spuId);
        if (skuListBySpuId==null||skuListBySpuId.size()<=0){
            throw  new RuntimeException("当前没有数据被查询到,无法导入索引库");
        }
        //将集合转换为json
        String jsonSkuList  = JSON.toJSONString(skuListBySpuId);
        List<SkuInfo> skuInfoList = JSON.parseArray(jsonSkuList, SkuInfo.class);
        for (SkuInfo skuInfo : skuInfoList) {
            Map map = JSON.parseObject(skuInfo.getSpec(), Map.class);
            skuInfo.setSpecMap(map);
        }
        //添加索引库
        esManagerMapper.saveAll(skuInfoList);
    }
    //根据souid删除es索引库中相关的sku数据
    @Override
    public void delDataBySpuId(String spuId) {
        List<Sku> skuList  = skuFeign.findSkuListBySpuId(spuId);
        if (skuList==null||skuList.size()<=0){
            throw  new RuntimeException("当前没有数据被查询到,无法导入索引库");
        }
        for (Sku sku : skuList) {
            esManagerMapper.deleteById(Long.valueOf(sku.getId()));
        }
    }
}
