package com.bitstrat.ai.domain.serverWatch;

import com.bitstrat.ai.constant.ABOrderSideType;
import io.netty.channel.Channel;
import lombok.Data;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/6/3 18:05
 * @Content
 */

@Data
public class ABOrderWatch {
    /**
     * A/B
     */
    private String type;

    /**
     * see {@link ABOrderSideType}
     */
    private String side;

    /**
     * 对应监听的市价channel
     */
    private Channel channel;

    private String exchange;

    private String symbol;

    /**
     * 合约还是现货
     * see {@link com.bitstrat.ai.constant.ABOrderType}
     */
    private String orderType;


}
