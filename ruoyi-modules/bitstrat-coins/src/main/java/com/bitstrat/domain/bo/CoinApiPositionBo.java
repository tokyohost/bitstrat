package com.bitstrat.domain.bo;

import com.bitstrat.domain.CoinApiPosition;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * API 历史仓位数据业务对象 coin_api_position
 *
 * @author Lion Li
 * @date 2025-12-29
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = CoinApiPosition.class, reverseConvertGenerate = false)
public class CoinApiPositionBo extends BaseEntity {

    /**
     * id
     */
    @NotNull(message = "id不能为空", groups = { EditGroup.class })
    private Long id;

    /**
     * api id
     */
    private Long apiId;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 仓位ID
     */
    private String positionId;

    /**
     * 币对
     */
    private String coin;

    /**
     * 方向
     */
    private String side;

    /**
     * 数量
     */
    private BigDecimal size;

    /**
     * 开仓价
     */
    private BigDecimal open;

    /**
     * 平仓价
     */
    private BigDecimal close;

    /**
     * 仓位类型
     */
    private String marginMode;

    /**
     * 盈亏净值
     */
    private BigDecimal netProfit;

    /**
     * pnl
     */
    private BigDecimal pnl;

    /**
     * 资金费
     */
    private BigDecimal totalFunding;

    /**
     * 开仓手续费
     */
    private BigDecimal openFee;

    /**
     * 平仓手续费
     */
    private BigDecimal closeFee;

    /**
     * 更新时间
     */
    private String utime;

    /**
     * 创建时间
     */
    private String ctime;

    /**
     * 时间戳
     */
    private Date time;


}
