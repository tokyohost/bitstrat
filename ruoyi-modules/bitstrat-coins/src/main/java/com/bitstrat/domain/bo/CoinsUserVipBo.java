package com.bitstrat.domain.bo;

import com.bitstrat.domain.CoinsUserVip;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.*;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 用户VIP 状态业务对象 coins_user_vip
 *
 * @author Lion Li
 * @date 2025-05-14
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = CoinsUserVip.class, reverseConvertGenerate = false)
public class CoinsUserVipBo extends BaseEntity {

    /**
     * id
     */
    @NotNull(message = "id不能为空", groups = { EditGroup.class })
    private Long id;

    /**
     * 用户id
     */
    @NotNull(message = "用户id不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long userId;

    /**
     * VIP ID
     */
    @NotNull(message = "VIP ID不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long vipId;

    /**
     * 购买时间
     */
    @NotNull(message = "购买时间不能为空", groups = { AddGroup.class, EditGroup.class })
    private Date buyTime;

    /**
     * 过期时间
     */
    @NotNull(message = "过期时间不能为空", groups = { AddGroup.class, EditGroup.class })
    private Date expireTime;

    /**
     * 会员状态 1-正常 2-禁用 3-过期
     */
    @NotNull(message = "会员状态 1-正常 2-禁用 3-过期不能为空", groups = { AddGroup.class, EditGroup.class })
    private Integer status;

    /**
     * 是否是续费
     */
    private Integer isRenew;

    /**
     * 续费id
     */
    private Long renewId;

}
