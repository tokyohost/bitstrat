package com.bitstrat.domain.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.fastjson.annotation.JSONField;
import com.bitstrat.domain.CoinsApi;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;


/**
 * 交易所API视图对象 coins_api
 *
 * @author Lion Li
 * @date 2025-04-14
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = CoinsApi.class)
public class CoinsApiVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @ExcelProperty(value = "id")
    private Long id;

    @ExcelProperty(value = "name")
    private String name;
    /**
     * api key
     */
    @ExcelProperty(value = "api key")
    private String apiKey;

    /**
     * api security
     */
    @ExcelProperty(value = "api security")
    private String apiSecurity;

    /**
     * 交易所
     */
    @ExcelProperty(value = "交易所")
    private String exchangeName;

    /**
     * 用户id
     */
    @ExcelProperty(value = "用户id")
    private Long userId;

    private String passphrase;

    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
    private String aesKey;

    private String iv;

    private BigDecimal balance;
    private BigDecimal freeBalance;
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date balanceUpdate;

    private String type;
}
