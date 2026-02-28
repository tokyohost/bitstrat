package com.bitstrat.controller;

import java.util.List;

import com.bitstrat.service.ICoinsBotAccountService;
import lombok.RequiredArgsConstructor;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.*;
import cn.dev33.satoken.annotation.SaCheckPermission;
import org.dromara.common.satoken.utils.LoginHelper;
import org.springframework.transaction.annotation.Transactional;
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
import com.bitstrat.domain.vo.CoinsAbBotVo;
import com.bitstrat.domain.bo.CoinsAbBotBo;
import com.bitstrat.service.ICoinsAbBotService;
import org.dromara.common.mybatis.core.page.TableDataInfo;

/**
 * 套利机器人
 *
 * @author Lion Li
 * @date 2025-05-24
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/system/abBot")
public class CoinsAbBotController extends BaseController {

    private final ICoinsAbBotService coinsAbBotService;
    private final ICoinsBotAccountService coinsBotAccountService;

    /**
     * 查询套利机器人列表
     */
    @SaCheckPermission("system:abBot:list")
    @GetMapping("/list")
    public TableDataInfo<CoinsAbBotVo> list(CoinsAbBotBo bo, PageQuery pageQuery) {
        return coinsAbBotService.queryPageList(bo, pageQuery);
    }

    /**
     * 导出套利机器人列表
     */
    @SaCheckPermission("system:abBot:export")
    @Log(title = "套利机器人", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(CoinsAbBotBo bo, HttpServletResponse response) {
        List<CoinsAbBotVo> list = coinsAbBotService.queryList(bo);
        ExcelUtil.exportExcel(list, "套利机器人", CoinsAbBotVo.class, response);
    }

    /**
     * 获取套利机器人详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("system:abBot:query")
    @GetMapping("/{id}")
    public R<CoinsAbBotVo> getInfo(@NotNull(message = "主键不能为空")
                                     @PathVariable Long id) {
        CoinsAbBotVo coinsAbBotVo = coinsAbBotService.queryById(id);
        coinsAbBotVo.setCanUseApis(coinsBotAccountService.selectRelatedByBotId(id));
        return R.ok(coinsAbBotVo);
    }

    /**
     * 新增套利机器人
     */
    @SaCheckPermission("system:abBot:add")
    @Log(title = "套利机器人", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    @Transactional(rollbackFor = Exception.class)
    public R<Void> add(@Validated(AddGroup.class) @RequestBody CoinsAbBotBo bo) {
        bo.setUserId(LoginHelper.getUserId());
        bo.setStatus(1L);// 已创建
        Boolean b = coinsAbBotService.insertByBo(bo);
        if(b){
            coinsBotAccountService.createOrUpdateRelated(bo.getId(), bo.getCanUseApis());
        }
        return toAjax(b);
    }

    /**
     * 修改套利机器人
     */
    @SaCheckPermission("system:abBot:edit")
    @Log(title = "套利机器人", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody CoinsAbBotBo bo) {
        return toAjax(coinsAbBotService.updateByBo(bo));
    }

    /**
     * 删除套利机器人
     *
     * @param ids 主键串
     */
    @SaCheckPermission("system:abBot:remove")
    @Log(title = "套利机器人", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable Long[] ids) {
        return toAjax(coinsAbBotService.deleteWithValidByIds(List.of(ids), true));
    }
}
