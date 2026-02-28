package com.bitstrat.controller;

import java.util.List;
import java.util.Map;

import com.bitstrat.domain.bo.CoinsUserVipBo;
import com.bitstrat.domain.vo.CoinsUserVipInfoVo;
import com.bitstrat.domain.vo.CoinsUserVipVo;
import com.bitstrat.service.ICoinsUserVipService;
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
 * 用户VIP 状态
 *
 * @author Lion Li
 * @date 2025-05-14
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/system/userVip")
public class CoinsUserVipController extends BaseController {

    private final ICoinsUserVipService coinsUserVipService;

    /**
     * 购买vip
     * todo 先简单入库,后面增加支付
     */
//    @SaCheckPermission("system:userVip:purchaseVip")
    @PostMapping("/purchaseVip")
    public R<Void> purchaseVip(@RequestBody CoinsUserVipBo bo) {
        Long userId = bo.getUserId();
        Long vipId = bo.getVipId();
        // 先简单入库,后面增加支付
        coinsUserVipService.purchaseVip(userId, vipId);
        return R.ok();
    }

    @PostMapping("/getUserVipInfo")
    public R<CoinsUserVipInfoVo> getUserVipInfo(@RequestBody Map<String, Object> map) {
        Long userId = Long.parseLong(map.get("userId").toString());
        return R.ok(coinsUserVipService.getUserVipInfo(userId));
    }

    /**
     * 检测是否有购买资格
     */
    @PostMapping("/checkPurchaseVip")
    public R<Integer> checkPurchaseVip(@RequestBody Map<String, Object> map) {
        Long userId = Long.parseLong(map.get("userId").toString());
        Long vipId = Long.parseLong(map.get("vipId").toString());
        return R.ok(coinsUserVipService.checkPurchaseVip(userId, vipId));
    }

    /**
     * 查询用户VIP 状态列表
     */
    @SaCheckPermission("system:userVip:list")
    @GetMapping("/list")
    public TableDataInfo<CoinsUserVipVo> list(CoinsUserVipBo bo, PageQuery pageQuery) {
        return coinsUserVipService.queryPageList(bo, pageQuery);
    }

    /**
     * 导出用户VIP 状态列表
     */
    @SaCheckPermission("system:userVip:export")
    @Log(title = "用户VIP 状态", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(CoinsUserVipBo bo, HttpServletResponse response) {
        List<CoinsUserVipVo> list = coinsUserVipService.queryList(bo);
        ExcelUtil.exportExcel(list, "用户VIP 状态", CoinsUserVipVo.class, response);
    }

    /**
     * 获取用户VIP 状态详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("system:userVip:query")
    @GetMapping("/{id}")
    public R<CoinsUserVipVo> getInfo(@NotNull(message = "主键不能为空")
                                     @PathVariable Long id) {
        return R.ok(coinsUserVipService.queryById(id));
    }

    /**
     * 新增用户VIP 状态
     */
    @SaCheckPermission("system:userVip:add")
    @Log(title = "用户VIP 状态", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody CoinsUserVipBo bo) {
        return toAjax(coinsUserVipService.insertByBo(bo));
    }

    /**
     * 修改用户VIP 状态
     */
    @SaCheckPermission("system:userVip:edit")
    @Log(title = "用户VIP 状态", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody CoinsUserVipBo bo) {
        return toAjax(coinsUserVipService.updateByBo(bo));
    }

    /**
     * 删除用户VIP 状态
     *
     * @param ids 主键串
     */
    @SaCheckPermission("system:userVip:remove")
    @Log(title = "用户VIP 状态", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable Long[] ids) {
        return toAjax(coinsUserVipService.deleteWithValidByIds(List.of(ids), true));
    }
}
