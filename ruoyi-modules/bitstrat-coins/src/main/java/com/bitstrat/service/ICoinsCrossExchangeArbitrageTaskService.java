package com.bitstrat.service;

import com.bitstrat.domain.Account;
import com.bitstrat.domain.CoinsCrossExchangeArbitrageTask;
import com.bitstrat.domain.StartTaskVo;
import com.bitstrat.domain.vo.AbTaskFrom;
import com.bitstrat.domain.vo.ArbitrageFormData;
import com.bitstrat.domain.vo.CoinsCrossExchangeArbitrageTaskVo;
import com.bitstrat.domain.bo.CoinsCrossExchangeArbitrageTaskBo;
import com.bitstrat.domain.vo.CreateArbitrageTaskVo;
import com.bitstrat.mapper.CoinsCrossExchangeArbitrageTaskMapper;
import org.dromara.common.core.domain.R;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 跨交易所套利任务Service接口
 *
 * @author Lion Li
 * @date 2025-04-19
 */
public interface ICoinsCrossExchangeArbitrageTaskService {

    List<CoinsCrossExchangeArbitrageTaskVo> queryListWithWarning(Map<String, Object> params);
    /**
     * 查询跨交易所套利任务
     *
     * @param id 主键
     * @return 跨交易所套利任务
     */
    CoinsCrossExchangeArbitrageTaskVo queryById(Long id);

    /**
     * 分页查询跨交易所套利任务列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 跨交易所套利任务分页列表
     */
    TableDataInfo<CoinsCrossExchangeArbitrageTaskVo> queryPageList(CoinsCrossExchangeArbitrageTaskBo bo, PageQuery pageQuery);

    /**
     * 查询符合条件的跨交易所套利任务列表
     *
     * @param bo 查询条件
     * @return 跨交易所套利任务列表
     */
    List<CoinsCrossExchangeArbitrageTaskVo> queryList(CoinsCrossExchangeArbitrageTaskBo bo);

    /**
     * 新增跨交易所套利任务
     *
     * @param bo 跨交易所套利任务
     * @return 是否新增成功
     */
    Boolean insertByBo(CoinsCrossExchangeArbitrageTaskBo bo);

    /**
     * 修改跨交易所套利任务
     *
     * @param bo 跨交易所套利任务
     * @return 是否修改成功
     */
    Boolean updateByBo(CoinsCrossExchangeArbitrageTaskBo bo);

    /**
     * 校验并批量删除跨交易所套利任务信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);

    R createTask(CreateArbitrageTaskVo createArbitrageTaskVo);
    void oncePlace2ExOrder(AbTaskFrom from);
    R startTask(StartTaskVo task);

    R createCrossArbitrage(CreateArbitrageTaskVo createArbitrageTaskVo);

    /**
     * 分批建仓
     * @param createArbitrageTaskVo
     */
    public void createBatchOrder(CreateArbitrageTaskVo createArbitrageTaskVo);
    /**
     * 同步持仓状态
     *
     * @param rows
     * @return
     */
    List<CoinsCrossExchangeArbitrageTaskVo> syncPosition(List<CoinsCrossExchangeArbitrageTaskVo> rows, Long userId);

    R<Void> closePosition(CreateArbitrageTaskVo createArbitrageTaskVo);

    /**
     * 查询还没有平仓的任务
     *
     * @return
     */
    List<CoinsCrossExchangeArbitrageTaskVo> queryHandleList();

    public void updateVos(List<CoinsCrossExchangeArbitrageTaskVo> tasks);

    /**
     * 重新计算手续费
     */
    void updateFee();

    /**
     * 同步单个任务
     *
     * @param taskId
     * @return
     */
    R syncTask(Long taskId);

    /**
     * 格式化持仓数量，有的交易所是张(okx)有的是usdt 全部格式化为具体币种数量
     *
     * @param rows
     * @param userId
     * @return
     */
    List<CoinsCrossExchangeArbitrageTaskVo> formateSize(List<CoinsCrossExchangeArbitrageTaskVo> rows, Long userId);

    CoinsCrossExchangeArbitrageTaskVo formateSize(CoinsCrossExchangeArbitrageTaskVo data, Long userId);

    void oncePlace2ExOrderClosePosition(AbTaskFrom from);

    CoinsCrossExchangeArbitrageTaskMapper getBaseMapper();

    void updateTaskStatus(Long id, Long status);

    List<CoinsCrossExchangeArbitrageTaskVo> formateARY(List<CoinsCrossExchangeArbitrageTaskVo> rows, Long userId);

    CoinsCrossExchangeArbitrageTaskVo queryActiveTaskByUserIdAndSymbol(String symbol, Long userId);

    CoinsCrossExchangeArbitrageTaskVo queryActiveTaskByUserIdSymbolAndAccountId(String symbol, Long userId, Long account);

    List<CoinsCrossExchangeArbitrageTaskVo> queryActiveTaskByUserIdAndAccountId(Long userId, Long id);
}
