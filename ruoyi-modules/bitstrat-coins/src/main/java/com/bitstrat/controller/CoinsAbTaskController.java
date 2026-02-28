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
import com.bitstrat.domain.vo.CoinsAbTaskVo;
import com.bitstrat.domain.bo.CoinsAbTaskBo;
import com.bitstrat.service.ICoinsAbTaskService;
import org.dromara.common.mybatis.core.page.TableDataInfo;

/**
 * 价差套利任务
 *
 * @author Lion Li
 * @date 2025-06-08
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/system/abTask")
public class CoinsAbTaskController extends BaseController {

    private final ICoinsAbTaskService coinsAbTaskService;

    /**
     * 查询价差套利任务列表
     */
    @SaCheckPermission("system:abTask:list")
    @GetMapping("/list")
    public TableDataInfo<CoinsAbTaskVo> list(CoinsAbTaskBo bo, PageQuery pageQuery) {
        return coinsAbTaskService.queryPageList(bo, pageQuery);
    }

    /**
     * 导出价差套利任务列表
     */
    @SaCheckPermission("system:abTask:export")
    @Log(title = "价差套利任务", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(CoinsAbTaskBo bo, HttpServletResponse response) {
        List<CoinsAbTaskVo> list = coinsAbTaskService.queryList(bo);
        ExcelUtil.exportExcel(list, "价差套利任务", CoinsAbTaskVo.class, response);
    }

    /**
     * 获取价差套利任务详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("system:abTask:query")
    @GetMapping("/{id}")
    public R<CoinsAbTaskVo> getInfo(@NotNull(message = "主键不能为空")
                                     @PathVariable Long id) {
        return R.ok(coinsAbTaskService.queryById(id));
    }

    /**
     * 新增价差套利任务
     */
    @SaCheckPermission("system:abTask:add")
    @Log(title = "价差套利任务", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody CoinsAbTaskBo bo) {
        return toAjax(coinsAbTaskService.insertByBo(bo));
    }

    /**
     * 修改价差套利任务
     */
    @SaCheckPermission("system:abTask:edit")
    @Log(title = "价差套利任务", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody CoinsAbTaskBo bo) {
        return toAjax(coinsAbTaskService.updateByBo(bo));
    }

    /**
     * 删除价差套利任务
     *
     * @param ids 主键串
     */
    @SaCheckPermission("system:abTask:remove")
    @Log(title = "价差套利任务", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable Long[] ids) {
        return toAjax(coinsAbTaskService.deleteWithValidByIds(List.of(ids), true));
    }
}
