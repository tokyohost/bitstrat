package com.bitstrat.controller;

import java.util.List;

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
import com.bitstrat.domain.vo.CoinsNotifyConfigVo;
import com.bitstrat.domain.bo.CoinsNotifyConfigBo;
import com.bitstrat.service.ICoinsNotifyConfigService;
import org.dromara.common.mybatis.core.page.TableDataInfo;

/**
 * 用户通知设置
 *
 * @author Lion Li
 * @date 2025-04-25
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/system/notifyConfig")
public class CoinsNotifyConfigController extends BaseController {

    private final ICoinsNotifyConfigService coinsNotifyConfigService;

    /**
     * 查询用户通知设置列表
     */
    @SaCheckPermission("system:notifyConfig:list")
    @GetMapping("/list")
    public TableDataInfo<CoinsNotifyConfigVo> list(CoinsNotifyConfigBo bo, PageQuery pageQuery) {
        return coinsNotifyConfigService.queryPageList(bo, pageQuery);
    }

    /**
     * 导出用户通知设置列表
     */
    @SaCheckPermission("system:notifyConfig:export")
    @Log(title = "用户通知设置", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(CoinsNotifyConfigBo bo, HttpServletResponse response) {
        bo.setUserId(LoginHelper.getUserId());
        List<CoinsNotifyConfigVo> list = coinsNotifyConfigService.queryList(bo);
        ExcelUtil.exportExcel(list, "用户通知设置", CoinsNotifyConfigVo.class, response);
    }

    /**
     * 获取用户通知设置详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("system:notifyConfig:query")
    @GetMapping("/{id}")
    public R<CoinsNotifyConfigVo> getInfo(@NotNull(message = "主键不能为空")
                                     @PathVariable Long id) {
        return R.ok(coinsNotifyConfigService.queryById(id));
    }

    /**
     * 新增用户通知设置
     */
    @SaCheckPermission("system:notifyConfig:add")
    @Log(title = "用户通知设置", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody CoinsNotifyConfigBo bo) {
        return toAjax(coinsNotifyConfigService.insertByBo(bo));
    }

    /**
     * 修改用户通知设置
     */
    @SaCheckPermission("system:notifyConfig:edit")
    @Log(title = "用户通知设置", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody CoinsNotifyConfigBo bo) {
        return toAjax(coinsNotifyConfigService.updateByBo(bo));
    }

    /**
     * 删除用户通知设置
     *
     * @param ids 主键串
     */
    @SaCheckPermission("system:notifyConfig:remove")
    @Log(title = "用户通知设置", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable Long[] ids) {
        return toAjax(coinsNotifyConfigService.deleteWithValidByIds(List.of(ids), true));
    }
}
