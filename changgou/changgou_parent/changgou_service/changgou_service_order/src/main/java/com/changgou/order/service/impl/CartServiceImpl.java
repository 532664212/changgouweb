package com.changgou.order.service.impl;

import com.changgou.entity.Result;
import com.changgou.goods.feign.SkuFeign;
import com.changgou.goods.feign.SpuFeign;
import com.changgou.goods.pojo.Sku;
import com.changgou.goods.pojo.Spu;
import com.changgou.order.pojo.OrderItem;
import com.changgou.order.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private RedisTemplate redisTemplate;

    private static final String CART="Cart_";

    @Autowired
    private SkuFeign skuFeign;
    @Autowired
    private SpuFeign spuFeign;

    /**
     * 添加购物车
     * @param skuId
     * @param num
     */
    @Override
    public void add(String skuId, Integer num, String username) {
        /**
         * 1）查询redis中的数据
         * 2）如果redis中已经有了，则追加数量，重新计算金额
         * 3）如果没有，将商品添加到缓存
         */
        OrderItem orderItem = (OrderItem) redisTemplate.boundHashOps(CART + username).get(skuId);
        if (orderItem!=null){
            orderItem.setNum(orderItem.getNum()+num);
            if (orderItem.getNum()<=0){
               redisTemplate.boundHashOps(CART + username).delete(skuId);
               return;
            }
            orderItem.setMoney(orderItem.getNum()*orderItem.getPrice());
            orderItem.setPayMoney(orderItem.getNum()*orderItem.getPrice());
        }else {
            Result<Sku> skuResult = skuFeign.findById(skuId);
            Sku sku = skuResult.getData();
            Result<Spu> spuResult = spuFeign.findSpuById(sku.getSpuId());
            Spu spu = spuResult.getData();
            orderItem= saveOrderItem(sku,spu,num);
            redisTemplate.boundHashOps(CART+username).put(skuId,orderItem);
        }
    }
    /**
     * 获取购物车列表数据
     * @param username
     * @return
     */
    @Override
    public Map list(String username) {
        Map map = new HashMap();
        //商品数量与总价格
        Integer totalNum = 0;
        Integer totalPrice = 0;
        List<OrderItem> orderItemList  = redisTemplate.boundHashOps(CART + username).values();
        map.put("orderItemList",orderItemList);
        for (OrderItem orderItem : orderItemList) {
            totalNum+=orderItem.getNum();
            totalPrice+=totalNum*orderItem.getPrice();
            map.put("totalNum",totalNum);
            map.put("totalPrice",totalPrice);
        }
        return map;
    }
    private OrderItem saveOrderItem(Sku sku, Spu spu, Integer num) {


        OrderItem orderItem = new OrderItem();
        orderItem.setSpuId(sku.getSpuId());
        orderItem.setSkuId(sku.getId());
        orderItem.setName(sku.getName());
        orderItem.setPrice(sku.getPrice());
        orderItem.setNum(num);
        orderItem.setMoney(num*orderItem.getPrice());       //单价*数量
        orderItem.setPayMoney(num*orderItem.getPrice());    //实付金额
        orderItem.setImage(sku.getImage());
        orderItem.setWeight(sku.getWeight()*num);           //重量=单个重量*数量
        //分类ID设置
        orderItem.setCategoryId1(spu.getCategory1Id());
        orderItem.setCategoryId2(spu.getCategory2Id());
        orderItem.setCategoryId3(spu.getCategory3Id());
        return orderItem;
    }
}
