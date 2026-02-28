package com.bitstrat.ai.domain;

import com.alibaba.fastjson2.JSONObject;
import com.bitstrat.ai.constant.BusinessType;
import com.bitstrat.domain.Account;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import lombok.Data;
import org.dromara.common.core.domain.model.LoginUser;

import java.util.List;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/5/28 17:31
 * @Content
 */

@Data
public class CompareContext {
    /**
     * see {@link BusinessType}
     */
    private String type;
    private List<CompareItem> compareList;
    private String clientId;
    private String token;
    private LoginUser loginUser;
    private Long userId;

    private Channel channel;
    private ChannelHandlerContext channelHandlerContext;

    /**
     * 虚拟Account
     */
    private Account account;



    @Override
    public String toString() {
        return JSONObject.toJSONString(this);
    }
}
