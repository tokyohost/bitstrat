package com.bitstrat.domain.vo;

import com.bitstrat.domain.CoinsBalanceLog;
import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import org.dromara.common.excel.annotation.ExcelDictFormat;
import org.dromara.common.excel.convert.ExcelDictConvert;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;



/**
 * 账户余额变动日志视图对象 coins_balance_log
 *
 * @author Lion Li
 * @date 2025-11-20
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = CoinsBalanceLog.class)
public class CoinsBalanceLogVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @ExcelProperty(value = "主键ID")
    private Long id;

    /**
     * 用户ID
     */
    @ExcelProperty(value = "用户ID")
    private Long userId;

    /**
     * 变动前余额
     */
    @ExcelProperty(value = "变动前余额")
    private BigDecimal beforeBalance;

    /**
     * 变动金额（正为增加，负为扣减）
     */
    @ExcelProperty(value = "变动金额", converter = ExcelDictConvert.class)
    @ExcelDictFormat(readConverterExp = "正=为增加，负为扣减")
    private BigDecimal changeAmount;

    /**
     * 变动后余额
     */
    @ExcelProperty(value = "变动后余额")
    private BigDecimal afterBalance;

    /**
     * 变动类型：1=充值，2=消费，3=退款，4=赠送
     */
    @ExcelProperty(value = "变动类型：1=充值，2=消费，3=退款，4=赠送")
    private Long type;

    /**
     * 状态 1-处理中 2-已完成 3-异常
     */
    private Long status;

    /**
     * 备注信息，例如订单号/充值方式
     */
    @ExcelProperty(value = "备注信息，例如订单号/充值方式")
    private String remark;


    private Date createTime;

    private String tradeNo;

    private String tradeStatus;
}
