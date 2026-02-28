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
import com.bitstrat.domain.vo.CoinsFeedLikeLogVo;
import com.bitstrat.domain.bo.CoinsFeedLikeLogBo;
import com.bitstrat.service.ICoinsFeedLikeLogService;
import org.dromara.common.mybatis.core.page.TableDataInfo;

/**
 * 策略广场like日志
 *
 * @author Lion Li
 * @date 2025-12-12
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/system/feedLikeLog")
public class CoinsFeedLikeLogController extends BaseController {

    private final ICoinsFeedLikeLogService coinsFeedLikeLogService;

    /**
     * 查询策略广场like日志列表
     */
    @SaCheckPermission("system:feedLikeLog:list")
    @GetMapping("/list")
    public TableDataInfo<CoinsFeedLikeLogVo> list(CoinsFeedLikeLogBo bo, PageQuery pageQuery) {
        return coinsFeedLikeLogService.queryPageList(bo, pageQuery);
    }

    /**
     * 导出策略广场like日志列表
     */
    @SaCheckPermission("system:feedLikeLog:export")
    @Log(title = "策略广场like日志", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(CoinsFeedLikeLogBo bo, HttpServletResponse response) {
        List<CoinsFeedLikeLogVo> list = coinsFeedLikeLogService.queryList(bo);
        ExcelUtil.exportExcel(list, "策略广场like日志", CoinsFeedLikeLogVo.class, response);
    }

    /**
     * 获取策略广场like日志详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("system:feedLikeLog:query")
    @GetMapping("/{id}")
    public R<CoinsFeedLikeLogVo> getInfo(@NotNull(message = "主键不能为空")
                                     @PathVariable Long id) {
        return R.ok(coinsFeedLikeLogService.queryById(id));
    }

    /**
     * 新增策略广场like日志
     */
    @SaCheckPermission("system:feedLikeLog:add")
    @Log(title = "策略广场like日志", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody CoinsFeedLikeLogBo bo) {
        return toAjax(coinsFeedLikeLogService.insertByBo(bo));
    }

    /**
     * 修改策略广场like日志
     */
    @SaCheckPermission("system:feedLikeLog:edit")
    @Log(title = "策略广场like日志", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody CoinsFeedLikeLogBo bo) {
        return toAjax(coinsFeedLikeLogService.updateByBo(bo));
    }

    /**
     * 删除策略广场like日志
     *
     * @param ids 主键串
     */
    @SaCheckPermission("system:feedLikeLog:remove")
    @Log(title = "策略广场like日志", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable Long[] ids) {
        return toAjax(coinsFeedLikeLogService.deleteWithValidByIds(List.of(ids), true));
    }
}
