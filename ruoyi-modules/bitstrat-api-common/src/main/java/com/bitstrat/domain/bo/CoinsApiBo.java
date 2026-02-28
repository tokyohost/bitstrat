package com.bitstrat.domain.bo;

import com.bitstrat.domain.CoinsApi;
import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import org.dromara.common.mybatis.core.domain.BaseEntity;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 交易所API业务对象 coins_api
 *
 * @author Lion Li
 * @date 2025-04-14
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = CoinsApi.class, reverseConvertGenerate = false)
public class CoinsApiBo extends BaseEntity {

    /**
     * id
     */
    private Long id;

    @NotBlank(message = "api 名称不能为空", groups = { AddGroup.class, EditGroup.class })
    private String name;
    /**
     * api key
     */
    @NotBlank(message = "api key不能为空", groups = { AddGroup.class, EditGroup.class })
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

    private String aesKey;

    private String iv;
    private BigDecimal balance;
    private BigDecimal freeBalance;
    private Date balanceUpdate;

    private String type;

}
