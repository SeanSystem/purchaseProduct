package com.example.highconcurrencydemo.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 封装返回结果
 *
 * @author Sean
 * 2020/02/23
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result {

    private boolean success = false;

    private String message;
}
