package com.bitstrat.domain.bo;

import com.bitstrat.domain.CoinsBotAccount;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.*;

/**
 * 机器人可使用账户业务对象 coins_bot_account
 *
 * @author Lion Li
 * @date 2025-05-24
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = CoinsBotAccount.class, reverseConvertGenerate = false)
public class CoinsBotAccountBo extends BaseEntity {

    /**
     * id
     */
    @NotNull(message = "id不能为空", groups = { EditGroup.class })
    private Long id;

    /**
     * 机器人id
     */
    private Long botId;

    /**
     * api account id
     */
    private Long accountId;

    /**
     * 用户id
     */
    @NotNull(message = "用户id不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long userId;


}
