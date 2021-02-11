package com.changgou.service;

public interface ESManagerService {
    void createMappingAndIndex();

    void importAll();

    public void importDataBySpuId(String spuId);

    void delDataBySpuId(String spuId);
}
