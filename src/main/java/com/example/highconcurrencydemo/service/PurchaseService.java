package com.example.highconcurrencydemo.service;

import com.example.highconcurrencydemo.entity.PurchaseRecord;

import java.util.List;

/**
 * 商品抢购服务接口
 *
 * @author Sean
 * 2020/02/23
 */
public interface PurchaseService {

    /**
     * 抢购商品
     *
     * @param userId   用户id
     * @param id       商品id
     * @param quantity 购买数量
     * @return 购买结果
     */
    boolean purchase(Integer userId, Integer id, int quantity);

    /**
     * 通过redis处理商品抢购
     *
     * @param userId   用户id
     * @param id       商品id
     * @param quantity 购买数量
     * @return 购买结果
     */
    boolean purchaseRedis(Integer userId, Integer id, int quantity);

    /**
     * redis中的商品购买记录存库
     *
     * @param purchaseRecordList 购买记录
     */
    void dealRedisPurchase(List<PurchaseRecord> purchaseRecordList);
}
