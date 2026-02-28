package com.bitstrat.domain.bo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.bitstrat.domain.CoinAITaskBalance;
import com.bitstrat.domain.HistoryPosition;
import org.dromara.common.mybatis.core.domain.BaseEntity;
import org.dromara.common.core.validate.EditGroup;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Date;

/**
 * AI 测试趋势业务对象 coin_test_ai
 *
 * @author Lion Li
 * @date 2025-10-29
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = CoinAITaskBalance.class, reverseConvertGenerate = false)
public class CoinAITaskBalanceBo extends BaseEntity {

    /**
     * id
     */
    @NotNull(message = "id不能为空", groups = { EditGroup.class })
    private Long id;

    /**
     * 金额
     */
    private BigDecimal equity;

    /**
     * 可用
     */
    private BigDecimal freeBalance;

    /**
     * 时间戳
     */
    @TableField(value = "`time`")
    private Date time;

    private Long taskId;
    private Long strategyId;


    private ZonedDateTime startDate;

    private ZonedDateTime endDate;


    public static CoinApiPositionBo cover(HistoryPosition historyPosition) {
        CoinApiPositionBo coinApiPositionBo = new CoinApiPositionBo();
        coinApiPositionBo.setCoin(historyPosition.getSymbol());
        coinApiPositionBo.setSide(historyPosition.getHoldSide());
        coinApiPositionBo.setSize(historyPosition.getCloseTotalPos());
        coinApiPositionBo.setOpen(historyPosition.getOpenAvgPrice());
        coinApiPositionBo.setClose(historyPosition.getCloseAvgPrice());
        coinApiPositionBo.setMarginMode(historyPosition.getMarginMode());
        coinApiPositionBo.setNetProfit(historyPosition.getNetProfit());
        coinApiPositionBo.setPnl(historyPosition.getPnl());
        coinApiPositionBo.setOpenFee(historyPosition.getOpenFee());
        coinApiPositionBo.setCloseFee(historyPosition.getCloseFee());
        coinApiPositionBo.setTotalFunding(historyPosition.getTotalFunding());
        coinApiPositionBo.setUtime(historyPosition.getUtime());
        coinApiPositionBo.setCtime(historyPosition.getCtime());

        return coinApiPositionBo;
    }
}
