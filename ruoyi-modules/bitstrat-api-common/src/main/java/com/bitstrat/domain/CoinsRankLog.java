package com.bitstrat.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.tenant.core.TenantEntity;

import java.io.Serial;

/**
 * 山寨币排行日志对象 coins_rank_log
 *
 * @author Lion Li
 * @date 2025-04-05
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("coins_rank_log")
public class CoinsRankLog extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 币种id
     */
    private Long rankId;

    /**
     * 得分
     */
    private Long score;

    /**
     * 当前市价
     */
    private String marketPrice;

    /**
     * 涨跌百分比
     */
    private Long percentage;


}
