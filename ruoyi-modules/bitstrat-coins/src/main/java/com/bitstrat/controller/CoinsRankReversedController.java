package com.bitstrat.controller;

import java.util.List;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.bitstrat.init.SyncCoinsRankReversed;
import com.bitstrat.init.SyncStatusContextReversed;
import com.bitstrat.service.impl.CommonServce;
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
import com.bitstrat.domain.vo.CoinsRankReversedVo;
import com.bitstrat.domain.bo.CoinsRankReversedBo;
import com.bitstrat.service.ICoinsRankReversedService;
import org.dromara.common.mybatis.core.page.TableDataInfo;

/**
 * 山寨币排行(反向)
 *
 * @author Lion Li
 * @date 2025-04-06
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/system/rankReversed")
public class CoinsRankReversedController extends BaseController {
    private final SyncCoinsRankReversed syncCoinsRank;
    private final ICoinsRankReversedService coinsRankReversedService;

    private final CommonServce commonServce;
    /**
     * 查询山寨币排行列表
     */
    @SaCheckLogin
    @GetMapping("/syncRank")
    public R<String> syncRank() throws Exception {
        if (SyncStatusContextReversed.getSyncStatus() == 1) {
            throw new RuntimeException("正在同步中，请稍后");
        }
        commonServce.getSyncExecutorService().submit(()->{
            try {
                syncCoinsRank.run();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        return R.ok("同步成功");
    }
    /**
     * 查询山寨币排行(反向)列表
     */
    @SaCheckPermission("system:rankReversed:list")
    @GetMapping("/list")
    public TableDataInfo<CoinsRankReversedVo> list(CoinsRankReversedBo bo, PageQuery pageQuery) {
        TableDataInfo<CoinsRankReversedVo> coinsRankVoTableDataInfo = coinsRankReversedService.queryPageList(bo, pageQuery);
        coinsRankVoTableDataInfo.setExtCode(SyncStatusContextReversed.getSyncStatus());
        coinsRankVoTableDataInfo.setExtInfo(SyncStatusContextReversed.getLastFinishTime());
        return coinsRankVoTableDataInfo;
    }

    /**
     * 导出山寨币排行(反向)列表
     */
    @SaCheckPermission("system:rankReversed:export")
    @Log(title = "山寨币排行(反向)", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(CoinsRankReversedBo bo, HttpServletResponse response) {
        List<CoinsRankReversedVo> list = coinsRankReversedService.queryList(bo);
        ExcelUtil.exportExcel(list, "山寨币排行(反向)", CoinsRankReversedVo.class, response);
    }

    /**
     * 获取山寨币排行(反向)详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("system:rankReversed:query")
    @GetMapping("/{id}")
    public R<CoinsRankReversedVo> getInfo(@NotNull(message = "主键不能为空")
                                     @PathVariable Long id) {
        return R.ok(coinsRankReversedService.queryById(id));
    }

    /**
     * 新增山寨币排行(反向)
     */
    @SaCheckPermission("system:rankReversed:add")
    @Log(title = "山寨币排行(反向)", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody CoinsRankReversedBo bo) {
        return toAjax(coinsRankReversedService.insertByBo(bo));
    }

    /**
     * 修改山寨币排行(反向)
     */
    @SaCheckPermission("system:rankReversed:edit")
    @Log(title = "山寨币排行(反向)", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody CoinsRankReversedBo bo) {
        return toAjax(coinsRankReversedService.updateByBo(bo));
    }

    /**
     * 删除山寨币排行(反向)
     *
     * @param ids 主键串
     */
    @SaCheckPermission("system:rankReversed:remove")
    @Log(title = "山寨币排行(反向)", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable Long[] ids) {
        return toAjax(coinsRankReversedService.deleteWithValidByIds(List.of(ids), true));
    }
}
