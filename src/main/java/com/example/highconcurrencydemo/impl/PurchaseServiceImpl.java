package com.example.highconcurrencydemo.impl;

import com.example.highconcurrencydemo.dao.ProductMapper;
import com.example.highconcurrencydemo.dao.PurchaseRecordMapper;
import com.example.highconcurrencydemo.entity.Product;
import com.example.highconcurrencydemo.entity.PurchaseRecord;
import com.example.highconcurrencydemo.service.PurchaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import redis.clients.jedis.Jedis;

import java.math.BigDecimal;
import java.util.List;

/**
 * 商品抢购服务类
 *
 * @author Sean
 * 2020/02/23
 */
@Service
public class PurchaseServiceImpl implements PurchaseService {

    @Autowired
    private ProductMapper productMapper;
    @Autowired
    private PurchaseRecordMapper purchaseRecordMapper;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    String purchaseScript =
            "redis.call('sadd', KEYS[1], ARGV[2]) \n"
                    + "local productPurchaseList = KEYS[2]..ARGV[2] \n"
                    + "local userId = ARGV[1] \n"
                    + "local product = 'product_'..ARGV[2] \n"
                    + "local quantity = tonumber(ARGV[3]) \n"
                    + "local stock = tonumber(redis.call('hget', product,'stock')) \n"
                    + "local price = tonumber(redis.call('hget', product, 'price')) \n"
                    + "local purchase_date = ARGV[4] \n"
                    + "if stock < quantity then return 0 end \n"
                    + "stock = stock - quantity \n"
                    + "redis.call('hset', product, 'stock', tostring(stock)) \n"
                    + "local sum = price * quantity \n"
                    + "local purchaseRecord = userId..','..quantity..','"
                    + "..sum..','..price..','..purchase_date \n"
                    + "redis.call('rpush', productPurchaseList, purchaseRecord) \n"
                    + "return 1 \n";

    private static final String PURCHASE_PRODUCT_LIST = "purchase_list_";

    private static final String PRODUCT_SCHEDULE_SET = "product_schedule_set";

    private String shal = null;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean purchase(Integer userId, Integer id, int quantity) {
        RedisTemplate<Object, Object> objectObjectRedisTemplate = new RedisTemplate<>();
        long start = System.currentTimeMillis();
        //乐观锁，失败重入机制（按时间）,也可以按次数
        while (true) {
            long end = System.currentTimeMillis();
            if (end - start > 100) {
                return false;
            }
            //获取商品
            Product product = productMapper.selectByPrimaryKey(id);
            //获取商品库存版本号
            Integer version = product.getVersion();
            //比较库存和商品数量
            if (product.getStock() < quantity) {
                return false;
            }
            //扣减库存，由版本号控制
            int result = productMapper.decreaseProduct(id, quantity, version);
            //失败重入
            if (result == 0) {
                continue;
            }
            //初始化购买记录
            PurchaseRecord record = initPurchaseRecord(userId, product, quantity);
            purchaseRecordMapper.insert(record);
            return true;
        }

    }

    /**
     * 初始化购买记录
     *
     * @param userId   用户id
     * @param product  购买商品
     * @param quantity 购买数量
     * @return
     */
    private PurchaseRecord initPurchaseRecord(Integer userId, Product product, int quantity) {
        PurchaseRecord record = new PurchaseRecord();
        record.setNote("购买日期：" + System.currentTimeMillis());
        record.setPrice(product.getPrice());
        record.setProductId(product.getId());
        record.setQuantity(quantity);
        BigDecimal sum = product.getPrice().multiply(BigDecimal.valueOf(quantity));
        record.setSum(sum);
        record.setUserId(userId);
        return record;
    }

    @Override
    public boolean purchaseRedis(Integer userId, Integer id, int quantity) {
        long purchaseDate = System.currentTimeMillis();
        try (Jedis jedis = (Jedis) stringRedisTemplate.getConnectionFactory().getConnection().getNativeConnection()) {
            if (shal == null) {
                shal = jedis.scriptLoad(purchaseScript);
            }
            Object res = jedis.evalsha(shal, 2, PRODUCT_SCHEDULE_SET,
                    PURCHASE_PRODUCT_LIST, userId + "", id + "", quantity + "", purchaseDate + "");
            long result = (long) res;
            return result == 1 ? true : false;
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void dealRedisPurchase(List<PurchaseRecord> purchaseRecordList) {
        for (PurchaseRecord prp : purchaseRecordList){
            purchaseRecordMapper.insertSelective(prp);
            productMapper.decProduct(prp.getProductId(), prp.getQuantity());
        }
    }
}
