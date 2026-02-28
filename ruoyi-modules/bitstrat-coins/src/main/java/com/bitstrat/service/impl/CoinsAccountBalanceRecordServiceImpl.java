package com.bitstrat.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import com.bitstrat.domain.CoinsAccountBalanceRecord;
import com.bitstrat.domain.bo.CoinsAccountBalanceRecordBo;
import com.bitstrat.domain.vo.CoinsAccountBalanceRecordVo;
import com.bitstrat.mapper.CoinsAccountBalanceRecordMapper;
import com.bitstrat.service.ICoinsAccountBalanceRecordService;
import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.dromara.common.satoken.utils.LoginHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 账户余额记录Service业务层处理
 *
 * @author Lion Li
 * @date 2025-05-07
 */
@RequiredArgsConstructor
@Service
public class CoinsAccountBalanceRecordServiceImpl implements ICoinsAccountBalanceRecordService {

    private final CoinsAccountBalanceRecordMapper baseMapper;

    /**
     * 查询账户余额记录
     *
     * @param id 主键
     * @return 账户余额记录
     */
    @Override
    public CoinsAccountBalanceRecordVo queryById(Long id){
        return baseMapper.selectVoById(id);
    }

    /**
     * 查询当天是否已经生成过数据，如果有则删除该条再次生成
     */
    @Override
    public void deleteOneRecordDayDataByConditions(CoinsAccountBalanceRecordBo conditions) {
        LambdaQueryWrapper<CoinsAccountBalanceRecord> queryWrapper = new LambdaQueryWrapper<>();
        Date recordDate = conditions.getRecordDate();
        Date todayStart = Objects.nonNull(recordDate)? DateUtil.beginOfDay(recordDate) : null; // 2024-05-10 00:00:00
        Date todayEnd = Objects.nonNull(recordDate)? DateUtil.endOfDay(recordDate) : null;     // 2024-05-10 23:59:59
        queryWrapper.eq(Objects.nonNull(conditions.getExchange()), CoinsAccountBalanceRecord::getExchange, conditions.getExchange());
        queryWrapper.eq(Objects.nonNull(conditions.getUserId()), CoinsAccountBalanceRecord::getUserId, conditions.getUserId());
        if (conditions.getRecordDate() != null) {
            queryWrapper.between(CoinsAccountBalanceRecord::getRecordDate, todayStart, todayEnd);
            baseMapper.delete(queryWrapper);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void saveNewBalanceRecord(CoinsAccountBalanceRecordBo bo) {
        this.deleteOneRecordDayDataByConditions(bo);
        this.insertByBo(bo);
    }

    /**
     * 分页查询账户余额记录列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 账户余额记录分页列表
     */
    @Override
    public TableDataInfo<CoinsAccountBalanceRecordVo> queryPageList(CoinsAccountBalanceRecordBo bo, PageQuery pageQuery) {
        bo.setUserId(LoginHelper.getUserId());
        LambdaQueryWrapper<CoinsAccountBalanceRecord> lqw = buildQueryWrapper(bo);
        Page<CoinsAccountBalanceRecordVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }


    /**
     * 查询符合条件的账户余额记录列表
     *
     * @param bo 查询条件
     * @return 账户余额记录列表
     */
    @Override
    public Map<String, List<CoinsAccountBalanceRecordVo>> queryRecordsInDays(CoinsAccountBalanceRecordBo bo) {
        bo.setUserId(LoginHelper.getUserId());
        LambdaQueryWrapper<CoinsAccountBalanceRecord> lqw = buildQueryWrapper(bo);
        // 查询最近N天的数据
        if (bo.getDays() != null) {
            lqw.ge(CoinsAccountBalanceRecord::getRecordDate, LocalDate.now().minusDays(bo.getDays()));
        }
        List<CoinsAccountBalanceRecordVo> coinsAccountBalanceRecordVos = baseMapper.selectVoList(lqw);

        // 按 exchange 分组，并按日期排序
        Map<String, List<CoinsAccountBalanceRecordVo>> groupedByExchangeSortedByDate = coinsAccountBalanceRecordVos.stream()
            .collect(Collectors.groupingBy(
                CoinsAccountBalanceRecordVo::getExchange,
                Collectors.toList()
            ))
            .entrySet().stream()
            .peek(entry -> entry.setValue(entry.getValue().stream()
                .sorted(Comparator.comparing(CoinsAccountBalanceRecordVo::getRecordDate))
                .collect(Collectors.toList())))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        // 获取查询范围内的所有日期，不包括当天
        List<Date> allDates = new ArrayList<>();
        LocalDate startDate = LocalDate.now().minusDays(bo.getDays());
        LocalDate endDate = LocalDate.now().minusDays(1); // 不包括当天
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            allDates.add(DateUtil.parse(date.toString()));
        }

        // 对每个分类补充缺失日期的记录
        for (Map.Entry<String, List<CoinsAccountBalanceRecordVo>> entry : groupedByExchangeSortedByDate.entrySet()) {
            List<CoinsAccountBalanceRecordVo> records = entry.getValue();
            List<Date> existingDates = records.stream()
                .map(CoinsAccountBalanceRecordVo::getRecordDate)
                .toList();

            for (Date date : allDates) {
                if (!existingDates.contains(date)) {
                    CoinsAccountBalanceRecordVo zeroRecord = new CoinsAccountBalanceRecordVo();
                    zeroRecord.setRecordDate(date);
                    zeroRecord.setExchange(entry.getKey());
                    zeroRecord.setBalance(BigDecimal.ZERO);
                    zeroRecord.setUsdtBalance(BigDecimal.ZERO);
                    zeroRecord.setFreeBalance(BigDecimal.ZERO);
                    zeroRecord.setCashBalance(BigDecimal.ZERO);
                    records.add(zeroRecord);
                }
            }
            // 重新按日期排序
            records.sort(Comparator.comparing(CoinsAccountBalanceRecordVo::getRecordDate));
        }

        // 新增逻辑：将所有 exchange 的每日汇总金额作为一个新的分组类别 "all"
        Map<Date, CoinsAccountBalanceRecordVo> allExchangeSummary = new java.util.HashMap<>();
        for (Map.Entry<String, List<CoinsAccountBalanceRecordVo>> entry : groupedByExchangeSortedByDate.entrySet()) {
            for (CoinsAccountBalanceRecordVo record : entry.getValue()) {
                Date recordDate = record.getRecordDate();
                CoinsAccountBalanceRecordVo summaryRecord = allExchangeSummary.getOrDefault(recordDate, new CoinsAccountBalanceRecordVo());
                summaryRecord.setRecordDate(recordDate);
                summaryRecord.setExchange("all");
                summaryRecord.setBalance(summaryRecord.getBalance().add(record.getBalance()));
                summaryRecord.setUsdtBalance(summaryRecord.getUsdtBalance().add(record.getUsdtBalance()));
                summaryRecord.setFreeBalance(summaryRecord.getFreeBalance().add(record.getFreeBalance()));
                summaryRecord.setCashBalance(summaryRecord.getCashBalance().add(record.getCashBalance()));
                allExchangeSummary.put(recordDate, summaryRecord);
            }
        }

        // 将 "sum" 分组的结果转换为 List 并排序
        List<CoinsAccountBalanceRecordVo> allExchangeList = new ArrayList<>(allExchangeSummary.values());
        allExchangeList.sort(Comparator.comparing(CoinsAccountBalanceRecordVo::getRecordDate));

        // 将 "sum" 分组结果加入到最终结果中
        groupedByExchangeSortedByDate.put("sum", allExchangeList);

        return groupedByExchangeSortedByDate;
    }

    /**
     * 查询符合条件的账户余额记录列表
     *
     * @param bo 查询条件
     * @return 账户余额记录列表
     */
    @Override
    public List<CoinsAccountBalanceRecordVo> queryList(CoinsAccountBalanceRecordBo bo) {
        bo.setUserId(LoginHelper.getUserId());
        LambdaQueryWrapper<CoinsAccountBalanceRecord> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<CoinsAccountBalanceRecord> buildQueryWrapper(CoinsAccountBalanceRecordBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<CoinsAccountBalanceRecord> lqw = Wrappers.lambdaQuery();
        lqw.orderByAsc(CoinsAccountBalanceRecord::getId);
        lqw.eq(bo.getUserId() != null, CoinsAccountBalanceRecord::getUserId, bo.getUserId());
        lqw.eq(StringUtils.isNotBlank(bo.getExchange()), CoinsAccountBalanceRecord::getExchange, bo.getExchange());
        lqw.eq(bo.getBalance() != null, CoinsAccountBalanceRecord::getBalance, bo.getBalance());
        lqw.eq(bo.getCashBalance() != null, CoinsAccountBalanceRecord::getCashBalance, bo.getCashBalance());
        lqw.eq(bo.getUsdtBalance() != null, CoinsAccountBalanceRecord::getUsdtBalance, bo.getUsdtBalance());
        lqw.eq(bo.getFreeBalance() != null, CoinsAccountBalanceRecord::getFreeBalance, bo.getFreeBalance());
        lqw.eq(bo.getRecordTime() != null, CoinsAccountBalanceRecord::getRecordTime, bo.getRecordTime());
        lqw.eq(bo.getRecordDate() != null, CoinsAccountBalanceRecord::getRecordDate, bo.getRecordDate());
        return lqw;
    }

    /**
     * 新增账户余额记录
     *
     * @param bo 账户余额记录
     * @return 是否新增成功
     */
    @Override
    public Boolean insertByBo(CoinsAccountBalanceRecordBo bo) {
        CoinsAccountBalanceRecord add = MapstructUtils.convert(bo, CoinsAccountBalanceRecord.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    /**
     * 修改账户余额记录
     *
     * @param bo 账户余额记录
     * @return 是否修改成功
     */
    @Override
    public Boolean updateByBo(CoinsAccountBalanceRecordBo bo) {
        CoinsAccountBalanceRecord update = MapstructUtils.convert(bo, CoinsAccountBalanceRecord.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(CoinsAccountBalanceRecord entity){
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 校验并批量删除账户余额记录信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    @Override
    public Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid) {
        if(isValid){
            //TODO 做一些业务上的校验,判断是否需要校验
        }
        return baseMapper.deleteByIds(ids) > 0;
    }

    /**
     * 查询最近N天相较前日的总余额涨幅数组
     * @param bo
     * @return
     */
    @Override
    public List<CoinsAccountBalanceRecordVo> queryDailyGrowthPercentageList(CoinsAccountBalanceRecordBo bo) {
        Map<String, List<CoinsAccountBalanceRecordVo>> recordsInDays = queryRecordsInDays(bo);
        List<CoinsAccountBalanceRecordVo> sumBalanceRecordList = recordsInDays.get("sum");
        List<CoinsAccountBalanceRecordVo> growthPercentageList = new ArrayList<>();
        if (CollectionUtil.isNotEmpty(sumBalanceRecordList)) {
            // 计算每天相比前一天的总余额涨幅
            CoinsAccountBalanceRecordVo firstDayBalanceRecordVo = sumBalanceRecordList.get(0);
            firstDayBalanceRecordVo.setGrowth(BigDecimal.ZERO);
            firstDayBalanceRecordVo.setGrowthPercentage(BigDecimal.ZERO);
            growthPercentageList.add(firstDayBalanceRecordVo);
            if (sumBalanceRecordList.size() > 1) {
                for (int i = 1; i < sumBalanceRecordList.size(); i++) {
                    CoinsAccountBalanceRecordVo currentRecord = sumBalanceRecordList.get(i);
                    CoinsAccountBalanceRecordVo previousRecord = sumBalanceRecordList.get(i - 1);
                    currentRecord.setGrowth(currentRecord.getBalance().subtract(previousRecord.getBalance()));
                    // 检查除数是否为零，避免除以零异常
                    if (previousRecord.getBalance().compareTo(BigDecimal.ZERO) != 0) {
                        currentRecord.setGrowthPercentage(currentRecord.getGrowth().divide(previousRecord.getBalance(), 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100")));
                    } else {
                        currentRecord.setGrowthPercentage(BigDecimal.ZERO);
                    }
                    growthPercentageList.add(currentRecord);
                }
            }

        }
        return growthPercentageList;
    }

    @Override
    public Map<String, BigDecimal> queryAnnualizedReturn(CoinsAccountBalanceRecordBo bo) {
        List<CoinsAccountBalanceRecordVo> queryDailyGrowthPercentageList = queryDailyGrowthPercentageList(bo);
        Map<String, BigDecimal> result = new HashMap<>();
        if (CollectionUtil.isNotEmpty(queryDailyGrowthPercentageList)) {
            // 计算3天年化收益
            BigDecimal threeDaysReturn = calculateAnnualizedReturn(queryDailyGrowthPercentageList, 3);
            result.put("3d", threeDaysReturn);

            // 计算7天年化收益
            BigDecimal sevenDaysReturn = calculateAnnualizedReturn(queryDailyGrowthPercentageList, 7);
            result.put("7d", sevenDaysReturn);

            // 计算30天年化收益
            BigDecimal thirtyDaysReturn = calculateAnnualizedReturn(queryDailyGrowthPercentageList, 30);
            result.put("30d", thirtyDaysReturn);
        }
        return result;
    }

    private BigDecimal calculateAnnualizedReturn(List<CoinsAccountBalanceRecordVo> dailyGrowthList, int days) {
        if (days <= 0) {
            throw new IllegalArgumentException("天数必须大于0");
        }
        // 比如三日年化计算是三日，则需要取最近3天数据，所以days减1
        days = days - 1;
        int size = dailyGrowthList.size();
        if (size < days) {
            days = size;
        }
        if (days == 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal sum = BigDecimal.ZERO;
        // 从最近一个日期往前倒计算
        for (int i = size - 1; i >= size - days; i--) {
            sum = sum.add(dailyGrowthList.get(i).getGrowthPercentage());
        }
        BigDecimal avgDailyReturn = sum.divide(new BigDecimal(days), 4, RoundingMode.HALF_UP);
        BigDecimal annualizedReturn = BigDecimal.ONE.add(avgDailyReturn.divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP))
            .pow(365)
            .subtract(BigDecimal.ONE)
            .multiply(new BigDecimal("100"));
        return annualizedReturn.setScale(2, RoundingMode.HALF_UP);
    }
}
