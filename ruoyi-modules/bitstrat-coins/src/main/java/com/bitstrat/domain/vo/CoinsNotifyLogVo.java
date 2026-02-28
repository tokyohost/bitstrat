package com.bitstrat.domain.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.bitstrat.domain.CoinsNotifyLog;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import com.alibaba.fastjson.annotation.JSONField;

/**
 * 通知日志视图对象 coins_notify_log
 *
 * @author Lion Li
 * @date 2025-04-26
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = CoinsNotifyLog.class)
public class CoinsNotifyLogVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 日志ID
     */
    @ExcelProperty(value = "日志ID")
    private Long id;

    /**
     * 通知类型 1-钉钉机器人通知 2-TG通知
     */
    @ExcelProperty(value = "通知类型 1-钉钉机器人通知 2-TG通知")
    private String notifyType;

    /**
     * 通知内容
     */
    @ExcelProperty(value = "通知内容")
    private String notifyContent;

    /**
     * 通知状态 1-成功 2-失败
     */
    @ExcelProperty(value = "通知状态 1-成功 2-失败")
    private String notifyStatus;

    /**
     * 错误信息
     */
    @ExcelProperty(value = "错误信息")
    private String errorMessage;

    /**
     * 用户ID
     */
    @ExcelProperty(value = "用户ID")
    private Long userId;

    /**
     * 创建时间
     * 序列化时转换为年月日时分秒的字符串格式
     */
    @ExcelProperty(value = "创建时间")
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

}
