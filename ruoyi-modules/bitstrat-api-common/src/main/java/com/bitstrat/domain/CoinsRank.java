package com.bitstrat.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.tenant.core.TenantEntity;

import java.io.Serial;
import java.util.Date;

/**
 * 山寨币排行对象 coins_rank
 *
 * @author Lion Li
 * @date 2025-04-05
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("coins_rank")
public class CoinsRank extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 币种
     */
    private String symbol;

    /**
     * 得分
     */
    private Double score;

    /**
     * 当前市价
     */
    private String marketPrice;

    /**
     * 涨跌百分比
     */
    private Double percentage;

    private String tenantId;

    private Date createTime;

    private Date updateTime;
    @TableField(value = "`rank`")
    private Integer rank;

    private String historyRecord;
}
