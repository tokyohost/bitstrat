package com.bitstrat.service;

import com.bitstrat.domain.bo.CoinsAccountBalanceRecordBo;
import com.bitstrat.domain.vo.CoinsAccountBalanceRecordVo;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 账户余额记录Service接口
 *
 * @author Lion Li
 * @date 2025-05-07
 */
public interface ICoinsAccountBalanceRecordService {

    /**
     * 查询账户余额记录
     *
     * @param id 主键
     * @return 账户余额记录
     */
    CoinsAccountBalanceRecordVo queryById(Long id);

    /**
     * 分页查询账户余额记录列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 账户余额记录分页列表
     */
    TableDataInfo<CoinsAccountBalanceRecordVo> queryPageList(CoinsAccountBalanceRecordBo bo, PageQuery pageQuery);

    /**
     * 查询当天是否已经生成过数据，如果有则删除该条再次生成
     */
    void deleteOneRecordDayDataByConditions(CoinsAccountBalanceRecordBo conditions);
    /**
     * 查询符合条件的账户余额记录列表
     *
     * @param bo 查询条件
     * @return 账户余额记录列表
     */
    List<CoinsAccountBalanceRecordVo> queryList(CoinsAccountBalanceRecordBo bo);
    Map<String, List<CoinsAccountBalanceRecordVo>> queryRecordsInDays(CoinsAccountBalanceRecordBo bo);
    public void saveNewBalanceRecord(CoinsAccountBalanceRecordBo bo);
    /**
     * 新增账户余额记录
     *
     * @param bo 账户余额记录
     * @return 是否新增成功
     */
    Boolean insertByBo(CoinsAccountBalanceRecordBo bo);

    /**
     * 修改账户余额记录
     *
     * @param bo 账户余额记录
     * @return 是否修改成功
     */
    Boolean updateByBo(CoinsAccountBalanceRecordBo bo);

    /**
     * 校验并批量删除账户余额记录信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);

    List<CoinsAccountBalanceRecordVo> queryDailyGrowthPercentageList(CoinsAccountBalanceRecordBo bo);

    Map<String, BigDecimal> queryAnnualizedReturn(CoinsAccountBalanceRecordBo bo);
}
