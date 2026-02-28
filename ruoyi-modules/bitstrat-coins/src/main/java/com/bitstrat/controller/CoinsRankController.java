package com.bitstrat.controller;

import java.util.List;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.bitstrat.init.SyncCoinsRank;
import com.bitstrat.init.SyncStatusContext;
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
import com.bitstrat.domain.vo.CoinsRankVo;
import com.bitstrat.domain.bo.CoinsRankBo;
import com.bitstrat.service.ICoinsRankService;
import org.dromara.common.mybatis.core.page.TableDataInfo;

/**
 * 山寨币排行
 *
 * @author Lion Li
 * @date 2025-04-05
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/system/rank")
public class CoinsRankController extends BaseController {
//    @Autowired
    private final SyncCoinsRank syncCoinsRank;
    private final CommonServce commonServce;
    private final ICoinsRankService coinsRankService;

    /**
     * 查询山寨币排行列表
     */
    @SaCheckPermission("system:rank:list")
    @GetMapping("/list")
    public TableDataInfo<CoinsRankVo> list(CoinsRankBo bo, PageQuery pageQuery) {
        TableDataInfo<CoinsRankVo> coinsRankVoTableDataInfo = coinsRankService.queryPageList(bo, pageQuery);
        coinsRankVoTableDataInfo.setExtCode(SyncStatusContext.getSyncStatus());
        coinsRankVoTableDataInfo.setExtInfo(SyncStatusContext.getLastFinishTime());
        return coinsRankVoTableDataInfo;
    }
    /**
     * 查询山寨币排行列表
     */
    @SaCheckLogin
    @GetMapping("/syncRank")
    public R<String> syncRank() throws Exception {
        if (SyncStatusContext.getSyncStatus() == 1) {
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
     * 导出山寨币排行列表
     */
    @SaCheckPermission("system:rank:export")
    @Log(title = "山寨币排行", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(CoinsRankBo bo, HttpServletResponse response) {
        List<CoinsRankVo> list = coinsRankService.queryList(bo);
        ExcelUtil.exportExcel(list, "山寨币排行", CoinsRankVo.class, response);
    }

    /**
     * 获取山寨币排行详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("system:rank:query")
    @GetMapping("/{id}")
    public R<CoinsRankVo> getInfo(@NotNull(message = "主键不能为空")
                                     @PathVariable Long id) {
        return R.ok(coinsRankService.queryById(id));
    }

    /**
     * 新增山寨币排行
     */
    @SaCheckPermission("system:rank:add")
    @Log(title = "山寨币排行", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody CoinsRankBo bo) {
        return toAjax(coinsRankService.insertByBo(bo));
    }

    /**
     * 修改山寨币排行
     */
    @SaCheckPermission("system:rank:edit")
    @Log(title = "山寨币排行", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody CoinsRankBo bo) {
        return toAjax(coinsRankService.updateByBo(bo));
    }

    /**
     * 删除山寨币排行
     *
     * @param ids 主键串
     */
    @SaCheckPermission("system:rank:remove")
    @Log(title = "山寨币排行", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable Long[] ids) {
        return toAjax(coinsRankService.deleteWithValidByIds(List.of(ids), true));
    }
}
