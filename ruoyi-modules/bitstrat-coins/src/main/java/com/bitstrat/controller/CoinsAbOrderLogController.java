package com.bitstrat.controller;

import java.util.List;

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
import com.bitstrat.domain.vo.CoinsAbOrderLogVo;
import com.bitstrat.domain.bo.CoinsAbOrderLogBo;
import com.bitstrat.service.ICoinsAbOrderLogService;
import org.dromara.common.mybatis.core.page.TableDataInfo;

/**
 * 价差套利日志
 *
 * @author Lion Li
 * @date 2025-06-08
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/system/abOrderLog")
public class CoinsAbOrderLogController extends BaseController {

    private final ICoinsAbOrderLogService coinsAbOrderLogService;

    /**
     * 查询价差套利日志列表
     */
    @SaCheckPermission("system:abOrderLog:list")
    @GetMapping("/list")
    public TableDataInfo<CoinsAbOrderLogVo> list(CoinsAbOrderLogBo bo, PageQuery pageQuery) {
        return coinsAbOrderLogService.queryPageList(bo, pageQuery);
    }

    /**
     * 导出价差套利日志列表
     */
    @SaCheckPermission("system:abOrderLog:export")
    @Log(title = "价差套利日志", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(CoinsAbOrderLogBo bo, HttpServletResponse response) {
        List<CoinsAbOrderLogVo> list = coinsAbOrderLogService.queryList(bo);
        ExcelUtil.exportExcel(list, "价差套利日志", CoinsAbOrderLogVo.class, response);
    }

    /**
     * 获取价差套利日志详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("system:abOrderLog:query")
    @GetMapping("/{id}")
    public R<CoinsAbOrderLogVo> getInfo(@NotNull(message = "主键不能为空")
                                     @PathVariable Long id) {
        return R.ok(coinsAbOrderLogService.queryById(id));
    }

    /**
     * 新增价差套利日志
     */
    @SaCheckPermission("system:abOrderLog:add")
    @Log(title = "价差套利日志", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody CoinsAbOrderLogBo bo) {
        return toAjax(coinsAbOrderLogService.insertByBo(bo));
    }

    /**
     * 修改价差套利日志
     */
    @SaCheckPermission("system:abOrderLog:edit")
    @Log(title = "价差套利日志", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody CoinsAbOrderLogBo bo) {
        return toAjax(coinsAbOrderLogService.updateByBo(bo));
    }

    /**
     * 删除价差套利日志
     *
     * @param ids 主键串
     */
    @SaCheckPermission("system:abOrderLog:remove")
    @Log(title = "价差套利日志", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable Long[] ids) {
        return toAjax(coinsAbOrderLogService.deleteWithValidByIds(List.of(ids), true));
    }
}
