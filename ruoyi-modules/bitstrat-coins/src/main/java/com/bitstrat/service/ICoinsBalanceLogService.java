package com.bitstrat.service;

import com.bitstrat.domain.CoinsAiTask;
import com.bitstrat.domain.CoinsBalanceLog;
import com.bitstrat.domain.bo.CoinAiTaskRequestBo;
import com.bitstrat.domain.bo.CoinsAiTaskBo;
import com.bitstrat.domain.vo.CoinsBalanceLogVo;
import com.bitstrat.domain.bo.CoinsBalanceLogBo;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

/**
 * 账户余额变动日志Service接口
 *
 * @author Lion Li
 * @date 2025-11-20
 */
public interface ICoinsBalanceLogService {

    /**
     * 查询账户余额变动日志
     *
     * @param id 主键
     * @return 账户余额变动日志
     */
    CoinsBalanceLogVo queryById(Long id);

    /**
     * 分页查询账户余额变动日志列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 账户余额变动日志分页列表
     */
    TableDataInfo<CoinsBalanceLogVo> queryPageList(CoinsBalanceLogBo bo, PageQuery pageQuery);

    /**
     * 查询符合条件的账户余额变动日志列表
     *
     * @param bo 查询条件
     * @return 账户余额变动日志列表
     */
    List<CoinsBalanceLogVo> queryList(CoinsBalanceLogBo bo);

    /**
     * 新增账户余额变动日志
     *
     * @param bo 账户余额变动日志
     * @return 是否新增成功
     */
    Boolean insertByBo(CoinsBalanceLogBo bo);

    /**
     * 修改账户余额变动日志
     *
     * @param bo 账户余额变动日志
     * @return 是否修改成功
     */
    Boolean updateByBo(CoinsBalanceLogBo bo);

    /**
     * 校验并批量删除账户余额变动日志信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);

    void reduceBalanceByLog(CoinsBalanceLog coinsBalanceLog);
    void addBalanceByUserId(BigDecimal amount, Long userId);

    CoinsBalanceLogBo queryBoById(String outTradeNo);

    void updateOrderPaid(String outTradeNo, String tradeNo, String tradeStatus);

    void checkBalance(CoinAiTaskRequestBo coinTestAiRequest, CoinsAiTask coinsAiTask);
    void checkBalance(CoinsAiTaskBo coinsAiTask);

    void updateOrderFail(String outTradeNo, String tradeNo, String type);

    void updateOrderCancel(String outTradeNo, String tradeNo, String type);
}
