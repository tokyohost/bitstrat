package com.bitstrat.service;

import com.bitstrat.domain.WebsocketExStatus;
import com.bitstrat.domain.vo.CoinsApiVo;
import com.bitstrat.domain.bo.CoinsApiBo;
import com.bitstrat.mapper.CoinsApiMapper;
import jakarta.validation.constraints.NotNull;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 交易所APIService接口
 *
 * @author Lion Li
 * @date 2025-04-14
 */
public interface ICoinsApiService {

    /**
     * 查询交易所API
     *
     * @param id 主键
     * @return 交易所API
     */
    CoinsApiVo queryById(Long id);

    /**
     * 分页查询交易所API列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 交易所API分页列表
     */
    TableDataInfo<CoinsApiVo> queryPageList(CoinsApiBo bo, PageQuery pageQuery);

    /**
     * 查询符合条件的交易所API列表
     *
     * @param bo 查询条件
     * @return 交易所API列表
     */
    List<CoinsApiVo> queryList(CoinsApiBo bo);

    /**
     * 新增交易所API
     *
     * @param bo 交易所API
     * @return 是否新增成功
     */
    Boolean insertByBo(CoinsApiBo bo);

    /**
     * 修改交易所API
     *
     * @param bo 交易所API
     * @return 是否修改成功
     */
    Boolean updateByBo(CoinsApiBo bo);

    /**
     * 校验并批量删除交易所API信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);

    CoinsApiMapper getBaseMapper();
    public List<WebsocketExStatus> getWebsocketExStatuses(Long userId);
    List<CoinsApiVo> queryApiByUserId(Long userId);

    CoinsApiVo queryApiByUserIdAndExchange(Long userId, String exchange);

    CoinsApiVo queryByUserAndId(Long userId, @NotNull(message = "主键不能为空") Long id);

    List<CoinsApiVo> queryApiListByUserIdAndExchange(Long userId, String ex);

    List<CoinsApiVo> queryByIds(List<Long> allIds);

    void syncBalance(List<CoinsApiVo> coinsApiVos);

    void updateBalanceAndFreeById(Long accountId, BigDecimal balance, BigDecimal freeBalance);
}
