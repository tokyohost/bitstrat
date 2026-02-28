package org.dromara.system.domain.bo;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/12/8 16:16
 * @Content
 */

@Data
public class SysUserAddBalanceBo {
    @NotNull(message = "userId cannot be null")
    private Long userId;
    @NotNull(message = "balance cannot be null")
    private BigDecimal balance;
    @NotNull(message = "type cannot be empty")
    private Long type;
    private String remark;
}
