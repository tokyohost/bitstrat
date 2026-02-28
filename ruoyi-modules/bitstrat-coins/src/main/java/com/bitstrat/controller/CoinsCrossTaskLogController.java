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
import com.bitstrat.domain.vo.CoinsCrossTaskLogVo;
import com.bitstrat.domain.bo.CoinsCrossTaskLogBo;
import com.bitstrat.service.ICoinsCrossTaskLogService;
import org.dromara.common.mybatis.core.page.TableDataInfo;

/**
 * 跨交易所任务日志
 *
 * @author Lion Li
 * @date 2025-04-19
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/system/crossTaskLog")
public class CoinsCrossTaskLogController extends BaseController {

    private final ICoinsCrossTaskLogService coinsCrossTaskLogService;

    /**
     * 查询跨交易所任务日志列表
     */
    @SaCheckLogin
    @GetMapping("/list")
    public TableDataInfo<CoinsCrossTaskLogVo> list(CoinsCrossTaskLogBo bo, PageQuery pageQuery) {
        return coinsCrossTaskLogService.queryPageList(bo, pageQuery);
    }

    /**
     * 导出跨交易所任务日志列表
     */
    @SaCheckLogin
    @Log(title = "跨交易所任务日志", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(CoinsCrossTaskLogBo bo, HttpServletResponse response) {
        List<CoinsCrossTaskLogVo> list = coinsCrossTaskLogService.queryList(bo);
        ExcelUtil.exportExcel(list, "跨交易所任务日志", CoinsCrossTaskLogVo.class, response);
    }

    /**
     * 获取跨交易所任务日志详细信息
     *
     * @param id 主键
     */
    @SaCheckLogin
    @GetMapping("/{id}")
    public R<CoinsCrossTaskLogVo> getInfo(@NotNull(message = "主键不能为空")
                                     @PathVariable Long id) {
        return R.ok(coinsCrossTaskLogService.queryById(id));
    }

    /**
     * 新增跨交易所任务日志
     */
    @SaCheckLogin
    @Log(title = "跨交易所任务日志", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody CoinsCrossTaskLogBo bo) {
        return toAjax(coinsCrossTaskLogService.insertByBo(bo));
    }

    /**
     * 修改跨交易所任务日志
     */
    @SaCheckLogin
    @Log(title = "跨交易所任务日志", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody CoinsCrossTaskLogBo bo) {
        return toAjax(coinsCrossTaskLogService.updateByBo(bo));
    }

    /**
     * 删除跨交易所任务日志
     *
     * @param ids 主键串
     */
    @SaCheckPermission("system:crossTaskLog:remove")
    @Log(title = "跨交易所任务日志", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable Long[] ids) {
        return toAjax(coinsCrossTaskLogService.deleteWithValidByIds(List.of(ids), true));
    }
}
