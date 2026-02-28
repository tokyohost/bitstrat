package com.bitstrat.service.impl;

import com.bitstrat.domain.CoinsUserVip;
import com.bitstrat.domain.CoinsVipLevel;
import com.bitstrat.domain.bo.CoinsUserVipBo;
import com.bitstrat.domain.vo.CoinsUserVipInfoVo;
import com.bitstrat.domain.vo.CoinsUserVipVo;
import com.bitstrat.mapper.CoinsUserVipMapper;
import com.bitstrat.mapper.CoinsVipLevelMapper;
import com.bitstrat.service.ICoinsUserVipService;
import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Collection;
import java.util.stream.Collectors;

import static com.bitstrat.constant.CheckPurchaseVipConstant.*;

/**
 * 用户VIP 状态Service业务层处理
 *
 * @author Lion Li
 * @date 2025-05-14
 */
@RequiredArgsConstructor
@Service
public class CoinsUserVipServiceImpl implements ICoinsUserVipService {

    private final CoinsUserVipMapper baseMapper;

    @Autowired
    private CoinsVipLevelMapper coinsVipLevelMapper;

    /**
     * 查询用户VIP 状态
     *
     * @param id 主键
     * @return 用户VIP 状态
     */
    @Override
    public CoinsUserVipVo queryById(Long id){
        return baseMapper.selectVoById(id);
    }

    /**
     * 分页查询用户VIP 状态列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 用户VIP 状态分页列表
     */
    @Override
    public TableDataInfo<CoinsUserVipVo> queryPageList(CoinsUserVipBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<CoinsUserVip> lqw = buildQueryWrapper(bo);
        Page<CoinsUserVipVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }


    /**
     * 查询符合条件的用户VIP 状态列表
     *
     * @param bo 查询条件
     * @return 用户VIP 状态列表
     */
    @Override
    public List<CoinsUserVipVo> queryList(CoinsUserVipBo bo) {
        LambdaQueryWrapper<CoinsUserVip> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<CoinsUserVip> buildQueryWrapper(CoinsUserVipBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<CoinsUserVip> lqw = Wrappers.lambdaQuery();
        lqw.orderByAsc(CoinsUserVip::getId);
        lqw.eq(bo.getUserId() != null, CoinsUserVip::getUserId, bo.getUserId());
        lqw.eq(bo.getVipId() != null, CoinsUserVip::getVipId, bo.getVipId());
        lqw.eq(bo.getBuyTime() != null, CoinsUserVip::getBuyTime, bo.getBuyTime());
        lqw.eq(bo.getExpireTime() != null, CoinsUserVip::getExpireTime, bo.getExpireTime());
        lqw.eq(bo.getStatus() != null, CoinsUserVip::getStatus, bo.getStatus());
        return lqw;
    }

    /**
     * 新增用户VIP 状态
     *
     * @param bo 用户VIP 状态
     * @return 是否新增成功
     */
    @Override
    public Boolean insertByBo(CoinsUserVipBo bo) {
        CoinsUserVip add = MapstructUtils.convert(bo, CoinsUserVip.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    /**
     * 修改用户VIP 状态
     *
     * @param bo 用户VIP 状态
     * @return 是否修改成功
     */
    @Override
    public Boolean updateByBo(CoinsUserVipBo bo) {
        CoinsUserVip update = MapstructUtils.convert(bo, CoinsUserVip.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(CoinsUserVip entity){
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 校验并批量删除用户VIP 状态信息
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
     * 购买会员
     *
     * @param userId 用户ID
     * @param vipId  会员等级ID
     * @return 是否购买成功
     */
    @Transactional
    @Override
    public boolean purchaseVip(Long userId, Long vipId) {
        CoinsVipLevel vipLevel = coinsVipLevelMapper.selectById(vipId);
        if (vipLevel == null) {
            return false;
        }

        // 查询用户当前未过期的会员信息
        CoinsUserVipInfoVo currentUserVip = getUserVipInfo(userId);

        boolean isRenew = currentUserVip != null && currentUserVip.getLevel().equals(vipLevel.getLevel());
        if (isRenew) {
            // 如果当前会员等级与购买的会员等级相同，则进行续期操作
            renewVip(currentUserVip.getId(), vipLevel.getAvaliableDay(), currentUserVip.getExpireTime());
        }

        // 如果没有未过期的会员或会员等级不同，则创建新的会员记录
        CoinsUserVipBo userVip = new CoinsUserVipBo();
        userVip.setUserId(userId);
        userVip.setVipId(vipId);
        Date buyTime = new Date();
        userVip.setBuyTime(buyTime);
        userVip.setExpireTime(calculateExpireTime(buyTime, vipLevel.getAvaliableDay()));
        userVip.setStatus(1); // 1表示有效
        userVip.setCreateTime(buyTime);
        userVip.setUpdateTime(buyTime);
        userVip.setIsRenew(isRenew?1:0);
        userVip.setRenewId(isRenew?currentUserVip.getId():null); // 新增记录续费记录ID
        return insertByBo(userVip);
    }

    /**
     * 续期会员
     *
     * @param userVipId 会员记录ID
     * @param avaliableDay 增加的有效期天数
     * @return 是否续期成功
     */
    private boolean renewVip(Long userVipId, Integer avaliableDay, Date lastExpireTime) {
        // 计算新的过期时间
        Date newExpireTime = calculateExpireTime(lastExpireTime, avaliableDay);
        CoinsUserVip userVip = new  CoinsUserVip();
        userVip.setId(userVipId);
        userVip.setExpireTime(newExpireTime);
        userVip.setUpdateTime(new Date()); // 更新更新时间
        // 更新数据库记录
        return baseMapper.updateById(userVip) > 0;
    }

    /**
     * 查询用户会员信息
     *
     * @param userId 用户ID
     * @return 用户会员信息
     */
    @Override
    public CoinsUserVipInfoVo getUserVipInfo(Long userId) {
        List<CoinsUserVipInfoVo> coinsUserVipInfoVos = baseMapper.selectUserVipInfoList(userId);
        if (coinsUserVipInfoVos.isEmpty()) {
            return null;
        }
        return coinsUserVipInfoVos.get(0);
    }

    /**
     * 计算会员到期时间
     *
     * @param avaliableDay 会员有效期天数
     * @return 到期时间
     */
    @Override
    public Date calculateExpireTime(Date date, Integer avaliableDay) {
        long currentTime = date.getTime();
        long expireTime = currentTime + avaliableDay * 24L * 60 * 60 * 1000;
        return new Date(expireTime);
    }

    /**
     * 检查用户是否购买了低于、等于或高于当前未过期的会员等级
     *
     * @param userId 用户ID
     * @param vipId  会员等级ID
     * @return 0: 用户无会员；1: 等于当前未过期会员等级；2: 低于当前未过期会员等级；3: 高于当前未过期会员等级
     */
    @Override
    public Integer checkPurchaseVip(Long userId, Long vipId) {
        // 查询用户当前未过期的会员信息
        CoinsUserVipInfoVo currentUserVip = getUserVipInfo(userId);

        if (currentUserVip == null) {
            // 用户无会员
            return USER_VIP_CAN_PURCHASE;
        }

        // 获取当前未过期的会员等级ID
        Long currentVipId = currentUserVip.getVipId();

        if (vipId.equals(currentVipId)) {
            // 购买的会员等级等于当前未过期的会员等级
            return USER_VIP_RENEW;
        } else if (vipId < currentVipId) {
            // 购买的会员等级低于当前未过期的会员等级
            return USER_VIP_CANT_PURCHASE;
        } else {
            // 购买的会员等级高于当前未过期的会员等级
            return USER_VIP_UPGRADE;
        }
    }
}
