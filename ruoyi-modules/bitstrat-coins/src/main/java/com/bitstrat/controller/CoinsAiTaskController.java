package com.bitstrat.controller;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.bitstrat.constant.AITaskStatus;
import com.bitstrat.domain.ApiSettingVo;
import com.bitstrat.domain.ApiVO;
import com.bitstrat.domain.CoinsAiTask;
import com.bitstrat.domain.QueryProfitParam;
import com.bitstrat.domain.bo.CoinAITaskBalanceBo;
import com.bitstrat.domain.vo.*;
import com.bitstrat.service.*;
import lombok.RequiredArgsConstructor;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.*;
import cn.dev33.satoken.annotation.SaCheckPermission;
import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.common.core.validate.StartGroup;
import org.dromara.common.ratelimiter.annotation.RateLimiter;
import org.dromara.common.satoken.utils.LoginHelper;
import org.redisson.api.RAtomicLong;
import org.redisson.api.RedissonClient;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.scripting.config.LangNamespaceHandler;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;
import org.dromara.common.idempotent.annotation.RepeatSubmit;
import org.dromara.common.log.annotation.Log;
import org.dromara.common.web.core.BaseController;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.core.domain.R;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import org.dromara.common.log.enums.BusinessType;
import org.dromara.common.excel.utils.ExcelUtil;
import com.bitstrat.domain.bo.CoinsAiTaskBo;
import org.dromara.common.mybatis.core.page.TableDataInfo;

/**
 * AI任务
 *
 * @author Lion Li
 * @date 2025-11-24
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/system/aiTask")
public class CoinsAiTaskController extends BaseController {

    private final ICoinsAiTaskService coinsAiTaskService;
    private final ICoinsBalanceLogService coinsBalanceLogService;
    private final ICoinsApiService coinsApiService;
    private final RedissonClient redissonClient;
    private final ICoinAITaskBalanceService coinTestAiService;
    private final AiService aiService;
    private final ICoinApiPositionService coinApiPositionService;

    /**
     * 查询AI任务列表
     */
    @GetMapping("/list")
    public TableDataInfo<CoinsAiTaskVo> list(CoinsAiTaskBo bo, PageQuery pageQuery) {
        bo.setCreateUserId(LoginHelper.getUserId());
        return coinsAiTaskService.queryPageList(bo, pageQuery);
    }

    /**
     * 查询AI 测试趋势列表（精简版）
     */
    @GetMapping("/loadChartDataSimple")
    @Cacheable(value = "loadChartDataSimple:cache#300s#300s#2000", key = "'taskId'+':'+#bo.taskId")
    public R<com.alibaba.fastjson2.JSONObject> loadChartDataSimple(CoinAITaskBalanceBo bo, PageQuery pageQuery) {
        bo.setCreateBy(LoginHelper.getUserId());
        if (bo.getTaskId() == null) {
            return R.fail("params error");
        }
        ZonedDateTime now = ZonedDateTime.now();
        bo.setStartDate(now.minusHours(24)); //最近三个月
        bo.setEndDate(now);
        // 查询原始数据
        List<CoinAITaskBalanceVo> voList = coinTestAiService.queryList(bo);

        // 按时间升序
        List<CoinAITaskBalanceVo> sortedList = voList.stream()
            .sorted(Comparator.comparing(CoinAITaskBalanceVo::getTime))
            .collect(Collectors.toList());

        // 准备返回数组（只保留数值，不返回复杂配置）
        JSONArray dataArray = new JSONArray();
        for (CoinAITaskBalanceVo vo : sortedList) {
            if (vo.getEquity() != null) {
                dataArray.add(vo.getEquity().setScale(3, BigDecimal.ROUND_HALF_UP));
            } else {
                dataArray.add(BigDecimal.valueOf(-1));
            }
        }

        // 返回 JSON 示例：{ "data": [100.123, 101.234, ...] }
        JSONObject result = new JSONObject();
        result.put("data", dataArray);

        return R.ok(result);
    }

    @GetMapping("/queryTaskProfit")
    public R<List<TaskProfitByDay>> queryTaskProfit(@Validated QueryProfitParam queryProfitParam) {
        if (queryProfitParam.getStartTime() == null) {
            // 当月初：00:00:00
            queryProfitParam.setStartTime(
                LocalDate.now()
                    .withDayOfMonth(1)
                    .atStartOfDay()
            );
        }

        if (queryProfitParam.getEndTime() == null) {
            // 当月底：23:59:59.999999999（推荐用“下月初”做左闭右开）
            queryProfitParam.setEndTime(
                LocalDate.now()
                    .plusMonths(1)
                    .withDayOfMonth(1)
                    .atStartOfDay()
            );
        }

        List<TaskProfitByDay> taskProfitByDays = coinsAiTaskService.queryTaskProfit(queryProfitParam);


        CoinsAiTaskVo coinsAiTaskVo = coinsAiTaskService.queryById(queryProfitParam.getTaskId());
        if (Objects.isNull(coinsAiTaskVo)) {
            return R.fail();
        }
        Long apiId = coinsAiTaskVo.getApiId();
        Long userId = LoginHelper.getUserId();
        if(!userId.equals(coinsAiTaskVo.getCreateUserId())){
            return R.fail();
        }
        queryProfitParam.setApiId(apiId);
        long startTimeStamp = queryProfitParam.getStartTime()
            .atZone(ZoneId.systemDefault()) // 关键：指定时区
            .toInstant()
            .toEpochMilli();
        long endTimeStamp = queryProfitParam.getEndTime()
            .atZone(ZoneId.systemDefault()) // 关键：指定时区
            .toInstant()
            .toEpochMilli();
        queryProfitParam.setStartTimeStamp(startTimeStamp);
        queryProfitParam.setEndTimeStamp(endTimeStamp);
        List<TaskAnalysisByDay> taskProfitByDayAnalysis = coinsAiTaskService.queryTaskDayProfit(queryProfitParam);
        for (TaskAnalysisByDay profitByDayAnalysis : taskProfitByDayAnalysis) {
            if(Objects.nonNull(profitByDayAnalysis.getProfit())) {
                BigDecimal profit = profitByDayAnalysis.getProfit().setScale(2, BigDecimal.ROUND_HALF_UP);
                profitByDayAnalysis.setProfit(profit);
            }
            if(Objects.nonNull(profitByDayAnalysis.getPlRatio())) {
                BigDecimal plRatio = profitByDayAnalysis.getPlRatio().setScale(2, BigDecimal.ROUND_HALF_UP);
                profitByDayAnalysis.setPlRatio(plRatio);
            }
            if(Objects.nonNull(profitByDayAnalysis.getLsRatio())){
                BigDecimal lsRatio = profitByDayAnalysis.getLsRatio().setScale(2, BigDecimal.ROUND_HALF_UP);
                profitByDayAnalysis.setLsRatio(lsRatio);
            }
            if(Objects.nonNull(profitByDayAnalysis.getWinRatio())) {
                BigDecimal winratio = profitByDayAnalysis.getWinRatio().setScale(2, BigDecimal.ROUND_HALF_UP);
                profitByDayAnalysis.setWinRatio(winratio);
            }
            if(Objects.nonNull(profitByDayAnalysis.getTotalFee())) {
                BigDecimal totalFee = profitByDayAnalysis.getTotalFee().setScale(2, BigDecimal.ROUND_HALF_UP);
                profitByDayAnalysis.setTotalFee(totalFee);
            }
            if(Objects.nonNull(profitByDayAnalysis.getFundingFee())) {
                BigDecimal fundingFee = profitByDayAnalysis.getFundingFee().setScale(2, BigDecimal.ROUND_HALF_UP);
                profitByDayAnalysis.setFundingFee(fundingFee);
            }
            if(Objects.nonNull(profitByDayAnalysis.getAvgHoldHours())) {
                BigDecimal avgHoldHours = profitByDayAnalysis.getAvgHoldHours().setScale(2, BigDecimal.ROUND_HALF_UP);
                profitByDayAnalysis.setAvgHoldHours(avgHoldHours);
            }
        }
        Map<String, TaskAnalysisByDay> dayMap = taskProfitByDayAnalysis.stream().collect(Collectors.toMap(TaskAnalysisByDay::getDay, item -> item, (a, b) -> a));
        for (TaskProfitByDay taskProfitByDay : taskProfitByDays) {
            taskProfitByDay.setProfit(taskProfitByDay.getProfit().setScale(2, BigDecimal.ROUND_HALF_UP));
            TaskAnalysisByDay orDefault = dayMap.getOrDefault(taskProfitByDay.getDay(), null);
            taskProfitByDay.setDayAnalysis(orDefault);
        }

        return R.ok(taskProfitByDays);
    }
    @GetMapping("/queryTaskDayProfit")
    public R<List<TaskAnalysisByDay>> queryTaskDayProfit(@Validated QueryProfitParam queryProfitParam) {
        if (queryProfitParam.getStartTime() == null) {
            // 当月初：00:00:00
            queryProfitParam.setStartTime(
                LocalDate.now()
                    .withDayOfMonth(1)
                    .atStartOfDay()
            );
        }

        if (queryProfitParam.getEndTime() == null) {
            // 当月底：23:59:59.999999999（推荐用“下月初”做左闭右开）
            queryProfitParam.setEndTime(
                LocalDate.now()
                    .plusMonths(1)
                    .withDayOfMonth(1)
                    .atStartOfDay()
            );
        }
        CoinsAiTaskVo coinsAiTaskVo = coinsAiTaskService.queryById(queryProfitParam.getTaskId());
        if (Objects.isNull(coinsAiTaskVo)) {
            return R.fail();
        }
        Long apiId = coinsAiTaskVo.getApiId();
        Long userId = LoginHelper.getUserId();
        if(!userId.equals(coinsAiTaskVo.getCreateUserId())){
            return R.fail();
        }
        queryProfitParam.setApiId(apiId);
        Locale locale = LocaleContextHolder.getLocale();
        long startTimeStamp = queryProfitParam.getStartTime()
            .atZone(ZoneId.systemDefault()) // 关键：指定时区
            .toInstant()
            .toEpochMilli();
        long endTimeStamp = queryProfitParam.getEndTime()
            .atZone(ZoneId.systemDefault()) // 关键：指定时区
            .toInstant()
            .toEpochMilli();
        queryProfitParam.setStartTimeStamp(startTimeStamp);
        queryProfitParam.setEndTimeStamp(endTimeStamp);
        List<TaskAnalysisByDay> taskProfitByDays = coinsAiTaskService.queryTaskDayProfit(queryProfitParam);
        return R.ok("ok", taskProfitByDays);
    }


    /**
     * 导出AI任务列表
     */
    @SaCheckPermission("system:aiTask:export")
    @Log(title = "AI任务", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(CoinsAiTaskBo bo, HttpServletResponse response) {
        List<CoinsAiTaskVo> list = coinsAiTaskService.queryList(bo);
        ExcelUtil.exportExcel(list, "AI任务", CoinsAiTaskVo.class, response);
    }

    /**
     * 获取AI任务详细信息
     *
     * @param id 主键
     */
    @GetMapping("/{id}")
    public R<CoinsAiTaskVo> getInfo(@NotNull(message = "主键不能为空")
                                     @PathVariable Long id) {
        CoinsAiTaskVo coinsAiTaskVo = coinsAiTaskService.queryById(id);
        CoinsApiVo coinsApiVo = coinsApiService.queryById(coinsAiTaskVo.getApiId());
        ApiVO convert = MapstructUtils.convert(coinsApiVo, ApiVO.class);
        coinsAiTaskVo.setAccount(convert);
        if(coinsAiTaskVo.getLeverageMin()==null  || coinsAiTaskVo.getLeverageMax()==null){
            coinsAiTaskVo.setLeverage(List.of());
        }else{
            coinsAiTaskVo.setLeverage(List.of(coinsAiTaskVo.getLeverageMin(), coinsAiTaskVo.getLeverageMax()));
        }

        return R.ok(coinsAiTaskVo);
    }
    /**
     * 启动AI任务
     *
     * @param id 主键
     */
    @GetMapping("/start/{id}")
    public R<CoinsAiTaskBo> start(@NotNull(message = "主键不能为空")
                                     @PathVariable Long id) {

        CoinsAiTaskBo taskBo = coinsAiTaskService.queryBoById(id);
        if(!Objects.equals(taskBo.getCreateUserId(), LoginHelper.getUserId())){
            return R.fail();
        }
        taskBo.setStatus(AITaskStatus.RUNNING.getCode());
        taskBo.setStartTime(new Date());
        //检查余额
        coinsBalanceLogService.checkBalance(taskBo);
        coinsAiTaskService.updateByBo(taskBo);
        return R.ok(taskBo);
    }

    @SaCheckPermission("system:aiTask:invoke")
    @Log(title = "手动执行AI任务", businessType = BusinessType.FORCE)
    @PostMapping("/invokeAiTask")
    @RateLimiter(key = "#bo.id",time = 60)
    public R<CoinsAiTaskBo> invokeAiTask(@RequestBody @Validated(StartGroup.class) CoinsAiTaskBo bo) {

        CoinsAiTaskBo taskBo = coinsAiTaskService.queryBoById(bo.getId());
        if(!Objects.equals(taskBo.getCreateUserId(), LoginHelper.getUserId())){
            return R.fail();
        }
        if(!taskBo.getStatus().equals(AITaskStatus.RUNNING.getCode())){
            return R.fail("请先启动任务！");
        }
        //触发一次
        taskBo.setLastRunTime(new Date());
        coinsAiTaskService.updateByBo(taskBo);
        aiService.invokeAiTask(MapstructUtils.convert(taskBo,CoinsAiTask.class));
        return R.ok(taskBo);
    }
    /**
     * 停止AI任务
     *
     * @param id 主键
     */
    @GetMapping("/stop/{id}")
    public R<CoinsAiTaskBo> stop(@NotNull(message = "主键不能为空")
                                     @PathVariable Long id) {

        CoinsAiTaskBo taskBo = coinsAiTaskService.queryBoById(id);
        if(!Objects.equals(taskBo.getCreateUserId(), LoginHelper.getUserId())){
            return R.fail();
        }
        taskBo.setStatus(AITaskStatus.STOPD.getCode());
        taskBo.setUpdateTime(new Date());
        coinsAiTaskService.updateByBo(taskBo);
        return R.ok(taskBo);
    }

    /**
     * 新增AI任务
     */
    @Log(title = "AI任务", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody CoinsAiTaskBo bo) {
        bo.setCreateUserId(LoginHelper.getUserId());
        bo.setCreateBy(LoginHelper.getUserId());
        bo.setCreateTime(new Date());
        bo.setStatus(AITaskStatus.CREATED.getCode());
        coinsAiTaskService.checkExistsTask(bo.getApiId());
//        coinsAiTaskService.checkMaxTask(LoginHelper.getUserId(),4);

        return toAjax(coinsAiTaskService.insertByBo(bo));
    }

    /**
     * 修改AI任务
     */
    @SaCheckPermission("system:aiTask:edit")
    @Log(title = "AI任务", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody CoinsAiTaskBo bo) {
        bo.setUpdateBy(LoginHelper.getUserId());
        return toAjax(coinsAiTaskService.updateByBo(bo));
    }

    /**
     * 删除AI任务
     *
     * @param ids 主键串
     */
    @SaCheckPermission("system:aiTask:remove")
    @Log(title = "AI任务", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable Long[] ids) {
        for (Long id : ids) {
            RAtomicLong invokedTimesRedis = redissonClient.getAtomicLong("invokedTimes:"+id);
            invokedTimesRedis.delete();
        }

        return toAjax(coinsAiTaskService.deleteWithValidByIds(List.of(ids), true));
    }
}
