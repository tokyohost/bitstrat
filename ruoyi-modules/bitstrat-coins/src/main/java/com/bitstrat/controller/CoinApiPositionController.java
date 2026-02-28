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
import com.bitstrat.domain.vo.CoinApiPositionVo;
import com.bitstrat.domain.bo.CoinApiPositionBo;
import com.bitstrat.service.ICoinApiPositionService;
import org.dromara.common.mybatis.core.page.TableDataInfo;

/**
 * API 历史仓位数据
 *
 * @author Lion Li
 * @date 2025-12-29
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/system/apiPosition")
public class CoinApiPositionController extends BaseController {

    private final ICoinApiPositionService coinApiPositionService;

    /**
     * 查询API 历史仓位数据列表
     */
    @SaCheckPermission("system:apiPosition:list")
    @GetMapping("/list")
    public TableDataInfo<CoinApiPositionVo> list(CoinApiPositionBo bo, PageQuery pageQuery) {
        return coinApiPositionService.queryPageList(bo, pageQuery);
    }

    /**
     * 导出API 历史仓位数据列表
     */
    @SaCheckPermission("system:apiPosition:export")
    @Log(title = "API 历史仓位数据", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(CoinApiPositionBo bo, HttpServletResponse response) {
        List<CoinApiPositionVo> list = coinApiPositionService.queryList(bo);
        ExcelUtil.exportExcel(list, "API 历史仓位数据", CoinApiPositionVo.class, response);
    }

    /**
     * 获取API 历史仓位数据详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("system:apiPosition:query")
    @GetMapping("/{id}")
    public R<CoinApiPositionVo> getInfo(@NotNull(message = "主键不能为空")
                                     @PathVariable Long id) {
        return R.ok(coinApiPositionService.queryById(id));
    }

    /**
     * 新增API 历史仓位数据
     */
    @SaCheckPermission("system:apiPosition:add")
    @Log(title = "API 历史仓位数据", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody CoinApiPositionBo bo) {
        return toAjax(coinApiPositionService.insertByBo(bo));
    }

    /**
     * 修改API 历史仓位数据
     */
    @SaCheckPermission("system:apiPosition:edit")
    @Log(title = "API 历史仓位数据", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody CoinApiPositionBo bo) {
        return toAjax(coinApiPositionService.updateByBo(bo));
    }

    /**
     * 删除API 历史仓位数据
     *
     * @param ids 主键串
     */
    @SaCheckPermission("system:apiPosition:remove")
    @Log(title = "API 历史仓位数据", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable Long[] ids) {
        return toAjax(coinApiPositionService.deleteWithValidByIds(List.of(ids), true));
    }
}
