package com.bitstrat.domain.bo;

import com.bitstrat.domain.CoinsAiConfig;
import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.core.validate.EditGroup;
import org.dromara.common.mybatis.core.domain.BaseEntity;

import java.math.BigDecimal;

/**
 * ai 流水线配置业务对象 coins_ai_config
 *
 * @author Lion Li
 * @date 2025-04-01
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = CoinsAiConfig.class, reverseConvertGenerate = false)
public class CoinsAiConfigBo extends BaseEntity {

    /**
     * id
     */
    @NotNull(message = "id不能为空", groups = { EditGroup.class })
    private Long id;

    /**
     * 流水线名称
     */
    private String flowName;

    /**
     * api地址
     */
    private String url;

    /**
     * token
     */
    private String token;

    private String imgUrl;

    private BigDecimal price;

    /**
     * 回调地址
     */
    private String callback;

    /**
     * 时间粒度
     */
    private String interval;

    private String type;
}
