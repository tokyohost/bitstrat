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
import com.bitstrat.domain.vo.CoinsBotAccountVo;
import com.bitstrat.domain.bo.CoinsBotAccountBo;
import com.bitstrat.service.ICoinsBotAccountService;
import org.dromara.common.mybatis.core.page.TableDataInfo;

/**
 * 机器人可使用账户
 *
 * @author Lion Li
 * @date 2025-05-24
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/system/botAccount")
public class CoinsBotAccountController extends BaseController {

    private final ICoinsBotAccountService coinsBotAccountService;

    /**
     * 查询机器人可使用账户列表
     */
    @SaCheckPermission("system:botAccount:list")
    @GetMapping("/list")
    public TableDataInfo<CoinsBotAccountVo> list(CoinsBotAccountBo bo, PageQuery pageQuery) {
        return coinsBotAccountService.queryPageList(bo, pageQuery);
    }

    /**
     * 导出机器人可使用账户列表
     */
    @SaCheckPermission("system:botAccount:export")
    @Log(title = "机器人可使用账户", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(CoinsBotAccountBo bo, HttpServletResponse response) {
        List<CoinsBotAccountVo> list = coinsBotAccountService.queryList(bo);
        ExcelUtil.exportExcel(list, "机器人可使用账户", CoinsBotAccountVo.class, response);
    }

    /**
     * 获取机器人可使用账户详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("system:botAccount:query")
    @GetMapping("/{id}")
    public R<CoinsBotAccountVo> getInfo(@NotNull(message = "主键不能为空")
                                     @PathVariable Long id) {
        return R.ok(coinsBotAccountService.queryById(id));
    }

    /**
     * 新增机器人可使用账户
     */
    @SaCheckPermission("system:botAccount:add")
    @Log(title = "机器人可使用账户", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody CoinsBotAccountBo bo) {
        return toAjax(coinsBotAccountService.insertByBo(bo));
    }

    /**
     * 修改机器人可使用账户
     */
    @SaCheckPermission("system:botAccount:edit")
    @Log(title = "机器人可使用账户", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody CoinsBotAccountBo bo) {
        return toAjax(coinsBotAccountService.updateByBo(bo));
    }

    /**
     * 删除机器人可使用账户
     *
     * @param ids 主键串
     */
    @SaCheckPermission("system:botAccount:remove")
    @Log(title = "机器人可使用账户", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable Long[] ids) {
        return toAjax(coinsBotAccountService.deleteWithValidByIds(List.of(ids), true));
    }
}
