package com.bitstrat.controller;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.alibaba.fastjson2.JSONObject;
import com.bitstrat.config.DeviceConnectionManager;
import com.bitstrat.constant.MessageType;
import com.bitstrat.domain.Account;
import com.bitstrat.domain.bybit.ByBitAccount;
import com.bitstrat.domain.msg.ActiveLossPoint;
import com.bitstrat.domain.msg.ActiveLossPointData;
import com.bitstrat.domain.server.Message;
import com.bitstrat.domain.vo.EnableLossPointBody;
import com.bitstrat.domain.vo.SyncLossPointBody;
import com.bitstrat.service.impl.CommonServce;
import com.bitstrat.store.RoleCenter;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.RequiredArgsConstructor;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.*;
import cn.dev33.satoken.annotation.SaCheckPermission;
import lombok.SneakyThrows;
import org.dromara.common.satoken.utils.LoginHelper;
import org.springframework.beans.factory.annotation.Autowired;
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
import com.bitstrat.domain.vo.CoinsLossPointVo;
import com.bitstrat.domain.bo.CoinsLossPointBo;
import com.bitstrat.service.ICoinsLossPointService;
import org.dromara.common.mybatis.core.page.TableDataInfo;

/**
 * 滑点管理
 *
 * @author Lion Li
 * @date 2025-04-11
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/system/lossPoint")
public class CoinsLossPointController extends BaseController {

    private final ICoinsLossPointService coinsLossPointService;
    @Autowired
    RoleCenter roleCenter;

    @Autowired
    CommonServce commonServce;

    @Autowired
    DeviceConnectionManager deviceConnectionManager;
    /**
     * 查询滑点管理列表
     */
    @SaCheckPermission("system:lossPoint:list")
    @GetMapping("/list")
    public TableDataInfo<CoinsLossPointVo> list(CoinsLossPointBo bo, PageQuery pageQuery) {
        if (!LoginHelper.isSuperAdmin()) {
            bo.setCreateBy(LoginHelper.getUserId());
        }

        TableDataInfo<CoinsLossPointVo> coinsLossPointVoTableDataInfo = coinsLossPointService.queryPageList(bo, pageQuery);
        List<CoinsLossPointVo> rows = coinsLossPointVoTableDataInfo.getRows();
        Map<Long, ActiveLossPoint> lossPointMap = roleCenter.getSymbolLossPointMap().values().stream().collect(Collectors.toMap(ActiveLossPoint::getId, item -> item,(a,b)->a));
        for (CoinsLossPointVo row : rows) {
            if(lossPointMap.containsKey(row.getId())) {
                ActiveLossPoint activeLossPoint = lossPointMap.get(row.getId());
                row.setStatus(1);
                row.setNodeName(activeLossPoint.getNodeName());
            }else{
                row.setStatus(0);
            }
        }
//        rows
        return coinsLossPointVoTableDataInfo;
    }

    /**
     * 查询滑点管理列表
     */
    @SneakyThrows
    @SaCheckLogin
    @PostMapping("/sync")
    public R sync(@RequestBody SyncLossPointBody body) {
        CoinsLossPointVo coinsLossPointVo = coinsLossPointService.queryById(body.getLossPointId());

        ActiveLossPoint activeLossPoint = new ActiveLossPoint();
        activeLossPoint.setId(coinsLossPointVo.getId());
        activeLossPoint.setPrice(coinsLossPointVo.getPrice());
        activeLossPoint.setQuantity(coinsLossPointVo.getQuantity());
        activeLossPoint.setSymbol(coinsLossPointVo.getSymbol());
        activeLossPoint.setTriggerPrice1(coinsLossPointVo.getTriggerPrice1());
        activeLossPoint.setTriggerPrice2(coinsLossPointVo.getTriggerPrice2());
        activeLossPoint.setStopLossCalcLimit(coinsLossPointVo.getStopLossCalcLimit());
        List<Account> userAccountByExchange = commonServce.getUserAccountByExchange(LoginHelper.getUserId(), coinsLossPointVo.getExchangeName());
        if (userAccountByExchange.isEmpty()) {
            throw new RuntimeException("用户未配置"+coinsLossPointVo.getExchangeName()+" API");
        }
        activeLossPoint.setAccount((ByBitAccount) userAccountByExchange.get(0));

        activeLossPoint.setRetread(coinsLossPointVo.getRetread());
        activeLossPoint.setExchangeName(coinsLossPointVo.getExchangeName());

        ActiveLossPointData activeLossPointData = new ActiveLossPointData();
        activeLossPointData.setActiveLossPoints(List.of(activeLossPoint));
        Channel deviceChannel = deviceConnectionManager.getDeviceChannel(body.getClientId());
        Message message = new Message();
        message.setTimestamp(System.currentTimeMillis());
        message.setType(MessageType.UPDATE_LOSS_POINT);
        message.setData(activeLossPointData);
        ChannelFuture channelFuture = deviceChannel.writeAndFlush(new TextWebSocketFrame(JSONObject.toJSONString(message)));
        CountDownLatch countDownLatch = new CountDownLatch(1);
        AtomicReference<R> rData = new AtomicReference<>();
        if (channelFuture != null) {
            channelFuture.addListener(result -> {
                try{
                    if (result.cause() != null) {
                        //error
                        result.cause().printStackTrace();
                        rData.set(R.fail(result.cause().getMessage()));
                    } else {
                        //success
                        rData.set(R.ok("已下发"));
                    }
                }finally {
                    countDownLatch.countDown();
                }

            });
        }else{
            rData.set(R.fail("ERROR"));
            countDownLatch.countDown();
        }
        countDownLatch.await();

        commonServce.doOrderSubscript(coinsLossPointVo.getExchangeName(),body.getClientId(),coinsLossPointVo);
        return rData.get();
    }

    /**
     * stop滑点
     */
    @SneakyThrows
    @SaCheckLogin
    @PostMapping("/enable")
    public R enable(@RequestBody EnableLossPointBody body) {
        CoinsLossPointBo coinsLossPointBo = new CoinsLossPointBo();
        coinsLossPointBo.setId(body.getLossPointId());
        coinsLossPointBo.setEnable(body.getEnable());
        coinsLossPointService.updateByBo(coinsLossPointBo);
        if(body.getEnable() == 1) {
            //禁用
            SyncLossPointBody syncLossPointBody = new SyncLossPointBody();
            syncLossPointBody.setLossPointId(body.getLossPointId());
            CoinsLossPointVo coinsLossPointVo = coinsLossPointService.queryById(body.getLossPointId());
            syncLossPointBody.setClientId(coinsLossPointVo.getNodeClientId());
            this.stop(syncLossPointBody);
        }
        return R.ok();
    }
    /**
     * 下发所有
     */
    @SneakyThrows
    @SaCheckLogin
    @PostMapping("/syncAll")
    public R syncAll(@RequestBody SyncLossPointBody body) {
        coinsLossPointService.syncAll();
        return R.ok();
    }
    /**
     * 删除所有
     */
    @SneakyThrows
    @SaCheckLogin
    @PostMapping("/syncDeleteAll")
    public R syncDeleteAll(@RequestBody SyncLossPointBody body) {
        coinsLossPointService.syncDeleteAll();
        return R.ok();
    }
    /**
     * stop滑点
     */
    @SneakyThrows
    @SaCheckLogin
    @PostMapping("/stop")
    public R stop(@RequestBody SyncLossPointBody body) {
        CoinsLossPointVo coinsLossPointVo = coinsLossPointService.queryById(body.getLossPointId());

        ActiveLossPoint activeLossPoint = new ActiveLossPoint();
        activeLossPoint.setId(coinsLossPointVo.getId());
        activeLossPoint.setPrice(coinsLossPointVo.getPrice());
        activeLossPoint.setQuantity(coinsLossPointVo.getQuantity());
        activeLossPoint.setSymbol(coinsLossPointVo.getSymbol());
        activeLossPoint.setStopLossCalcLimit(coinsLossPointVo.getStopLossCalcLimit());
        ByBitAccount byBitAccount = commonServce.getByBitAccount();
        activeLossPoint.setAccount(byBitAccount);

        activeLossPoint.setRetread(coinsLossPointVo.getRetread());
        activeLossPoint.setExchangeName(coinsLossPointVo.getExchangeName());

        ActiveLossPointData activeLossPointData = new ActiveLossPointData();
        activeLossPointData.setActiveLossPoints(List.of(activeLossPoint));
        activeLossPointData.setDelete(true);
        Channel deviceChannel = deviceConnectionManager.getDeviceChannel(body.getClientId());
        Message message = new Message();
        message.setTimestamp(System.currentTimeMillis());
        message.setType(MessageType.UPDATE_LOSS_POINT);
        message.setData(activeLossPointData);
        ChannelFuture channelFuture = deviceChannel.writeAndFlush(new TextWebSocketFrame(JSONObject.toJSONString(message)));
        CountDownLatch countDownLatch = new CountDownLatch(1);
        AtomicReference<R> rData = new AtomicReference<>();
        if (channelFuture != null) {
            channelFuture.addListener(result -> {
                try{
                    if (result.cause() != null) {
                        //error
                        result.cause().printStackTrace();
                        rData.set(R.fail(result.cause().getMessage()));
                    } else {
                        //success
                        rData.set(R.ok("已删除"));
                    }
                }finally {
                    countDownLatch.countDown();
                }

            });
        }else{
            rData.set(R.fail("ERROR"));
            countDownLatch.countDown();
        }
        countDownLatch.await();
        return rData.get();
    }




    /**
     * 导出滑点管理列表
     */
    @SaCheckPermission("system:lossPoint:export")
    @Log(title = "滑点管理", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(CoinsLossPointBo bo, HttpServletResponse response) {
        List<CoinsLossPointVo> list = coinsLossPointService.queryList(bo);
        ExcelUtil.exportExcel(list, "滑点管理", CoinsLossPointVo.class, response);
    }

    /**
     * 获取滑点管理详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("system:lossPoint:query")
    @GetMapping("/{id}")
    public R<CoinsLossPointVo> getInfo(@NotNull(message = "主键不能为空")
                                     @PathVariable Long id) {
        return R.ok(coinsLossPointService.queryById(id));
    }

    /**
     * 新增滑点管理
     */
    @SaCheckPermission("system:lossPoint:add")
    @Log(title = "滑点管理", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody CoinsLossPointBo bo) {
        bo.setCreateBy(LoginHelper.getUserId());
        return toAjax(coinsLossPointService.insertByBo(bo));
    }

    /**
     * 修改滑点管理
     */
    @SaCheckPermission("system:lossPoint:edit")
    @Log(title = "滑点管理", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody CoinsLossPointBo bo) {
        return toAjax(coinsLossPointService.updateByBo(bo));
    }

    /**
     * 删除滑点管理
     *
     * @param ids 主键串
     */
    @SaCheckPermission("system:lossPoint:remove")
    @Log(title = "滑点管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable Long[] ids) {
        return toAjax(coinsLossPointService.deleteWithValidByIds(List.of(ids), true));
    }
}
