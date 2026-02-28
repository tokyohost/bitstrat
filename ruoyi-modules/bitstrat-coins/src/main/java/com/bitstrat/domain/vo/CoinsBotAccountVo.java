package com.bitstrat.domain.vo;

import com.bitstrat.domain.CoinsBotAccount;
import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import org.dromara.common.excel.annotation.ExcelDictFormat;
import org.dromara.common.excel.convert.ExcelDictConvert;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;



/**
 * 机器人可使用账户视图对象 coins_bot_account
 *
 * @author Lion Li
 * @date 2025-05-24
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = CoinsBotAccount.class)
public class CoinsBotAccountVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @ExcelProperty(value = "id")
    private Long id;

    /**
     * 机器人id
     */
    @ExcelProperty(value = "机器人id")
    private Long botId;

    /**
     * api account id
     */
    @ExcelProperty(value = "api account id")
    private Long accountId;

    /**
     * 用户id
     */
    @ExcelProperty(value = "用户id")
    private Long userId;


}
