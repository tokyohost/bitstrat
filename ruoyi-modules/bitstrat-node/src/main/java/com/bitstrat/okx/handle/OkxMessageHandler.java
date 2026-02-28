package com.bitstrat.okx.handle;

import com.alibaba.fastjson2.JSONObject;
import com.bitstrat.cache.MarketPriceCache;
import com.bitstrat.constant.ExchangeType;
import com.bitstrat.domain.server.Message;
import com.bitstrat.okx.model.Constant.OkxChannelConstant;
import com.bitstrat.okx.model.OkxMarketPriceItem;
import com.bitstrat.okx.model.OkxMarketPriceReceive;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OkxMessageHandler {

    @Autowired
    MarketPriceCache cache;

    public void process(String message) {
        OkxMarketPriceReceive okxMarketPriceReceive = JSONObject.parseObject(message, OkxMarketPriceReceive.class);
        if(StringUtils.isNotEmpty(okxMarketPriceReceive.getEvent())){
            if(okxMarketPriceReceive.getEvent().equalsIgnoreCase("subscribe")){
                //订阅确定消息，不处理
                return;
            }
        }
        if (okxMarketPriceReceive.getArg().getChannel().equalsIgnoreCase(OkxChannelConstant.MARK_PRICE)) {
            //处理行情
            List<OkxMarketPriceItem> data = okxMarketPriceReceive.getData();
            for (OkxMarketPriceItem item : data) {
                cache.updatePrice(ExchangeType.OKX.getName(),item.getInstId(),item.getMarkPx());
            }
        }
    }
}
