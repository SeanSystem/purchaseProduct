package com.example.highconcurrencydemo.dao;

import com.example.highconcurrencydemo.entity.Product;
import org.apache.ibatis.annotations.Param;

public interface ProductMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(Product record);

    int insertSelective(Product record);

    Product selectByPrimaryKey(Integer id);

    int decreaseProduct(@Param("id") Integer id, @Param("quantity") Integer quantity,@Param("version") Integer version);

    int decProduct(@Param("id") Integer id, @Param("quantity") Integer quantity);

    int updateByPrimaryKeySelective(Product record);

    int updateByPrimaryKey(Product record);
}