package com.example.highconcurrencydemo.entity;

import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;

@Data
public class PurchaseRecord {
    /**
    * 编号
    */
    private Integer id;

    /**
    * 用户编号
    */
    private Integer userId;

    /**
    * 商品编号
    */
    private Integer productId;

    /**
    * 商品价格
    */
    private BigDecimal price;

    /**
    * 购买数量
    */
    private Integer quantity;

    /**
    * 总价
    */
    private BigDecimal sum;

    /**
    * 购买时间
    */
    private Date purchaseDate;

    /**
    * 备注
    */
    private String note;
}