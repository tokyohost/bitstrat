package com.bitstrat.domain.bo;

import com.bitstrat.domain.CoinsFeedLikeLog;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.*;

/**
 * 策略广场like日志业务对象 coins_feed_like_log
 *
 * @author Lion Li
 * @date 2025-12-12
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = CoinsFeedLikeLog.class, reverseConvertGenerate = false)
public class CoinsFeedLikeLogBo extends BaseEntity {

    /**
     * 主键ID
     */
    @NotNull(message = "主键ID不能为空", groups = { EditGroup.class })
    private Long id;

    /**
     * feed ID
     */
    private Long feedId;

    /**
     * 点赞用户
     */
    private Long userId;


}
