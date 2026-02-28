package com.bitstrat.controller;

import java.util.List;

import cn.dev33.satoken.annotation.SaCheckLogin;
import lombok.RequiredArgsConstructor;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.*;
import cn.dev33.satoken.annotation.SaCheckPermission;
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
import com.bitstrat.domain.vo.CoinsTaskLogVo;
import com.bitstrat.domain.bo.CoinsTaskLogBo;
import com.bitstrat.service.ICoinsTaskLogService;
import org.dromara.common.mybatis.core.page.TableDataInfo;

/**
 * 任务买入卖出日志
 *
 * @author Lion Li
 * @date 2025-04-01
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/system/taskLog")
public class CoinsTaskLogController extends BaseController {

    private final ICoinsTaskLogService coinsTaskLogService;

    /**
     * 查询任务买入卖出日志列表
     */
    @SaCheckLogin
    @GetMapping("/list")
    public TableDataInfo<CoinsTaskLogVo> list(CoinsTaskLogBo bo, PageQuery pageQuery) {
        return coinsTaskLogService.queryPageList(bo, pageQuery);
    }

    /**
     * 导出任务买入卖出日志列表
     */
    @SaCheckLogin
    @Log(title = "任务买入卖出日志", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(CoinsTaskLogBo bo, HttpServletResponse response) {
        List<CoinsTaskLogVo> list = coinsTaskLogService.queryList(bo);
        ExcelUtil.exportExcel(list, "任务买入卖出日志", CoinsTaskLogVo.class, response);
    }

    /**
     * 获取任务买入卖出日志详细信息
     *
     * @param id 主键
     */
    @SaCheckLogin
    @GetMapping("/{id}")
    public R<CoinsTaskLogVo> getInfo(@NotNull(message = "主键不能为空")
                                     @PathVariable Long id) {
        return R.ok(coinsTaskLogService.queryById(id));
    }

    /**
     * 新增任务买入卖出日志
     */
    @SaCheckLogin
    @Log(title = "任务买入卖出日志", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody CoinsTaskLogBo bo) {
        return toAjax(coinsTaskLogService.insertByBo(bo));
    }

    /**
     * 修改任务买入卖出日志
     */
    @SaCheckLogin
    @Log(title = "任务买入卖出日志", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody CoinsTaskLogBo bo) {
        return toAjax(coinsTaskLogService.updateByBo(bo));
    }

    /**
     * 删除任务买入卖出日志
     *
     * @param ids 主键串
     */
    @SaCheckLogin
    @Log(title = "任务买入卖出日志", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable Long[] ids) {
        return toAjax(coinsTaskLogService.deleteWithValidByIds(List.of(ids), true));
    }
}
