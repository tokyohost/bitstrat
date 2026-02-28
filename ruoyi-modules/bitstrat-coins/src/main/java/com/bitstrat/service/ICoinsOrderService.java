package com.bitstrat.service;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bitstrat.domain.ContractOrder;
import com.bitstrat.domain.vo.CoinsOrderVo;
import com.bitstrat.domain.bo.CoinsOrderBo;
import com.bitstrat.mapper.CoinsOrderMapper;
import org.dromara.common.core.domain.R;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 订单列表Service接口
 *
 * @author Lion Li
 * @date 2025-04-21
 */
public interface ICoinsOrderService {

    /**
     * 查询订单列表
     *
     * @param id 主键
     * @return 订单列表
     */
    CoinsOrderVo queryById(Long id);

    /**
     * 分页查询订单列表列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 订单列表分页列表
     */
    TableDataInfo<CoinsOrderVo> queryPageList(CoinsOrderBo bo, PageQuery pageQuery);

    /**
     * 查询符合条件的订单列表列表
     *
     * @param bo 查询条件
     * @return 订单列表列表
     */
    List<CoinsOrderVo> queryList(CoinsOrderBo bo);

    /**
     * 新增订单列表
     *
     * @param bo 订单列表
     * @return 是否新增成功
     */
    Boolean insertByBo(CoinsOrderBo bo);

    /**
     * 修改订单列表
     *
     * @param bo 订单列表
     * @return 是否修改成功
     */
    Boolean updateByBo(CoinsOrderBo bo);

    /**
     * 校验并批量删除订单列表信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);

    List<CoinsOrderVo> syncOrder(List<CoinsOrderVo> rows,Long userId,boolean async);

    /**
     * 定时任务需要根据不同的订单去查找api
     * @param rows
     * @return
     */
    List<CoinsOrderVo> syncOrderTask(List<CoinsOrderVo> rows);

    /**
     * 查询未处于最终状态的订单
     * @return
     */
    List<CoinsOrderVo> queryUnEndOrderList();

    List<CoinsOrderVo> queryAllOrderByTaskIds(List<Long> taskids);

    /**
     * 填充市价
     * @param rows
     * @return
     */
    List<CoinsOrderVo> queryMarketPrice(List<CoinsOrderVo> rows);

    R updatePrice(CoinsOrderBo bo);

    List<CoinsOrderVo> formatSize(List<CoinsOrderVo> orderVoList, Long userId);

    List<CoinsOrderVo> queryAllOrderByTaskIdAndClosePositionFlag(Long id, long closePositionOrder);

    List<CoinsOrderVo> queryBatchUnEndOrderCount(Long batchId,Integer doneBatch);

    void updateDbPrice(Long id, BigDecimal nowPrice);

    CoinsOrderVo queryByOrderId(String ordId);

    CoinsOrderMapper getBaseMapper();

    void updateDbPriceAndOrderId(Long id, BigDecimal nowPrice, String orderId);

    Integer queryOrderCountByBatchId(Long id,Integer doneStatus);

    void updateOrderByContractOrder(ContractOrder contractOrder);

    void publishToWs(CoinsOrderBo coinsOrderBo,Long userId);
}
