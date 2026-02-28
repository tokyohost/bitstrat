package com.bitstrat.controller;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import lombok.RequiredArgsConstructor;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.*;
import cn.dev33.satoken.annotation.SaCheckPermission;
import org.dromara.common.satoken.utils.LoginHelper;
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
import com.bitstrat.domain.vo.CoinAITaskBalanceVo;
import com.bitstrat.domain.bo.CoinAITaskBalanceBo;
import com.bitstrat.service.ICoinAITaskBalanceService;
import org.dromara.common.mybatis.core.page.TableDataInfo;

/**
 * AI 测试趋势
 *
 * @author Lion Li
 * @date 2025-10-29
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/system/aiLogs")
public class CoinTestAiController extends BaseController {

    private final ICoinAITaskBalanceService coinTestAiService;

    /**
     * 查询AI 测试趋势列表
     */
    @SaCheckPermission("system:aiLogs:list")
    @GetMapping("/list")
    public TableDataInfo<CoinAITaskBalanceVo> list(CoinAITaskBalanceBo bo, PageQuery pageQuery) {
        return coinTestAiService.queryPageList(bo, pageQuery);
    }
    /**
     * 查询AI 测试趋势列表
     */
    @SaCheckPermission("system:aiLogs:list")
    @GetMapping("/loadChartData")
    public R<com.alibaba.fastjson2.JSONObject> loadChartData(CoinAITaskBalanceBo bo, PageQuery pageQuery) {
        bo.setCreateBy(LoginHelper.getUserId());
        if (bo.getTaskId() == null) {
            return R.fail("params error");
        }
        List<CoinAITaskBalanceVo> coinAITaskBalanceVos = coinTestAiService.queryList(bo);
        List<CoinAITaskBalanceVo> collect = coinAITaskBalanceVos.stream().sorted(Comparator.comparing(CoinAITaskBalanceVo::getTime)).collect(Collectors.toList());
        /**
         * const xData = ref(['2025-10-20', '2025-10-21', '2025-10-22', '2025-10-23']);
         * const seriesData = ref([
         *   { name: 'equity', data: [100, 200, 150, 300], smooth: true, area: true },
         *   { name: 'freeBalance', data: [80, 120, 180, 210], smooth: true }
         * ]);
         */
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        JSONArray xData = new JSONArray();
        JSONArray equityData = new JSONArray();
        JSONArray fireeBalanceData = new JSONArray();
        for (CoinAITaskBalanceVo coinAITaskBalanceVo : collect) {
            xData.add(simpleDateFormat.format(coinAITaskBalanceVo.getTime()));
            if (Objects.nonNull(coinAITaskBalanceVo.getEquity())) {
                equityData.add(coinAITaskBalanceVo.getEquity().setScale(3, BigDecimal.ROUND_HALF_UP));
            }else{
                equityData.add(BigDecimal.valueOf(Long.parseLong("-1")));
            }
            if (Objects.nonNull(coinAITaskBalanceVo.getFreeBalance())) {
                fireeBalanceData.add(coinAITaskBalanceVo.getFreeBalance().setScale(3, BigDecimal.ROUND_HALF_UP));
            }else{
                fireeBalanceData.add(BigDecimal.valueOf(Long.parseLong("-1")));
            }
        }
        JSONObject equity = new JSONObject();
        equity.put("name", "equity");
        equity.put("data", equityData);
        equity.put("smooth",true);
        equity.put("area", true);
//        JSONObject freeBalance = new JSONObject();
//        freeBalance.put("name", "freeBalance");
//        freeBalance.put("data", fireeBalanceData);
//        freeBalance.put("smooth",true);

        JSONArray seriesData = new JSONArray();
        seriesData.add(equity);
//        seriesData.add(freeBalance);

        JSONObject result = new JSONObject();
        result.put("xData", xData);
        result.put("seriesData", seriesData);
        return R.ok(result);
    }


/**
     * 查询AI 测试趋势列表
     */
    @SaCheckPermission("system:aiLogs:list")
    @GetMapping("/loadChartDataFreeBalance")
    public R<com.alibaba.fastjson2.JSONObject> loadChartDataFreeBalance(CoinAITaskBalanceBo bo, PageQuery pageQuery) {
        bo.setCreateBy(LoginHelper.getUserId());
        if (bo.getTaskId() == null) {
            return R.fail("params error");
        }

        List<CoinAITaskBalanceVo> coinAITaskBalanceVos = coinTestAiService.queryList(bo);
        List<CoinAITaskBalanceVo> collect = coinAITaskBalanceVos.stream().sorted(Comparator.comparing(CoinAITaskBalanceVo::getTime)).collect(Collectors.toList());
        /**
         * const xData = ref(['2025-10-20', '2025-10-21', '2025-10-22', '2025-10-23']);
         * const seriesData = ref([
         *   { name: 'equity', data: [100, 200, 150, 300], smooth: true, area: true },
         *   { name: 'freeBalance', data: [80, 120, 180, 210], smooth: true }
         * ]);
         */
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        JSONArray xData = new JSONArray();
        JSONArray equityData = new JSONArray();
        JSONArray fireeBalanceData = new JSONArray();
        for (CoinAITaskBalanceVo coinAITaskBalanceVo : collect) {
            xData.add(simpleDateFormat.format(coinAITaskBalanceVo.getTime()));
            if (Objects.nonNull(coinAITaskBalanceVo.getEquity())) {
                equityData.add(coinAITaskBalanceVo.getEquity().setScale(3, BigDecimal.ROUND_HALF_UP));
            }else{
                equityData.add(BigDecimal.valueOf(Long.parseLong("-1")));
            }
            if (Objects.nonNull(coinAITaskBalanceVo.getFreeBalance())) {
                fireeBalanceData.add(coinAITaskBalanceVo.getFreeBalance().setScale(3, BigDecimal.ROUND_HALF_UP));
            }else{
                fireeBalanceData.add(BigDecimal.valueOf(Long.parseLong("-1")));
            }
        }
        JSONObject equity = new JSONObject();
        equity.put("name", "equity");
        equity.put("data", equityData);
        equity.put("smooth",true);
        equity.put("area", true);
        JSONObject freeBalance = new JSONObject();
        freeBalance.put("name", "freeBalance");
        freeBalance.put("data", fireeBalanceData);
        freeBalance.put("smooth",true);

        JSONArray seriesData = new JSONArray();
//        seriesData.add(equity);
        seriesData.add(freeBalance);

        JSONObject result = new JSONObject();
        result.put("xData", xData);
        result.put("seriesData", seriesData);
        return R.ok(result);
    }

    /**
     * 导出AI 测试趋势列表
     */
    @SaCheckPermission("system:aiLogs:export")
    @Log(title = "AI 测试趋势", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(CoinAITaskBalanceBo bo, HttpServletResponse response) {
        List<CoinAITaskBalanceVo> list = coinTestAiService.queryList(bo);
        ExcelUtil.exportExcel(list, "AI 测试趋势", CoinAITaskBalanceVo.class, response);
    }

    /**
     * 获取AI 测试趋势详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("system:aiLogs:query")
    @GetMapping("/{id}")
    public R<CoinAITaskBalanceVo> getInfo(@NotNull(message = "主键不能为空")
                                     @PathVariable Long id) {
        return R.ok(coinTestAiService.queryById(id));
    }

    /**
     * 新增AI 测试趋势
     */
    @SaCheckPermission("system:aiLogs:add")
    @Log(title = "AI 测试趋势", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody CoinAITaskBalanceBo bo) {
        return toAjax(coinTestAiService.insertByBo(bo));
    }

    /**
     * 修改AI 测试趋势
     */
    @SaCheckPermission("system:aiLogs:edit")
    @Log(title = "AI 测试趋势", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody CoinAITaskBalanceBo bo) {
        return toAjax(coinTestAiService.updateByBo(bo));
    }

    /**
     * 删除AI 测试趋势
     *
     * @param ids 主键串
     */
    @SaCheckPermission("system:aiLogs:remove")
    @Log(title = "AI 测试趋势", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable Long[] ids) {
        return toAjax(coinTestAiService.deleteWithValidByIds(List.of(ids), true));
    }
}
