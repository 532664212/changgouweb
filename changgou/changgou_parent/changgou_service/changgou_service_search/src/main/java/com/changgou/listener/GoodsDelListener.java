package com.changgou.listener;

import com.changgou.config.RabbitMQConfig;
import com.changgou.service.ESManagerService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class GoodsDelListener {
    @Autowired
    private ESManagerService esManagerService;
    @RabbitListener(queues = RabbitMQConfig.SEARCH_DEL_QUEUE)
    public  void receiveMessage(String spuId){
        System.out.println("接收到的消息为:   "+spuId);
        esManagerService.delDataBySpuId(spuId);
    }
}
