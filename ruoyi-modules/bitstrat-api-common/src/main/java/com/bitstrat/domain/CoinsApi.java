package com.bitstrat.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.tenant.core.TenantEntity;

import java.io.Serial;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 交易所API对象 coins_api
 *
 * @author Lion Li
 * @date 2025-04-14
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("coins_api")
public class CoinsApi extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    private Long id;

    private String name;

    /**
     * api key
     */
    private String apiKey;

    /**
     * api security
     */
    private String apiSecurity;

    /**
     * 交易所
     */
    private String exchangeName;

    /**
     * 用户id
     */
    private Long userId;

    private String passphrase;
    private Date createTime;

    private String aesKey;

    private String iv;

    private BigDecimal balance;
    private BigDecimal freeBalance;
    private Date balanceUpdate;

    private String type;
}
