package com.bitstrat.controller;

import java.util.List;

import com.bitstrat.domain.bo.CoinsNotifyLogBo;
import com.bitstrat.domain.vo.CoinsNotifyLogVo;
import com.bitstrat.service.ICoinsNotifyLogService;
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
import org.dromara.common.mybatis.core.page.TableDataInfo;

/**
 * 通知日志
 *
 * @author Lion Li
 * @date 2025-04-26
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/monitor/notifyLog")
public class CoinsNotifyLogController extends BaseController {

    private final ICoinsNotifyLogService coinsNotifyLogService;

    /**
     * 查询通知日志列表
     */
    @SaCheckPermission("monitor:notifyLog:list")
    @GetMapping("/list")
    public TableDataInfo<CoinsNotifyLogVo> list(CoinsNotifyLogBo bo, PageQuery pageQuery) {
        return coinsNotifyLogService.queryPageList(bo, pageQuery);
    }

    /**
     * 导出通知日志列表
     */
    @SaCheckPermission("monitor:notifyLog:export")
    @Log(title = "通知日志", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(CoinsNotifyLogBo bo, HttpServletResponse response) {
        List<CoinsNotifyLogVo> list = coinsNotifyLogService.queryList(bo);
        ExcelUtil.exportExcel(list, "通知日志", CoinsNotifyLogVo.class, response);
    }

    /**
     * 获取通知日志详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("monitor:notifyLog:query")
    @GetMapping("/{id}")
    public R<CoinsNotifyLogVo> getInfo(@NotNull(message = "主键不能为空")
                                     @PathVariable Long id) {
        return R.ok(coinsNotifyLogService.queryById(id));
    }

    /**
     * 新增通知日志
     */
    @SaCheckPermission("monitor:notifyLog:add")
    @Log(title = "通知日志", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody CoinsNotifyLogBo bo) {
        return toAjax(coinsNotifyLogService.insertByBo(bo));
    }

    /**
     * 修改通知日志
     */
    @SaCheckPermission("monitor:notifyLog:edit")
    @Log(title = "通知日志", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody CoinsNotifyLogBo bo) {
        return toAjax(coinsNotifyLogService.updateByBo(bo));
    }

    /**
     * 删除通知日志
     *
     * @param ids 主键串
     */
    @SaCheckPermission("monitor:notifyLog:remove")
    @Log(title = "通知日志", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable Long[] ids) {
        return toAjax(coinsNotifyLogService.deleteWithValidByIds(List.of(ids), true));
    }
}
