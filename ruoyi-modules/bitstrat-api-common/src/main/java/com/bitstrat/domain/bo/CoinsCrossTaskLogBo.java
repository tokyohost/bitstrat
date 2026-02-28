package com.bitstrat.domain.bo;

import com.bitstrat.domain.CoinsCrossTaskLog;
import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.core.validate.EditGroup;
import org.dromara.common.mybatis.core.domain.BaseEntity;

/**
 * 跨交易所任务日志业务对象 coins_cross_task_log
 *
 * @author Lion Li
 * @date 2025-04-19
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = CoinsCrossTaskLog.class, reverseConvertGenerate = false)
public class CoinsCrossTaskLogBo extends BaseEntity {

    /**
     * id
     */
    @NotNull(message = "id不能为空", groups = { EditGroup.class })
    private Long id;

    /**
     * 任务id
     */
    private Long taskId;

    /**
     * 日志
     */
    private String msg;


}
