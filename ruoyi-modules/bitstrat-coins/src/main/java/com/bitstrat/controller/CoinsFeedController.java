package com.bitstrat.controller;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.bitstrat.constant.FeedStatus;
import com.bitstrat.domain.bo.CoinAITaskBalanceBo;
import com.bitstrat.domain.bo.CoinsFeedLikeLogBo;
import com.bitstrat.domain.vo.CoinAITaskBalanceVo;
import com.bitstrat.domain.vo.CoinsAiTaskVo;
import com.bitstrat.domain.vo.CoinsFeedLikeLogVo;
import com.bitstrat.service.ICoinAITaskBalanceService;
import com.bitstrat.service.ICoinsAiTaskService;
import com.bitstrat.service.ICoinsFeedLikeLogService;
import com.bitstrat.task.CalcFeedPostProfit;
import lombok.RequiredArgsConstructor;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.*;
import cn.dev33.satoken.annotation.SaCheckPermission;
import org.dromara.common.satoken.utils.LoginHelper;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.transaction.annotation.Transactional;
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
import com.bitstrat.domain.vo.CoinsFeedVo;
import com.bitstrat.domain.bo.CoinsFeedBo;
import com.bitstrat.service.ICoinsFeedService;
import org.dromara.common.mybatis.core.page.TableDataInfo;

/**
 * 策略广场
 *
 * @author Lion Li
 * @date 2025-12-12
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/system/feed")
public class CoinsFeedController extends BaseController {

    private final ICoinsFeedService coinsFeedService;
    private final ICoinAITaskBalanceService coinTestAiService;
    private final ICoinsAiTaskService coinsAiTaskService;
    private final CalcFeedPostProfit calcFeedPostProfit;
    private final ICoinsFeedLikeLogService coinsFeedLikeLogService;
    /**
     * 查询策略广场列表
     */

    @GetMapping("/list")
    public TableDataInfo<CoinsFeedVo> list(CoinsFeedBo bo, PageQuery pageQuery) {
        bo.setStatus(FeedStatus.PUBLISH.getStatus());
        pageQuery.setOrderByColumn("sort");

        return coinsFeedService.queryPageList(bo, pageQuery);
    }

    /**
     * 导出策略广场列表
     */
    @SaCheckPermission("system:feed:export")
    @Log(title = "策略广场", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(CoinsFeedBo bo, HttpServletResponse response) {
        List<CoinsFeedVo> list = coinsFeedService.queryList(bo);
        ExcelUtil.exportExcel(list, "策略广场", CoinsFeedVo.class, response);
    }

    /**
     * 获取策略广场详细信息
     *
     * @param id 主键
     */
    @GetMapping("/{id}")
    public R<CoinsFeedVo> getInfo(@NotNull(message = "主键不能为空")
                                     @PathVariable Long id) {
        Long userId = LoginHelper.getUserId();
        CoinsFeedVo coinsFeedVo = coinsFeedService.queryById(id);
        if(coinsFeedVo.getUserId().longValue() == userId.longValue()){
            return R.ok(coinsFeedVo);
        }
        return R.ok();
    }
    /**
     * 停止分享
     *
     * @param id 主键
     */
    @GetMapping("/stop/{id}")
    public R<Void> stop(@NotNull(message = "ID不能为空")
                                     @PathVariable Long id) {
        Long userId = LoginHelper.getUserId();
        CoinsFeedVo coinsFeedVo = coinsFeedService.queryById(id);
        if(coinsFeedVo.getUserId().longValue() == userId.longValue()){
            CoinsFeedBo feedBo = new CoinsFeedBo();
            feedBo.setId(id);
            feedBo.setStatus(FeedStatus.DELETED.getStatus());
            return toAjax(coinsFeedService.updateByBo(feedBo));
        }
        return R.fail("ERROR PARAMS");
    }
    /**
     * like分享
     *
     * @param id 主键
     */
    @GetMapping("/like/{id}")
    @Transactional
    public R<Void> like(@NotNull(message = "ID不能为空")
                                     @PathVariable Long id) {
        Long userId = LoginHelper.getUserId();
        CoinsFeedLikeLogVo coinsFeedLikeLogVo = coinsFeedLikeLogService.selectLogByFeedIdAndUserId(id, userId);
        if(Objects.isNull(coinsFeedLikeLogVo)){
            CoinsFeedVo coinsFeedVo = coinsFeedService.queryById(id);
            coinsFeedService.updateLikeCountById(coinsFeedVo.getId(),1);
            CoinsFeedLikeLogBo coinsFeedLikeLogBo = new CoinsFeedLikeLogBo();
            coinsFeedLikeLogBo.setUserId(userId);
            coinsFeedLikeLogBo.setFeedId(id);
            coinsFeedLikeLogBo.setCreateTime(new Date());
            return toAjax(coinsFeedLikeLogService.insertByBo(coinsFeedLikeLogBo));
        }else{
            coinsFeedService.updateLikeCountById(coinsFeedLikeLogVo.getFeedId(),-1);
            coinsFeedLikeLogService.deleteWithValidByIds(List.of(coinsFeedLikeLogVo.getId()),false);
        }

        return R.ok("ok");
    }



    /**
     * 新增策略广场
     */
    @Log(title = "策略广场", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody CoinsFeedBo bo) {
        bo.setUserId(LoginHelper.getUserId());
        bo.setStatus(FeedStatus.PUBLISH.getStatus());
        bo.setSort(0L);
        bo.setLikeCount(0L);
        bo.setShareTime(ZonedDateTime.now());
        CoinsAiTaskVo coinsAiTaskVo = coinsAiTaskService.queryById(bo.getStrategyId());
        if(Objects.nonNull(coinsAiTaskVo) && coinsAiTaskVo.getCreateUserId().longValue() == LoginHelper.getUserId().longValue()){
            Boolean b = coinsFeedService.insertByBo(bo);
            CoinsFeedVo coinsFeedVo = coinsFeedService.queryById(bo.getId());
            calcFeedPostProfit.calc(coinsFeedVo);
            return toAjax(b);
        }else{
            return R.fail("ERROR PARAMS");
        }
    }


    /**
     * 查询AI 测试趋势列表（精简版）
     */
    @GetMapping("/loadFeedChartDataSimple")
    @Cacheable(value = "loadFeedChartDataSimple:cache#300s#300s#2000", key = "'taskId'+':'+#bo.id")
    public R<com.alibaba.fastjson2.JSONObject> loadFeedChartDataSimple(CoinAITaskBalanceBo bo, PageQuery pageQuery) {
        bo.setCreateBy(LoginHelper.getUserId());
        if (bo.getId() == null) {
            return R.fail("params error");
        }
        ZonedDateTime now = ZonedDateTime.now();
        bo.setStartDate(now.minusMonths(3)); //最近三个月
        bo.setEndDate(now);
        CoinsFeedVo coinsFeedVo = coinsFeedService.queryById(bo.getId());
        if(!FeedStatus.PUBLISH.getStatus().equals(coinsFeedVo.getStatus())){
            return R.ok("ok", new JSONObject());
        }
        CoinAITaskBalanceBo queryParams = new CoinAITaskBalanceBo();
        queryParams.setTaskId(coinsFeedVo.getStrategyId());
        queryParams.setStartDate(bo.getStartDate());
        queryParams.setEndDate(bo.getEndDate());
        // 查询原始数据
        List<CoinAITaskBalanceVo> voList = coinTestAiService.queryList(queryParams);

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

    /**
     * 修改策略广场
     */
    @Log(title = "策略广场", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody CoinsFeedBo bo) {
        Long userId = LoginHelper.getUserId();
        CoinsFeedVo coinsFeedVo = coinsFeedService.queryById(bo.getId());
        if(coinsFeedVo.getUserId().longValue() == userId.longValue()){
            bo.setShareTime(null);
            bo.setLikeCount(null);
            bo.setProfit3m(null);
            bo.setViewCount(null);
            bo.setUserId(LoginHelper.getUserId());
            return toAjax(coinsFeedService.updateByBo(bo));
        }

        return R.fail();

    }

    /**
     * 删除策略广场
     *
     * @param ids 主键串
     */
    @SaCheckPermission("system:feed:remove")
    @Log(title = "策略广场", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable Long[] ids) {
        return toAjax(coinsFeedService.deleteWithValidByIds(List.of(ids), true));
    }
}
