package com.bitstrat.controller;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.bitstrat.service.KeyCryptoService;
import com.bitstrat.store.ExecuteService;
import lombok.RequiredArgsConstructor;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.*;
import cn.dev33.satoken.annotation.SaCheckPermission;
import lombok.SneakyThrows;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.encrypt.annotation.ApiEncrypt;
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
import com.bitstrat.domain.vo.CoinsApiVo;
import com.bitstrat.domain.bo.CoinsApiBo;
import com.bitstrat.service.ICoinsApiService;
import org.dromara.common.mybatis.core.page.TableDataInfo;

/**
 * 交易所API
 *
 * @author Lion Li
 * @date 2025-04-14
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/system/api")
public class CoinsApiController extends BaseController {

    private final ICoinsApiService coinsApiService;

    private final KeyCryptoService keyCryptoService;
    private final ExecuteService executeService;
    ConcurrentHashMap<Long, CountDownLatch> syncBalanceLock = new ConcurrentHashMap<>();
    /**
     * 查询交易所API列表
     */
    @SneakyThrows
    @SaCheckPermission("system:api:list")
    @GetMapping("/list")
    @ApiEncrypt(response = true)
    public TableDataInfo<CoinsApiVo> list(CoinsApiBo bo, PageQuery pageQuery) {
//        boolean superAdmin = LoginHelper.isSuperAdmin();
//        if(!superAdmin) {
            bo.setUserId(LoginHelper.getUserId());
//        }
        TableDataInfo<CoinsApiVo> coinsApiVoTableDataInfo = coinsApiService.queryPageList(bo, pageQuery);
        //解密
        List<CoinsApiVo> rows = coinsApiVoTableDataInfo.getRows();
        for (CoinsApiVo row : rows) {
            row.setApiSecurity("****");
            row.setPassphrase("****");
            row.setAesKey("****");
            row.setIv("****");
        }

        return coinsApiVoTableDataInfo;
    }

    /**
     * 同步自己api 的余额
     * @return
     */
    @SaCheckLogin
    @GetMapping("/syncBalance")
    public synchronized R  syncBalance() {

        CountDownLatch countDown = syncBalanceLock.get(LoginHelper.getUserId());
        if (countDown == null || countDown.getCount() == 0) {
            CountDownLatch countDownNew = new CountDownLatch(1);
            syncBalanceLock.put(LoginHelper.getUserId(), countDownNew);
            List<CoinsApiVo> coinsApiVos = coinsApiService.queryApiByUserId(LoginHelper.getUserId());
            executeService.getApiBalanceExecute().submit(()->{
                try {
                    coinsApiService.syncBalance(coinsApiVos);
                } finally {
                    countDownNew.countDown();
                }
            });
            return R.ok("已提交同步任务");
        }else{
            return R.ok("后台正在同步,请稍后");
        }

    }



    /**
     * 导出交易所API列表
     */
//    @SaCheckPermission("system:api:export")
//    @Log(title = "交易所API", businessType = BusinessType.EXPORT)
//    @PostMapping("/export")
//    public void export(CoinsApiBo bo, HttpServletResponse response) {
//        boolean superAdmin = LoginHelper.isSuperAdmin();
//        if(!superAdmin) {
//            bo.setCreateBy(LoginHelper.getUserId());
//        }
//        List<CoinsApiVo> list = coinsApiService.queryList(bo);
//        ExcelUtil.exportExcel(list, "交易所API", CoinsApiVo.class, response);
//    }

    /**
     * 获取交易所API详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("system:api:query")
    @GetMapping("/{id}")
    @ApiEncrypt(response = true)
    public R<CoinsApiVo> getInfo(@NotNull(message = "主键不能为空")
                                     @PathVariable Long id) {
        //避免api 被轮询！
        return R.ok(coinsApiService.queryByUserAndId(LoginHelper.getUserId(),id));
    }

    /**
     * 新增交易所API
     */
    @SaCheckPermission("system:api:add")
    @Log(title = "交易所API", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    @ApiEncrypt(response = true)
    public R<Void> add(@Validated(AddGroup.class) @RequestBody CoinsApiBo bo) {
        bo.setUserId(LoginHelper.getUserId());
        bo.setCreateTime(new Date());
        return toAjax(coinsApiService.insertByBo(bo));
    }

    /**
     * 修改交易所API
     */
    @SaCheckPermission("system:api:edit")
    @Log(title = "交易所API", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    @ApiEncrypt(response = true)
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody CoinsApiBo bo) {
        bo.setUserId(LoginHelper.getUserId());
        return toAjax(coinsApiService.updateByBo(bo));
    }



    /**
     * 删除交易所API
     *
     * @param ids 主键串
     */
    @SaCheckPermission("system:api:remove")
    @Log(title = "交易所API", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    @ApiEncrypt(response = true)
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable Long[] ids) {
        return toAjax(coinsApiService.deleteWithValidByIds(List.of(ids), true));
    }
}
