package com.bitstrat.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;

import java.io.Serial;
import java.math.BigDecimal;

/**
 * ai 流水线配置对象 coins_ai_config
 *
 * @author Lion Li
 * @date 2025-04-01
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("coins_ai_config")
public class CoinsAiConfig extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 流水线名称
     */
    private String flowName;

    /**
     * api地址
     */
    private String url;

    /**
     * token
     */
    private String token;

    private String imgUrl;

    private BigDecimal price;

    /**
     * 回调地址
     */
    private String callback;

    /**
     * 时间粒度
     */
    @TableField("`interval`")
    private String interval;

    private String type;

}
