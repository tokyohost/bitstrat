package com.bitstrat.service;

import com.bitstrat.domain.CoinsUserVip;
import com.bitstrat.domain.bo.CoinsUserVipBo;
import com.bitstrat.domain.vo.CoinsUserVipInfoVo;
import com.bitstrat.domain.vo.CoinsUserVipVo;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;

import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * 用户VIP 状态Service接口
 *
 * @author Lion Li
 * @date 2025-05-14
 */
public interface ICoinsUserVipService {

    /**
     * 查询用户VIP 状态
     *
     * @param id 主键
     * @return 用户VIP 状态
     */
    CoinsUserVipVo queryById(Long id);

    /**
     * 分页查询用户VIP 状态列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 用户VIP 状态分页列表
     */
    TableDataInfo<CoinsUserVipVo> queryPageList(CoinsUserVipBo bo, PageQuery pageQuery);

    /**
     * 查询符合条件的用户VIP 状态列表
     *
     * @param bo 查询条件
     * @return 用户VIP 状态列表
     */
    List<CoinsUserVipVo> queryList(CoinsUserVipBo bo);

    /**
     * 新增用户VIP 状态
     *
     * @param bo 用户VIP 状态
     * @return 是否新增成功
     */
    Boolean insertByBo(CoinsUserVipBo bo);

    /**
     * 修改用户VIP 状态
     *
     * @param bo 用户VIP 状态
     * @return 是否修改成功
     */
    Boolean updateByBo(CoinsUserVipBo bo);

    /**
     * 校验并批量删除用户VIP 状态信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);

    boolean purchaseVip(Long userId, Long vipId);

    CoinsUserVipInfoVo getUserVipInfo(Long userId);

    Date calculateExpireTime(Date date, Integer avaliableDay);

    Integer checkPurchaseVip(Long userId, Long vipId);
}
