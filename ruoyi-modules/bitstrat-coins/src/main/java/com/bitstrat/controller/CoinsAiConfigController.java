package com.bitstrat.controller;


import cn.dev33.satoken.annotation.SaCheckLogin;
import com.bitstrat.domain.CoinsAiConfig;
import com.bitstrat.domain.vo.CoinsAiConfigSelectVo;
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
import com.bitstrat.domain.vo.CoinsAiConfigVo;
import com.bitstrat.domain.bo.CoinsAiConfigBo;
import com.bitstrat.service.ICoinsAiConfigService;
import org.dromara.common.mybatis.core.page.TableDataInfo;

import java.util.List;
import java.util.stream.Collectors;

/**
 * ai 流水线配置
 *
 * @author Lion Li
 * @date 2025-04-01
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/system/aiConfig")
public class CoinsAiConfigController extends BaseController {

    private final ICoinsAiConfigService coinsAiConfigService;

    /**
     * 查询ai 流水线配置列表
     */
    @SaCheckPermission("system:aiConfig:list")
    @GetMapping("/list")
    public TableDataInfo<CoinsAiConfigVo> list(CoinsAiConfigBo bo, PageQuery pageQuery) {
        TableDataInfo<CoinsAiConfigVo> dataInfo = coinsAiConfigService.queryPageList(bo, pageQuery);
        List<CoinsAiConfigVo> rows = dataInfo.getRows();
        for (CoinsAiConfigVo row : rows) {
            row.setToken("***");
            row.setUrl("***");
        }

        return dataInfo;
    }
    /**
     * 查询ai 流水线配置列表
     */
    @SaCheckLogin
    @GetMapping("/list4Select")
    public R<List<CoinsAiConfigSelectVo>> list4Select(CoinsAiConfigBo bo) {
        List<CoinsAiConfigVo> coinsAiConfigVos = coinsAiConfigService.querySelectList(bo);
        List<CoinsAiConfigSelectVo> collect = coinsAiConfigVos.stream().map(CoinsAiConfigSelectVo::objToVo).collect(Collectors.toList());
        return R.ok(collect);
    }

    /**
     * 导出ai 流水线配置列表
     */
    @SaCheckPermission("system:aiConfig:export")
    @Log(title = "ai 流水线配置", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(CoinsAiConfigBo bo, HttpServletResponse response) {
        List<CoinsAiConfigVo> list = coinsAiConfigService.queryList(bo);
        ExcelUtil.exportExcel(list, "ai 流水线配置", CoinsAiConfigVo.class, response);
    }

    /**
     * 获取ai 流水线配置详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("system:aiConfig:query")
    @GetMapping("/{id}")
    public R<CoinsAiConfigVo> getInfo(@NotNull(message = "主键不能为空")
                                     @PathVariable Long id) {
        return R.ok(coinsAiConfigService.queryById(id));
    }

    /**
     * 新增ai 流水线配置
     */
    @SaCheckPermission("system:aiConfig:add")
    @Log(title = "ai 流水线配置", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody CoinsAiConfigBo bo) {
        return toAjax(coinsAiConfigService.insertByBo(bo));
    }

    /**
     * 修改ai 流水线配置
     */
    @SaCheckPermission("system:aiConfig:edit")
    @Log(title = "ai 流水线配置", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody CoinsAiConfigBo bo) {
        return toAjax(coinsAiConfigService.updateByBo(bo));
    }

    /**
     * 删除ai 流水线配置
     *
     * @param ids 主键串
     */
    @SaCheckPermission("system:aiConfig:remove")
    @Log(title = "ai 流水线配置", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable Long[] ids) {
        return toAjax(coinsAiConfigService.deleteWithValidByIds(List.of(ids), true));
    }
}
