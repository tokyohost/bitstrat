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
import com.bitstrat.domain.vo.CoinsBatchOrderVo;
import com.bitstrat.domain.bo.CoinsBatchOrderBo;
import com.bitstrat.service.ICoinsBatchOrderService;
import org.dromara.common.mybatis.core.page.TableDataInfo;

/**
 * 分批任务订单记录
 *
 * @author Lion Li
 * @date 2025-04-26
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/system/batchOrder")
public class CoinsBatchOrderController extends BaseController {

    private final ICoinsBatchOrderService coinsBatchOrderService;

    /**
     * 查询分批任务订单记录列表
     */
    @SaCheckPermission("system:batchOrder:list")
    @GetMapping("/list")
    public TableDataInfo<CoinsBatchOrderVo> list(CoinsBatchOrderBo bo, PageQuery pageQuery) {
        return coinsBatchOrderService.queryPageList(bo, pageQuery);
    }

    /**
     * 导出分批任务订单记录列表
     */
    @SaCheckPermission("system:batchOrder:export")
    @Log(title = "分批任务订单记录", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(CoinsBatchOrderBo bo, HttpServletResponse response) {
        List<CoinsBatchOrderVo> list = coinsBatchOrderService.queryList(bo);
        ExcelUtil.exportExcel(list, "分批任务订单记录", CoinsBatchOrderVo.class, response);
    }

    /**
     * 获取分批任务订单记录详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("system:batchOrder:query")
    @GetMapping("/{id}")
    public R<CoinsBatchOrderVo> getInfo(@NotNull(message = "主键不能为空")
                                     @PathVariable Long id) {
        return R.ok(coinsBatchOrderService.queryById(id));
    }

    /**
     * 新增分批任务订单记录
     */
    @SaCheckPermission("system:batchOrder:add")
    @Log(title = "分批任务订单记录", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody CoinsBatchOrderBo bo) {
        return toAjax(coinsBatchOrderService.insertByBo(bo));
    }

    /**
     * 修改分批任务订单记录
     */
    @SaCheckPermission("system:batchOrder:edit")
    @Log(title = "分批任务订单记录", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody CoinsBatchOrderBo bo) {
        return toAjax(coinsBatchOrderService.updateByBo(bo));
    }

    /**
     * 删除分批任务订单记录
     *
     * @param ids 主键串
     */
    @SaCheckPermission("system:batchOrder:remove")
    @Log(title = "分批任务订单记录", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable Long[] ids) {
        return toAjax(coinsBatchOrderService.deleteWithValidByIds(List.of(ids), true));
    }
}
