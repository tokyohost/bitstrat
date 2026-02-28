package com.bitstrat.controller;

import java.util.List;

import cn.dev33.satoken.annotation.SaCheckLogin;
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
import com.bitstrat.domain.vo.CoinsBalanceLogVo;
import com.bitstrat.domain.bo.CoinsBalanceLogBo;
import com.bitstrat.service.ICoinsBalanceLogService;
import org.dromara.common.mybatis.core.page.TableDataInfo;

/**
 * 账户余额变动日志
 *
 * @author Lion Li
 * @date 2025-11-20
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/system/balanceLog")
public class CoinsBalanceLogController extends BaseController {

    private final ICoinsBalanceLogService coinsBalanceLogService;

    /**
     * 查询账户余额变动日志列表
     */
//    @SaCheckPermission("system:balanceLog:list")
    @SaCheckLogin
    @GetMapping("/list")
    public TableDataInfo<CoinsBalanceLogVo> list(CoinsBalanceLogBo bo, PageQuery pageQuery) {
        pageQuery.setOrderByColumn("create_time");
        pageQuery.setIsAsc("desc");
        bo.setUserId(LoginHelper.getUserId());
        return coinsBalanceLogService.queryPageList(bo, pageQuery);
    }

    /**
     * 导出账户余额变动日志列表
     */
    @SaCheckPermission("system:balanceLog:export")
    @Log(title = "账户余额变动日志", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(CoinsBalanceLogBo bo, HttpServletResponse response) {
        List<CoinsBalanceLogVo> list = coinsBalanceLogService.queryList(bo);
        ExcelUtil.exportExcel(list, "账户余额变动日志", CoinsBalanceLogVo.class, response);
    }

    /**
     * 获取账户余额变动日志详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("system:balanceLog:query")
    @GetMapping("/{id}")
    public R<CoinsBalanceLogVo> getInfo(@NotNull(message = "主键不能为空")
                                     @PathVariable Long id) {
        return R.ok(coinsBalanceLogService.queryById(id));
    }

    /**
     * 新增账户余额变动日志
     */
    @SaCheckPermission("system:balanceLog:add")
    @Log(title = "账户余额变动日志", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody CoinsBalanceLogBo bo) {
        return toAjax(coinsBalanceLogService.insertByBo(bo));
    }

    /**
     * 修改账户余额变动日志
     */
    @SaCheckPermission("system:balanceLog:edit")
    @Log(title = "账户余额变动日志", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody CoinsBalanceLogBo bo) {
        return toAjax(coinsBalanceLogService.updateByBo(bo));
    }

    /**
     * 删除账户余额变动日志
     *
     * @param ids 主键串
     */
    @SaCheckPermission("system:balanceLog:remove")
    @Log(title = "账户余额变动日志", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable Long[] ids) {
        return toAjax(coinsBalanceLogService.deleteWithValidByIds(List.of(ids), true));
    }
}
