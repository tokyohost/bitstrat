package com.bitstrat.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.bitstrat.domain.CoinsApi;
import com.bitstrat.domain.bo.CoinsApiBo;
import com.bitstrat.domain.vo.CoinsApiVo;
import com.bitstrat.mapper.CoinsApiMapper;
import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.bitstrat.domain.bo.CoinsBotAccountBo;
import com.bitstrat.domain.vo.CoinsBotAccountVo;
import com.bitstrat.domain.CoinsBotAccount;
import com.bitstrat.mapper.CoinsBotAccountMapper;
import com.bitstrat.service.ICoinsBotAccountService;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 机器人可使用账户Service业务层处理
 *
 * @author Lion Li
 * @date 2025-05-24
 */
@RequiredArgsConstructor
@Service
public class CoinsBotAccountServiceImpl implements ICoinsBotAccountService {

    private final CoinsBotAccountMapper baseMapper;
    private final CoinsApiMapper coinsApiMapper;

    /**
     * 查询机器人可使用账户
     *
     * @param id 主键
     * @return 机器人可使用账户
     */
    @Override
    public CoinsBotAccountVo queryById(Long id){
        return baseMapper.selectVoById(id);
    }

    /**
     * 分页查询机器人可使用账户列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 机器人可使用账户分页列表
     */
    @Override
    public TableDataInfo<CoinsBotAccountVo> queryPageList(CoinsBotAccountBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<CoinsBotAccount> lqw = buildQueryWrapper(bo);
        Page<CoinsBotAccountVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询符合条件的机器人可使用账户列表
     *
     * @param bo 查询条件
     * @return 机器人可使用账户列表
     */
    @Override
    public List<CoinsBotAccountVo> queryList(CoinsBotAccountBo bo) {
        LambdaQueryWrapper<CoinsBotAccount> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<CoinsBotAccount> buildQueryWrapper(CoinsBotAccountBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<CoinsBotAccount> lqw = Wrappers.lambdaQuery();
        lqw.orderByAsc(CoinsBotAccount::getId);
        lqw.eq(bo.getBotId() != null, CoinsBotAccount::getBotId, bo.getBotId());
        lqw.eq(bo.getAccountId() != null, CoinsBotAccount::getAccountId, bo.getAccountId());
        lqw.eq(bo.getUserId() != null, CoinsBotAccount::getUserId, bo.getUserId());
        return lqw;
    }

    /**
     * 新增机器人可使用账户
     *
     * @param bo 机器人可使用账户
     * @return 是否新增成功
     */
    @Override
    public Boolean insertByBo(CoinsBotAccountBo bo) {
        CoinsBotAccount add = MapstructUtils.convert(bo, CoinsBotAccount.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    /**
     * 修改机器人可使用账户
     *
     * @param bo 机器人可使用账户
     * @return 是否修改成功
     */
    @Override
    public Boolean updateByBo(CoinsBotAccountBo bo) {
        CoinsBotAccount update = MapstructUtils.convert(bo, CoinsBotAccount.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(CoinsBotAccount entity){
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 校验并批量删除机器人可使用账户信息
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

    @Override
    public void deleteByBotId(Long botId) {
        LambdaUpdateWrapper<CoinsBotAccount> deleteWrapper = new LambdaUpdateWrapper<CoinsBotAccount>()
            .eq(CoinsBotAccount::getBotId, botId);
        baseMapper.delete(deleteWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createOrUpdateRelated(Long botId, List<CoinsApiVo> canUseApis) {
        //先删除再插入
        this.deleteByBotId(botId);
        //插入
        for (CoinsApiVo canUseApi : canUseApis) {
            CoinsBotAccount coinsBotAccount = new CoinsBotAccount();
            coinsBotAccount.setBotId(botId);
            coinsBotAccount.setAccountId(canUseApi.getId());
            coinsBotAccount.setUserId(canUseApi.getUserId());
            this.baseMapper.insert(coinsBotAccount);
        }

    }

    @Override
    public List<CoinsApiVo> selectRelatedByBotId(Long botId) {
        LambdaQueryWrapper<CoinsBotAccount> eqed = new LambdaQueryWrapper<CoinsBotAccount>()
            .eq(CoinsBotAccount::getBotId, botId);
        List<CoinsBotAccount> coinsBotAccounts = this.baseMapper.selectList(eqed);
        Set<Long> accountIds = coinsBotAccounts.stream().map(CoinsBotAccount::getAccountId).collect(Collectors.toSet());
        List<CoinsApiVo> coinsApiVos = coinsApiMapper.selectVoByIds(accountIds);
        return coinsApiVos;
    }

}
