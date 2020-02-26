package com.example.highconcurrencydemo.controller;

import com.example.highconcurrencydemo.pojo.Result;
import com.example.highconcurrencydemo.service.PurchaseService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

/**
 * 请购商品请求接口
 *
 * @author Sean
 * 2020/02/23
 */
@Api(tags = "测试请购商品接口")
@Controller
public class PurchaseController {

    @Autowired
    private PurchaseService purchaseService;

    @ApiOperation("跳转到测试页面")
    @GetMapping("/test")
    public String testPage(){
        return "test";
    }

    @ApiOperation("抢购商品")
    @PostMapping("/purchase")
    @ResponseBody
    public Result purchase(Integer userId, Integer productId, Integer quantity){
        boolean purchase = purchaseService.purchase(userId, productId, quantity);
        String message = purchase == true ? "请购成功" : "请购失败";
        Result result = new Result(purchase, message);
        return result;
    }

    @ApiOperation("通过redis实现商品抢购")
    @PostMapping("/purchaseByRedis")
    @ResponseBody
    public Result purchaseByRedis(Integer userId, Integer productId, Integer quantity){
        boolean purchase = purchaseService.purchaseRedis(userId, productId, quantity);
        String message = purchase == true ? "请购成功" : "请购失败";
        Result result = new Result(purchase, message);
        return result;
    }
}
