package com.bitstrat.constant;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/5/26 17:33
 * @Content 套利机器人状态
 */

public class AbBotStatusConstant {
    //1-已创建 2-正在运行 3-已持仓 4-已终止 5-正在建仓 6-正在平仓
    public static final Long CREATED = 1L;
    public static final Long RUNNING = 2L;
    public static final Long HOLD = 3L;
    public static final Long STOP = 4L;
    public static final Long CREATE_POSITION = 5L;
    public static final Long CLOSE_POSITION = 6L;

}
