package com.bitstrat.controller;

import java.util.List;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.bitstrat.domain.StartTaskVo;
import com.bitstrat.domain.bo.CoinsOrderBo;
import com.bitstrat.domain.vo.ArbitrageFormData;
import com.bitstrat.domain.vo.CreateArbitrageTaskVo;
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
import com.bitstrat.domain.vo.CoinsCrossExchangeArbitrageTaskVo;
import com.bitstrat.domain.bo.CoinsCrossExchangeArbitrageTaskBo;
import com.bitstrat.service.ICoinsCrossExchangeArbitrageTaskService;
import org.dromara.common.mybatis.core.page.TableDataInfo;

/**
 * 跨交易所套利任务
 *
 * @author Lion Li
 * @date 2025-04-19
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/system/crossExchangeArbitrageTask")
public class CoinsCrossExchangeArbitrageTaskController extends BaseController {

    private final ICoinsCrossExchangeArbitrageTaskService coinsCrossExchangeArbitrageTaskService;


    @SaCheckLogin
    @PostMapping("/createTask")
    public R createTask(@RequestBody CreateArbitrageTaskVo createArbitrageTaskVo) {

        return coinsCrossExchangeArbitrageTaskService.createTask(createArbitrageTaskVo);
    }
    @SaCheckLogin
    @PostMapping("/startTask")
    public R startTask(@RequestBody StartTaskVo task) {

        return coinsCrossExchangeArbitrageTaskService.startTask(task);
    }

    /**
     * 新增订单列表
     */
    @SaCheckLogin
    @Log(title = "创建订单", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping("/createOrder")
    public R<Void> createOrder(@RequestBody CreateArbitrageTaskVo createArbitrageTaskVo) {
        return coinsCrossExchangeArbitrageTaskService.createCrossArbitrage(createArbitrageTaskVo);
    }
    /**
     * 平仓
     */
    @SaCheckLogin
    @Log(title = "平仓", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping("/closePosition")
    public R<Void> closePosition(@RequestBody CreateArbitrageTaskVo createArbitrageTaskVo) {
        return coinsCrossExchangeArbitrageTaskService.closePosition(createArbitrageTaskVo);
    }

    /**
     * 查询跨交易所套利任务列表
     */
    @SaCheckPermission("system:crossExchangeArbitrageTask:list")
    @GetMapping("/syncTask")
    public R syncTask(Long taskId) {
        return coinsCrossExchangeArbitrageTaskService.syncTask(taskId);
    }
    /**
     * 查询跨交易所套利任务列表
     */
    @SaCheckPermission("system:crossExchangeArbitrageTask:list")
    @GetMapping("/list")
    public TableDataInfo<CoinsCrossExchangeArbitrageTaskVo> list(CoinsCrossExchangeArbitrageTaskBo bo, PageQuery pageQuery) {
//        if (!LoginHelper.isSuperAdmin()) {
        bo.setUserId(LoginHelper.getUserId());
//        }
//        pageQuery.setOrderByColumn("status");
//        pageQuery.setIsAsc("asc");
        TableDataInfo<CoinsCrossExchangeArbitrageTaskVo> coinsCrossExchangeArbitrageTaskVoTableDataInfo = coinsCrossExchangeArbitrageTaskService.queryPageList(bo, pageQuery);
        List<CoinsCrossExchangeArbitrageTaskVo> rows = coinsCrossExchangeArbitrageTaskVoTableDataInfo.getRows();

        coinsCrossExchangeArbitrageTaskVoTableDataInfo.setRows(coinsCrossExchangeArbitrageTaskService.formateSize(rows,LoginHelper.getUserId()));

        coinsCrossExchangeArbitrageTaskVoTableDataInfo.setRows(coinsCrossExchangeArbitrageTaskService.formateARY(rows,LoginHelper.getUserId()));

        return coinsCrossExchangeArbitrageTaskVoTableDataInfo;
    }

    /**
     * 导出跨交易所套利任务列表
     */
    @SaCheckPermission("system:crossExchangeArbitrageTask:export")
    @Log(title = "跨交易所套利任务", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(CoinsCrossExchangeArbitrageTaskBo bo, HttpServletResponse response) {
        if(!LoginHelper.isSuperAdmin()) {
            bo.setUserId(LoginHelper.getUserId());
        }
        List<CoinsCrossExchangeArbitrageTaskVo> list = coinsCrossExchangeArbitrageTaskService.queryList(bo);
        ExcelUtil.exportExcel(list, "跨交易所套利任务", CoinsCrossExchangeArbitrageTaskVo.class, response);
    }

    /**
     * 获取跨交易所套利任务详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("system:crossExchangeArbitrageTask:query")
    @GetMapping("/{id}")
    public R<CoinsCrossExchangeArbitrageTaskVo> getInfo(@NotNull(message = "主键不能为空")
                                     @PathVariable Long id) {
        CoinsCrossExchangeArbitrageTaskVo coinsCrossExchangeArbitrageTaskVo = coinsCrossExchangeArbitrageTaskService.queryById(id);
        CoinsCrossExchangeArbitrageTaskVo data = coinsCrossExchangeArbitrageTaskService.formateSize(coinsCrossExchangeArbitrageTaskVo, LoginHelper.getUserId());
        return R.ok(data);
    }

    /**
     * 新增跨交易所套利任务
     */
    @SaCheckPermission("system:crossExchangeArbitrageTask:add")
    @Log(title = "跨交易所套利任务", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody CoinsCrossExchangeArbitrageTaskBo bo) {
        return toAjax(coinsCrossExchangeArbitrageTaskService.insertByBo(bo));
    }

    /**
     * 修改跨交易所套利任务
     */
    @SaCheckPermission("system:crossExchangeArbitrageTask:edit")
    @Log(title = "跨交易所套利任务", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody CoinsCrossExchangeArbitrageTaskBo bo) {
        return toAjax(coinsCrossExchangeArbitrageTaskService.updateByBo(bo));
    }

    /**
     * 删除跨交易所套利任务
     *
     * @param ids 主键串
     */
    @SaCheckPermission("system:crossExchangeArbitrageTask:remove")
    @Log(title = "跨交易所套利任务", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable Long[] ids) {
        return toAjax(coinsCrossExchangeArbitrageTaskService.deleteWithValidByIds(List.of(ids), true));
    }
}
