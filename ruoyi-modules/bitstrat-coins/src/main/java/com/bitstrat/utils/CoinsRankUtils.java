package com.bitstrat.utils;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.bitstrat.domain.bo.CoinsRankBo;
import com.bitstrat.domain.bo.CoinsRankReversedBo;
import com.bitstrat.domain.bybit.ScoreHistoryItem;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.dromara.common.core.service.ConfigService;
import org.dromara.common.core.utils.SpringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * @author xuehui_li
 * @Version 1.0
 * @date 2025/4/7 9:02
 * @Content
 */

@Slf4j
public class CoinsRankUtils {

    private final static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    public static void calcCoinsRankScore(CoinsRankBo coinsRankBo, int score) {
        ConfigService bean = SpringUtils.getBean(ConfigService.class);
        String normalRankHours = bean.getConfigValue("normal_rank_hours");
        int normalHours = 96;
        if(StringUtils.isNotEmpty(normalRankHours) && NumberUtils.isDigits(normalRankHours)) {
            normalHours = NumberUtils.toInt(normalRankHours);
        }
        normalHours = normalHours * 2;
        if (Objects.isNull(coinsRankBo.getScore())) {
            coinsRankBo.setScore(0d);
        }
        coinsRankBo.setScore(coinsRankBo.getScore() + score);
        ScoreHistoryItem scoreHistoryItem = new ScoreHistoryItem();
        scoreHistoryItem.setScore(score);
        scoreHistoryItem.setTime(formatter.format(LocalDateTime.now()));
        //获取历史分数，并重新计算分数
        if (StringUtils.isEmpty(coinsRankBo.getHistoryRecord())) {
            ArrayList<ScoreHistoryItem> scoreList = new ArrayList<>();

            scoreList.add(scoreHistoryItem);
            String historyRecord = JSONObject.toJSONString(scoreList);
            Integer newScore = calcScore(scoreList);
            coinsRankBo.setScore(newScore.doubleValue());
            coinsRankBo.setHistoryRecord(historyRecord);
        }else{
            String historyRecord = coinsRankBo.getHistoryRecord();
            List<ScoreHistoryItem> scoreHistory = JSONArray.parseArray(historyRecord, ScoreHistoryItem.class);
            if (scoreHistory.size() >= normalHours) {
                //删除最早的数据
                while(scoreHistory.size() >= normalHours){
                    ScoreHistoryItem remove = scoreHistory.remove(0);
                    log.info("已移除最早的得分记录: symbol {} time {} score {}",coinsRankBo.getSymbol(),remove.getTime(),remove.getScore());
                    ScoreHistoryItem remove1 = scoreHistory.remove(0);
                    log.info("已移除最早的得分记录: symbol {} time {} score {}",coinsRankBo.getSymbol(),remove1.getTime(),remove1.getScore());
                }

                scoreHistory.add(scoreHistoryItem);
                Integer newScore = calcScore(scoreHistory);
                coinsRankBo.setScore(newScore.doubleValue());
            }else{
                scoreHistory.add(scoreHistoryItem);
                Integer newScore = calcScore(scoreHistory);
                coinsRankBo.setScore(newScore.doubleValue());
            }
            historyRecord = JSONObject.toJSONString(scoreHistory);
            coinsRankBo.setHistoryRecord(historyRecord);
        }
        coinsRankBo.setUpdateTime(new Date());
    }
    public static void calcCoinsRankScoreReversed(CoinsRankReversedBo coinsRankBo, int score) {
        ConfigService bean = SpringUtils.getBean(ConfigService.class);
        String normalRankHours = bean.getConfigValue("normal_rank_hours");
        int normalHours = 96;
        if(StringUtils.isNotEmpty(normalRankHours) && NumberUtils.isDigits(normalRankHours)) {
            normalHours = NumberUtils.toInt(normalRankHours);
        }
        normalHours = normalHours * 2;
        if (Objects.isNull(coinsRankBo.getScore())) {
            coinsRankBo.setScore(0L);
        }
        coinsRankBo.setScore(coinsRankBo.getScore() + score);
        ScoreHistoryItem scoreHistoryItem = new ScoreHistoryItem();
        scoreHistoryItem.setScore(score);
        scoreHistoryItem.setTime(formatter.format(LocalDateTime.now()));
        //获取历史分数，并重新计算分数
        if (StringUtils.isEmpty(coinsRankBo.getHistoryRecord())) {
            ArrayList<ScoreHistoryItem> scoreList = new ArrayList<>();

            scoreList.add(scoreHistoryItem);
            String historyRecord = JSONObject.toJSONString(scoreList);
            Integer newScore = calcScore(scoreList);
            coinsRankBo.setScore(Long.valueOf(newScore));
            coinsRankBo.setHistoryRecord(historyRecord);
        }else{
            String historyRecord = coinsRankBo.getHistoryRecord();
            List<ScoreHistoryItem> scoreHistory = JSONArray.parseArray(historyRecord, ScoreHistoryItem.class);
            if (scoreHistory.size() >= normalHours) {
                //删除最早的数据
                while(scoreHistory.size() >= normalHours){
                    ScoreHistoryItem remove = scoreHistory.remove(0);
                    log.info("已移除最早的得分记录: symbol {} time {} score {}",coinsRankBo.getSymbol(),remove.getTime(),remove.getScore());
                    ScoreHistoryItem remove1 = scoreHistory.remove(0);
                    log.info("已移除最早的得分记录: symbol {} time {} score {}",coinsRankBo.getSymbol(),remove1.getTime(),remove1.getScore());
                }
                scoreHistory.add(scoreHistoryItem);
                Integer newScore = calcScore(scoreHistory);
                coinsRankBo.setScore((long) newScore.doubleValue());
            }else{
                scoreHistory.add(scoreHistoryItem);
                Integer newScore = calcScore(scoreHistory);
                coinsRankBo.setScore((long) newScore.doubleValue());
            }
            historyRecord = JSONObject.toJSONString(scoreHistory);
            coinsRankBo.setHistoryRecord(historyRecord);
        }
        coinsRankBo.setUpdateTime(new Date());
    }

    private static Integer calcScore(List<ScoreHistoryItem> historyItems) {
        Integer score = 0;
        for (ScoreHistoryItem scoreHistoryItem : historyItems) {
            score += scoreHistoryItem.getScore();
        }
        return score;
    }
}
