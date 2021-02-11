package com.changgou.controller;


import com.changgou.entity.Page;
import com.changgou.search.SkuInfo;
import com.changgou.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Set;

@Controller
@RequestMapping("/search")
public class SearchController {
    @Autowired
    private SearchService searchService;


    /**
     * 全文检索
     * @return
     */

    @GetMapping
    @ResponseBody
    public Map search(@RequestParam Map<String,String> searchMap){
        //特殊符号处理
        handlerSearchMap(searchMap);
        Map resultMap= searchService.search(searchMap);
        return resultMap;
    }

    //搜索页面   http://localhost:9009/search/list?keywords=手机&brand=三星&spec_颜色=粉色&
    //入参：Map
    //返回值 Map
    //由于页面是thymeleaf 完成的 属于服务器内页面渲染 跳转页面
    @GetMapping("/list")
    public String search(@RequestParam Map<String,String> searchMap, Model model){
        handlerSearchMap(searchMap);
        String s = searchMap.get("keywords");
        Map<String,Object> resultMap= searchService.search(searchMap);
        model.addAttribute("searchMap",searchMap);
        model.addAttribute("result",resultMap);

        StringBuilder url=new StringBuilder("/search/list");
        if (searchMap!=null&&searchMap.size()>0){
            url.append("?");
            for (String paramKey : searchMap.keySet()) {
                if (!"sortRule".equals(paramKey)
                        && !"sortField".equals(paramKey)
                        && !"pageNum".equals(paramKey)){
                    url.append(paramKey).append("=").append(searchMap.get(paramKey)).append("&");
                }
                String urlString = url.toString();
                //去除路径上的最后一个&
                urlString=urlString.substring(0,urlString.length()-1);
                model.addAttribute("url",urlString);
            }
        }else {
            model.addAttribute("url",url);
        }
        long total = Long.parseLong(resultMap.get("total").toString());
        Integer pageNum = Integer.parseInt(resultMap.get("pageNum").toString());
        Page<SkuInfo> page= new Page<SkuInfo>(total,pageNum,Page.pageSize);
        model.addAttribute("page",page);
        return "search";
    }

    private void handlerSearchMap(Map<String,String> searchMap) {
        if (searchMap!=null){
            Set<Map.Entry<String, String>> entrySet = searchMap.entrySet();

            for (Map.Entry<String, String> entry : entrySet) {
                if (entry.getKey().startsWith("spec_")){
                    searchMap.put(entry.getKey(),entry.getValue().replace("+","%2B"));
                }
            }
        }
    }
}
