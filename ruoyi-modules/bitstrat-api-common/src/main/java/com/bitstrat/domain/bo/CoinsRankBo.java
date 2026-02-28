package com.bitstrat.domain.bo;

import com.bitstrat.domain.CoinsRank;
import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.core.validate.EditGroup;
import org.dromara.common.mybatis.core.domain.BaseEntity;

import java.util.Date;

/**
 * 山寨币排行业务对象 coins_rank
 *
 * @author Lion Li
 * @date 2025-04-05
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = CoinsRank.class, reverseConvertGenerate = false)
public class CoinsRankBo extends BaseEntity {

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
    private Integer rank;

    private String historyRecord;

}
