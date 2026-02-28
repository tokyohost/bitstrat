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
import com.bitstrat.domain.vo.CoinsRankLogVo;
import com.bitstrat.domain.bo.CoinsRankLogBo;
import com.bitstrat.service.ICoinsRankLogService;
import org.dromara.common.mybatis.core.page.TableDataInfo;

/**
 * 山寨币排行日志
 *
 * @author Lion Li
 * @date 2025-04-05
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/system/rankLog")
public class CoinsRankLogController extends BaseController {

    private final ICoinsRankLogService coinsRankLogService;

    /**
     * 查询山寨币排行日志列表
     */
    @SaCheckLogin
    @GetMapping("/list")
    public TableDataInfo<CoinsRankLogVo> list(CoinsRankLogBo bo, PageQuery pageQuery) {
        return coinsRankLogService.queryPageList(bo, pageQuery);
    }

    /**
     * 导出山寨币排行日志列表
     */
    @SaCheckLogin
    @Log(title = "山寨币排行日志", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(CoinsRankLogBo bo, HttpServletResponse response) {
        List<CoinsRankLogVo> list = coinsRankLogService.queryList(bo);
        ExcelUtil.exportExcel(list, "山寨币排行日志", CoinsRankLogVo.class, response);
    }

    /**
     * 获取山寨币排行日志详细信息
     *
     * @param id 主键
     */
    @SaCheckLogin
    @GetMapping("/{id}")
    public R<CoinsRankLogVo> getInfo(@NotNull(message = "主键不能为空")
                                     @PathVariable Long id) {
        return R.ok(coinsRankLogService.queryById(id));
    }

    /**
     * 新增山寨币排行日志
     */
    @SaCheckLogin
    @Log(title = "山寨币排行日志", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody CoinsRankLogBo bo) {
        return toAjax(coinsRankLogService.insertByBo(bo));
    }

    /**
     * 修改山寨币排行日志
     */
    @SaCheckLogin
    @Log(title = "山寨币排行日志", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody CoinsRankLogBo bo) {
        return toAjax(coinsRankLogService.updateByBo(bo));
    }

    /**
     * 删除山寨币排行日志
     *
     * @param ids 主键串
     */
    @SaCheckLogin
    @Log(title = "山寨币排行日志", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable Long[] ids) {
        return toAjax(coinsRankLogService.deleteWithValidByIds(List.of(ids), true));
    }
}
