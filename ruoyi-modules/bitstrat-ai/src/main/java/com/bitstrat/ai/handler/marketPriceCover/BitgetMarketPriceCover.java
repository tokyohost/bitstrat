package com.bitstrat.ai.handler.marketPriceCover;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.bitstrat.ai.distuptor.MarketPrice;
import com.bitstrat.ai.domain.vo.BitgetTickerVo;
import com.bitstrat.constant.ExchangeType;
import com.bitstrat.domain.wsdomain.BitgetArg;
import com.bitstrat.domain.wsdomain.BitgetWsMsgData;
import com.bitstrat.wsClients.msg.receive.LinerReceiveMsg;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/5/29 18:50
 * @Content
 */
@Component
public class BitgetMarketPriceCover implements MarketPriceCover{
    @Override
    public String exchangeName() {
        return ExchangeType.BITGET.getName();
    }

    @Override
    public List<MarketPrice> coverLinerMsg(LinerReceiveMsg linerMsg) {
        String msg = linerMsg.getMsg();
        BitgetWsMsgData bitgetWsMsgData = JSONObject.parseObject(msg, BitgetWsMsgData.class);
        BitgetArg arg = bitgetWsMsgData.getArg();
        List<MarketPrice> marketPrices = new ArrayList<>();
        if ("ticker".equalsIgnoreCase(arg.getChannel())
        && "snapshot".equalsIgnoreCase(bitgetWsMsgData.getAction())) {
            //处理ticker数据
            JSONArray data = bitgetWsMsgData.getData();
            for (Object datum : data) {
                BitgetTickerVo bitgetTickerVo = JSONObject.from(datum).to(BitgetTickerVo.class);
                MarketPrice marketPrice = new MarketPrice(bitgetTickerVo.getTs(),bitgetTickerVo.getLastPr());
                marketPrice.setSymbol(linerMsg.getConnectionConfig().getOtherConfig().getSymbol());
                marketPrice.setBidPrice(bitgetTickerVo.getBidPr());
                marketPrice.setAskPrice(bitgetTickerVo.getAskPr());
                marketPrice.setIndexPrice(bitgetTickerVo.getIndexPrice());
                marketPrice.setMarkPrice(bitgetTickerVo.getMarkPrice());
                marketPrice.setExchange(ExchangeType.BITGET.getName());
                marketPrices.add(marketPrice);
            }

        }
//
        return marketPrices;
    }
}
