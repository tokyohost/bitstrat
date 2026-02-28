package com.bitstrat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.bitstrat.constant.CoinsConstant;
import com.bitstrat.constant.ExchangeType;
import com.bitstrat.domain.Account;
import com.bitstrat.domain.AccountBalance;
import com.bitstrat.domain.WebsocketExStatus;
import com.bitstrat.domain.vo.WebsocketStatus;
import com.bitstrat.service.KeyCryptoService;
import com.bitstrat.strategy.ExchangeApiManager;
import com.bitstrat.strategy.ExchangeService;
import com.bitstrat.utils.AccountUtils;
import com.bitstrat.wsClients.ExchangeConnectionManager;
import com.bitstrat.wsClients.WsClusterManager;
import com.bitstrat.wsClients.constant.SocketConstant;
import com.bitstrat.wsClients.constant.WebSocketType;
import com.bitstrat.wsClients.domian.ConnectionConfig;
import io.netty.channel.Channel;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.dromara.common.core.constant.CacheNames;
import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.dromara.common.satoken.utils.LoginHelper;
import org.jetbrains.annotations.NotNull;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import com.bitstrat.domain.bo.CoinsApiBo;
import com.bitstrat.domain.vo.CoinsApiVo;
import com.bitstrat.domain.CoinsApi;
import com.bitstrat.mapper.CoinsApiMapper;
import com.bitstrat.service.ICoinsApiService;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 交易所APIService业务层处理
 *
 * @author Lion Li
 * @date 2025-04-14
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class CoinsApiServiceImpl implements ICoinsApiService {

    private final CoinsApiMapper baseMapper;
    private final KeyCryptoService keyCryptoService;

    private final ExchangeApiManager exchangeApiManager;

    private final ExchangeConnectionManager exchangeConnectionManager;

    private final WsClusterManager wsClusterManager;

    /**
     * 查询交易所API
     *
     * @param id 主键
     * @return 交易所API
     */
    @Override
    public CoinsApiVo queryById(Long id){
        CoinsApiVo coinsApiVo = baseMapper.selectVoById(id);
        return keyCryptoService.decryptApi(coinsApiVo);
    }

    /**
     * 分页查询交易所API列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 交易所API分页列表
     */
    @Override
    public TableDataInfo<CoinsApiVo> queryPageList(CoinsApiBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<CoinsApi> lqw = buildQueryWrapper(bo);
        Page<CoinsApiVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        List<CoinsApiVo> records = result.getRecords();
        for (CoinsApiVo record : records) {
            keyCryptoService.decryptApi(record);
        }
        result.setRecords(records);
        return TableDataInfo.build(result);
    }

    /**
     * 查询符合条件的交易所API列表
     *
     * @param bo 查询条件
     * @return 交易所API列表
     */
    @Override
    public List<CoinsApiVo> queryList(CoinsApiBo bo) {
        LambdaQueryWrapper<CoinsApi> lqw = buildQueryWrapper(bo);
        List<CoinsApiVo> coinsApiVos = baseMapper.selectVoList(lqw);
        for (CoinsApiVo coinsApiVo : coinsApiVos) {
            keyCryptoService.decryptApi(coinsApiVo);
        }
        return coinsApiVos;
    }

    private LambdaQueryWrapper<CoinsApi> buildQueryWrapper(CoinsApiBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<CoinsApi> lqw = Wrappers.lambdaQuery();
        lqw.eq(StringUtils.isNotBlank(bo.getApiKey()), CoinsApi::getApiKey, bo.getApiKey());
        lqw.eq(StringUtils.isNotBlank(bo.getApiSecurity()), CoinsApi::getApiSecurity, bo.getApiSecurity());
        lqw.like(StringUtils.isNotBlank(bo.getExchangeName()), CoinsApi::getExchangeName, bo.getExchangeName());
        lqw.eq(bo.getUserId() != null, CoinsApi::getUserId, bo.getUserId());
        lqw.eq(bo.getCreateBy() != null, CoinsApi::getCreateBy, bo.getCreateBy());
        return lqw;
    }

    /**
     * 新增交易所API
     *
     * @param bo 交易所API
     * @return 是否新增成功
     */
    @Override
    @CacheEvict(value = "userApi:cache#5m#1h#20", key = "#bo.userId+':'+#bo.exchangeName")
    public Boolean insertByBo(CoinsApiBo bo) {
        CoinsApi add = MapstructUtils.convert(bo, CoinsApi.class);
        validEntityBeforeSave(add);
        //加密
        keyCryptoService.encryptApi(add);

        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    /**
     * 修改交易所API
     *
     * @param bo 交易所API
     * @return 是否修改成功
     */
    @Override
    @CacheEvict(value = "userApi:cache#5m#1h#20", key = "#bo.userId+':'+#bo.exchangeName")
    public Boolean updateByBo(CoinsApiBo bo) {
        CoinsApi update = MapstructUtils.convert(bo, CoinsApi.class);
//        validEntityBeforeSave(update);
        //加密
        CoinsApi dbCoinsApi = baseMapper.selectById(bo.getId());
        update.setIv(dbCoinsApi.getIv());
        update.setAesKey(dbCoinsApi.getAesKey());
        keyCryptoService.encryptApi(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(CoinsApi entity){
        //TODO 做一些数据校验,如唯一约束
        QueryWrapper<CoinsApi> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(CoinsApi::getExchangeName, entity.getExchangeName())
                    .eq(CoinsApi::getApiKey, entity.getApiKey());
        Long l = baseMapper.selectCount(queryWrapper);
        if (l > 0) {
            throw new RuntimeException("您已配置了 "+ entity.getApiKey() + " ，不允许重复配置");
        }

    }

    /**
     * 校验并批量删除交易所API信息
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
    public CoinsApiMapper getBaseMapper() {
        return baseMapper;
    }

    @Override
    public List<CoinsApiVo> queryApiByUserId(Long userId) {
        QueryWrapper<CoinsApi> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(CoinsApi::getUserId, userId);
        List<CoinsApiVo> coinsApiVos = baseMapper.selectVoList(queryWrapper);
        for (CoinsApiVo coinsApiVo : coinsApiVos) {
            keyCryptoService.decryptApi(coinsApiVo);
        }
        return coinsApiVos;
    }

    @NotNull
    public List<WebsocketExStatus> getWebsocketExStatuses(Long userId) {
        List<WebsocketExStatus> result = new ArrayList<>();
        List<CoinsApiVo> coinsApiVos = this.queryApiByUserId(userId);
        Map<String, List<CoinsApiVo>> exchangeApilist = coinsApiVos.stream().collect(Collectors.groupingBy((item)->item.getExchangeName().toLowerCase()));


        for (ExchangeType ex : ExchangeType.values()) {
            WebsocketExStatus websocketExStatus = new WebsocketExStatus();
            websocketExStatus.setExchangeName(ex.getName());

            List<CoinsApiVo> exApis = exchangeApilist.getOrDefault(ex.getName(), new ArrayList<>());
            List<WebsocketStatus> exSocketStatus = new ArrayList<>();
            List<Channel> channels = exchangeConnectionManager.getChannel(userId + "", ex.getName(), WebSocketType.PRIVATE);
//            for (CoinsApiVo exApi : exApis) {
            HashSet<Long> activeApiId = new HashSet<>();
            for (Channel channel : channels) {
                WebsocketStatus websocketStatus = new WebsocketStatus();
                websocketStatus.setExchange(ex.getName());
                websocketStatus.setWsType(WebSocketType.PRIVATE);
                if (Objects.nonNull(channel)) {
                    ConnectionConfig connectionConfig = channel.attr(SocketConstant.connectionConfig).get();
                    activeApiId.add(connectionConfig.getAccount().getId());
                    websocketStatus.setExchange(ex.getName());
                    websocketStatus.setApiId(connectionConfig.getAccount().getId());
                    websocketStatus.setApiName(connectionConfig.getAccount().getName());
                    websocketStatus.setNodeName(wsClusterManager.getNodeId());
                    websocketStatus.setStatus(channel.isActive() ? "active" : "inactive");
                    if(connectionConfig != null && channel.isActive()){
                        if (connectionConfig.getDely() != null) {
                            websocketStatus.setDely(connectionConfig.getDely() / 2);
                        }else{
                            websocketStatus.setDely(null);
                        }
                    }else{
                        websocketStatus.setStatus("inactive");
                    }
                }else{
                    websocketStatus.setStatus("inactive");
                }
                exSocketStatus.add(websocketStatus);
            }
//            }
            //处理没有创建链接的api
            for (CoinsApiVo exApi : exApis) {
                if(activeApiId.contains(exApi.getId())){
                    //已创建了
                }else{
                    //判断其他节点是否存在链接
                    String profix = ex.getName() + ":" + WebSocketType.PRIVATE + ":" + exApi.getId();
                    boolean online = wsClusterManager.isOnline(exApi.getUserId() + "", profix);
                    if (online) {
                        //在其他节点上
                        WebsocketStatus websocketStatus = new WebsocketStatus();
                        String userNode = wsClusterManager.getUserNode(exApi.getUserId() + "", profix);
                        Long dely = wsClusterManager.getApiDely(exApi.getId(),userNode);
                        if(Objects.nonNull(dely)){
                            websocketStatus.setDely(dely);
                        }else{
                            websocketStatus.setDely(null);
                        }
                        websocketStatus.setExchange(ex.getName());
                        websocketStatus.setWsType(WebSocketType.PRIVATE);
                        websocketStatus.setStatus("active");
                        websocketStatus.setNodeName(userNode);

                        websocketStatus.setApiId(exApi.getId());
                        websocketStatus.setApiName(exApi.getName());
                        exSocketStatus.add(websocketStatus);
                    }else{
                        WebsocketStatus websocketStatus = new WebsocketStatus();
                        websocketStatus.setExchange(ex.getName());
                        websocketStatus.setWsType(WebSocketType.PRIVATE);
                        websocketStatus.setStatus("inactive");
                        websocketStatus.setDely(null);
                        websocketStatus.setApiId(exApi.getId());
                        websocketStatus.setApiName(exApi.getName());
                        exSocketStatus.add(websocketStatus);
                    }

                }
            }

            websocketExStatus.setDatas(exSocketStatus);


            result.add(websocketExStatus);
        }
        return result;
    }

    @Override
    @Cacheable(value = "userApi:cache#5m#1h#20", key = "#userId+':'+#exchange",condition = "#userId != null && #exchange != null ")
    public CoinsApiVo queryApiByUserIdAndExchange(Long userId, String exchange) {
        QueryWrapper<CoinsApi> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(CoinsApi::getUserId, userId)
            .eq(CoinsApi::getExchangeName, exchange)
            .last("limit 1");
        CoinsApiVo coinsApiVo = baseMapper.selectVoOne(queryWrapper);

        return coinsApiVo;
    }

    @Override
    public CoinsApiVo queryByUserAndId(Long userId, Long id) {
        QueryWrapper<CoinsApi> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(CoinsApi::getUserId, userId)
            .eq(CoinsApi::getId, id);
        CoinsApiVo coinsApiVo = baseMapper.selectVoOne(queryWrapper);
        CoinsApiVo decryptApi = keyCryptoService.decryptApi(coinsApiVo);
        return decryptApi;
    }


    @Override
    public List<CoinsApiVo> queryApiListByUserIdAndExchange(Long userId, String ex) {
        QueryWrapper<CoinsApi> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(CoinsApi::getUserId, userId)
            .eq(CoinsApi::getExchangeName, ex);
        List<CoinsApiVo> coinsApiVos = baseMapper.selectVoList(queryWrapper);
        for (CoinsApiVo coinsApiVo : coinsApiVos) {
            keyCryptoService.decryptApi(coinsApiVo);
        }
        return coinsApiVos;
    }

    @Override
    public List<CoinsApiVo> queryByIds(List<Long> allIds) {
        if(CollectionUtils.isEmpty(allIds)){
            return List.of();
        }
        QueryWrapper<CoinsApi> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().in(CoinsApi::getId, allIds);
        List<CoinsApiVo> coinsApiVos = baseMapper.selectVoList(queryWrapper);
        for (CoinsApiVo coinsApiVo : coinsApiVos) {
            keyCryptoService.decryptApi(coinsApiVo);
        }
        return coinsApiVos;
    }

    @Override
//    @Transactional
    public void syncBalance(List<CoinsApiVo> coinsApiVos) {
        log.info("开始更新api 余额---");
        //同步balance
        Date date = new Date();
        for (CoinsApiVo coinsApiVo : coinsApiVos) {
            ExchangeService exchangeService = exchangeApiManager.getExchangeService(coinsApiVo.getExchangeName());
            Account account = AccountUtils.coverToAccount(coinsApiVo);
            AccountBalance balance = exchangeService.getBalance(account, CoinsConstant.USDT);
            coinsApiVo.setBalance(balance.getBalance());
            coinsApiVo.setFreeBalance(balance.getFreeBalance());
            coinsApiVo.setBalanceUpdate(date);
            LambdaUpdateWrapper<CoinsApi> updateWrapper = new LambdaUpdateWrapper<CoinsApi>();
            updateWrapper.eq(CoinsApi::getId, coinsApiVo.getId())
                .set(CoinsApi::getBalance, coinsApiVo.getBalance())
                .set(CoinsApi::getFreeBalance, coinsApiVo.getFreeBalance())
                .set(CoinsApi::getBalanceUpdate, coinsApiVo.getBalanceUpdate());
            baseMapper.update(updateWrapper);
        }
        log.info("结束更新api 余额---");
    }

    @Override
    public void updateBalanceAndFreeById(Long accountId, BigDecimal balance, BigDecimal freeBalance) {
        if(Objects.isNull(accountId)){
            log.error("Account Id is null !");
            return;
        }
        baseMapper.updateBalanceAndFreeById(accountId, balance, freeBalance);
    }

}
