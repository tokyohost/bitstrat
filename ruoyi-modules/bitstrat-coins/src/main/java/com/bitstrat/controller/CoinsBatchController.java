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
import com.bitstrat.domain.vo.CoinsBatchVo;
import com.bitstrat.domain.bo.CoinsBatchBo;
import com.bitstrat.service.ICoinsBatchService;
import org.dromara.common.mybatis.core.page.TableDataInfo;

/**
 * 分批订单任务
 *
 * @author Lion Li
 * @date 2025-04-26
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/system/batch")
public class CoinsBatchController extends BaseController {

    private final ICoinsBatchService coinsBatchService;

    /**
     * 查询分批订单任务列表
     */
    @SaCheckPermission("system:batch:list")
    @GetMapping("/list")
    public TableDataInfo<CoinsBatchVo> list(CoinsBatchBo bo, PageQuery pageQuery) {
        return coinsBatchService.queryPageList(bo, pageQuery);
    }

    /**
     * 导出分批订单任务列表
     */
    @SaCheckPermission("system:batch:export")
    @Log(title = "分批订单任务", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(CoinsBatchBo bo, HttpServletResponse response) {
        List<CoinsBatchVo> list = coinsBatchService.queryList(bo);
        ExcelUtil.exportExcel(list, "分批订单任务", CoinsBatchVo.class, response);
    }

    /**
     * 获取分批订单任务详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("system:batch:query")
    @GetMapping("/{id}")
    public R<CoinsBatchVo> getInfo(@NotNull(message = "主键不能为空")
                                     @PathVariable Long id) {
        return R.ok(coinsBatchService.queryById(id));
    }

    /**
     * 新增分批订单任务
     */
    @SaCheckPermission("system:batch:add")
    @Log(title = "分批订单任务", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody CoinsBatchBo bo) {
        return toAjax(coinsBatchService.insertByBo(bo));
    }

    /**
     * 修改分批订单任务
     */
    @SaCheckPermission("system:batch:edit")
    @Log(title = "分批订单任务", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody CoinsBatchBo bo) {
        return toAjax(coinsBatchService.updateByBo(bo));
    }
    /**
     * 修改分批订单任务
     */
    @SaCheckPermission("system:batch:edit")
    @Log(title = "停止任务", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping("/stop")
    public R<Void> stop( @RequestBody CoinsBatchBo bo) {
        if (bo.getId() == null) {
            return R.fail();
        }
        return toAjax(coinsBatchService.stop(bo));
    }

    /**
     * 删除分批订单任务
     *
     * @param ids 主键串
     */
    @SaCheckPermission("system:batch:remove")
    @Log(title = "分批订单任务", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable Long[] ids) {
        return toAjax(coinsBatchService.deleteWithValidByIds(List.of(ids), true));
    }
}
