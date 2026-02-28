package com.bitstrat.controller;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

import com.bitstrat.init.SyncTaskRunner;
import com.bitstrat.init.TaskRunner;
import com.bitstrat.service.impl.CommonServce;
import lombok.RequiredArgsConstructor;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.*;
import cn.dev33.satoken.annotation.SaCheckPermission;
import org.dromara.common.satoken.utils.LoginHelper;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;
import org.dromara.common.idempotent.annotation.RepeatSubmit;
import org.dromara.common.log.annotation.Log;
import org.dromara.common.web.core.BaseController;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.core.domain.R;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import org.dromara.common.log.enums.BusinessType;
import org.dromara.common.excel.utils.ExcelUtil;
import com.bitstrat.domain.vo.CoinsTaskVo;
import com.bitstrat.domain.bo.CoinsTaskBo;
import com.bitstrat.service.ICoinsTaskService;
import org.dromara.common.mybatis.core.page.TableDataInfo;

/**
 * 任务管理
 *
 * @author Lion Li
 * @date 2025-04-01
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/system/task")
public class CoinsTaskController extends BaseController {

    private final ICoinsTaskService coinsTaskService;

    private final CommonServce commonServce;

    private final SyncTaskRunner syncTaskRunner;

    private final TaskRunner taskRunner;

    /**
     * 查询任务管理列表
     */
    @SaCheckPermission("system:task:list")
    @GetMapping("/list")
    public TableDataInfo<CoinsTaskVo> list(CoinsTaskBo bo, PageQuery pageQuery) {
        if (!LoginHelper.isSuperAdmin()) {
            bo.setCreateBy(LoginHelper.getUserId());
        }
        return coinsTaskService.queryPageList(bo, pageQuery);
    }

    /**
     * 导出任务管理列表
     */
    @SaCheckPermission("system:task:export")
    @Log(title = "任务管理", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(CoinsTaskBo bo, HttpServletResponse response) {
        List<CoinsTaskVo> list = coinsTaskService.queryList(bo);
        ExcelUtil.exportExcel(list, "任务管理", CoinsTaskVo.class, response);
    }

    /**
     * 获取任务管理详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("system:task:query")
    @GetMapping("/{id}")
    public R<CoinsTaskVo> getInfo(@NotNull(message = "主键不能为空")
                                     @PathVariable Long id) {
        return R.ok(coinsTaskService.queryById(id));
    }
    /**
     * 停止任务
     *
     * @param id 主键
     */
    @SaCheckPermission("system:task:stop")
    @GetMapping("/stop/{id}")
    public R<CoinsTaskVo> stop(@NotNull(message = "主键不能为空")
                                     @PathVariable Long id) {
        CoinsTaskVo coinsTaskVo = coinsTaskService.queryById(id);
        ScheduledFuture<?> remove = commonServce.getSyncTaskSchedulerMap().remove(id);
        remove.cancel(true);
        ScheduledFuture<?> removetask = commonServce.getTaskSchedulerMap().remove(id);
        removetask.cancel(true);
        CoinsTaskBo coinsTaskBo = new CoinsTaskBo();
        coinsTaskBo.setStatus(3L);
        coinsTaskBo.setId(coinsTaskVo.getId());
        coinsTaskService.updateByBo(coinsTaskBo);
        return R.ok();
    }
    /**
     * 获取任务管理详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("system:task:start")
    @GetMapping("/start/{id}")
    public R<CoinsTaskVo> start(@NotNull(message = "主键不能为空")
                                     @PathVariable Long id) {
        CoinsTaskVo coinsTaskVo = coinsTaskService.queryById(id);
        ScheduledFuture<?> remove = commonServce.getSyncTaskSchedulerMap().remove(id);
        if(remove != null) {
            remove.cancel(true);
        }
        //启动同步任务
        syncTaskRunner.startSyncTask(coinsTaskVo);
        //启动Task任务
        ConcurrentHashMap<Long, ScheduledFuture<?>> taskSchedulerMap = commonServce.getTaskSchedulerMap();
        ScheduledFuture<?> taskRemove = taskSchedulerMap.remove(coinsTaskVo.getId());
        if(taskRemove != null) {
            taskRemove.cancel(true);
        }
        taskRunner.startTask(coinsTaskVo);

        CoinsTaskBo coinsTaskBo = new CoinsTaskBo();
        coinsTaskBo.setStatus(2L);
        coinsTaskBo.setId(coinsTaskVo.getId());
        coinsTaskService.updateByBo(coinsTaskBo);
        return R.ok();
    }

    /**
     * 新增任务管理
     */
    @SaCheckPermission("system:task:add")
    @Log(title = "任务管理", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody CoinsTaskBo bo) {
        bo.setStatus(1L);
        return toAjax(coinsTaskService.insertByBo(bo));
    }

    /**
     * 修改任务管理
     */
    @SaCheckPermission("system:task:edit")
    @Log(title = "任务管理", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody CoinsTaskBo bo) {
        return toAjax(coinsTaskService.updateByBo(bo));
    }

    /**
     * 删除任务管理
     *
     * @param ids 主键串
     */
    @SaCheckPermission("system:task:remove")
    @Log(title = "任务管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空")
                          @PathVariable Long[] ids) {
        return toAjax(coinsTaskService.deleteWithValidByIds(List.of(ids), true));
    }
}
