package com.bitstrat.domain.bo;

import com.bitstrat.domain.CoinsRankLog;
import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.core.validate.EditGroup;
import org.dromara.common.mybatis.core.domain.BaseEntity;

/**
 * 山寨币排行日志业务对象 coins_rank_log
 *
 * @author Lion Li
 * @date 2025-04-05
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = CoinsRankLog.class, reverseConvertGenerate = false)
public class CoinsRankLogBo extends BaseEntity {

    /**
     * id
     */
    @NotNull(message = "id不能为空", groups = { EditGroup.class })
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
