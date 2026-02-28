package com.bitstrat.init;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.RandomUtil;
import com.alibaba.fastjson2.JSONObject;
import com.bitstrat.constant.ExchangeType;
import com.bitstrat.constant.SideType;
import com.bitstrat.constant.WebsocketMsgType;
import com.bitstrat.domain.Account;
import com.bitstrat.domain.PositionWsData;
import com.bitstrat.domain.WebsocketMsgData;
import com.bitstrat.domain.bo.CoinsTaskBo;
import com.bitstrat.domain.vo.CoinsOrderVo;
import com.bitstrat.domain.vo.CoinsTaskVo;
import com.bitstrat.domain.wsdomain.BybitPositionItem;
import com.bitstrat.service.BybitService;
import com.bitstrat.service.ICoinsTaskService;
import com.bitstrat.service.impl.CommonServce;
import com.bitstrat.utils.BitStratThreadFactory;
import org.apache.commons.lang3.math.NumberUtils;
import org.dromara.common.core.utils.SpringUtils;
import org.dromara.common.websocket.utils.WebSocketUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/4/2 19:59
 * @Content
 */

@Component
@Slf4j
public class TestTaskRunner implements ApplicationRunner {

    @Autowired
    ICoinsTaskService coinsTaskService;

    @Autowired
    CommonServce commonServce;

    @Autowired
    BybitService bybitService;

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2,BitStratThreadFactory.forName("task-scheduler"));
    @Override
    public void run(ApplicationArguments args) throws Exception {
        AtomicReference<CoinsOrderVo> coinsOrderVoCache = new AtomicReference<>();
//        scheduler.scheduleWithFixedDelay(()->{
//            try {
//                WebsocketMsgData<CoinsOrderVo> websocketMsgData = new WebsocketMsgData<>();
//                CoinsOrderVo coinsOrderVo;
//                if(coinsOrderVoCache.get()!=null){
//                    coinsOrderVo = coinsOrderVoCache.get();
//                    coinsOrderVo.setStatus("FILL");
//                    coinsOrderVoCache.set(null);
//                    websocketMsgData.setData(coinsOrderVo);
//                    websocketMsgData.setType(WebsocketMsgType.ORDER);
//                    WebSocketUtils.sendMessage(1L,websocketMsgData.toJSONString());
//                    return;
//                }else{
//                    coinsOrderVo = new CoinsOrderVo();
//                    coinsOrderVo.setStatus("CREATE");
//                }
//
//                coinsOrderVo.setOrderId(IdUtil.simpleUUID());
//                coinsOrderVo.setEx(ExchangeType.OKX.getName());
//                coinsOrderVo.setSize(RandomUtil.randomNumbers(4));
//                coinsOrderVo.setPrice(RandomUtil.randomNumbers(4));
//                coinsOrderVo.setAvgPrice(RandomUtil.randomNumbers(4));
//                coinsOrderVo.setFee(RandomUtil.randomNumbers(2));
////            coinsOrderVo.setStatus("CREATE");
//                coinsOrderVo.setSide(RandomUtil.randomInt(0,99)  > 50? SideType.LONG:SideType.SHORT);
//                coinsOrderVo.setClosePositionOrder(RandomUtil.randomInt(0,99)  > 50? 1L:0L);
//                coinsOrderVo.setCreateTime(new Date());
//                coinsOrderVo.setSymbol("BTC");
//                coinsOrderVo.setUpdateTime(new Date());
//                if(coinsOrderVoCache.get()==null){
//                    coinsOrderVoCache.set(coinsOrderVo);
//                }
//                websocketMsgData.setData(coinsOrderVo);
//                websocketMsgData.setType(WebsocketMsgType.ORDER);
//                WebSocketUtils.sendMessage(1L,websocketMsgData.toJSONString());
//                log.info("测试订单已发送");
//            } catch (Exception e){
//                e.printStackTrace();
//            }
//        },3,3,TimeUnit.SECONDS);
//        scheduler.scheduleWithFixedDelay(()->{
//            try {
//                runPosition();
//            } catch (Exception e) {
//               e.printStackTrace();
//            }
//            log.info("测试持仓已发送");
//        },5,5,TimeUnit.SECONDS);


    }

    private void runPosition() {
        ArrayList<Account> accounts = new ArrayList<>();
        for (long i = 100; i <103; i++) {
            Account account = new Account();
            account.setId(i);
            account.setName("account"+i);
            accounts.add(account);
        }
        for (Account account : accounts) {
            List<PositionWsData> wsDatas = mockRandomPositions(account.getId(),account.getName(),3); // 每个账户3条数据

            WebsocketMsgData<List<PositionWsData>> msg = new WebsocketMsgData<>();
            msg.setData(wsDatas);
            msg.setType(WebsocketMsgType.POSITION);
            msg.setAccountId(account.getId());
            msg.setExchangeName(wsDatas.get(0).getExchange()); // 用第一个的数据设定 exchange

            WebSocketUtils.sendMessage(1L, msg.toJSONString());
        }




    }
    private List<PositionWsData> mockRandomPositions(Long accountId, String accountName, int count) {
        List<PositionWsData> list = new ArrayList<>();
        String name = randomEnum(ExchangeType.class).getName();
        for (int i = 0; i < count; i++) {
            PositionWsData data = new PositionWsData();
            data.setExchange(name);
            data.setSymbol("BTC");
            data.setSize(BigDecimal.valueOf(Math.random() * 10 + 0.1).setScale(3, RoundingMode.HALF_UP));
            data.setSide(RandomUtil.randomInt(2) > 50?SideType.LONG:SideType.SHORT);
            data.setAvgPrice(BigDecimal.valueOf(20000 + Math.random() * 5000).setScale(2, RoundingMode.HALF_UP));
            data.setFundingFee(BigDecimal.ZERO);
            data.setFee(BigDecimal.valueOf(Math.random()).setScale(4, RoundingMode.HALF_UP));
            data.setProfit(BigDecimal.valueOf(Math.random() * 200 - 100).setScale(2, RoundingMode.HALF_UP));
            data.setUnrealizedProfit(BigDecimal.valueOf(Math.random() * 50 - 25).setScale(2, RoundingMode.HALF_UP));
            data.setMarginType(Math.random() > 0.5 ? "cross" : "isolated");
            data.setMarginPrice(BigDecimal.valueOf(Math.random() * 100).setScale(2, RoundingMode.HALF_UP));
            data.setMarginRatio(BigDecimal.valueOf(Math.random()).setScale(3, RoundingMode.HALF_UP));
            data.setLeverage(BigDecimal.valueOf(ThreadLocalRandom.current().nextInt(1, 21)));
            data.setLiqPrice(BigDecimal.valueOf(18000 + Math.random() * 2000).setScale(2, RoundingMode.HALF_UP));
            data.setClosed(Math.random() > 0.8);
            data.setUpdateTime(new Date());
            data.setAccountId(accountId);
            data.setAccountName(accountName);
            data.setPosType("normal");

            list.add(data);
        }
        return list;
    }
    public static <T extends Enum<?>> T randomEnum(Class<T> clazz) {
        T[] enumConstants = clazz.getEnumConstants();
        int index = ThreadLocalRandom.current().nextInt(enumConstants.length);
        return enumConstants[index];
    }
}
