package com.changgou.service;

import java.util.Map;

public interface SearchService {
    /**
     * 全文检索
     * @param searchMap  查询参数
     * @return
     */
    Map search(Map<String,String> searchMap);
}
