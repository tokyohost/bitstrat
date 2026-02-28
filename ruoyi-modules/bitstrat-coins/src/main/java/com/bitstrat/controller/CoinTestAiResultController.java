package com.bitstrat.controller;

import java.util.List;
import java.util.Objects;

import lombok.RequiredArgsConstructor;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.*;
import cn.dev33.satoken.annotation.SaCheckPermission;
import org.dromara.common.core.domain.model.LoginUser;
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
import com.bitstrat.domain.vo.CoinTestAiResultVo;
import com.bitstrat.domain.bo.CoinTestAiResultBo;
import com.bitstrat.service.ICoinTestAiResultService;
import org.dromara.common.mybatis.core.page.TableDataInfo;

/**
 * AI 操作日志
 *
 * @author Lion Li
 * @date 2025-10-30
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/system/testAiResult")
public class CoinTestAiResultController extends BaseController {

    private final ICoinTestAiResultService coinTestAiResultService;

    /**
     * 查询AI 操作日志列表
     */
    @SaCheckPermission("system:testAiResult:list")
    @GetMapping("/list")
    public TableDataInfo<CoinTestAiResultVo> list(CoinTestAiResultBo bo, PageQuery pageQuery) {
        pageQuery.setOrderByColumn("create_time");
        pageQuery.setIsAsc("desc");
        if (Objects.isNull(bo.getTaskId())) {
            TableDataInfo<CoinTestAiResultVo> dataInfo = new TableDataInfo<>();
            dataInfo.setRows(List.of());
            dataInfo.setTotal(0L);
            return dataInfo;
        }
        if (!LoginHelper.isSuperAdmin()) {
            LoginUser loginUser = LoginHelper.getLoginUser();
            bo.setCreateBy(loginUser.getUserId());
        }

        return coinTestAiResultService.queryPageList(bo, pageQuery);
    }

    /**
     * 导出AI 操作日志列表
     */
    @SaCheckPermission("system:testAiResult:export")
    @Log(title = "AI 操作日志", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(CoinTestAiResultBo bo, HttpServletResponse response) {
        List<CoinTestAiResultVo> list = coinTestAiResultService.queryList(bo);
        ExcelUtil.exportExcel(list, "AI 操作日志", CoinTestAiResultVo.class, response);
    }

    /**
     * 获取AI 操作日志详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("system:testAiResult:query")
    @GetMapping("/{id}")
    public R<CoinTestAiResultVo> getInfo(@NotNull(message = "主键不能为空")
                                     @PathVariable Long id) {
        return R.ok(coinTestAiResultService.queryById(id));
    }

    /**
     * 新增AI 操作日志
     */
    @SaCheckPermission("system:testAiResult:add")
    @Log(title = "AI 操作日志", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody CoinTestAiResultBo bo) {
        return toAjax(coinTestAiResultService.insertByBo(bo));
    }

    /**
     * 修改AI 操作日志
     */
    @SaCheckPermission("system:testAiResult:edit")
    @Log(title = "AI 操作日志", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody CoinTestAiResultBo bo) {
        return toAjax(coinTestAiResultService.updateByBo(bo));
    }

    /**
     * 删除AI 操作日志
     *
     * @param ids 主键串
     */
    @SaCheckPermission("system:testAiResult:remove")
    @Log(title = "AI 操作日志", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable Long[] ids) {
        return toAjax(coinTestAiResultService.deleteWithValidByIds(List.of(ids), true));
    }
}
