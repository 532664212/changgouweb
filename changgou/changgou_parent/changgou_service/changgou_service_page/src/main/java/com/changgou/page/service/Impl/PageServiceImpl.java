package com.changgou.page.service.Impl;

import com.alibaba.fastjson.JSON;
import com.changgou.entity.Result;
import com.changgou.goods.feign.CategoryFeign;
import com.changgou.goods.feign.SkuFeign;
import com.changgou.goods.feign.SpuFeign;
import com.changgou.goods.pojo.Category;
import com.changgou.goods.pojo.Sku;
import com.changgou.goods.pojo.Spu;
import com.changgou.page.service.PageService;
import com.sun.javafx.collections.MappingChange;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PageServiceImpl implements PageService {
    @Autowired
    private TemplateEngine templateEngine;
    @Value("${pagepath}")
    private String pagepath;

    @Override
    public void generateItemPage(String spuId) {
        //获取context对象,用于存放商品详情数据
        Context context = new Context();
        Map<String, Object> itemData = findItemData(spuId);
        context.setVariables(itemData);
        File dir = new File(pagepath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        //定义输出流,进行文件生成
        File file = new File(dir + "/" + spuId + ".html");
        Writer out= null;
        try{
            //生成文件
            /**
             * 1.模板名称
             * 2.context对象,包含了模板需要的数据
             * 3.输出流,指定文件输出位置
             */
            out=new PrintWriter(file);
            templateEngine.process("item", context, out);
        }catch (Exception e){
            e.printStackTrace();
            //记录日志
        }finally {
            try {
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
    @Autowired
    private SpuFeign spuFeign;
    @Autowired
    private  SkuFeign skuFeign;
    @Autowired
    private CategoryFeign categoryFeign;

    private Map<String, Object> findItemData(String spuId) {
        Map<String, Object> resultMap = new HashMap<>();
        //获取spu信息
        Result<Spu> spuResult  = spuFeign.findSpuById(spuId);
        Spu spu= spuResult.getData();
        resultMap.put("spu",spu);

        //获取图片信息
        if (spu!=null){
            if (StringUtils.isNotEmpty(spu.getImages())){
                resultMap.put("imageList",spu.getImages().split(","));
            }
        }
        //获取分类信息
//        Category category1 = (Category) categoryFeign.findById(spu.getCategory1Id()).getData();
        Category category1 = categoryFeign.findById(spu.getCategory1Id()).getData();
        resultMap.put("category1",category1);
        Category category2 =  categoryFeign.findById(spu.getCategory2Id()).getData();
        resultMap.put("category2",category2);
        Category category3 =  categoryFeign.findById(spu.getCategory3Id()).getData();
        resultMap.put("category3",category3);
        //获取sku集合信息
        List<Sku> skuList = skuFeign.findSkuListBySpuId(spuId);
        resultMap.put("skuList",skuList);

        resultMap.put("specificationList",JSON.parseObject(spu.getSpecItems(),Map.class));
        return resultMap;
    }
}
