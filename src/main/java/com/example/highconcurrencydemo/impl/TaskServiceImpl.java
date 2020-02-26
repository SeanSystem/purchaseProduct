package com.example.highconcurrencydemo.impl;

import com.example.highconcurrencydemo.entity.PurchaseRecord;
import com.example.highconcurrencydemo.service.PurchaseService;
import com.example.highconcurrencydemo.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundListOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * 处理定时任务服务
 *
 * @author Sean
 * 2020/02/26
 */
@Service
public class TaskServiceImpl implements TaskService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private PurchaseService purchaseService;

    private static final String PURCHASE_PRODUCT_LIST = "purchase_list_";
    private static final String PRODUCT_SCHEDULE_SET = "product_schedule_set";
    /**
     * 每次取出一千条
     */
    private static final int ONE_TIME_SIZE = 1000;

    /**
     * 每分钟执行一次
     */
   // @Scheduled(fixedRate = 1000 * 60)
    @Override
    public void purchaseTask() {
        System.out.println("定时任务开始。。。。。。");
        Set<String> productIdList = stringRedisTemplate.opsForSet().members(PRODUCT_SCHEDULE_SET);
        List<PurchaseRecord> purchaseRecordList = new ArrayList<>();
        for (String productId : productIdList) {
            String purchaseKey = PURCHASE_PRODUCT_LIST + productId;
            Long size = stringRedisTemplate.opsForList().size(purchaseKey);
            Long times = size % ONE_TIME_SIZE == 0 ? size / ONE_TIME_SIZE : size / ONE_TIME_SIZE + 1;
            for (int i = 0; i < times; i++) {
                List<String> prList = null;
                BoundListOperations<String, String> ops = stringRedisTemplate.boundListOps(purchaseKey);
                if (i == 0) {
                    prList = ops.range(i * ONE_TIME_SIZE, (i + 1) * ONE_TIME_SIZE);
                } else {
                    prList = ops.range(i * ONE_TIME_SIZE + 1, (i + 1) * ONE_TIME_SIZE);
                }
                for (String prStr : prList) {
                    PurchaseRecord purchaseRecord = this.createPurchaseRecord(productId, prStr);
                    purchaseRecordList.add(purchaseRecord);
                }
                try {
                    //该方法采用新建事务的方式，不会导致全局事务回滚
                    purchaseService.dealRedisPurchase(purchaseRecordList);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                //清除列表
                purchaseRecordList.clear();
            }
            //删除购买列表
            stringRedisTemplate.delete(purchaseKey);
            //从商品集合中删除商品
            stringRedisTemplate.opsForSet().remove(PRODUCT_SCHEDULE_SET, productId);
        }
        System.out.println("定时任务结束。。。。。。");
    }

    /**
     * 封装购买记录
     *
     * @param productId 商品id
     * @param prStr     商品购买记录
     * @return 购买记录
     */
    private PurchaseRecord createPurchaseRecord(String productId, String prStr) {
        String[] arr = prStr.split(",");
        int userId = Integer.parseInt(arr[0]);
        int quantity = Integer.parseInt(arr[1]);
        BigDecimal sum = new BigDecimal(arr[2]);
        BigDecimal price = new BigDecimal(arr[3]);
        Long time = Long.parseLong(arr[4]);
        Date purchaseTime = new Date(time);
        PurchaseRecord record = new PurchaseRecord();
        record.setProductId(Integer.parseInt(productId));
        record.setPurchaseDate(purchaseTime);
        record.setPrice(price);
        record.setQuantity(quantity);
        record.setSum(sum);
        record.setUserId(userId);
        record.setNote("购买日志，时间：" + purchaseTime.getTime());
        return record;
    }
}
