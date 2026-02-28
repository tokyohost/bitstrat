package com.bitstrat.service;

import com.bitstrat.domain.vo.CoinsNotifyConfigVo;
import com.bitstrat.domain.bo.CoinsNotifyConfigBo;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;

import java.util.Collection;
import java.util.List;

/**
 * 用户通知设置Service接口
 *
 * @author Lion Li
 * @date 2025-04-25
 */
public interface ICoinsNotifyConfigService {

    /**
     * 查询用户通知设置
     *
     * @param id 主键
     * @return 用户通知设置
     */
    CoinsNotifyConfigVo queryById(Long id);

    /**
     * 分页查询用户通知设置列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 用户通知设置分页列表
     */
    TableDataInfo<CoinsNotifyConfigVo> queryPageList(CoinsNotifyConfigBo bo, PageQuery pageQuery);

    /**
     * 查询符合条件的用户通知设置列表
     *
     * @param bo 查询条件
     * @return 用户通知设置列表
     */
    List<CoinsNotifyConfigVo> queryList(CoinsNotifyConfigBo bo);

    /**
     * 新增用户通知设置
     *
     * @param bo 用户通知设置
     * @return 是否新增成功
     */
    Boolean insertByBo(CoinsNotifyConfigBo bo);

    /**
     * 修改用户通知设置
     *
     * @param bo 用户通知设置
     * @return 是否修改成功
     */
    Boolean updateByBo(CoinsNotifyConfigBo bo);

    /**
     * 校验并批量删除用户通知设置信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);

    List<CoinsNotifyConfigVo> queryConfigByUserId(Long userId);
}
