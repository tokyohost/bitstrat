package com.bitstrat.domain.bo;

import com.bitstrat.domain.CoinsVipLevel;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.*;

/**
 * VIP 权限业务对象 coins_vip_level
 *
 * @author Lion Li
 * @date 2025-05-14
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = CoinsVipLevel.class, reverseConvertGenerate = false)
public class CoinsVipLevelBo extends BaseEntity {

    /**
     * id
     */
    @NotNull(message = "id不能为空", groups = { EditGroup.class })
    private Long id;

    /**
     * VIP名称
     */
    private String name;

    /**
     * VIP等级
     */
    private Long level;

    /**
     * 最大套利金额 USDT
     */
    private Long maxAbAmount;

    /**
     * 最大同时允许运行中状态的任务数量
     */
    private Long maxActiveTask;

    /**
     * VIP状态，1-正常 2-禁用 3-不可购买
     */
    private Long status;

    /**
     * VIP 开通金额 单位USDT
     */
    private Long price;

    /**
     * 可用时长 单位-天
     */
    private Long avaliableDay;


}
