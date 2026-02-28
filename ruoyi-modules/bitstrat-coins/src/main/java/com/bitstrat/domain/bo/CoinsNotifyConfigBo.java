package com.bitstrat.domain.bo;

import com.bitstrat.domain.CoinsNotifyConfig;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.*;

/**
 * 用户通知设置业务对象 coins_notify_config
 *
 * @author Lion Li
 * @date 2025-04-25
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = CoinsNotifyConfig.class, reverseConvertGenerate = false)
public class CoinsNotifyConfigBo extends BaseEntity {

    /**
     * id
     */
    private Long id;

    /**
     * 配置类型 1-钉钉机器人通知 2-TG通知
     */
    @NotBlank(message = "配置类型 1-钉钉机器人通知 2-TG通知不能为空", groups = { AddGroup.class, EditGroup.class })
    private String type;

    /**
     * 钉钉 token
     */
    private String dingToken;

    /**
     * 钉钉 secret
     */
    private String dingSecret;

    /**
     * tg chart id
     */
    private String telegramChatId;

    /**
     * 用户id
     */
    private Long userId;


}
