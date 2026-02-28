package com.bitstrat.domain.bo;

import com.bitstrat.domain.CoinsAbOrderLog;
import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.core.validate.EditGroup;
import org.dromara.common.mybatis.core.domain.BaseEntity;

/**
 * 价差套利日志业务对象 coins_ab_order_log
 *
 * @author Lion Li
 * @date 2025-06-08
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = CoinsAbOrderLog.class, reverseConvertGenerate = false)
public class CoinsAbOrderLogBo extends BaseEntity {

    /**
     * id
     */
    @NotNull(message = "id不能为空", groups = { EditGroup.class })
    private Long id;

    /**
     * account a
     */
    private Long accountA;

    /**
     * account b
     */
    private Long accountB;

    /**
     * exchangea
     */
    private String exchangeA;

    /**
     * exchangeb
     */
    private String exchangeB;

    /**
     * TaskId
     */
    private String taskId;

    /**
     * 日志
     */
    private String log;


}
