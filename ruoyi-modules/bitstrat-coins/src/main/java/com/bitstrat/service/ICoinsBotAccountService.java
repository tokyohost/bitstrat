package com.bitstrat.service;

import com.bitstrat.domain.bo.CoinsApiBo;
import com.bitstrat.domain.vo.CoinsApiVo;
import com.bitstrat.domain.vo.CoinsBotAccountVo;
import com.bitstrat.domain.bo.CoinsBotAccountBo;
import jakarta.validation.constraints.NotNull;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;

import java.util.Collection;
import java.util.List;

/**
 * 机器人可使用账户Service接口
 *
 * @author Lion Li
 * @date 2025-05-24
 */
public interface ICoinsBotAccountService {

    /**
     * 查询机器人可使用账户
     *
     * @param id 主键
     * @return 机器人可使用账户
     */
    CoinsBotAccountVo queryById(Long id);

    /**
     * 分页查询机器人可使用账户列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 机器人可使用账户分页列表
     */
    TableDataInfo<CoinsBotAccountVo> queryPageList(CoinsBotAccountBo bo, PageQuery pageQuery);

    /**
     * 查询符合条件的机器人可使用账户列表
     *
     * @param bo 查询条件
     * @return 机器人可使用账户列表
     */
    List<CoinsBotAccountVo> queryList(CoinsBotAccountBo bo);

    /**
     * 新增机器人可使用账户
     *
     * @param bo 机器人可使用账户
     * @return 是否新增成功
     */
    Boolean insertByBo(CoinsBotAccountBo bo);

    /**
     * 修改机器人可使用账户
     *
     * @param bo 机器人可使用账户
     * @return 是否修改成功
     */
    Boolean updateByBo(CoinsBotAccountBo bo);

    /**
     * 校验并批量删除机器人可使用账户信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);

    void deleteByBotId(Long botId);

    void createOrUpdateRelated( Long botId, List<CoinsApiVo> canUseApis);

    List<CoinsApiVo> selectRelatedByBotId( Long botId);
}
