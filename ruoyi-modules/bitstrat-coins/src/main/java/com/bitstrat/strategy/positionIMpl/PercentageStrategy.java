package com.bitstrat.strategy.positionIMpl;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.bitstrat.domain.bo.PositionVo;
import com.bitstrat.domain.vo.CoinsTaskVo;
import com.bitstrat.strategy.PositionStrategy;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.math.NumberUtils;
import org.dromara.common.core.utils.StringUtils;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/4/3 15:53
 * @Content
 */
@Component
@Slf4j
public class PercentageStrategy implements PositionStrategy {
    @Override
    public String typeName() {
        return "PercentageStrategy";
    }

    @Override
    public String desc() {
        return "PercentageStrategyDesc";
    }

    @Override
    public Integer typeId() {
        return 3;
    }

    @Override
    public boolean check(CoinsTaskVo task, PositionVo positionVo) {
        //判断市价和持仓成本价
        BigDecimal avgPrice = positionVo.getAvgPrice();
        BigDecimal marketPrice = positionVo.getMarketPrice();




        if ("buy".equalsIgnoreCase(positionVo.getType())) {
            if(avgPrice == null ||avgPrice.doubleValue() == 0){
                //表示还没有持仓
                return true;
            }

            String buyRoleParams = task.getBuyRoleParams();
            if (NumberUtils.isParsable(buyRoleParams)) {
                BigDecimal threshold = new BigDecimal(buyRoleParams); // 可能是正也可能是负

                // (市价 - 成本价) / 成本价 * 100 = 涨跌幅
                BigDecimal delta = marketPrice.subtract(avgPrice)
                    .divide(avgPrice, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
                log.info("持仓价 {} 市场价 {}  百分比值{}",avgPrice,marketPrice,delta);

                if (threshold.compareTo(BigDecimal.ZERO) > 0) {
                    // 补仓逻辑：跌幅 >= 正阈值
                    return delta.compareTo(threshold.negate()) <= 0;
                } else {
                    // 追涨逻辑：涨幅 >= 阈值（threshold 本身是负数）
                    return delta.compareTo(threshold.negate()) >= 0;
                }
            } else {
                log.error("买入条件 buyRoleParams 非法: {}", buyRoleParams);
                return false;
            }

        } else if ("sell".equalsIgnoreCase(positionVo.getType())) {
            if(avgPrice == null || avgPrice.doubleValue() == 0){
                //表示还没有持仓
                return false;
            }
            String sellRoleParams = task.getSellRoleParams();
            if (NumberUtils.isParsable(sellRoleParams)) {
                BigDecimal threshold = new BigDecimal(sellRoleParams); // 可能是正也可能是负

                BigDecimal delta = marketPrice.subtract(avgPrice)
                    .divide(avgPrice, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
                log.info("持仓价 {} 市场价 {}  百分比值{}",avgPrice,marketPrice,delta);

                if (threshold.compareTo(BigDecimal.ZERO) > 0) {
                    // 止盈逻辑：涨幅 >= 阈值
                    return delta.compareTo(threshold) >= 0;
                } else {
                    // 止损逻辑：跌幅 >= 阈值（threshold 是负数）
                    return delta.compareTo(threshold) <= 0;
                }
            } else {
                log.error("卖出条件 sellRoleParams 非法: {}", sellRoleParams);
                return false;
            }
        }
        return false;
    }
    public static void main(String[] args) {


        Logger logger = (Logger) LoggerFactory.getLogger(PercentageStrategy.class);
        // 添加 ListAppender 临时收集日志
        ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);
//        CoinsTaskVo coinsTaskVo = new CoinsTaskVo();
//        coinsTaskVo.setBuyRoleParams("buy");
//        coinsTaskVo.setBuyRoleParams("5");
//
//        PositionVo positionVo = new PositionVo();
//        positionVo.setMarketPrice(new BigDecimal("5.4"));
//        positionVo.setAvgPrice(new BigDecimal("5.7"));
//        positionVo.setType("buy");
//        PercentageStrategy percentageStrategy = new PercentageStrategy();
//        boolean check = percentageStrategy.check(coinsTaskVo, positionVo);
//        System.out.println(check);

        CoinsTaskVo coinsTaskVo = new CoinsTaskVo();
        coinsTaskVo.setBuyRoleParams("buy");
        coinsTaskVo.setSellRoleParams("-5");

        PositionVo positionVo = new PositionVo();
        positionVo.setMarketPrice(new BigDecimal("5.4"));
        positionVo.setAvgPrice(new BigDecimal("5.7"));
        positionVo.setType("sell");
        PercentageStrategy percentageStrategy = new PercentageStrategy();
        boolean check = percentageStrategy.check(coinsTaskVo, positionVo);
        System.out.println(check);
        // 获取日志内容
        List<String> logs = listAppender.list.stream()
            .map(ILoggingEvent::getFormattedMessage)
            .collect(Collectors.toList());

        logs.forEach(System.out::println);

//        System.out.printf(NumberUtils.isParsable("-12")+"");
    }
}


