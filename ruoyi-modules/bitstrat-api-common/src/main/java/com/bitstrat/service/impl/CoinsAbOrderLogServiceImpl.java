package com.bitstrat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bitstrat.domain.CoinsAbOrderLog;
import com.bitstrat.domain.Event.AckSendAndSaveABOrderLogEvent;
import com.bitstrat.domain.WebsocketMsgData;
import com.bitstrat.domain.abOrder.OrderTask;
import com.bitstrat.domain.bo.CoinsAbOrderLogBo;
import com.bitstrat.domain.vo.CoinsAbOrderLogVo;
import com.bitstrat.mapper.CoinsAbOrderLogMapper;
import com.bitstrat.service.ICoinsAbOrderLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.websocket.utils.WebSocketUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.bitstrat.constant.WebsocketMsgType.AB_ORDER_LOG;

/**
 * 价差套利日志Service业务层处理
 *
 * @author Lion Li
 * @date 2025-06-08
 */
@RequiredArgsConstructor
@Service
@Slf4j
public class CoinsAbOrderLogServiceImpl implements ICoinsAbOrderLogService {

    ExecutorService websocketNotifyExecute = Executors.newWorkStealingPool(2);

    private final CoinsAbOrderLogMapper baseMapper;

    /**
     * 查询价差套利日志
     *
     * @param id 主键
     * @return 价差套利日志
     */
    @Override
    public CoinsAbOrderLogVo queryById(Long id){
        return baseMapper.selectVoById(id);
    }

    /**
     * 分页查询价差套利日志列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 价差套利日志分页列表
     */
    @Override
    public TableDataInfo<CoinsAbOrderLogVo> queryPageList(CoinsAbOrderLogBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<CoinsAbOrderLog> lqw = buildQueryWrapper(bo);
        Page<CoinsAbOrderLogVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询符合条件的价差套利日志列表
     *
     * @param bo 查询条件
     * @return 价差套利日志列表
     */
    @Override
    public List<CoinsAbOrderLogVo> queryList(CoinsAbOrderLogBo bo) {
        LambdaQueryWrapper<CoinsAbOrderLog> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<CoinsAbOrderLog> buildQueryWrapper(CoinsAbOrderLogBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<CoinsAbOrderLog> lqw = Wrappers.lambdaQuery();
        lqw.orderByAsc(CoinsAbOrderLog::getId);
        lqw.eq(bo.getAccountA() != null, CoinsAbOrderLog::getAccountA, bo.getAccountA());
        lqw.eq(bo.getAccountB() != null, CoinsAbOrderLog::getAccountB, bo.getAccountB());
        lqw.eq(StringUtils.isNotBlank(bo.getExchangeA()), CoinsAbOrderLog::getExchangeA, bo.getExchangeA());
        lqw.eq(StringUtils.isNotBlank(bo.getExchangeB()), CoinsAbOrderLog::getExchangeB, bo.getExchangeB());
        lqw.eq(StringUtils.isNotBlank(bo.getTaskId()), CoinsAbOrderLog::getTaskId, bo.getTaskId());
        lqw.eq(StringUtils.isNotBlank(bo.getLog()), CoinsAbOrderLog::getLog, bo.getLog());
        return lqw;
    }

    /**
     * 新增价差套利日志
     *
     * @param bo 价差套利日志
     * @return 是否新增成功
     */
    @Override
    public Boolean insertByBo(CoinsAbOrderLogBo bo) {
        CoinsAbOrderLog add = MapstructUtils.convert(bo, CoinsAbOrderLog.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    /**
     * 修改价差套利日志
     *
     * @param bo 价差套利日志
     * @return 是否修改成功
     */
    @Override
    public Boolean updateByBo(CoinsAbOrderLogBo bo) {
        CoinsAbOrderLog update = MapstructUtils.convert(bo, CoinsAbOrderLog.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(CoinsAbOrderLog entity){
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 校验并批量删除价差套利日志信息
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
    public void sendAndSaveLog(OrderTask orderTask, String formatted) {
        log.info(formatted);
        websocketNotifyExecute.submit(()->{
            CoinsAbOrderLog coinsAbOrderLog = new CoinsAbOrderLog();
            coinsAbOrderLog.setTaskId(orderTask.getTaskId());
            coinsAbOrderLog.setAccountA(orderTask.getAccountA().getId());
            coinsAbOrderLog.setAccountB(orderTask.getAccountB().getId());
            coinsAbOrderLog.setExchangeA(orderTask.getExchangeA());
            coinsAbOrderLog.setExchangeB(orderTask.getExchangeB());
            coinsAbOrderLog.setLog(formatted);
            coinsAbOrderLog.setCreateTime(new Date());
            baseMapper.insert(coinsAbOrderLog);
            WebsocketMsgData<CoinsAbOrderLog> coinsAbOrderLogWebsocketMsgData = new WebsocketMsgData<>();
            coinsAbOrderLogWebsocketMsgData.setData(coinsAbOrderLog);
            coinsAbOrderLogWebsocketMsgData.setType(AB_ORDER_LOG);
            WebSocketUtils.sendMessage(orderTask.getUserId(),coinsAbOrderLogWebsocketMsgData.toJSONString());
        });

    }

    @EventListener(classes = AckSendAndSaveABOrderLogEvent.class)
    public void sendAndSaveLog(AckSendAndSaveABOrderLogEvent orderTask) {
        this.sendAndSaveLog(orderTask.getOrderTask(), orderTask.getMsg());
    }
}
