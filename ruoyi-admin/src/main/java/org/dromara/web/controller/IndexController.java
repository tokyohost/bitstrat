package org.dromara.web.controller;

import cn.dev33.satoken.annotation.SaIgnore;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.core.utils.I18nDateTimeUtil;
import org.dromara.common.core.utils.SpringUtils;
import org.dromara.common.core.utils.StringUtils;
import org.json.JSONObject;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Locale;

/**
 * 首页
 *
 * @author Lion Li
 */
@SaIgnore
@RequiredArgsConstructor
@RestController
public class IndexController {
    @Resource
    private I18nDateTimeUtil timeUtil;
    /**
     * 访问首页，提示语
     */
    @GetMapping("/")
    public String index() {
        return StringUtils.format("404, server has running");
    }




    @GetMapping("/time")
    public R<TimeVo> getTime() {
        Locale locale = LocaleContextHolder.getLocale();
        LocalDateTime now = LocalDateTime.now();
        TimeVo timeVo = new TimeVo(timeUtil.format(now, locale),new Date(),LocalDateTime.now());
        return R.ok("ok",timeVo);
    }

    record TimeVo(String format, Date current, LocalDateTime currentLocalDate){}

}
