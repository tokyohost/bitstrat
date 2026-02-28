package com.bitstrat.service;

import com.bitstrat.domain.vo.CoinApiPositionVo;
import com.bitstrat.domain.bo.CoinApiPositionBo;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;

import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * API 历史仓位数据Service接口
 *
 * @author Lion Li
 * @date 2025-12-29
 */
public interface ICoinApiPositionService {

    /**
     * 查询API 历史仓位数据
     *
     * @param id 主键
     * @return API 历史仓位数据
     */
    CoinApiPositionVo queryById(Long id);

    /**
     * 分页查询API 历史仓位数据列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return API 历史仓位数据分页列表
     */
    TableDataInfo<CoinApiPositionVo> queryPageList(CoinApiPositionBo bo, PageQuery pageQuery);

    /**
     * 查询符合条件的API 历史仓位数据列表
     *
     * @param bo 查询条件
     * @return API 历史仓位数据列表
     */
    List<CoinApiPositionVo> queryList(CoinApiPositionBo bo);

    /**
     * 新增API 历史仓位数据
     *
     * @param bo API 历史仓位数据
     * @return 是否新增成功
     */
    Boolean insertByBo(CoinApiPositionBo bo);

    /**
     * 修改API 历史仓位数据
     *
     * @param bo API 历史仓位数据
     * @return 是否修改成功
     */
    Boolean updateByBo(CoinApiPositionBo bo);

    /**
     * 校验并批量删除API 历史仓位数据信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);

    List<String> selectIdsByApiIdAndCurrentId(List<String> posIds, Long apiId);

    Double querySharpeRatioByApiIdAndStartTime(Long apiId, Date createTime);
}
