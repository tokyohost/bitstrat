package com.bitstrat.mapper;

import com.bitstrat.domain.CoinsAiTask;
import com.bitstrat.domain.QueryProfitParam;
import com.bitstrat.domain.vo.CoinsAiTaskVo;
import com.bitstrat.domain.vo.TaskAnalysisByDay;
import com.bitstrat.domain.vo.TaskProfitByDay;
import org.apache.ibatis.annotations.Param;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;

import java.util.List;

/**
 * AI任务Mapper接口
 *
 * @author Lion Li
 * @date 2025-11-24
 */
public interface CoinsAiTaskMapper extends BaseMapperPlus<CoinsAiTask, CoinsAiTaskVo> {

    List<TaskProfitByDay> queryTaskProfit(@Param("queryProfitParam") QueryProfitParam queryProfitParam);

    List<TaskAnalysisByDay> queryTaskDayProfit(@Param("queryProfitParam") QueryProfitParam queryProfitParam);
}
