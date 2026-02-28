package com.bitstrat.controller;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.bitstrat.domain.vo.CoinsBatchVo;
import com.bitstrat.service.ICoinsBatchService;
import lombok.RequiredArgsConstructor;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.*;
import cn.dev33.satoken.annotation.SaCheckPermission;
import org.dromara.common.satoken.utils.LoginHelper;
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
import com.bitstrat.domain.vo.CoinsOrderVo;
import com.bitstrat.domain.bo.CoinsOrderBo;
import com.bitstrat.service.ICoinsOrderService;
import org.dromara.common.mybatis.core.page.TableDataInfo;

/**
 * 订单列表
 *
 * @author Lion Li
 * @date 2025-04-21
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/system/order")
public class CoinsOrderController extends BaseController {

    private final ICoinsOrderService coinsOrderService;

    private final ICoinsBatchService batchService;

    /**
     * 改价
     */
    @SaCheckPermission("system:order:list")
    @PostMapping("/updatePrice")
    public R updatePrice(@RequestBody CoinsOrderBo bo) {

        return coinsOrderService.updatePrice(bo);
    }
    /**
     * 查询订单列表列表
     */
    @SaCheckPermission("system:order:list")
    @GetMapping("/list")
    public TableDataInfo<CoinsOrderVo> list(CoinsOrderBo bo, PageQuery pageQuery) {
        bo.setCreateBy(LoginHelper.getUserId());
        TableDataInfo<CoinsOrderVo> coinsOrderVoTableDataInfo = coinsOrderService.queryPageList(bo, pageQuery);
        List<CoinsOrderVo> rows = coinsOrderVoTableDataInfo.getRows();
        List<CoinsOrderVo> coinsOrderVos = coinsOrderService.queryMarketPrice(rows);
        List<CoinsOrderVo> orderVoList = coinsOrderService.syncOrder(coinsOrderVos, LoginHelper.getUserId(),true);
        Set<Long> batchIds = rows.stream().map(CoinsOrderVo::getBatchId).collect(Collectors.toSet());
        List<CoinsBatchVo> coinsBatchVos = batchService.queryByIds(batchIds);
        Map<Long, CoinsBatchVo> batchMap = coinsBatchVos.stream().collect(Collectors.toMap(CoinsBatchVo::getId, item -> item, (a, b) -> a));
        for (CoinsOrderVo row : orderVoList) {
            if (row.getBatchId() != null) {
                CoinsBatchVo coinsBatchVo = batchMap.get(row.getBatchId());
                if (Objects.nonNull(coinsBatchVo)) {
                    row.setBatchTotal(coinsBatchVo.getBatchTotal());
                }
            }
        }

        coinsOrderVoTableDataInfo.setRows(coinsOrderService.formatSize(orderVoList, LoginHelper.getUserId()));


        return coinsOrderVoTableDataInfo;
    }

    /**
     * 导出订单列表列表
     */
    @SaCheckPermission("system:order:export")
    @Log(title = "订单列表", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(CoinsOrderBo bo, HttpServletResponse response) {
        List<CoinsOrderVo> list = coinsOrderService.queryList(bo);
        ExcelUtil.exportExcel(list, "订单列表", CoinsOrderVo.class, response);
    }

    /**
     * 获取订单列表详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("system:order:query")
    @GetMapping("/{id}")
    public R<CoinsOrderVo> getInfo(@NotNull(message = "主键不能为空")
                                     @PathVariable Long id) {
        return R.ok(coinsOrderService.queryById(id));
    }

    /**
     * 新增订单列表
     */
    @SaCheckPermission("system:order:add")
    @Log(title = "订单列表", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody CoinsOrderBo bo) {
        return toAjax(coinsOrderService.insertByBo(bo));
    }


    /**
     * 修改订单列表
     */
    @SaCheckPermission("system:order:edit")
    @Log(title = "订单列表", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody CoinsOrderBo bo) {
        return toAjax(coinsOrderService.updateByBo(bo));
    }

    /**
     * 删除订单列表
     *
     * @param ids 主键串
     */
    @SaCheckPermission("system:order:remove")
    @Log(title = "订单列表", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable Long[] ids) {
        return toAjax(coinsOrderService.deleteWithValidByIds(List.of(ids), true));
    }
}
