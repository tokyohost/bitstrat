package com.bitstrat.service;

import com.bitstrat.domain.bo.CoinsNotifyLogBo;
import com.bitstrat.domain.vo.CoinsNotifyLogVo;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;

import java.util.Collection;
import java.util.List;

/**
 * 通知日志Service接口
 *
 * @author Lion Li
 * @date 2025-04-26
 */
public interface ICoinsNotifyLogService {

    /**
     * 查询通知日志
     *
     * @param id 主键
     * @return 通知日志
     */
    CoinsNotifyLogVo queryById(Long id);

    /**
     * 分页查询通知日志列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 通知日志分页列表
     */
    TableDataInfo<CoinsNotifyLogVo> queryPageList(CoinsNotifyLogBo bo, PageQuery pageQuery);

    /**
     * 查询符合条件的通知日志列表
     *
     * @param bo 查询条件
     * @return 通知日志列表
     */
    List<CoinsNotifyLogVo> queryList(CoinsNotifyLogBo bo);

    /**
     * 新增通知日志
     *
     * @param bo 通知日志
     * @return 是否新增成功
     */
    Boolean insertByBo(CoinsNotifyLogBo bo);

    /**
     * 修改通知日志
     *
     * @param bo 通知日志
     * @return 是否修改成功
     */
    Boolean updateByBo(CoinsNotifyLogBo bo);

    /**
     * 校验并批量删除通知日志信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);
}
