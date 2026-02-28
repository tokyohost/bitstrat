package com.bitstrat.controller;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import com.bitstrat.domain.CoinAiTaskRequest;
import com.bitstrat.domain.bo.CoinAiTaskRequestBo;
import com.bitstrat.domain.bo.CoinsAiConfigBo;
import com.bitstrat.domain.vo.CoinAiTaskRequestVo;
import com.bitstrat.domain.vo.CoinsAiConfigShow;
import com.bitstrat.domain.vo.CoinsAiConfigVo;
import com.bitstrat.service.ICoinsAiConfigService;
import lombok.RequiredArgsConstructor;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.*;
import cn.dev33.satoken.annotation.SaCheckPermission;
import org.dromara.common.core.domain.model.LoginUser;
import org.dromara.common.core.utils.MapstructUtils;
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
import com.bitstrat.service.ICoinTestAiRequestService;
import org.dromara.common.mybatis.core.page.TableDataInfo;

/**
 * AI 用户请求提示词
 *
 * @author Lion Li
 * @date 2025-11-01
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/system/testAiRequest")
public class CoinTestAiRequestController extends BaseController {

    private final ICoinTestAiRequestService coinTestAiRequestService;

    private final ICoinsAiConfigService aiConfigService;

    /**
     * 查询AI 用户请求提示词列表
     */
    @SaCheckPermission("system:testAiRequest:list")
    @GetMapping("/list")
    public TableDataInfo<CoinAiTaskRequestVo> list(CoinAiTaskRequestBo bo, PageQuery pageQuery) {
//        if (!LoginHelper.isSuperAdmin()) {
            LoginUser loginUser = LoginHelper.getLoginUser();
            bo.setCreateBy(loginUser.getUserId());
//        }
        pageQuery.setOrderByColumn("create_time");
        pageQuery.setIsAsc("desc");
        if(Objects.isNull(bo.getTaskId())) {
            return TableDataInfo.build(List.of());
        }

        List<CoinsAiConfigVo> coinsAiConfigVos = aiConfigService.queryList(new CoinsAiConfigBo());
        Map<Long, CoinsAiConfigShow> collected = coinsAiConfigVos.stream().collect(Collectors.toMap(CoinsAiConfigVo::getId, item -> MapstructUtils.convert(item, CoinsAiConfigShow.class)));
        TableDataInfo<CoinAiTaskRequestVo> dataInfo = coinTestAiRequestService.queryPageList(bo, pageQuery);
        List<CoinAiTaskRequestVo> rows = dataInfo.getRows();
        for (CoinAiTaskRequestVo row : rows) {
            Long aiId = row.getAiId();
            if(Objects.nonNull(aiId)) {
                row.setAiConfig(collected.get(aiId));
            }
        }

        return dataInfo;
    }

    /**
     * 导出AI 用户请求提示词列表
     */
    @SaCheckPermission("system:testAiRequest:export")
    @Log(title = "AI 用户请求提示词", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(CoinAiTaskRequestBo bo, HttpServletResponse response) {
        List<CoinAiTaskRequestVo> list = coinTestAiRequestService.queryList(bo);
        ExcelUtil.exportExcel(list, "AI 用户请求提示词", CoinAiTaskRequestVo.class, response);
    }

    /**
     * 获取AI 用户请求提示词详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("system:testAiRequest:query")
    @GetMapping("/{id}")
    public R<CoinAiTaskRequestVo> getInfo(@NotNull(message = "主键不能为空")
                                     @PathVariable Long id) {
        return R.ok(coinTestAiRequestService.queryById(id));
    }
    /**
     * 获取AI 用户请求提示词详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("system:testAiRequest:query")
    @GetMapping("/requestKey/{key}")
    public R<CoinAiTaskRequestVo> getInfoByRequestKey(@NotNull(message = "主键不能为空")
                                     @PathVariable String key) {
        CoinAiTaskRequest coinAiTaskRequest = coinTestAiRequestService.queryByRequestKey(key);
        CoinAiTaskRequestVo convert = MapstructUtils.convert(coinAiTaskRequest, CoinAiTaskRequestVo.class);
        return R.ok(convert);
    }

    /**
     * 新增AI 用户请求提示词
     */
    @SaCheckPermission("system:testAiRequest:add")
    @Log(title = "AI 用户请求提示词", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody CoinAiTaskRequestBo bo) {
        return toAjax(coinTestAiRequestService.insertByBo(bo));
    }

    /**
     * 修改AI 用户请求提示词
     */
    @SaCheckPermission("system:testAiRequest:edit")
    @Log(title = "AI 用户请求提示词", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody CoinAiTaskRequestBo bo) {
        return toAjax(coinTestAiRequestService.updateByBo(bo));
    }

    /**
     * 删除AI 用户请求提示词
     *
     * @param ids 主键串
     */
    @SaCheckPermission("system:testAiRequest:remove")
    @Log(title = "AI 用户请求提示词", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable Long[] ids) {
        return toAjax(coinTestAiRequestService.deleteWithValidByIds(List.of(ids), true));
    }
}
