package com.bitstrat.controller;

import java.util.List;

import com.bitstrat.domain.bo.CoinsArbitrageWarningConfigBo;
import com.bitstrat.domain.vo.CoinsArbitrageWarningConfigVo;
import com.bitstrat.service.ICoinsArbitrageWarningConfigService;
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
 * 用户配置套利警告
 *
 * @author Lion Li
 * @date 2025-05-04
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/system/arbitrageWarningConfig")
public class CoinsArbitrageWarningConfigController extends BaseController {

    private final ICoinsArbitrageWarningConfigService coinsArbitrageWarningConfigService;

    /**
     * 查询用户配置套利警告列表
     */
    @SaCheckPermission("system:arbitrageWarningConfig:list")
    @GetMapping("/list")
    public TableDataInfo<CoinsArbitrageWarningConfigVo> list(CoinsArbitrageWarningConfigBo bo, PageQuery pageQuery) {
        return coinsArbitrageWarningConfigService.queryPageList(bo, pageQuery);
    }

    /**
     * 导出用户配置套利警告列表
     */
    @SaCheckPermission("system:arbitrageWarningConfig:export")
    @Log(title = "用户配置套利警告", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(CoinsArbitrageWarningConfigBo bo, HttpServletResponse response) {
        List<CoinsArbitrageWarningConfigVo> list = coinsArbitrageWarningConfigService.queryList(bo);
        ExcelUtil.exportExcel(list, "用户配置套利警告", CoinsArbitrageWarningConfigVo.class, response);
    }

    /**
     * 获取用户配置套利警告详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("system:arbitrageWarningConfig:query")
    @GetMapping("/{id}")
    public R<CoinsArbitrageWarningConfigVo> getInfo(@NotNull(message = "主键不能为空")
                                     @PathVariable Long id) {
        return R.ok(coinsArbitrageWarningConfigService.queryById(id));
    }


    /**
     * 获取用户配置套利警告详细信息
     *
     */
    @GetMapping("/getByTaskId/{arbitrageType}/{taskId}")
    public R<CoinsArbitrageWarningConfigVo> getByTaskId(@PathVariable("arbitrageType") Integer arbitrageType,
                                                    @PathVariable("taskId") Long taskId) {
        return R.ok(coinsArbitrageWarningConfigService.getByTaskId(arbitrageType, taskId));
    }

    /**
     * 新增用户配置套利警告
     */
    @SaCheckPermission("system:arbitrageWarningConfig:add")
    @Log(title = "用户配置套利警告", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody CoinsArbitrageWarningConfigBo bo) {
        return toAjax(coinsArbitrageWarningConfigService.insertByBo(bo));
    }

    /**
     * 修改用户配置套利警告
     */
    @SaCheckPermission("system:arbitrageWarningConfig:edit")
    @Log(title = "用户配置套利警告", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody CoinsArbitrageWarningConfigBo bo) {
        return toAjax(coinsArbitrageWarningConfigService.updateByBo(bo));
    }

    /**
     * 删除用户配置套利警告
     *
     * @param ids 主键串
     */
    @SaCheckPermission("system:arbitrageWarningConfig:remove")
    @Log(title = "用户配置套利警告", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable Long[] ids) {
        return toAjax(coinsArbitrageWarningConfigService.deleteWithValidByIds(List.of(ids), true));
    }
}
