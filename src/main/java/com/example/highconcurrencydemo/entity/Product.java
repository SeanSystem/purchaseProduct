package com.example.highconcurrencydemo.entity;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class Product {
    /**
    * 编号
    */
    private Integer id;

    /**
    * 产品名称
    */
    private String productName;

    /**
    * 库存
    */
    private Integer stock;

    /**
    * 价格
    */
    private BigDecimal price;

    /**
    * 版本号
    */
    private Integer version;

    /**
    * 备注
    */
    private String note;
}