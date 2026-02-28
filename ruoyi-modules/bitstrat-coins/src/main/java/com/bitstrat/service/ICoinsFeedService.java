package com.bitstrat.service;

import com.bitstrat.domain.vo.CoinsFeedVo;
import com.bitstrat.domain.bo.CoinsFeedBo;
import jakarta.validation.constraints.NotNull;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;

import java.util.Collection;
import java.util.List;

/**
 * 策略广场Service接口
 *
 * @author Lion Li
 * @date 2025-12-12
 */
public interface ICoinsFeedService {

    /**
     * 查询策略广场
     *
     * @param id 主键
     * @return 策略广场
     */
    CoinsFeedVo queryById(Long id);

    /**
     * 分页查询策略广场列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 策略广场分页列表
     */
    TableDataInfo<CoinsFeedVo> queryPageList(CoinsFeedBo bo, PageQuery pageQuery);

    /**
     * 查询符合条件的策略广场列表
     *
     * @param bo 查询条件
     * @return 策略广场列表
     */
    List<CoinsFeedVo> queryList(CoinsFeedBo bo);

    /**
     * 新增策略广场
     *
     * @param bo 策略广场
     * @return 是否新增成功
     */
    Boolean insertByBo(CoinsFeedBo bo);

    /**
     * 修改策略广场
     *
     * @param bo 策略广场
     * @return 是否修改成功
     */
    Boolean updateByBo(CoinsFeedBo bo);

    /**
     * 校验并批量删除策略广场信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);

    List<CoinsFeedVo> queryListByTaskIds(List<Long> taskIds);

    void updateLikeCountById(@NotNull(message = "ID不能为空") Long id, int count);
}
