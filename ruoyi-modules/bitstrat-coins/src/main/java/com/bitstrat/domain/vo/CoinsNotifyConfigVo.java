package com.bitstrat.domain.vo;

import com.bitstrat.domain.CoinsNotifyConfig;
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
 * 用户通知设置视图对象 coins_notify_config
 *
 * @author Lion Li
 * @date 2025-04-25
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = CoinsNotifyConfig.class)
public class CoinsNotifyConfigVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    private Long id;

    /**
     * 配置类型 1-钉钉机器人通知 2-TG通知
     */
    @ExcelProperty(value = "配置类型 1-钉钉机器人通知 2-TG通知")
    private String type;

    /**
     * 钉钉 token
     */
    @ExcelProperty(value = "钉钉 token")
    private String dingToken;

    /**
     * 钉钉 secret
     */
    @ExcelProperty(value = "钉钉 secret")
    private String dingSecret;

    /**
     * tg chart id
     */
    @ExcelProperty(value = "tg chart id")
    private String telegramChatId;

    /**
     * 用户id
     */
    private Long userId;


}
