package com.changgou.order.feign;

import com.changgou.entity.Result;
import com.changgou.order.pojo.OrderItem;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@FeignClient(name = "order")
public interface CartFeign {
    /**
     * 添加购物车
     * @param skuId
     * @param num
     * @return
     */
    @GetMapping("/cart/add")
    public Result<OrderItem> add(@RequestParam("skuId") String skuId, @RequestParam("num")Integer num );
    @GetMapping("/cart/list")
    public Map list();
}
