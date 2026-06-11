package com.agent.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Sa-Token 拦截器注册与登录校验放行规则。
 */
@Configuration
public class SaTokenConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SaInterceptor(handler ->
                        SaRouter.match("/**")
                                // 放行白名单
                                .notMatch("/api/auth/login")
                                .notMatch("/error")
                                .notMatch("/actuator/**")
                                // 静态资源（演示页面）
                                .notMatch("/")
                                .notMatch("/index.html")
                                .notMatch("/favicon.ico")
                                .notMatch("/static/**")
                                .notMatch("/*.html", "/*.css", "/*.js", "/*.ico", "/*.png")
                                .check(r -> StpUtil.checkLogin())))
                .addPathPatterns("/**");
    }
}
