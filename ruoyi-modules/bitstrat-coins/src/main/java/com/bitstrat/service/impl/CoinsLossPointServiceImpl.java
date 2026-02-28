package com.bitstrat.service.impl;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.bitstrat.config.DeviceConnectionManager;
import com.bitstrat.constant.MessageType;
import com.bitstrat.domain.Account;
import com.bitstrat.domain.CoinsApi;
import com.bitstrat.domain.bybit.ByBitAccount;
import com.bitstrat.domain.msg.ActiveLossPoint;
import com.bitstrat.domain.msg.ActiveLossPointData;
import com.bitstrat.domain.server.Message;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.apache.commons.collections4.CollectionUtils;
import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.dromara.common.satoken.utils.LoginHelper;
import org.springframework.stereotype.Service;
import com.bitstrat.domain.bo.CoinsLossPointBo;
import com.bitstrat.domain.vo.CoinsLossPointVo;
import com.bitstrat.domain.CoinsLossPoint;
import com.bitstrat.mapper.CoinsLossPointMapper;
import com.bitstrat.service.ICoinsLossPointService;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 滑点管理Service业务层处理
 *
 * @author Lion Li
 * @date 2025-04-11
 */
@RequiredArgsConstructor
@Service
public class CoinsLossPointServiceImpl implements ICoinsLossPointService {

    private final CoinsLossPointMapper baseMapper;

    private final CommonServce commonServce;

    private final DeviceConnectionManager deviceConnectionManager;

    /**
     * 查询滑点管理
     *
     * @param id 主键
     * @return 滑点管理
     */
    @Override
    public CoinsLossPointVo queryById(Long id){
        return baseMapper.selectVoById(id);
    }

    /**
     * 分页查询滑点管理列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 滑点管理分页列表
     */
    @Override
    public TableDataInfo<CoinsLossPointVo> queryPageList(CoinsLossPointBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<CoinsLossPoint> lqw = buildQueryWrapper(bo);
        Page<CoinsLossPointVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询符合条件的滑点管理列表
     *
     * @param bo 查询条件
     * @return 滑点管理列表
     */
    @Override
    public List<CoinsLossPointVo> queryList(CoinsLossPointBo bo) {
        LambdaQueryWrapper<CoinsLossPoint> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<CoinsLossPoint> buildQueryWrapper(CoinsLossPointBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<CoinsLossPoint> lqw = Wrappers.lambdaQuery();
        lqw.orderByAsc(CoinsLossPoint::getId);
        lqw.like(StringUtils.isNotBlank(bo.getExchangeName()), CoinsLossPoint::getExchangeName, bo.getExchangeName());
        lqw.eq(StringUtils.isNotBlank(bo.getSymbol()), CoinsLossPoint::getSymbol, bo.getSymbol());
        lqw.eq(bo.getPrice() != null, CoinsLossPoint::getPrice, bo.getPrice());
        lqw.eq(bo.getRetread() != null, CoinsLossPoint::getRetread, bo.getRetread());
        lqw.eq(bo.getQuantity() != null, CoinsLossPoint::getQuantity, bo.getQuantity());
        lqw.eq(bo.getCreateBy() != null, CoinsLossPoint::getCreateBy, bo.getCreateBy());
        return lqw;
    }

    /**
     * 新增滑点管理
     *
     * @param bo 滑点管理
     * @return 是否新增成功
     */
    @Override
    public Boolean insertByBo(CoinsLossPointBo bo) {
        CoinsLossPoint add = MapstructUtils.convert(bo, CoinsLossPoint.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    /**
     * 修改滑点管理
     *
     * @param bo 滑点管理
     * @return 是否修改成功
     */
    @Override
    public Boolean updateByBo(CoinsLossPointBo bo) {
        CoinsLossPoint update = MapstructUtils.convert(bo, CoinsLossPoint.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(CoinsLossPoint entity){
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 校验并批量删除滑点管理信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    @Override
    public Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid) {
        if(isValid){
            //TODO 做一些业务上的校验,判断是否需要校验
        }
        return baseMapper.deleteByIds(ids) > 0;
    }

    @Override
    public void syncAll() {
        QueryWrapper<CoinsLossPoint> coinsLossPointVoQueryWrapper = new QueryWrapper<>();
        LambdaQueryWrapper<CoinsLossPoint> lambdaQuery = coinsLossPointVoQueryWrapper.lambda();
        lambdaQuery.eq(CoinsLossPoint::getEnable, 2);
        lambdaQuery.eq(CoinsLossPoint::getCreateBy,LoginHelper.getUserId());
        List<CoinsLossPointVo> coinsLossPointVos = this.baseMapper.selectVoList(lambdaQuery);
//        ByBitAccount byBitAccount = commonServce.getByBitAccount();
        Map<String, List<CoinsLossPointVo>> nodePoints = coinsLossPointVos.stream().collect(Collectors.groupingBy(CoinsLossPointVo::getNodeClientId));
        nodePoints.forEach((nodeId,nodePoint)->{
            Channel deviceChannel = deviceConnectionManager.getDeviceChannel(nodeId);
            if(Objects.nonNull(deviceChannel) && deviceChannel.isActive()){
                Message message = new Message();
                message.setType(MessageType.UPDATE_LOSS_POINT);
                ActiveLossPointData activeLossPointData = new ActiveLossPointData();
                List<ActiveLossPoint> lossPoints = nodePoint.stream().map(coinsLossPointVo -> {
                    ActiveLossPoint activeLossPoint = new ActiveLossPoint();
                    activeLossPoint.setId(coinsLossPointVo.getId());
                    activeLossPoint.setPrice(coinsLossPointVo.getPrice());
                    activeLossPoint.setQuantity(coinsLossPointVo.getQuantity());
                    activeLossPoint.setSymbol(coinsLossPointVo.getSymbol());
                    activeLossPoint.setTriggerPrice1(coinsLossPointVo.getTriggerPrice1());
                    activeLossPoint.setTriggerPrice2(coinsLossPointVo.getTriggerPrice2());
                    activeLossPoint.setStopLossCalcLimit(coinsLossPointVo.getStopLossCalcLimit());
                    //todo 多租户时需要修改此处
                    List<Account> userAccountByExchange = commonServce.getUserAccountByExchange(LoginHelper.getUserId(), coinsLossPointVo.getExchangeName());
                    if(CollectionUtils.isEmpty(userAccountByExchange)){
                        throw new RuntimeException("用户未配置 "+ coinsLossPointVo.getExchangeName() + " API");
                    }
                    activeLossPoint.setAccount((ByBitAccount) userAccountByExchange.get(0));

                    activeLossPoint.setRetread(coinsLossPointVo.getRetread());
                    activeLossPoint.setExchangeName(coinsLossPointVo.getExchangeName());
                    return activeLossPoint;
                }).collect(Collectors.toList());
                activeLossPointData.setActiveLossPoints(lossPoints);
                message.setData(activeLossPointData);
                deviceChannel.writeAndFlush(new TextWebSocketFrame(JSONObject.toJSONString(message)));

            }else{
                throw new RuntimeException("下发节点异常 " + nodeId);
            }


        });


    }

    @Override
    public void syncDeleteAll() {
        QueryWrapper<CoinsLossPoint> coinsLossPointVoQueryWrapper = new QueryWrapper<>();
        LambdaQueryWrapper<CoinsLossPoint> lambdaQuery = coinsLossPointVoQueryWrapper.lambda();
        lambdaQuery.eq(CoinsLossPoint::getEnable, 2);
        lambdaQuery.eq(CoinsLossPoint::getCreateBy,LoginHelper.getUserId());
        List<CoinsLossPointVo> coinsLossPointVos = this.baseMapper.selectVoList(lambdaQuery);
//        ByBitAccount byBitAccount = commonServce.getByBitAccount();
        Map<String, List<CoinsLossPointVo>> nodePoints = coinsLossPointVos.stream().collect(Collectors.groupingBy(CoinsLossPointVo::getNodeClientId));
        nodePoints.forEach((nodeId,nodePoint)->{
            Channel deviceChannel = deviceConnectionManager.getDeviceChannel(nodeId);
            if(Objects.nonNull(deviceChannel) && deviceChannel.isActive()){
                Message message = new Message();
                message.setType(MessageType.UPDATE_LOSS_POINT);
                ActiveLossPointData activeLossPointData = new ActiveLossPointData();
                List<ActiveLossPoint> lossPoints = new ArrayList<>();
                activeLossPointData.setActiveLossPoints(lossPoints);
                activeLossPointData.setClearAll(true);
                message.setData(activeLossPointData);
                deviceChannel.writeAndFlush(new TextWebSocketFrame(JSONObject.toJSONString(message)));

            }else{
                throw new RuntimeException("下发节点异常 " + nodeId);
            }


        });
    }

    @Override
    public List<CoinsApi> selectAccountByClientId(String clientId) {
        return baseMapper.selectAccountByClientId(clientId);
    }
}
