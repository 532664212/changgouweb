package com.changgou.order.controller;

import com.changgou.entity.Result;
import com.changgou.order.feign.CartFeign;
import com.changgou.order.pojo.Order;
import com.changgou.order.pojo.OrderItem;
import com.changgou.user.feign.AddressFeign;
import com.changgou.user.pojo.Address;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/Worder")
public class OrderController {
    @Autowired
    private CartFeign cartFeign;
    @Autowired
    private AddressFeign addressFeign;
    @RequestMapping("/ready/order")
    public String readerOrder(Model model){
        //收件人的地址信息
        List<Address> addressList = addressFeign.list().getData();
        for (Address address : addressList) {
            if ("1".equals(address.getIsDefault())){
                model.addAttribute("deAddr",address);
                break;
            }
        }
        model.addAttribute("address",addressList);
        //购物车信息
        Map map = cartFeign.list();
        List<OrderItem> orderItemList = (List<OrderItem>) map.get("orderItemList");
        Integer totalMoney = (Integer) map.get("totalPrice");
        Integer totalNum = (Integer) map.get("totalNum");
        model.addAttribute("carts",orderItemList);
        model.addAttribute("totalMoney",totalMoney);
        model.addAttribute("totalNum",totalNum);
        return "order";
    }
}
