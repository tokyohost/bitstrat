package com.bitstrat.controller;

import java.util.List;

import com.bitstrat.domain.bo.CoinsVipLevelBo;
import com.bitstrat.domain.vo.CoinsVipLevelVo;
import com.bitstrat.service.ICoinsVipLevelService;
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
 * VIP 权限
 *
 * @author Lion Li
 * @date 2025-05-14
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/system/vipLevel")
public class CoinsVipLevelController extends BaseController {

    private final ICoinsVipLevelService coinsVipLevelService;

    /**
     * 查询VIP 权限列表
     */
    @SaCheckPermission("system:vipLevel:list")
    @GetMapping("/list")
    public TableDataInfo<CoinsVipLevelVo> list(CoinsVipLevelBo bo, PageQuery pageQuery) {
        return coinsVipLevelService.queryPageList(bo, pageQuery);
    }


    /**
     * 查询VIP可购买列表
     */
    @SaCheckPermission("system:vipLevel:list")
    @GetMapping("/getAvailableVipLevelList")
    public  R<List<CoinsVipLevelVo>> getAvailableVipLevelList(CoinsVipLevelBo bo) {
        return R.ok(coinsVipLevelService.getAvailableVipLevelList(bo));
    }

    /**
     * 导出VIP 权限列表
     */
    @SaCheckPermission("system:vipLevel:export")
    @Log(title = "VIP 权限", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(CoinsVipLevelBo bo, HttpServletResponse response) {
        List<CoinsVipLevelVo> list = coinsVipLevelService.queryList(bo);
        ExcelUtil.exportExcel(list, "VIP 权限", CoinsVipLevelVo.class, response);
    }

    /**
     * 获取VIP 权限详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("system:vipLevel:query")
    @GetMapping("/{id}")
    public R<CoinsVipLevelVo> getInfo(@NotNull(message = "主键不能为空")
                                     @PathVariable Long id) {
        return R.ok(coinsVipLevelService.queryById(id));
    }

    /**
     * 新增VIP 权限
     */
    @SaCheckPermission("system:vipLevel:add")
    @Log(title = "VIP 权限", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody CoinsVipLevelBo bo) {
        return toAjax(coinsVipLevelService.insertByBo(bo));
    }

    /**
     * 修改VIP 权限
     */
    @SaCheckPermission("system:vipLevel:edit")
    @Log(title = "VIP 权限", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody CoinsVipLevelBo bo) {
        return toAjax(coinsVipLevelService.updateByBo(bo));
    }

    /**
     * 删除VIP 权限
     *
     * @param ids 主键串
     */
    @SaCheckPermission("system:vipLevel:remove")
    @Log(title = "VIP 权限", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable Long[] ids) {
        return toAjax(coinsVipLevelService.deleteWithValidByIds(List.of(ids), true));
    }
}
