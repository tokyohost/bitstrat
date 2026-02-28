package com.bitstrat.controller;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.bitstrat.domain.bo.CoinsAccountBalanceRecordBo;
import com.bitstrat.domain.vo.CoinsAccountBalanceRecordVo;
import com.bitstrat.service.ICoinsAccountBalanceRecordService;
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
import org.dromara.common.mybatis.core.page.TableDataInfo;

/**
 * 账户余额记录
 *
 * @author Lion Li
 * @date 2025-05-07
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/system/accountBalanceRecord")
public class CoinsAccountBalanceRecordController extends BaseController {

    private final ICoinsAccountBalanceRecordService coinsAccountBalanceRecordService;

    /**
     * 查询账户余额记录列表
     */
    @SaCheckLogin
    @GetMapping("/list")
    public TableDataInfo<CoinsAccountBalanceRecordVo> list(CoinsAccountBalanceRecordBo bo, PageQuery pageQuery) {
        Long userId = LoginHelper.getUserId();
        bo.setUserId(userId);
        return coinsAccountBalanceRecordService.queryPageList(bo, pageQuery);
    }

    /**
     * 查询最近N天的记录
     */
    @PostMapping("/queryRecordsInDays")
    @SaCheckLogin
    public R<Map<String, List<CoinsAccountBalanceRecordVo>>> queryRecordsInDays(@RequestBody CoinsAccountBalanceRecordBo bo) {
        if (bo.getDays() == null) {
            bo.setDays(30);//如果没有输入默认30天
        }
        Long userId = LoginHelper.getUserId();
        bo.setUserId(userId);
        return R.ok(coinsAccountBalanceRecordService.queryRecordsInDays(bo));
    }

    /**
     * 查询最近N天相较前日的总余额涨幅数组
     */
    @PostMapping("/queryDailyGrowthPercentageList")
    @SaCheckLogin
    public R<List<CoinsAccountBalanceRecordVo>> queryDailyGrowthPercentageList(@RequestBody CoinsAccountBalanceRecordBo bo) {
        if (bo.getDays() == null) {
            bo.setDays(30);//如果没有输入默认30天
        }
        Long userId = LoginHelper.getUserId();
        bo.setUserId(userId);
        return R.ok(coinsAccountBalanceRecordService.queryDailyGrowthPercentageList(bo));
    }

    /**
     * 查询该用户3天、7天、30天的年化收益
     */
    @PostMapping("/queryAnnualizedReturn")
    @SaCheckLogin
    public R<Map<String, BigDecimal>> queryAnnualizedReturn(@RequestBody CoinsAccountBalanceRecordBo bo) {
        if (bo.getDays() == null) {
            bo.setDays(30);//如果没有输入默认30天
        }
        Long userId = LoginHelper.getUserId();
        bo.setUserId(userId);
        return R.ok(coinsAccountBalanceRecordService.queryAnnualizedReturn(bo));
    }


    /**
     * 导出账户余额记录列表
     */
    @SaCheckPermission("system:accountBalanceRecord:export")
    @Log(title = "账户余额记录", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(CoinsAccountBalanceRecordBo bo, HttpServletResponse response) {
        List<CoinsAccountBalanceRecordVo> list = coinsAccountBalanceRecordService.queryList(bo);
        ExcelUtil.exportExcel(list, "账户余额记录", CoinsAccountBalanceRecordVo.class, response);
    }

    /**
     * 获取账户余额记录详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("system:accountBalanceRecord:query")
    @GetMapping("/{id}")
    public R<CoinsAccountBalanceRecordVo> getInfo(@NotNull(message = "主键不能为空")
                                     @PathVariable Long id) {
        return R.ok(coinsAccountBalanceRecordService.queryById(id));
    }

    /**
     * 新增账户余额记录
     */
    @SaCheckPermission("system:accountBalanceRecord:add")
    @Log(title = "账户余额记录", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody CoinsAccountBalanceRecordBo bo) {
        return toAjax(coinsAccountBalanceRecordService.insertByBo(bo));
    }

    /**
     * 修改账户余额记录
     */
    @SaCheckPermission("system:accountBalanceRecord:edit")
    @Log(title = "账户余额记录", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody CoinsAccountBalanceRecordBo bo) {
        return toAjax(coinsAccountBalanceRecordService.updateByBo(bo));
    }

    /**
     * 删除账户余额记录
     *
     * @param ids 主键串
     */
    @SaCheckPermission("system:accountBalanceRecord:remove")
    @Log(title = "账户余额记录", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable Long[] ids) {
        return toAjax(coinsAccountBalanceRecordService.deleteWithValidByIds(List.of(ids), true));
    }
}
