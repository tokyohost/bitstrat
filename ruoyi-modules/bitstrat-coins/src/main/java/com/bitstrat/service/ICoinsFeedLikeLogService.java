package com.bitstrat.service;

import com.bitstrat.domain.vo.CoinsFeedLikeLogVo;
import com.bitstrat.domain.bo.CoinsFeedLikeLogBo;
import jakarta.validation.constraints.NotNull;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;

import java.util.Collection;
import java.util.List;

/**
 * 策略广场like日志Service接口
 *
 * @author Lion Li
 * @date 2025-12-12
 */
public interface ICoinsFeedLikeLogService {

    /**
     * 查询策略广场like日志
     *
     * @param id 主键
     * @return 策略广场like日志
     */
    CoinsFeedLikeLogVo queryById(Long id);

    /**
     * 分页查询策略广场like日志列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 策略广场like日志分页列表
     */
    TableDataInfo<CoinsFeedLikeLogVo> queryPageList(CoinsFeedLikeLogBo bo, PageQuery pageQuery);

    /**
     * 查询符合条件的策略广场like日志列表
     *
     * @param bo 查询条件
     * @return 策略广场like日志列表
     */
    List<CoinsFeedLikeLogVo> queryList(CoinsFeedLikeLogBo bo);

    /**
     * 新增策略广场like日志
     *
     * @param bo 策略广场like日志
     * @return 是否新增成功
     */
    Boolean insertByBo(CoinsFeedLikeLogBo bo);

    /**
     * 修改策略广场like日志
     *
     * @param bo 策略广场like日志
     * @return 是否修改成功
     */
    Boolean updateByBo(CoinsFeedLikeLogBo bo);

    /**
     * 校验并批量删除策略广场like日志信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);

    CoinsFeedLikeLogVo selectLogByFeedIdAndUserId(@NotNull(message = "ID不能为空") Long id, Long userId);

    List<CoinsFeedLikeLogVo> selectLogByFeedIdsAndUserId(List<Long> feedIds, Long userId);
}
