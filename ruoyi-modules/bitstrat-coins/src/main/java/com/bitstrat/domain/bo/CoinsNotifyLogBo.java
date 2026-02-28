package com.bitstrat.domain.bo;

import com.bitstrat.domain.CoinsNotifyLog;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.*;

/**
 * 通知日志业务对象 coins_notify_log
 *
 * @author Lion Li
 * @date 2025-04-26
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = CoinsNotifyLog.class, reverseConvertGenerate = false)
public class CoinsNotifyLogBo extends BaseEntity {

    /**
     * 日志ID
     */
    @NotNull(message = "日志ID不能为空", groups = { EditGroup.class })
    private Long id;

    /**
     * 通知类型 1-钉钉机器人通知 2-TG通知
     */
    @NotBlank(message = "通知类型 1-钉钉机器人通知 2-TG通知不能为空", groups = { AddGroup.class, EditGroup.class })
    private String notifyType;

    /**
     * 通知内容
     */
    @NotBlank(message = "通知内容不能为空", groups = { AddGroup.class, EditGroup.class })
    private String notifyContent;

    /**
     * 通知状态 1-成功 2-失败
     */
    @NotBlank(message = "通知状态 1-成功 2-失败不能为空", groups = { AddGroup.class, EditGroup.class })
    private String notifyStatus;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 用户ID
     */
    private Long userId;


}
