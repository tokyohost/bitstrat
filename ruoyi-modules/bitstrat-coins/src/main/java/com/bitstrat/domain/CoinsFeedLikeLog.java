package com.bitstrat.domain;

import org.dromara.common.mybatis.core.domain.BaseEntity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 策略广场like日志对象 coins_feed_like_log
 *
 * @author Lion Li
 * @date 2025-12-12
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("coins_feed_like_log")
public class CoinsFeedLikeLog extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id")
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
