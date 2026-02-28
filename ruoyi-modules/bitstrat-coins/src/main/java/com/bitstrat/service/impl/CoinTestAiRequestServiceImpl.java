package com.bitstrat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.bitstrat.domain.CoinAiTaskRequest;
import com.bitstrat.domain.bo.CoinAiTaskRequestBo;
import com.bitstrat.domain.vo.CoinAiTaskRequestVo;
import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.mybatis.core.page.PageQuery;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.bitstrat.mapper.CoinTestAiRequestMapper;
import com.bitstrat.service.ICoinTestAiRequestService;

import java.util.List;
import java.util.Map;
import java.util.Collection;
import java.util.Objects;

/**
 * AI 用户请求提示词Service业务层处理
 *
 * @author Lion Li
 * @date 2025-11-01
 */
@RequiredArgsConstructor
@Service
public class CoinTestAiRequestServiceImpl implements ICoinTestAiRequestService {

    private final CoinTestAiRequestMapper baseMapper;

    /**
     * 查询AI 用户请求提示词
     *
     * @param id 主键
     * @return AI 用户请求提示词
     */
    @Override
    public CoinAiTaskRequestVo queryById(Long id){
        return baseMapper.selectVoById(id);
    }

    /**
     * 分页查询AI 用户请求提示词列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return AI 用户请求提示词分页列表
     */
    @Override
    public TableDataInfo<CoinAiTaskRequestVo> queryPageList(CoinAiTaskRequestBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<CoinAiTaskRequest> lqw = buildQueryWrapper(bo);
        Page<CoinAiTaskRequestVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询符合条件的AI 用户请求提示词列表
     *
     * @param bo 查询条件
     * @return AI 用户请求提示词列表
     */
    @Override
    public List<CoinAiTaskRequestVo> queryList(CoinAiTaskRequestBo bo) {
        LambdaQueryWrapper<CoinAiTaskRequest> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<CoinAiTaskRequest> buildQueryWrapper(CoinAiTaskRequestBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<CoinAiTaskRequest> lqw = Wrappers.lambdaQuery();
        lqw.orderByAsc(CoinAiTaskRequest::getId);
        lqw.eq(StringUtils.isNotBlank(bo.getRequestKey()), CoinAiTaskRequest::getRequestKey, bo.getRequestKey());
        lqw.eq(StringUtils.isNotBlank(bo.getContent()), CoinAiTaskRequest::getContent, bo.getContent());
        lqw.eq(Objects.nonNull(bo.getCreateBy()), CoinAiTaskRequest::getCreateBy, bo.getCreateBy());
        lqw.eq(CoinAiTaskRequest::getTaskId, bo.getTaskId());
        return lqw;
    }

    /**
     * 新增AI 用户请求提示词
     *
     * @param bo AI 用户请求提示词
     * @return 是否新增成功
     */
    @Override
    public Boolean insertByBo(CoinAiTaskRequestBo bo) {
        CoinAiTaskRequest add = MapstructUtils.convert(bo, CoinAiTaskRequest.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    /**
     * 修改AI 用户请求提示词
     *
     * @param bo AI 用户请求提示词
     * @return 是否修改成功
     */
    @Override
    public Boolean updateByBo(CoinAiTaskRequestBo bo) {
        CoinAiTaskRequest update = MapstructUtils.convert(bo, CoinAiTaskRequest.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(CoinAiTaskRequest entity){
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 校验并批量删除AI 用户请求提示词信息
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
    public CoinAiTaskRequest queryByRequestKey(String key) {
        QueryWrapper<CoinAiTaskRequest> wrapper = new QueryWrapper<>();
        LambdaQueryWrapper<CoinAiTaskRequest> eq = wrapper.lambda().eq(CoinAiTaskRequest::getRequestKey, key);
        return baseMapper.selectOne(eq);
    }
}
