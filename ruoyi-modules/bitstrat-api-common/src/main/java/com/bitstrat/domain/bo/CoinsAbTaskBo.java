package com.bitstrat.domain.bo;

import com.bitstrat.domain.CoinsAbTask;
import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.core.validate.EditGroup;
import org.dromara.common.mybatis.core.domain.BaseEntity;

/**
 * 价差套利任务业务对象 coins_ab_task
 *
 * @author Lion Li
 * @date 2025-06-08
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = CoinsAbTask.class, reverseConvertGenerate = false)
public class CoinsAbTaskBo extends BaseEntity {

    /**
     * id
     */
    @NotNull(message = "id不能为空", groups = { EditGroup.class })
    private Long id;

    /**
     * 任务id
     */
    private String taskId;

    /**
     * 任务体
     */
    private String body;

    /**
     * 用户id
     */
    private Long userId;


}
