package com.bitstrat.service;

import com.bitstrat.domain.vo.CoinsBatchOrderVo;
import com.bitstrat.domain.bo.CoinsBatchOrderBo;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;

import java.util.Collection;
import java.util.List;

/**
 * 分批任务订单记录Service接口
 *
 * @author Lion Li
 * @date 2025-04-26
 */
public interface ICoinsBatchOrderService {

    /**
     * 查询分批任务订单记录
     *
     * @param id 主键
     * @return 分批任务订单记录
     */
    CoinsBatchOrderVo queryById(Long id);

    /**
     * 分页查询分批任务订单记录列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 分批任务订单记录分页列表
     */
    TableDataInfo<CoinsBatchOrderVo> queryPageList(CoinsBatchOrderBo bo, PageQuery pageQuery);

    /**
     * 查询符合条件的分批任务订单记录列表
     *
     * @param bo 查询条件
     * @return 分批任务订单记录列表
     */
    List<CoinsBatchOrderVo> queryList(CoinsBatchOrderBo bo);

    /**
     * 新增分批任务订单记录
     *
     * @param bo 分批任务订单记录
     * @return 是否新增成功
     */
    Boolean insertByBo(CoinsBatchOrderBo bo);

    /**
     * 修改分批任务订单记录
     *
     * @param bo 分批任务订单记录
     * @return 是否修改成功
     */
    Boolean updateByBo(CoinsBatchOrderBo bo);

    /**
     * 校验并批量删除分批任务订单记录信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);
}
