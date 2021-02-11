package com.changgou.service.impl;

import com.alibaba.fastjson.JSON;
import com.changgou.entity.PageResult;
import com.changgou.search.SkuInfo;
import com.changgou.service.SearchService;
import com.sun.org.apache.bcel.internal.generic.IF_ACMPEQ;
import org.apache.commons.lang.StringUtils;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.SearchResultMapper;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.aggregation.impl.AggregatedPageImpl;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.StringQuery;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class SearchServiceImpl implements SearchService {
    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;

    @Override
    public Map search(Map<String, String> searchMap) {
        if (searchMap!=null){
            Map<String,Object> resultMap= new HashMap<>();
            //组合条件对象
            BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
            //0:关键词
            if (StringUtils.isNotEmpty(searchMap.get("keywords"))){
                boolQuery.must(QueryBuilders.matchQuery("name",searchMap.get("keywords")).operator(Operator.AND));
            }
            //1:条件 品牌
            if (StringUtils.isNotEmpty(searchMap.get("brand"))){
                boolQuery.filter(QueryBuilders.termQuery("brandName",searchMap.get("brand")));
            }
            //2:条件 规格
            for (String key : searchMap.keySet()) {
                if (key.startsWith("spec_")){
                    String value = searchMap.get(key).replace("%2B", "+");
                    boolQuery.filter(QueryBuilders.termQuery("specMap."+key.substring(5)+".keyword",value));
                }
            }
            //3:条件 价格
            if (StringUtils.isNotEmpty(searchMap.get("price"))){
                String price = searchMap.get("price");
                String[] split = price.split("-");
                if (split.length==2){
                    boolQuery.filter(QueryBuilders.rangeQuery("price").lte(split[1]));
                }
                boolQuery.filter(QueryBuilders.rangeQuery("price").gte(split[0]));
            }
            //4. 原生搜索实现类
            NativeSearchQueryBuilder nativeSearchQueryBuilder = new NativeSearchQueryBuilder();
            nativeSearchQueryBuilder.withQuery(boolQuery);
            //5:高亮
            HighlightBuilder.Field field= new HighlightBuilder.Field("name")
                    .preTags("<span style='color:red'>")
                    .postTags("</span>");
            nativeSearchQueryBuilder.withHighlightFields(field);
            //6. 品牌聚合(分组)查询
            String skuBrand="skuBrand";
            nativeSearchQueryBuilder.addAggregation(AggregationBuilders.terms(skuBrand).field("brandName"));
            //7. 规格聚合(分组)查询
            String skuSpec="skuSpec";
            nativeSearchQueryBuilder.addAggregation(AggregationBuilders.terms(skuSpec).field("spec.keyword"));
            //8: 排序
            if (StringUtils.isNotEmpty(searchMap.get("sortField"))){
                if ("ASC".equals(searchMap.get("sortRule"))){
                    nativeSearchQueryBuilder.withSort(SortBuilders.fieldSort(searchMap.get("sortField")).order(SortOrder.ASC));
                }else {
                    nativeSearchQueryBuilder.withSort(SortBuilders.fieldSort(searchMap.get("sortField")).order(SortOrder.DESC));
                }
            }
            //9: 分页
            String pageNum = searchMap.get("pageNum");
            if (pageNum==null){
                pageNum="1";
            }
            String pageSize = searchMap.get("pageSize");
            if (pageSize==null){
                pageSize="30";
            }
            nativeSearchQueryBuilder.withPageable(PageRequest.of(Integer.parseInt(pageNum)-1,Integer.parseInt(pageSize)));
            //1: 执行查询, 返回结果对象
            AggregatedPage<SkuInfo> aggregatedPage = elasticsearchTemplate.queryForPage(nativeSearchQueryBuilder.build(), SkuInfo.class, new SearchResultMapper() {
                @Override
                public <T> AggregatedPage<T> mapResults(SearchResponse response, Class<T> clazz, Pageable pageable) {
                    List<T> list= new ArrayList<>();
                    SearchHits hits = response.getHits();
                    if (hits!=null){
                        for (SearchHit hit : hits) {
                            SkuInfo skuInfo = JSON.parseObject(hit.getSourceAsString(), SkuInfo.class);
                            Map<String, HighlightField> highlightFields = hit.getHighlightFields();
                            if (highlightFields!=null&&highlightFields.size()>0){
                                Text[] names = highlightFields.get("name").getFragments();
                               // String name1 = names[1].toString();
                                skuInfo.setName(highlightFields.get("name").getFragments()[0].toString());
                              //  skuInfo.setName(highlightFields.get("name").getFragments().toString())
                            }
                            list.add((T) skuInfo);

                        }
                    }
                    long total = hits.getTotalHits();
                    return new AggregatedPageImpl<T>(list,pageable,total,response.getAggregations());
                }
            });
            resultMap.put("total", aggregatedPage.getTotalElements());
            resultMap.put("totalPage", aggregatedPage.getTotalPages());
            resultMap.put("rows", aggregatedPage.getContent());
            //14. 获取品牌聚合结果
            StringTerms brandTerms= (StringTerms) aggregatedPage.getAggregation(skuBrand);
            List<String> brandList = brandTerms.getBuckets().stream().map(bucket -> bucket.getKeyAsString()).collect(Collectors.toList());
            resultMap.put("brandList",brandList);
            //15. 获取规格聚合结果
            StringTerms specTerms= (StringTerms) aggregatedPage.getAggregation(skuSpec);
            List<String> specList = specTerms.getBuckets().stream().map(bucket -> bucket.getKeyAsString()).collect(Collectors.toList());
            resultMap.put("specList",this.formartSpec(specList));
            //16. 返回当前页
            resultMap.put("pageNum",pageNum);
            return resultMap;
        }
        return null;

    }
//     [
//             "{'颜色': '金色', '版本': '4GB+64GB'}",
//             "{'颜色': '金色'}",
//             "{'颜色': '金色', '版本': '6GB+64GB'}",
//             "{'颜色': '金色', '版本': '6GB+128GB'}",
//             "{'颜色': '金色', '版本': '3GB+32GB'}",
//             "{'颜色': '金色', '版本': '2GB+16GB'}",
//             "{'颜色': '金色', '版本': '4GB+32GB'}",
//             "{'颜色': '金色', '版本': '4GB+128GB'}",
//             "{'颜色': '金色', '版本': '64G'}",
//             "{'颜色': '金色', '版本': '256G'}"
//             ]
//        {
//            "颜色": ["金色","黑色"]
//            '版本': [64G,"4GB+32GB"]
//        }
    public Map<String,Set<String>> formartSpec(List<String> specList){
        Map<String,Set<String>> resultMap= new HashMap<>();
        if (specList!=null&&specList.size()>0){
            for (String specJsonString : specList) {
                //将获取到的json转换为map
                Map<String,String> specMap  = JSON.parseObject(specJsonString, Map.class);
                for (String specKey : specMap.keySet()) {
                    Set<String> specSet  = resultMap.get(specKey);
                    if (specSet==null){
                        specSet= new HashSet<>();
                    }
                    //将规格信息存入set中
                    specSet.add(specMap.get(specKey));
                    //将set存入map
                    resultMap.put(specKey,specSet);
                }
            }
        }
        return resultMap;
    }
}
