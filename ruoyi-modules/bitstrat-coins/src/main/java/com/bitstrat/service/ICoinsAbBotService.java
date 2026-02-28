package com.bitstrat.service;

import com.bitstrat.domain.coinGlass.CoinFundingInfo;
import com.bitstrat.domain.vo.CoinsAbBotVo;
import com.bitstrat.domain.bo.CoinsAbBotBo;
import com.bitstrat.domain.vo.CoinsCrossExchangeArbitrageTaskVo;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;

import java.util.Collection;
import java.util.List;

/**
 * 套利机器人Service接口
 *
 * @author Lion Li
 * @date 2025-05-24
 */
public interface ICoinsAbBotService {

    /**
     * 查询套利机器人
     *
     * @param id 主键
     * @return 套利机器人
     */
    CoinsAbBotVo queryById(Long id);

    /**
     * 分页查询套利机器人列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 套利机器人分页列表
     */
    TableDataInfo<CoinsAbBotVo> queryPageList(CoinsAbBotBo bo, PageQuery pageQuery);

    /**
     * 查询符合条件的套利机器人列表
     *
     * @param bo 查询条件
     * @return 套利机器人列表
     */
    List<CoinsAbBotVo> queryList(CoinsAbBotBo bo);

    /**
     * 新增套利机器人
     *
     * @param bo 套利机器人
     * @return 是否新增成功
     */
    Boolean insertByBo(CoinsAbBotBo bo);

    /**
     * 修改套利机器人
     *
     * @param bo 套利机器人
     * @return 是否修改成功
     */
    Boolean updateByBo(CoinsAbBotBo bo);

    /**
     * 校验并批量删除套利机器人信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);

    List<CoinsAbBotVo> queryBotByStatus(List<Long> status);

    /**
     * 开仓
     */
    void startPosition(CoinsAbBotVo coinsAbBotVo, CoinFundingInfo coinFundingInfo);

    /**
     * 平仓
     * @param coinsAbBotVo
     * @param vo
     */
    void closePosition(CoinsAbBotVo coinsAbBotVo, CoinsCrossExchangeArbitrageTaskVo vo);

    void updateStatusById(Long botId, Long status);

    void checkCloseStatus(CoinsAbBotVo coinsAbBotVo);
}
