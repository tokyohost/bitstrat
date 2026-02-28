package com.bitstrat.domain.bo;

import com.bitstrat.domain.CoinsRankReversed;
import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.core.validate.EditGroup;
import org.dromara.common.mybatis.core.domain.BaseEntity;

import java.util.Date;

/**
 * 山寨币排行(反向)业务对象 coins_rank_reversed
 *
 * @author Lion Li
 * @date 2025-04-06
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = CoinsRankReversed.class, reverseConvertGenerate = false)
public class CoinsRankReversedBo extends BaseEntity {

    /**
     * id
     */
    @NotNull(message = "id不能为空", groups = { EditGroup.class })
    private Long id;

    /**
     * 币种
     */
    private String symbol;

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
    private Double percentage;

    /**
     * 排名
     */
    private Long rank;
    private String tenantId;

    private Date createTime;

    private String historyRecord;
    private Date updateTime;

}
