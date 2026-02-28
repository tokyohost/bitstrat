package com.bitstrat.controller;

import java.util.List;

import com.bitstrat.domain.bo.CoinsFinancialFlowRecordBo;
import com.bitstrat.domain.vo.CoinsFinancialFlowRecordVo;
import com.bitstrat.service.ICoinsFinancialFlowRecordService;
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
 * 交易所资金流水记录
 *
 * @author Lion Li
 * @date 2025-06-02
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/system/financialFlowRecord")
public class CoinsFinancialFlowRecordController extends BaseController {

    private final ICoinsFinancialFlowRecordService coinsFinancialFlowRecordService;

    /**
     * 查询交易所资金流水记录列表
     */
    @SaCheckPermission("system:financialFlowRecord:list")
    @GetMapping("/list")
    public TableDataInfo<CoinsFinancialFlowRecordVo> list(CoinsFinancialFlowRecordBo bo, PageQuery pageQuery) {
        return coinsFinancialFlowRecordService.queryPageList(bo, pageQuery);
    }

    /**
     * 导出交易所资金流水记录列表
     */
    @SaCheckPermission("system:financialFlowRecord:export")
    @Log(title = "交易所资金流水记录", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(CoinsFinancialFlowRecordBo bo, HttpServletResponse response) {
        List<CoinsFinancialFlowRecordVo> list = coinsFinancialFlowRecordService.queryList(bo);
        ExcelUtil.exportExcel(list, "交易所资金流水记录", CoinsFinancialFlowRecordVo.class, response);
    }

    /**
     * 获取交易所资金流水记录详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("system:financialFlowRecord:query")
    @GetMapping("/{id}")
    public R<CoinsFinancialFlowRecordVo> getInfo(@NotNull(message = "主键不能为空")
                                     @PathVariable Long id) {
        return R.ok(coinsFinancialFlowRecordService.queryById(id));
    }

    /**
     * 新增交易所资金流水记录
     */
    @SaCheckPermission("system:financialFlowRecord:add")
    @Log(title = "交易所资金流水记录", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody CoinsFinancialFlowRecordBo bo) {
        return toAjax(coinsFinancialFlowRecordService.insertByBo(bo));
    }

    /**
     * 修改交易所资金流水记录
     */
    @SaCheckPermission("system:financialFlowRecord:edit")
    @Log(title = "交易所资金流水记录", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody CoinsFinancialFlowRecordBo bo) {
        return toAjax(coinsFinancialFlowRecordService.updateByBo(bo));
    }

    /**
     * 删除交易所资金流水记录
     *
     * @param ids 主键串
     */
    @SaCheckPermission("system:financialFlowRecord:remove")
    @Log(title = "交易所资金流水记录", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable Long[] ids) {
        return toAjax(coinsFinancialFlowRecordService.deleteWithValidByIds(List.of(ids), true));
    }
}
